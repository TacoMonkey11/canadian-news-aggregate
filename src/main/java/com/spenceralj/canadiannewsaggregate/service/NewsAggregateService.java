package com.spenceralj.canadiannewsaggregate.service;

import com.spenceralj.canadiannewsaggregate.model.ArticleEntity;
import com.spenceralj.canadiannewsaggregate.model.NewsArticle;
import com.spenceralj.canadiannewsaggregate.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsAggregateService {
    private final GeminiAIService aiService;
    private final RSSFetcherService fetcherService;
    private final ArticleRepository articleRepository;

    public void injestFeed(NewsArticle.NewsSource source) {
        List<NewsArticle> articles = fetcherService.fetch(source);

        for (NewsArticle article : articles) {
            try {
                if (articleRepository.existsByLink(article.link())) {
                    log.debug("[Skipping] {} | Already Exists in DB", article.title());
                    continue;
                }

                if (article.publishedDate() != null && article.publishedDate().before(Date.from(Instant.now().minus(7, ChronoUnit.DAYS)))) {
                    // Ignore old articles
                    continue;
                }

                NewsArticle.Analysis decision = aiService.analyze(article);
                Thread.sleep(4200);

                if (decision.isRelevant()) {
                    log.info("[Relevant - Adding to Feed] {}", article.title());
                } else {
                    log.info("[Not Relevant - Recorded] {}", article.title());
                }

                articleRepository.save(ArticleEntity.builder()
                        .isRelevant(decision.isRelevant())
                        .link(article.link())
                        .title(article.title())
                        .sourceName(source.name())
                        .publishedDate(article.publishedDate())
                        .aiSummary(decision.tldr())
                        .tags(decision.tags())
                        .createdAt(LocalDateTime.now())
                        .build());
            } catch (Exception e) {
                Throwable root = e;
                while (root.getCause() != null) {
                    root = root.getCause();
                }

                String errorDetails = (e.getMessage() + " " + root.getMessage()).toLowerCase();

                if (errorDetails.contains("429") || errorDetails.contains("resource_exhausted") || errorDetails.contains("quota")) {
                    log.warn("Gemini Rate Limit reached! Paused ingestion until next cycle");
                    return;
                }

                log.error("Error processing article '{}': {}", article.title(), e.getMessage());
            }
        }
    }

    @Scheduled(initialDelay = 5000, fixedRate = 3600000)
    public void updateFeeds() {
        log.info("=== Starting scheduled feed ingestion cycle ===");
        ArrayList<NewsArticle.NewsSource> sources = new ArrayList<>();

        Path feedsPath = Path.of("feeds.txt");
        if (!Files.exists(feedsPath)) {
            log.error("feeds.txt file not found at: {}", feedsPath.toAbsolutePath());
            return;
        }

        try (var reader = new BufferedReader(new InputStreamReader(Files.newInputStream(feedsPath), StandardCharsets.UTF_8))) {
            List<String> lines = reader.lines()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                    .toList();

            for (String line : lines) {
                String[] parts = line.split("\\|", 2);
                if (parts.length == 2) {
                    sources.add(new NewsArticle.NewsSource(parts[0].trim(), parts[1].trim()));
                }
            }
            log.info("Loaded {} feeds from {}", sources.size(), feedsPath.toAbsolutePath());
        } catch (Exception e) {
            log.error("Failed to read {}: {}", feedsPath.toAbsolutePath(), e.getMessage(), e);
            return;
        }

        for (NewsArticle.NewsSource source : sources) {
            try {
                log.info("Processing feed: {}", source.name());
                injestFeed(source);
            } catch (Exception e) {
                log.error("Failed to ingest feed from {}: {}", source.name(), e.getMessage());
            }
        }
        log.info("=== Finished scheduled feed ingestion cycle ===");
    }
}

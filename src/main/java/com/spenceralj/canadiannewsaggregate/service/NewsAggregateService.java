package com.spenceralj.canadiannewsaggregate.service;

import com.spenceralj.canadiannewsaggregate.model.ArticleEntity;
import com.spenceralj.canadiannewsaggregate.model.NewsArticle;
import com.spenceralj.canadiannewsaggregate.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
            if (articleRepository.existsByLink(article.link())) {
                log.info("{} |:| Already Exists in DB", article.title());
                continue;
            }

            NewsArticle.Analysis decision = aiService.analyze(article);

            if (!decision.isRelevant()) {
                log.info("{} |:| Not relevant enough!", article.title());
                continue;
            }

            articleRepository.save(ArticleEntity.builder()
                    .link(article.link())
                    .title(article.title())
                    .sourceName(source.name())
                    .publishedDate(article.publishedDate())
                    .aiSummary(decision.tldr())
                    .createdAt(LocalDateTime.now())
                    .build());
        }
    }
}

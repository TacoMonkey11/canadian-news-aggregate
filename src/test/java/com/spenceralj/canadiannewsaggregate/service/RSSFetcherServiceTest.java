package com.spenceralj.canadiannewsaggregate.service;

import com.spenceralj.canadiannewsaggregate.model.NewsArticle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RSSFetcherServiceTest {

    @Autowired
    private RSSFetcherService rssFetcherService;

    @Test
    @DisplayName("Should fetch and parse Parliament of Canada LEGISinfo bills feed")
    void testFetchLegisInfo() {
        NewsArticle.NewsSource source = new NewsArticle.NewsSource(
                "Parliament of Canada (LEGISinfo)",
                "https://www.parl.ca/legisinfo/en/bills/rss"
        );

        List<NewsArticle> articles = rssFetcherService.fetch(source);

        assertNotNull(articles, "Articles list should not be null");
        assertFalse(articles.isEmpty(), "Articles list should not be empty");

        NewsArticle first = articles.get(0);
        assertNotNull(first.title(), "Article title should not be null");
        assertFalse(first.title().isBlank(), "Article title should not be blank");
        assertNotNull(first.link(), "Article link should not be null");
        assertTrue(first.link().startsWith("http"), "Article link should be a valid URL");
        assertEquals("Parliament of Canada (LEGISinfo)", first.source().name());
    }

    @Test
    @DisplayName("Should fetch and parse CBC Politics news feed")
    void testFetchCbcPolitics() {
        NewsArticle.NewsSource source = new NewsArticle.NewsSource(
                "CBC News (Politics)",
                "https://www.cbc.ca/cmlink/rss-politics"
        );

        List<NewsArticle> articles = rssFetcherService.fetch(source);

        assertNotNull(articles, "Articles list should not be null");
        assertFalse(articles.isEmpty(), "Articles list should not be empty");

        NewsArticle first = articles.get(0);
        assertNotNull(first.title());
        assertNotNull(first.link());
        assertNotNull(first.description());
    }
}

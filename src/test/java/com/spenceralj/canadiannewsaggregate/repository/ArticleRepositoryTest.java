package com.spenceralj.canadiannewsaggregate.repository;

import com.spenceralj.canadiannewsaggregate.model.ArticleEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ArticleRepositoryTest {

    @Autowired
    private ArticleRepository articleRepository;

    @Test
    @DisplayName("Should save, check existence by link, and query articles sorted by date")
    void testSaveAndQueryArticles() {
        String testLink = "https://example.gov.ca/news/bill-c100-test-" + System.currentTimeMillis();

        ArticleEntity article = ArticleEntity.builder()
                .title("Test Federal Infrastructure Investment")
                .link(testLink)
                .sourceName("Infrastructure Canada")
                .publishedDate(new Date())
                .isRelevant(true)
                .aiSummary("Federal government announces major highway and transit investments.")
                .tags(List.of("National", "Infrastructure"))
                .createdAt(LocalDateTime.now())
                .build();

        ArticleEntity saved = articleRepository.save(article);
        assertNotNull(saved.getId(), "Saved entity should have a generated ID");

        // Test existsByLink
        assertTrue(articleRepository.existsByLink(testLink), "existsByLink should return true for saved article");
        assertFalse(articleRepository.existsByLink("https://nonexistent-link.com"), "existsByLink should return false for unknown link");

        // Test findAllByOrderByPublishedDateDesc
        List<ArticleEntity> allArticles = articleRepository.findAllByIsRelevantTrueOrderByPublishedDateDesc();
        assertFalse(allArticles.isEmpty(), "Should retrieve saved articles");

        // Test findAllByTag with multiple matching tags
        List<ArticleEntity> matchedTwoTags = articleRepository.findAllByTag(List.of("National", "Infrastructure"), 2);
        assertFalse(matchedTwoTags.isEmpty(), "Should find article with both National and Infrastructure tags");

        // Test findAllByTag with non-matching tag combination
        List<ArticleEntity> noMatch = articleRepository.findAllByTag(List.of("National", "Healthcare"), 2);
        assertTrue(noMatch.isEmpty(), "Should not match when an article lacks one of the required tags");
    }
}

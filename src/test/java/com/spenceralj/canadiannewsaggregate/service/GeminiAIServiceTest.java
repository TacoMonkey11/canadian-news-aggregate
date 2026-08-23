package com.spenceralj.canadiannewsaggregate.service;

import com.spenceralj.canadiannewsaggregate.model.NewsArticle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class GeminiAIServiceTest {

    @Autowired
    private GeminiAIService geminiAIService;

    @Test
    @DisplayName("Should triage a high-significance federal bill as relevant")
    @EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
    void testTriageHighSignificanceBill() {
        NewsArticle article = new NewsArticle(
                "Bill C-234: An Act to amend the Greenhouse Gas Pollution Pricing Act",
                "https://www.parl.ca/legisinfo/en/bill/44-1/c-234",
                new NewsArticle.NewsSource("Parliament of Canada", "https://www.parl.ca/legisinfo/en/bills/rss"),
                new Date(),
                "This enactment amends the Greenhouse Gas Pollution Pricing Act to expand the definition of eligible farming machinery and provide exemptions from the carbon price for qualifying agricultural operations."
        );

        NewsArticle.TriageDecision decision = geminiAIService.triage(article);

        assertNotNull(decision, "Triage decision should not be null");
        assertTrue(decision.isRelevant(), "Major federal bill amendment should be marked as relevant");
        assertTrue(decision.significanceScore() >= 6, "Significance score should be >= 6, was: " + decision.significanceScore());
    }

    @Test
    @DisplayName("Should triage a low-significance community event as not relevant")
    @EnabledIfEnvironmentVariable(named = "GEMINI_API_KEY", matches = ".+")
    void testTriageLowSignificanceEvent() {
        NewsArticle article = new NewsArticle(
                "Minister Attends Annual Community Pancake Breakfast in Hometown",
                "https://example.gov.ca/news/community-breakfast",
                new NewsArticle.NewsSource("Government of Canada", "https://example.gov.ca/rss"),
                new Date(),
                "The Minister met with local residents and volunteers this morning at the community centre to celebrate the annual summer festival and flip pancakes with youth groups."
        );

        NewsArticle.TriageDecision decision = geminiAIService.triage(article);

        assertNotNull(decision, "Triage decision should not be null");
        assertFalse(decision.isRelevant(), "Community breakfast should not be marked as relevant policy");
        assertTrue(decision.significanceScore() <= 5, "Significance score should be <= 5, was: " + decision.significanceScore());
    }
}

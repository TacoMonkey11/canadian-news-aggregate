package com.spenceralj.canadiannewsaggregate.service;

import com.spenceralj.canadiannewsaggregate.model.NewsArticle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GeminiAIService {
    private final ChatClient client;

    public GeminiAIService(ChatClient.Builder chatClientBuilder) {
        this.client = chatClientBuilder.build();
    }

    public NewsArticle.Analysis analyze(NewsArticle article) {
        String prompt = """
                You are an expert Canadian public policy and legislative analyst.
                Analyze the following Canadian news item or government press release.

                Article Details:
                - Source: {source}
                - Title: {title}
                - Content/Description: {description}

                Tasks:
                1. RELEVANCE CHECK (`isRelevant`):
                   - Set `isRelevant = true` IF this represents genuine public policy, new or amended legislation/bills, regulatory updates, major public infrastructure/funding, taxation, or national/provincial governance.
                   - Set `isRelevant = false` IF this is routine operational noise (e.g., local ribbon-cuttings, minor committee appointments, partisan political campaigning/rhetoric, ceremonial awards, or general public awareness notices).

                2. PLAIN-ENGLISH SUMMARY (`tldr`):
                   - If `isRelevant = true`, write a detailed 2 to 4 sentence plain-English summary. Explain clearly: (1) what specific policy, bill, or project is being announced, (2) who or which sector is impacted, and (3) key figures, timelines, or practical implications. Avoid vague bureaucratic jargon.
                   - If `isRelevant = false`, return an empty string "".

                3. SECTOR & JURISDICTION TAGS (`tags`):
                   - If `isRelevant = true`, select 1 to 4 tags STRICTLY from this approved list (do not create custom tags or use ampersands):
                     - Jurisdiction Level: ["National", "Ontario", "Quebec", "British Columbia",
                                            "Alberta", "Manitoba", "Saskatchewan", "Nova Scotia",
                                            "New Brunswick", "Newfoundland and Labrador",
                                            "Prince Edward Island", "Municipal"]
                       (Note: "National" covers federal affairs, nationwide initiatives, and northern territories: Yukon, Northwest Territories, Nunavut)
                     - Policy Domain: ["Housing", "Real Estate", "Energy", "Environment", "Economy", "Taxation",
                                       "Infrastructure", "Transit", "Healthcare", "Agriculture", "Trade",
                                       "Foreign Affairs", "Public Safety", "Justice", "Indigenous Affairs",
                                       "Technology", "Telecommunications", "Labour", "Immigration", "Defence"]
                   - Always include at least one jurisdiction tag (e.g. "National", "Ontario", etc.) along with 1-3 policy domain tags.
                   - If `isRelevant = false`, return an empty list [].
                """;

        return client.prompt()
                .user(u -> u.text(prompt)
                        .param("source", article.source().name())
                        .param("title", article.title())
                        .param("description", article.description()))
                .call()
                .entity(NewsArticle.Analysis.class);
    }

}

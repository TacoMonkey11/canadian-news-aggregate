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
                   - If `isRelevant = true`, write a concise 1-2 sentence plain-English summary explaining what is changing, who is affected, and why it matters. Avoid bureaucratic jargon.
                   - If `isRelevant = false`, return an empty string "".

                3. SECTOR TAGS (`tags`):
                   - If `isRelevant = true`, select 1 to 3 tags STRICTLY from this approved list (do not create custom tags or use ampersands):
                     ["Housing", "Real Estate", "Energy", "Environment", "Economy", "Taxation", 
                      "Infrastructure", "Transit", "Healthcare", "Agriculture", "Trade", 
                      "Foreign Affairs", "Public Safety", "Justice", "Indigenous Affairs", 
                      "Technology", "Telecommunications", "Labour", "Immigration", "Defence"]
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

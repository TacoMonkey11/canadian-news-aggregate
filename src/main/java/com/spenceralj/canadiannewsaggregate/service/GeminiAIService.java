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

    public NewsArticle.TriageDecision triage(NewsArticle article) {
        String triagePrompt = """
                You are an expert Canadian public policy and legislative analyst.
                Analyze the following press release / news article to determine if it represents a significant policy, legislative, regulatory, or major infrastructure announcement in Canada.

                Article Details:
                - Source: {source}
                - Title: {title}
                - Summary/Description: {description}

                Evaluation Criteria:
                1. HIGH RELEVANCE (Score 7-10, isRelevant = true):
                   - Introduction or passage of new federal or provincial bills/legislation.
                   - New or amended regulations, statutory orders, or policy directives.
                   - Major public funding or infrastructure commitments (e.g. transit, housing, energy, healthcare capital).
                   - Major trade, taxation, or economic policy updates.

                2. LOW RELEVANCE (Score 1-5, isRelevant = false):
                   - Routine administrative notices or minor committee appointments.
                   - Local ribbon-cuttings, awards, or commemorative day proclamations.
                   - Political party campaigning, partisan commentary, or opinion pieces.
                   - General public awareness campaigns without policy or regulatory changes.

                Evaluate the article objectively and output your triage decision.
                """;

        return client.prompt()
                .user(u -> u.text(triagePrompt)
                        .param("source", article.source().name())
                        .param("title", article.title())
                        .param("description", article.description()))
                .call()
                .entity(NewsArticle.TriageDecision.class);
    }


}

package com.spenceralj.canadiannewsaggregate.model;

import java.util.Date;

public record NewsArticle(
        String title,
        String link,
        NewsSource source,
        Date publishedDate,
        String description
) {
    public record NewsSource(
            String name,
            String url
    ) {}

    public record TriageDecision(
            boolean isRelevant,
            int significanceScore
    ){}
}

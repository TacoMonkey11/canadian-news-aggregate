package com.spenceralj.canadiannewsaggregate.model;

import java.util.Date;
import java.util.List;

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

    public record Analysis(
            boolean isRelevant,
            String tldr,
            List<String> tags
    ){}
}

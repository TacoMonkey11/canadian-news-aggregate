package com.spenceralj.canadiannewsaggregate.controller;

import com.spenceralj.canadiannewsaggregate.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class NewsController {
    private final ArticleRepository repository;

    @GetMapping("/")
    public String enlighten(
            @RequestParam(name = "tags", required = false) List<String> tags,
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(name="limit", required = false, defaultValue="20") int limit,
            Model model
    ) {
        tags = (tags == null) ? List.of() : tags;

        model.addAttribute("articles", repository.findAllMatching(tags, tags.size(), query, limit));
        model.addAttribute("activeTags", tags);
        model.addAttribute("availableTags", repository.findAllTags());
        model.addAttribute("query", query);
        model.addAttribute("limit", limit);
        model.addAttribute("controller", this);
        model.addAttribute("totalRelevantArticles", repository.countArticleEntitiesByIsRelevantIsTrue());
        model.addAttribute("totalArticles", repository.count());
        return "index";
    }

    public String buildUrlParams(List<String> activeTags, String toggledTag, String query, int limit) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/");

        ArrayList<String> mutableTags = new ArrayList<>(activeTags);
        if (mutableTags.contains(toggledTag)) {
            mutableTags.remove(toggledTag);
        } else {
            mutableTags.add(toggledTag);
        }

        if (!mutableTags.isEmpty()) {
            builder.queryParam("tags", String.join(",", mutableTags));
        }

        if (query != null && !query.isBlank()) {
            builder.queryParam("q", query);
        }

        if (limit != 20) {
            builder.queryParam("limit", limit);
        }

        return builder.toUriString();
    }

    //TODO
    // Polish for deployment
    // Add a shit ton more feeds
    // Double the amount of tags

}
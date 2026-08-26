package com.spenceralj.canadiannewsaggregate.controller;

import com.spenceralj.canadiannewsaggregate.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class NewsController {
    private final ArticleRepository repository;

    @GetMapping("/")
    public String enlighten(
            @RequestParam(name = "tags", required = false) List<String> tags,
            Model model
    ) {
        if (tags == null || tags.isEmpty()) {
            model.addAttribute("articles", repository.findAllByIsRelevantTrueOrderByPublishedDateDesc());
        } else {
            model.addAttribute("articles", repository.findAllByTag(tags, tags.size()));
        }
        return "index";
    }

}
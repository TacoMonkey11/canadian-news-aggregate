package com.spenceralj.canadiannewsaggregate.controller;

import com.spenceralj.canadiannewsaggregate.model.ArticleEntity;
import com.spenceralj.canadiannewsaggregate.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class NewsController {
    private final ArticleRepository repository;

    @GetMapping("/")
    public String enlighten(Model model) {
        List<ArticleEntity> list = repository.findAllByOrderByPublishedDateDesc();
        model.addAttribute("articles", list);
        return "index";
    }

}

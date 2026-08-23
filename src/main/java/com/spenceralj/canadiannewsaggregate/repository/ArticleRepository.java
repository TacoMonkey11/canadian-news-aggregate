package com.spenceralj.canadiannewsaggregate.repository;

import com.spenceralj.canadiannewsaggregate.model.ArticleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArticleRepository extends JpaRepository<ArticleEntity, Long> {
    boolean existsByLink(String link);
    List<ArticleEntity> findAllByOrderByPublishedDateDesc();
}

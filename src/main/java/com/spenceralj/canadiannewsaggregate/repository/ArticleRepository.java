package com.spenceralj.canadiannewsaggregate.repository;

import com.spenceralj.canadiannewsaggregate.model.ArticleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ArticleRepository extends JpaRepository<ArticleEntity, Long> {
    boolean existsByLink(String link);
    List<ArticleEntity> findAllByIsRelevantTrueOrderByPublishedDateDesc();

    @Query("""
           SELECT a FROM ArticleEntity a
           JOIN a.tags t
           WHERE a.isRelevant = true AND t IN (:tags)
           GROUP BY a
           HAVING COUNT(DISTINCT t) = :tagCount
           ORDER BY a.publishedDate DESC
           """)
    List<ArticleEntity> findAllByTag(@Param("tags") List<String> tags, @Param("tagCount") int tagCount);
}

package com.spenceralj.canadiannewsaggregate.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "articles")
public class ArticleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String link;

    private String title;
    private String sourceName;
    private Date publishedDate;
    private boolean isRelevant;

    @Column(columnDefinition = "TEXT")
    private String aiSummary;

    @Column(columnDefinition = "TEXT")
    private String rawText;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "article_tags", joinColumns = @JoinColumn(name = "article_id"))
    @Column(name = "tag")
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    private LocalDateTime createdAt;

    @Transient
    public boolean isToday() {
        return LocalDate.now(ZoneId.of("America/Toronto")).equals(LocalDate.ofInstant(this.publishedDate.toInstant(), ZoneId.of("America/Toronto")));
    }

}


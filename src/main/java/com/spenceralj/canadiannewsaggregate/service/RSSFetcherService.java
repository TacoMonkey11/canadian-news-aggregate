package com.spenceralj.canadiannewsaggregate.service;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import com.spenceralj.canadiannewsaggregate.model.NewsArticle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class RSSFetcherService {

    public List<NewsArticle> fetch(NewsArticle.NewsSource source) {
        source.url();
        ArrayList<NewsArticle> articles = new ArrayList<>();

        try {
            java.net.URLConnection connection = new URL(source.url()).openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            SyndFeed feed = new SyndFeedInput().build(new XmlReader(connection));

            for (SyndEntry entry : feed.getEntries()) {
                String description = entry.getDescription() != null ? entry.getDescription().getValue() : "";
                Date date = entry.getUpdatedDate() != null ? entry.getUpdatedDate() : entry.getPublishedDate();
                NewsArticle article = new NewsArticle(entry.getTitle(), entry.getLink(), source, date, description);

                articles.add(article);
            }
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
        }

        return articles;
    }
}

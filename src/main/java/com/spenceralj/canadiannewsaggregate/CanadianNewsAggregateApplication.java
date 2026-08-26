package com.spenceralj.canadiannewsaggregate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CanadianNewsAggregateApplication {

    public static void main(String[] args) {
        SpringApplication.run(CanadianNewsAggregateApplication.class, args);
    }

}

package com.example.rewards;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Clock;

/** Entry point of the rewards service: boots Spring and the embedded H2 database seeded from schema.sql and data.sql. */
@SpringBootApplication
public class RewardsApplication {

    public static void main(String[] args) {
        SpringApplication.run(RewardsApplication.class, args);
    }

    /** Injected so the default three-month window can be pinned in tests. */
    @Bean
    Clock clock() {
        return Clock.systemDefaultZone();
    }
}

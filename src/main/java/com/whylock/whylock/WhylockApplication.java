    package com.whylock.whylock;

    import org.springframework.boot.SpringApplication;
    import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

    @SpringBootApplication
@EnableScheduling
    public class WhylockApplication {

        public static void main(String[] args) {
            SpringApplication.run(WhylockApplication.class, args);
        }

    }

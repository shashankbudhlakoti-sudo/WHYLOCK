package com.whylock.whylock.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class SchedulingConfig {
    // Enables @Scheduled methods app-wide (e.g. HourlyRescanScheduler)
}
package com.pharmacyhub.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration class to enable scheduling for tasks like token cleanup
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
    // This class enables scheduling functionality through @EnableScheduling
    // No additional beans or methods are required
}

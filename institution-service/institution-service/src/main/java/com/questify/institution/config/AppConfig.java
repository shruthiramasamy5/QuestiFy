package com.questify.institution.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestClient;

@Configuration
public class AppConfig {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    // Plain, non-load-balanced builder — marked @Primary so Eureka's internal
    // client (and anything that doesn't explicitly ask for the load-balanced
    // one) gets this by default instead of the LB builder.
    @Bean
    @Primary
    RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    /**
     * Load-balanced RestClient for service-to-service calls resolved through Eureka
     * (e.g. {@code http://papers-service/...}). Never point it at another service's
     * database - only at its HTTP API.
     */
    @Bean
    @org.springframework.cloud.client.loadbalancer.LoadBalanced
    RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder();
    }
}
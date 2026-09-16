package com.questify.analytics.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(AnalyticsProperties.class)
public class RestClientConfig {

    // Plain, non-load-balanced builder — marked @Primary so Eureka's internal
    // client (and anything else that doesn't explicitly ask for the
    // load-balanced one) picks this by default instead of the LB builder.
    @Bean
    @Primary
    RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    @LoadBalanced
    RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    RestClient discoveryRestClient(@LoadBalanced RestClient.Builder builder) {
        return builder.build();
    }
}
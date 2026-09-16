package com.questify.mlhealth.config;

import java.time.Clock;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(MlProperties.class)
public class MlHealthConfig {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    /** Plain (non load-balanced) client: the ML endpoint is an external system. */
    @Bean
    RestClient mlRestClient(MlProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.getTimeout());
        requestFactory.setReadTimeout(properties.getTimeout());
        return RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }
}

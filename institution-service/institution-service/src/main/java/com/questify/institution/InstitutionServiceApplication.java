package com.questify.institution;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import com.questify.institution.config.JwtProperties;

@SpringBootApplication
@EnableDiscoveryClient
@EnableJpaAuditing
@EnableConfigurationProperties(JwtProperties.class)
public class InstitutionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InstitutionServiceApplication.class, args);
    }
}

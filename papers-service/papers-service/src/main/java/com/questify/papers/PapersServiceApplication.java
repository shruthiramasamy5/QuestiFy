package com.questify.papers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class PapersServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PapersServiceApplication.class, args);
    }
}

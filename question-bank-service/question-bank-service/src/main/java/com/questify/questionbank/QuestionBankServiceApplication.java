package com.questify.questionbank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class QuestionBankServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(QuestionBankServiceApplication.class, args);
    }
}

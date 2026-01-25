package ru.practicum;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@Slf4j
@ConfigurationPropertiesScan
@EnableFeignClients
public class CommentServiceApp {
    public static void main(String[] args) {
        log.info("COMMENT-SERVICE APP STARTED");
        SpringApplication.run(CommentServiceApp.class, args);
    }
}

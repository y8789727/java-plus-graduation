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
public class RequestServiceApp {
    public static void main(String[] args) {
        log.info("REQUEST-SERVICE APP STARTED");
        SpringApplication.run(RequestServiceApp.class, args);
    }
}

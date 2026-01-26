package ru.practicum;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@Slf4j
@ConfigurationPropertiesScan
public class UserServiceApp {
    public static void main(String[] args) {
        log.info("USER-SERVICE APP STARTED");
        SpringApplication.run(UserServiceApp.class, args);
    }
}

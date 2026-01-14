package ru.practicum;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@Slf4j
@ConfigurationPropertiesScan
public class StatsServer {
    public static void main(String[] args) {
        log.info("Method launched (SpringApplication.run(StatsServer.class, args))");
        SpringApplication.run(StatsServer.class, args);
    }
}
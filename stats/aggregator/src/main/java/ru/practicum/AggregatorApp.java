package ru.practicum;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@Slf4j
@SpringBootApplication
@ConfigurationPropertiesScan
public class AggregatorApp {
    public static void main(String[] args) {
        log.info("AGGREGATOR STARTED");
        SpringApplication.run(AggregatorApp.class, args);
    }
}

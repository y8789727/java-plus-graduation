package ru.practicum;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@Slf4j
@SpringBootApplication
@ConfigurationPropertiesScan
public class AnalyzerApp {
    public static void main(String[] args) {
        log.info("ANALYZER STARTED");
        SpringApplication.run(AnalyzerApp.class, args);
    }
}

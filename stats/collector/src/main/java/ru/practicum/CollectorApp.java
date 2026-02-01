package ru.practicum;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import ru.practicum.producer.KafkaConfig;

@Slf4j
@SpringBootApplication
@EnableConfigurationProperties(KafkaConfig.class)
public class CollectorApp {
    public static void main(String[] args) {
        log.info("COLLECTOR STARTED");
        SpringApplication.run(CollectorApp.class, args);
    }
}

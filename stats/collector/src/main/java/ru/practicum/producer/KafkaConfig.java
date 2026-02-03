package ru.practicum.producer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Properties;

@Getter
@Setter
@ConfigurationProperties("collector.kafka")
@RequiredArgsConstructor
public class KafkaConfig {
    private final Properties properties;
    private final String topic;
}

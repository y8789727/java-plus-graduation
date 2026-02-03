package ru.practicum.producer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Properties;

@Getter
@Setter
@ConfigurationProperties("aggregator.kafka-producer")
@RequiredArgsConstructor
public class KafkaProducerConfig {
    private final Properties properties;
    private final String topic;
}

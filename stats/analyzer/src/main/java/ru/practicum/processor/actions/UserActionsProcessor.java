package ru.practicum.processor.actions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.service.UserActionsService;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActionsProcessor implements Runnable {

    private final UserActionsKafkaConfig kafkaConfig;
    private final UserActionsService userActionsService;

    @Override
    public void run() {
        KafkaConsumer<String, UserActionAvro> consumer = new KafkaConsumer<>(kafkaConfig.getProperties());

        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

        try {
            consumer.subscribe(List.of(kafkaConfig.getTopic()));

            while (true) {
                ConsumerRecords<String, UserActionAvro> records = consumer.poll(kafkaConfig.getTimeout());

                for (ConsumerRecord<String, UserActionAvro> record : records) {
                    userActionsService.updateUserActionInteractions(record.value());
                }

                consumer.commitSync();
            }
        } catch (WakeupException ignored) {
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий", e);
        } finally {
            try {
                consumer.commitSync();
            } finally {
                log.info("Закрываем консьюмер");
                consumer.close();
            }
        }
    }
}

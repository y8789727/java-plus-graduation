package ru.practicum;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.practicum.consumer.UserActionConsumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregatorRunner implements CommandLineRunner {

    private final UserActionConsumer userActionConsumer;

    @Override
    public void run(String... args) throws Exception {
        userActionConsumer.start();
    }
}

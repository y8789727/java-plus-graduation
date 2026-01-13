package ru.practicum;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.TestPropertySource;
import ru.practicum.dto.HitCreateDto;
import ru.practicum.dto.RequestStatsDto;
import ru.practicum.dto.ResponseStatsDto;
import ru.practicum.model.Stat;
import ru.practicum.repository.StatsRepository;
import ru.practicum.service.StatsServiceImpl;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@DataJpaTest
@ComponentScan(basePackages = "ru.practicum")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {"spring.datasource.url=jdbc:h2:mem:test"})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class StatsServiceImplTest {
    private final StatsServiceImpl statService;

    private final StatsRepository statsRepository;

    private LocalDateTime now;

    private LocalDateTime hourAgo;

    private LocalDateTime twoHoursAgo;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        hourAgo = now.minusHours(1);
        twoHoursAgo = now.minusHours(2);

        statsRepository.deleteAll();
    }

    @Test
    void saveHit_shouldSaveHitSuccessfully() {
        HitCreateDto createDto = new HitCreateDto("ewm-main-service", "/events/1", "192.168.1.1", now);

        statService.saveHit(createDto);

        List<Stat> stats = statsRepository.findAll();
        assertThat(stats).hasSize(1);
        assertThat(stats.getFirst())
                .extracting(Stat::getApp, Stat::getUri, Stat::getIp, Stat::getTimestamp)
                .containsExactly("ewm-main-service", "/events/1", "192.168.1.1", now);
    }

    @Test
    void getStats_shouldReturnEmptyListWhenNoData() {
        RequestStatsDto request = new RequestStatsDto(twoHoursAgo, now, null, false);

        List<ResponseStatsDto> result = statService.getStats(request);

        assertThat(result).isNotNull().isEmpty();
    }

    @Test
    void getStats_shouldReturnStatsForAllUris() {
        createTestData();
        RequestStatsDto request = new RequestStatsDto(twoHoursAgo, now, null, false);

        List<ResponseStatsDto> result = statService.getStats(request);

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(ResponseStatsDto::uri, ResponseStatsDto::hits)
                .containsExactly(
                        tuple("/events/1", 3L),
                        tuple("/events/2", 2L)
                );
    }

    @Test
    void getStats_shouldReturnStatsForSpecificUris() {
        createTestData();
        RequestStatsDto request = new RequestStatsDto(twoHoursAgo, now, List.of("/events/1"), false);

        List<ResponseStatsDto> result = statService.getStats(request);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst())
                .extracting(ResponseStatsDto::uri, ResponseStatsDto::hits)
                .containsExactly("/events/1", 3L);
    }

    @Test
    void getStats_shouldReturnUniqueStats() {
        createTestData();
        RequestStatsDto request = new RequestStatsDto(twoHoursAgo, now, null, true);

        List<ResponseStatsDto> result = statService.getStats(request);

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(ResponseStatsDto::uri, ResponseStatsDto::hits)
                .containsExactly(
                        tuple("/events/1", 2L),
                        tuple("/events/2", 2L)
                );
    }

    @Test
    void getStats_shouldReturnEmptyListForNonExistentUris() {
        createTestData();
        RequestStatsDto request = new RequestStatsDto(twoHoursAgo, now, List.of("/nonexistent"), false);

        List<ResponseStatsDto> result = statService.getStats(request);

        assertThat(result).isNotNull().isEmpty();
    }

    @Test
    void getStats_shouldHandleEmptyUrisList() {
        createTestData();
        RequestStatsDto request = new RequestStatsDto(twoHoursAgo, now, List.of(), false);

        List<ResponseStatsDto> result = statService.getStats(request);

        assertThat(result).hasSize(2);
    }

    @Test
    void getStats_shouldReturnStatsOrderedByHitsDesc() {
        createTestData();
        RequestStatsDto request = new RequestStatsDto(twoHoursAgo, now, null, false);

        List<ResponseStatsDto> result = statService.getStats(request);

        assertThat(result)
                .extracting(ResponseStatsDto::hits)
                .containsExactly(3L, 2L);
    }

    @Test
    void getStats_shouldFilterByDateRangeCorrectly() {
        createTestData();
        RequestStatsDto request = new RequestStatsDto(hourAgo.plusMinutes(9), hourAgo.plusMinutes(35), null, false);
        System.out.println(request);

        List<ResponseStatsDto> result = statService.getStats(request);
        System.out.println(result);

        assertThat(result).hasSize(2);
        assertThat(result.getFirst().uri()).isEqualTo("/events/1");
        assertThat(result.getFirst().hits()).isEqualTo(2);
    }

    private void createTestData() {
        statsRepository.save(Stat.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("192.168.1.1")
                .timestamp(hourAgo)
                .build());

        statsRepository.save(Stat.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("192.168.1.1")
                .timestamp(hourAgo.plusMinutes(10))
                .build());

        statsRepository.save(Stat.builder()
                .app("ewm-main-service")
                .uri("/events/1")
                .ip("192.168.1.2")
                .timestamp(hourAgo.plusMinutes(20))
                .build());

        statsRepository.save(Stat.builder()
                .app("ewm-main-service")
                .uri("/events/2")
                .ip("192.168.1.3")
                .timestamp(hourAgo.plusMinutes(30))
                .build());

        statsRepository.save(Stat.builder()
                .app("ewm-main-service")
                .uri("/events/2")
                .ip("192.168.1.4")
                .timestamp(hourAgo.plusMinutes(40))
                .build());

        statsRepository.save(Stat.builder()
                .app("ewm-main-service")
                .uri("/events/3")
                .ip("192.168.1.5")
                .timestamp(now.plusHours(1))
                .build());
    }
}
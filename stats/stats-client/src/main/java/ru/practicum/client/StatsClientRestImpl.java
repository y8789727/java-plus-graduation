package ru.practicum.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.dto.HitCreateDto;
import ru.practicum.dto.RequestStatsDto;
import ru.practicum.dto.ResponseStatsDto;
import ru.practicum.exception.StatsServerUnavailable;

import java.net.URI;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@Component
public class StatsClientRestImpl implements StatsClient {

    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String STATS_SERVICE_NAME = "stats-server";

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
    private RestClient restClient;
    private final DiscoveryClient discoveryClient;

    public StatsClientRestImpl (DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    @Override
    public void hit(HitCreateDto dto) {
        try {
            getRestClient().post()
                    .uri("/hit")
                    .contentType(APPLICATION_JSON)
                    .body(dto)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Failed to send hit to stats service", e);
            throw new StatsServerUnavailable("Ошибка вызова сервиса статистики с id: " + STATS_SERVICE_NAME);
        }
    }

    @Override
    public List<ResponseStatsDto> get(RequestStatsDto requestStatsDto) {
        try {
            URI uri = UriComponentsBuilder.fromHttpUrl(getInstance().getUri().toURL().toString())
                    .path("/stats")
                    .queryParam("start", requestStatsDto.getStart().format(dateTimeFormatter))
                    .queryParam("end", requestStatsDto.getEnd().format(dateTimeFormatter))
                    .queryParam("unique", requestStatsDto.getUnique())
                    .queryParamIfPresent("uris", Optional.ofNullable(requestStatsDto.getUris()))
                    .build()
                    .toUri();

            return getRestClient().get()
                    .uri(uri)
                    .accept(APPLICATION_JSON)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
        } catch (Exception e) {
            log.warn("Failed to get stats from stats service", e);
            throw new StatsServerUnavailable("Ошибка вызова сервиса статистики с id: " + STATS_SERVICE_NAME);
        }
    }

    private ServiceInstance getInstance() {
        try {
            return discoveryClient
                    .getInstances(STATS_SERVICE_NAME)
                    .getFirst();
        } catch (Exception exception) {
            throw new StatsServerUnavailable("Ошибка обнаружения адреса сервиса статистики с id: " + STATS_SERVICE_NAME);
        }
    }

    private RestClient getRestClient() {
        if (restClient == null) {
            try {
                this.restClient = RestClient.builder()
                        .baseUrl(getInstance().getUri().toURL().toString())
                        .build();
            } catch (Exception e) {
                log.warn("Failed to get stats from stats service", e);
                throw new StatsServerUnavailable("Ошибка вызова сервиса статистики с id: " + STATS_SERVICE_NAME);
            }
        }

        return restClient;
    }
}

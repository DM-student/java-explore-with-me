package ru.practicum.stats.httpclient;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.stats.dto.StatsGroupData;
import ru.practicum.stats.dto.StatsRecord;

import java.time.LocalDateTime;
import java.util.*;

/*
Я не до конца понимаю принцип работы авто-поиска бинов и зависимостей спринга
так что я оставлю такой вот "прототип", на основе которого будут делать
наследников, помечая их как бины, а также внедряя зависимости через
конструктор.
 */
public class StatsHttpClientPrototype {
    private final RestTemplate rest;

    public StatsHttpClientPrototype(String statsServerUrl, RestTemplateBuilder builder) {
        rest = builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(statsServerUrl))
                .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                .build();
    }

    private HttpHeaders defaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }

    private List<StatsGroupData> get(LocalDateTime start, LocalDateTime end, @Nullable List<String> uris, boolean unique) {
        Map<String, Object> parameters = new HashMap<>();

        parameters.put("start", start);
        parameters.put("end", start);
        parameters.put("unique", start);
        if (uris != null) {
            parameters.put("uris", uris);
        }

        HttpEntity<Object> requestEntity = new HttpEntity<>(null, defaultHeaders());
        ResponseEntity<StatsGroupData[]> statsServerResponse;

        statsServerResponse = rest.exchange("/stats", HttpMethod.GET, requestEntity, StatsGroupData[].class, parameters);
        return Arrays.stream(Objects.requireNonNull(statsServerResponse.getBody())).toList();
    }

    private void hit(StatsRecord statsToHit) throws StatsHttpClientHitException {
        HttpEntity<StatsRecord> requestEntity = new HttpEntity<>(statsToHit, defaultHeaders());
        ResponseEntity<Object> statsServerResponse;

        statsServerResponse = rest.exchange("/hit", HttpMethod.POST, requestEntity, Object.class);
        if (statsServerResponse.getStatusCode() != HttpStatus.CREATED) {
            throw new StatsHttpClientHitException();
        }
    }
}

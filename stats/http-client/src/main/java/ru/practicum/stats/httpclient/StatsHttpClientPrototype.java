package ru.practicum.stats.httpclient;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.stats.dto.StatsConstants;
import ru.practicum.stats.dto.StatsGroupData;
import ru.practicum.stats.dto.StatsRecord;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/*
Я не до конца понимаю принцип работы авто-поиска бинов и зависимостей спринга
так что я оставлю такой вот "прототип", на основе которого будут делать
наследников, помечая их как бины, а также внедряя зависимости через
конструктор.
 */
public class StatsHttpClientPrototype {
    private final RestTemplate rest;
    private final DateTimeFormatter formatter = StatsConstants.DATE_TIME_FORMATTER;

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

    public StatsGroupData[] get(LocalDateTime start, LocalDateTime end, @Nullable String[] uris, boolean unique) {
        HttpEntity<Object> requestEntity = new HttpEntity<>(null, defaultHeaders());
        ResponseEntity<StatsGroupData[]> statsServerResponse;

        UriComponentsBuilder uri = UriComponentsBuilder.fromPath("/stats")
                .queryParam("start", "{start}")
                .queryParam("end", "{end}")
                .queryParam("unique", "{unique}");

        if(uris == null) {
            statsServerResponse = rest.exchange(uri.encode().toUriString(), HttpMethod.GET, requestEntity, StatsGroupData[].class,
                    start.format(formatter), end.format(formatter), unique);
        } else {
            uri.queryParam("uris", "{uris}");
            statsServerResponse = rest.exchange(uri.encode().toUriString(), HttpMethod.GET, requestEntity, StatsGroupData[].class,
                    start.format(formatter), end.format(formatter), unique, uris);
        }
        return statsServerResponse.getBody();
    }

    public void hit(StatsRecord statsToHit) throws StatsHttpClientHitException {
        HttpEntity<StatsRecord> requestEntity = new HttpEntity<>(statsToHit, defaultHeaders());
        ResponseEntity<Object> statsServerResponse;

        statsServerResponse = rest.exchange("/hit", HttpMethod.POST, requestEntity, Object.class);
        if (statsServerResponse.getStatusCode() != HttpStatus.CREATED) {
            throw new StatsHttpClientHitException();
        }
    }
}

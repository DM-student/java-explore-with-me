package ru.practicum.gateway.stats;


import org.json.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;

@Service
public class Stats {
    final RestTemplate rest;

    public Stats(@Value("${stat-server.url}") String statServerUrl, RestTemplateBuilder builder) {
        rest = builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(statServerUrl))
                .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                .build();
    }

    public ResponseEntity<String> hit(String app, HttpServletRequest requestData) {

        JSONObject json = new JSONObject();
        json.append("app", app);
        json.append("uri", requestData.getContextPath());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<String> requestEntity = new HttpEntity<>(json.toString(), headers);
        return rest.exchange("/hit", HttpMethod.POST, requestEntity, String.class);
    }
}

package ru.practicum.main_service.server.utility;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import ru.practicum.stats.httpclient.StatsHttpClientPrototype;

@Component
public class StatsClient extends StatsHttpClientPrototype {

    @Autowired
    public StatsClient(@Value("${stats-server.url}") String statsServerUrl, RestTemplateBuilder builder) {
        super(statsServerUrl, builder);
    }
}

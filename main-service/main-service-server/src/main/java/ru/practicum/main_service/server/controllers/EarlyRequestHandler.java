package ru.practicum.main_service.server.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.main_service.server.utility.StatsClient;
import ru.practicum.stats.dto.StatsRecord;
import ru.practicum.stats.httpclient.StatsHttpClientHitException;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Component
public class EarlyRequestHandler {

    @Value("${service.name}")
    private String serviceName;

    @Autowired
    private StatsClient statsClient;

    public void handle(HttpServletRequest servletRequestEntity) throws StatsHttpClientHitException {
        StatsRecord statsRecord = new StatsRecord();

        statsRecord.setApp(serviceName);
        statsRecord.setUri(servletRequestEntity.getRequestURI());
        statsRecord.setIp(servletRequestEntity.getRemoteAddr());
        statsRecord.setTimestamp(LocalDateTime.now());

        statsClient.hit(statsRecord);
    }
}

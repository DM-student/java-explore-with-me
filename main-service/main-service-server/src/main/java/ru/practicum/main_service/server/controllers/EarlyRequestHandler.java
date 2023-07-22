package ru.practicum.main_service.server.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;
import ru.practicum.main_service.server.utility.StatsClient;

import javax.servlet.http.HttpServletRequest;

@Component
public class EarlyRequestHandler {
    @Autowired
    StatsClient statsClient;

    public void handle(HttpServletRequest servletRequestEntity) {
        statsClient.hit();
    }
}

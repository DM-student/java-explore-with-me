package ru.practicum.main_service.server.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.RequestEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main_service.server.dto.CompilationDtoResponse;
import ru.practicum.main_service.server.dto.EventDto;
import ru.practicum.main_service.server.dto.EventDtoResponse;
import ru.practicum.main_service.server.services.EventService;
import ru.practicum.main_service.server.utility.errors.BadRequestError;
import ru.practicum.stats.httpclient.StatsHttpClientHitException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class EventController {
    @Autowired
    EventService service;
    @Autowired
    EarlyRequestHandler earlyRequestHandler;

    @GetMapping("/users/{userId}/events")
    public List<EventDtoResponse> getAllForUser(HttpServletRequest servletRequest,
                                         @RequestParam int userId,
                                         @RequestParam(defaultValue = "0") int from,
                                         @RequestParam(defaultValue = "10") int size) throws StatsHttpClientHitException {
        earlyRequestHandler.handle(servletRequest);

        return service.getAllForUser(userId, from, size);
    }

    @PostMapping("/users/{userId}/events")
    public EventDtoResponse post(HttpServletRequest servletRequest,
                                         @RequestParam int userId,
                                         @RequestBody EventDto event,
                                         @RequestParam(defaultValue = "0") int from,
                                         @RequestParam(defaultValue = "10") int size) throws StatsHttpClientHitException {
        earlyRequestHandler.handle(servletRequest);

        event.setInitiator(userId);
        return service.postEvent(event);
    }

    @PatchMapping("/users/{userId}/events/{eventId}")
    public EventDtoResponse patch(HttpServletRequest servletRequest,
                                 @RequestParam int userId, @RequestParam int eventId,
                                 @RequestBody EventDto event,
                                 @RequestParam(defaultValue = "0") int from,
                                 @RequestParam(defaultValue = "10") int size) throws StatsHttpClientHitException {
        earlyRequestHandler.handle(servletRequest);

        event.setInitiator(userId);
        event.setId(eventId);
        return service.postEvent(event);
    }
}

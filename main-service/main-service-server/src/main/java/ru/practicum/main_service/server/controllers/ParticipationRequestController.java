package ru.practicum.main_service.server.controllers;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main_service.server.dto.CompilationDtoResponse;
import ru.practicum.main_service.server.dto.ParticipationRequestDto;
import ru.practicum.main_service.server.services.RequestsService;
import ru.practicum.stats.httpclient.StatsHttpClientHitException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
public class ParticipationRequestController {
    @Autowired
    private EarlyRequestHandler earlyRequestHandler;

    @Autowired
    private RequestsService service;

    @GetMapping("/users/{userId}/requests")
    public List<ParticipationRequestDto> getForUser(HttpServletRequest servletRequest,
                                             @PathVariable int userId) throws StatsHttpClientHitException {
        earlyRequestHandler.handle(servletRequest);

        return service.getRequests(userId);
    }

    @PostMapping("/users/{userId}/requests")
    public ResponseEntity<ParticipationRequestDto> post(HttpServletRequest servletRequest,
                                                       @PathVariable int userId, @RequestParam int eventId) throws StatsHttpClientHitException {
        earlyRequestHandler.handle(servletRequest);

        return new ResponseEntity<>(service.createRequest(userId, eventId), HttpStatus.CREATED);
    }

    @PatchMapping("/users/{userId}/requests/{requestId}/cancel")
    public ParticipationRequestDto cancel(HttpServletRequest servletRequest,
                                        @PathVariable int userId, @PathVariable int requestId) throws StatsHttpClientHitException {
        earlyRequestHandler.handle(servletRequest);

        return service.cancelRequest(userId, requestId);
    }

    @GetMapping("/users/{userId}/events/{eventId}/requests")
    public List<ParticipationRequestDto> getForUser(HttpServletRequest servletRequest,
                                                    @PathVariable int userId, @PathVariable int eventId) throws StatsHttpClientHitException {
        earlyRequestHandler.handle(servletRequest);

        return service.getRequestsForEvent(userId, eventId);
    }


    @PatchMapping("/users/{userId}/events/{eventId}/requests")
    public Map<String, List<ParticipationRequestDto>> patch(HttpServletRequest servletRequest,
                                                            @PathVariable int userId, @PathVariable int eventId,
                                                            @RequestBody String jsonString) throws StatsHttpClientHitException {
        earlyRequestHandler.handle(servletRequest);

        JSONObject json = new JSONObject(jsonString);
        return service.handleRequestsUpdate(userId, eventId, json);
    }
}

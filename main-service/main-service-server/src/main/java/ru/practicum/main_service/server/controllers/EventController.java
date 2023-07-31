package ru.practicum.main_service.server.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
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
    private EventService service;
    @Autowired
    private EarlyRequestHandler earlyRequestHandler;

    // PRIVATE ENDPOINTS

    @GetMapping("/users/{userId}/events")
    public List<EventDtoResponse> getAllForUser(HttpServletRequest servletRequest,
                                         @PathVariable int userId,
                                         @RequestParam(defaultValue = "0") int from,
                                         @RequestParam(defaultValue = "10") int size) throws StatsHttpClientHitException {
        earlyRequestHandler.handle(servletRequest);

        return service.getAllForUser(userId, from, size);
    }

    @GetMapping("/users/{userId}/events/{eventId}")
    public EventDtoResponse getForUser(HttpServletRequest servletRequest,
                                       @PathVariable int userId, @PathVariable int eventId) throws StatsHttpClientHitException {
        earlyRequestHandler.handle(servletRequest);

        return service.getByIdForUser(eventId, userId, servletRequest.getRemoteAddr());
    }


    @PostMapping("/users/{userId}/events")
    public ResponseEntity<EventDtoResponse> post(HttpServletRequest servletRequest,
                                                 @PathVariable int userId,
                                                 @RequestBody EventDto event) throws StatsHttpClientHitException {
        earlyRequestHandler.handle(servletRequest);

        event.setInitiator(userId);
        return new ResponseEntity<>(service.postEvent(event), HttpStatus.CREATED);
    }

    @PatchMapping("/users/{userId}/events/{eventId}")
    public EventDtoResponse patch(HttpServletRequest servletRequest,
                                 @PathVariable int userId, @PathVariable int eventId,
                                 @RequestBody EventDto event) throws StatsHttpClientHitException {
        earlyRequestHandler.handle(servletRequest);

        event.setInitiator(userId);
        event.setId(eventId);
        return service.patchEventUser(event);
    }

    // ADMIN ENDPOINTS

    @PatchMapping("/admin/events/{eventId}")
    public EventDtoResponse adminPatch(HttpServletRequest servletRequest,
                                  @PathVariable int eventId,
                                  @RequestBody EventDto event) throws StatsHttpClientHitException {
        earlyRequestHandler.handle(servletRequest);

        event.setId(eventId);
        return service.patchEvent(event);
    }

    @GetMapping("/admin/events")
    public List<EventDtoResponse> getAllAdmin(HttpServletRequest servletRequest,
                                         @RequestParam(required = false) List<Integer> users,
                                         @RequestParam(required = false) List<String> states,
                                         @RequestParam(required = false) List<Integer> categories,
                                         @RequestParam(required = false) String rangeStart,
                                         @RequestParam(required = false) String rangeEnd,
                                         @RequestParam(defaultValue = "0") int from,
                                         @RequestParam(defaultValue = "10") int size) throws StatsHttpClientHitException {
        earlyRequestHandler.handle(servletRequest);

        return service.getAllAdmin(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    // PUBLIC ENDPOINTS

    @GetMapping("/events")
    public List<EventDtoResponse> getAll(HttpServletRequest servletRequest,
                                         @RequestParam(required = false) String text,
                                         @RequestParam(required = false) Boolean paid,
                                         @RequestParam(required = false) List<Integer> categories,
                                         @RequestParam(required = false) String rangeStart,
                                         @RequestParam(required = false) String rangeEnd,
                                         @RequestParam(defaultValue = "false") boolean onlyAvailable,
                                         @RequestParam(required = false) EventService.Sort sort,
                                         @RequestParam(defaultValue = "0") int from,
                                         @RequestParam(defaultValue = "10") int size) throws StatsHttpClientHitException {
        earlyRequestHandler.handle(servletRequest);

        return service.getAll(text, paid, categories, rangeStart, rangeEnd, onlyAvailable, sort, from, size);
    }

    @GetMapping("/events/{eventId}")
    public EventDtoResponse getById(HttpServletRequest servletRequest,
                                    @PathVariable int eventId) throws StatsHttpClientHitException {
        earlyRequestHandler.handle(servletRequest);

        return service.getByIdPublished(eventId, servletRequest.getRemoteAddr());
    }
}

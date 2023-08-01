package ru.practicum.main_service.server.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.main_service.server.database.EventDatabase;
import ru.practicum.main_service.server.database.UserDatabase;
import ru.practicum.main_service.server.dto.EventDto;
import ru.practicum.main_service.server.dto.EventDtoResponse;
import ru.practicum.main_service.server.dto.MainServiceDtoConstants;
import ru.practicum.main_service.server.utility.errors.BadRequestError;
import ru.practicum.main_service.server.utility.errors.ConflictError;
import ru.practicum.main_service.server.utility.errors.NotFoundError;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Service
public class EventService {
    private static final DateTimeFormatter formatter = MainServiceDtoConstants.DATE_TIME_FORMATTER;

    public enum Sort {
        EVENT_DATE,
        VIEWS
    }

    @Autowired
    private EventDatabase database;
    @Autowired
    private UserDatabase userDatabase;

    public EventDtoResponse getById(int id) {
        return database.getEvent(id);
    }

    public EventDtoResponse getByIdPublished(int id, String ip) {
        EventDtoResponse event = database.getEvent(id);
        if (!Objects.equals(event.getState(), "PUBLISHED")) {
            throw new NotFoundError("Событие не найдено среди опубликованных.");
        }
        return event;
    }

    public EventDtoResponse getByIdForUser(int eventId, int userId, String ip) {
        EventDtoResponse event = database.getEvent(eventId);
        if (event.getInitiator().getId() != userId) {
            throw new BadRequestError("В событиях пользователя не найдено запрошенное.");
        }
        return event;
    }

    public EventDtoResponse postEvent(EventDto eventToPost) {
        eventToPost.setDefaultValues();
        eventToPost.setState("PENDING");
        eventToPost.setCreatedOn(formatter.format(LocalDateTime.now()));
        userDatabase.getUser(eventToPost.getInitiator());
        if (!eventToPost.isValid()) {
            throw new BadRequestError("Ошибка объекта.", eventToPost);
        }
        return database.createEvent(eventToPost);
    }

    public EventDtoResponse patchEventUser(EventDto eventToPost) {
        EventDtoResponse event = database.getEvent(eventToPost.getId());
        if (event.getState().equals("PUBLISHED")) {
            throw new ConflictError("Несанкционированная попытка изменить опубликованное событие.", eventToPost);
        }

        if (!Objects.equals(eventToPost.getInitiator(), event.getInitiator().getId())) {
            throw new BadRequestError("Вы не являетесь владельцем события.");
        }

        if (eventToPost.getStateAction() != null) {
            if (!(eventToPost.getStateAction().equals("SEND_TO_REVIEW") || eventToPost.getStateAction().equals("CANCEL_REVIEW"))) {
                throw new ConflictError("Несанкционированная попытка изменить состояние события.", eventToPost);
            }
        }

        return patchEvent(eventToPost);
    }

    public EventDtoResponse patchEvent(EventDto eventToPost) {
        eventToPost.setState(null); // Так как состояние меняется через stateAction.
        EventDtoResponse oldEvent = database.getEvent(eventToPost.getId());
        if (eventToPost.getStateAction() != null) {
            if (eventToPost.getStateAction().equals("PUBLISH_EVENT")) {
                if (!oldEvent.getState().equals("PENDING")) {
                    throw new ConflictError("Не возможна публикация данного события в его текущем состоянии.", eventToPost);
                }
                eventToPost.setPublishedOn(formatter.format(LocalDateTime.now()));
                eventToPost.setState("PUBLISHED");
            }
            if (eventToPost.getStateAction().equals("REJECT_EVENT")) {
                if (oldEvent.getState().equals("PUBLISHED")) {
                    throw new ConflictError("Не возможна отмена уже опубликованного события.", eventToPost);
                }
                eventToPost.setState("CANCELED");
            }
            if (eventToPost.getStateAction().equals("SEND_TO_REVIEW")) {
                eventToPost.setState("PENDING");
            }
            if (eventToPost.getStateAction().equals("CANCEL_REVIEW")) {
                if (oldEvent.getState().equals("PUBLISHED")) {
                    throw new ConflictError("Не возможна отмена уже опубликованного события.", eventToPost);
                }
                eventToPost.setState("CANCELED");
            }
        }
        if (!eventToPost.isValidSkipNulls()) {
            throw new BadRequestError("Ошибка объекта.", eventToPost);
        }

        return database.updateEvent(eventToPost);
    }

    public List<EventDtoResponse> getAllForUser(int userId, int from, int size) {
        String query = "initiator_id = " + userId;
        return database.getEvents(from, size, query);
    }


    public List<EventDtoResponse> getAll(String text, Boolean paid, List<Integer> categories,
                                         String rangeStart, String rangeEnd, boolean onlyAvailable,
                                         Sort sort, int from, int size) {
        // Валидация данных.

        if (rangeStart != null && rangeEnd != null) {
            if (LocalDateTime.parse(rangeStart, formatter).isAfter(LocalDateTime.parse(rangeEnd, formatter))) {
                throw new BadRequestError("В поисковом запросе дата начала позже даты конца.",
                        rangeStart + " | " + rangeEnd);
            }
        }

        return database.getEventsWithFilter(text, paid, categories, rangeStart, rangeEnd, onlyAvailable, sort, from, size);
    }

    public List<EventDtoResponse> getAllAdmin(List<Integer> users, List<String> states, List<Integer> categories,
                                              String rangeStart, String rangeEnd,
                                              int from, int size) {
        // Валидация данных.

        if (rangeStart != null && rangeEnd != null) {
            if (LocalDateTime.parse(rangeStart, formatter).isAfter(LocalDateTime.parse(rangeEnd, formatter))) {
                throw new BadRequestError("В поисковом запросе дата начала позже даты конца.",
                        rangeStart + " | " + rangeEnd);
            }
        }

        return database.getEventsWithAdminFilters(users, states, categories, rangeStart, rangeEnd, from, size);
    }
}


package ru.practicum.main_service.server.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.main_service.server.database.EventDatabase;
import ru.practicum.main_service.server.database.UserDatabase;
import ru.practicum.main_service.server.dto.EventDto;
import ru.practicum.main_service.server.dto.EventDtoResponse;
import ru.practicum.main_service.server.dto.MainServiceDtoConstants;
import ru.practicum.main_service.server.utility.errors.BadRequestError;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

    public EventDtoResponse getByIdPublished(int id) {
        EventDtoResponse event = database.getEvent(id);
        if(!Objects.equals(event.getState(), "PUBLISHED")) {
            throw new BadRequestError("Событие не найдено среди опубликованных.");
        }
        return event;
    }

    public EventDtoResponse getByIdForUser(int eventId, int userId) {
        EventDtoResponse event = database.getEvent(eventId);
        if(event.getInitiator().getId() != userId) {
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

        if(eventToPost.getInitiator() != event.getInitiator().getId()) {
            throw new BadRequestError("Вы не являетесь владельцем события.");
        }

        eventToPost.setState(null);
        return postEvent(eventToPost);
    }

    public EventDtoResponse patchEvent(EventDto eventToPost) {
        eventToPost.setState(null); // Так как состояние меняется через stateAction.
        if(eventToPost.getStateAction() != null) {
            if (eventToPost.getStateAction().equals("PUBLISH_EVENT")) {
                eventToPost.setState("PUBLISHED");
            }
            if (eventToPost.getStateAction().equals("REJECT_EVENT")) {
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


    public List<EventDtoResponse> getAllAdmin(String text, Boolean paid, List<Integer> categories,
                                              String rangeStart, String rangeEnd, boolean onlyAvailable,
                                              Sort sort, int from, int size) {
        StringBuilder query = new StringBuilder();
        boolean shouldAddAnd = false;

        if(text != null) {
            query.append("(");
            query.append("annotation LIKE '%");
            query.append(text);
            query.append("%' OR ");
            query.append("title LIKE '%");
            query.append(text);
            query.append("%' OR ");
            query.append("description LIKE '%");
            query.append(text);
            query.append("%')");

            shouldAddAnd = true;
        }

        if (shouldAddAnd) {
            query.append("AND");
            shouldAddAnd = false;
        }

        if(paid != null) {
            query.append("paid = ");
            query.append(paid);

            shouldAddAnd = true;
        }

        if (shouldAddAnd) {
            query.append("AND");
            shouldAddAnd = false;
        }

        // Категории
        if(categories != null && !categories.isEmpty()) {
            query.append("(");
            for (int i = 0; i < categories.size(); i++) {
                if(i > 0) {
                    query.append("OR");
                }
                query.append("category_id = ");
                query.append(categories.get(i));
            }
            query.append(")");
            shouldAddAnd = true;
        }

        if (shouldAddAnd) {
            query.append("AND");
            shouldAddAnd = false;
        }

        // Даты...
        if (rangeStart != null) {
            query.append("event_date >= ");
            query.append(rangeStart);
            shouldAddAnd = true;
        }

        if (shouldAddAnd) {
            query.append("AND");
            shouldAddAnd = false;
        }

        if (rangeEnd != null) {
            query.append("event_date < ");
            query.append(rangeEnd);
            shouldAddAnd = true;
        }

        if (shouldAddAnd) {
            query.append("AND");
            shouldAddAnd = false;
        }

        if (sort != null) {
            query.append("ORDER BY ");
            query.append(sort);
            shouldAddAnd = true;
        }
        List<EventDtoResponse> output = database.getEvents(from, size, query.toString());
        if(onlyAvailable) {
            return output.stream().filter(event -> event.getConfirmedRequests() < event.getParticipantLimit())
                    .collect(Collectors.toList());
        }
        return output;
    }
}


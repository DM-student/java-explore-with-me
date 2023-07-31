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

    public EventDtoResponse getByIdPublished(int id, String ip) {
        EventDtoResponse event = database.getEvent(id);
        if(!Objects.equals(event.getState(), "PUBLISHED")) {
            throw new NotFoundError("Событие не найдено среди опубликованных.");
        }
        database.incrementViews(id, ip);
        return event;
    }

    public EventDtoResponse getByIdForUser(int eventId, int userId, String ip) {
        EventDtoResponse event = database.getEvent(eventId);
        if(event.getInitiator().getId() != userId) {
            throw new BadRequestError("В событиях пользователя не найдено запрошенное.");
        }
        database.incrementViews(eventId, ip);
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

        if(eventToPost.getInitiator() != event.getInitiator().getId()) {
            throw new BadRequestError("Вы не являетесь владельцем события.");
        }

        if(eventToPost.getStateAction() != null) {
            if (!(eventToPost.getStateAction().equals("SEND_TO_REVIEW") || eventToPost.getStateAction().equals("CANCEL_REVIEW")))  {
                throw new ConflictError("Несанкционированная попытка изменить состояние события.", eventToPost);
            }
        }

        return patchEvent(eventToPost);
    }

    public EventDtoResponse patchEvent(EventDto eventToPost) {
        eventToPost.setState(null); // Так как состояние меняется через stateAction.
        EventDtoResponse oldEvent = database.getEvent(eventToPost.getId());
        if(eventToPost.getStateAction() != null) {
            if (eventToPost.getStateAction().equals("PUBLISH_EVENT")) {
                if(!oldEvent.getState().equals("PENDING")) {
                    throw new ConflictError("Не возможна публикация данного события в его текущем состоянии.", eventToPost);
                }
                eventToPost.setPublishedOn(formatter.format(LocalDateTime.now()));
                eventToPost.setState("PUBLISHED");
            }
            if (eventToPost.getStateAction().equals("REJECT_EVENT")) {
                if(oldEvent.getState().equals("PUBLISHED")) {
                    throw new ConflictError("Не возможна отмена уже опубликованного события.", eventToPost);
                }
                eventToPost.setState("CANCELED");
            }
            if (eventToPost.getStateAction().equals("SEND_TO_REVIEW")) {
                eventToPost.setState("PENDING");
            }
            if (eventToPost.getStateAction().equals("CANCEL_REVIEW")) {
                if(oldEvent.getState().equals("PUBLISHED")) {
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

        // Сам поиск
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
            query.append("%') ");

            shouldAddAnd = true;
        }

        if(paid != null) {
            if (shouldAddAnd) {
                query.append("AND ");
            }

            query.append("paid = ");
            query.append(paid);
            query.append(" ");

            shouldAddAnd = true;
        }

        // Категории
        if(categories != null && !categories.isEmpty()) {
            if (shouldAddAnd) {
                query.append("AND ");
            }

            query.append("(");
            for (int i = 0; i < categories.size(); i++) {
                if(i > 0) {
                    query.append("OR ");
                }
                query.append("category_id = ");
                query.append(categories.get(i));
                query.append(" ");
            }
            query.append(") ");
            shouldAddAnd = true;
        }

        // Даты...
        if (rangeStart != null) {
            if (shouldAddAnd) {
                query.append("AND ");
            }

            query.append("event_date >= '");
            query.append(rangeStart);
            query.append("' ");
            shouldAddAnd = true;
        }

        if (rangeEnd != null) {
            if (shouldAddAnd) {
                query.append("AND ");
            }

            query.append("event_date < '");
            query.append(rangeEnd);
            query.append("' ");
            shouldAddAnd = true;
        }

        if (shouldAddAnd) {
            query.append("AND ");
        }

        query.append("state = 'PUBLISHED' ");

        if (sort != null) {
            query.append("ORDER BY ");
            query.append(sort);
        }

        List<EventDtoResponse> output = database.getEvents(from, size, query.toString());
        if(onlyAvailable) {
            return output.stream().filter(event -> event.getConfirmedRequests() < event.getParticipantLimit())
                    .collect(Collectors.toList());
        }
        return output;
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

        // Сам поиск
        StringBuilder query = new StringBuilder();
        boolean shouldAddAnd = false;

        // Юзеры

        if(users != null && !users.isEmpty()) {
            query.append("(");
            for (int i = 0; i < users.size(); i++) {
                if(i > 0) {
                    query.append("OR ");
                }
                query.append("initiator_id = ");
                query.append(users.get(i));
                query.append(" ");
            }
            query.append(") ");
            shouldAddAnd = true;
        }

        // Состояния

        if(states != null && !states.isEmpty()) {
            if (shouldAddAnd) {
                query.append("AND ");
            }

            query.append("(");
            for (int i = 0; i < states.size(); i++) {
                if(i > 0) {
                    query.append("OR ");
                }
                query.append("state = '"); // Конечно есть опасность инъекции,
                query.append(states.get(i)); // но за-то полностью на стороне БД фильтрация происходить будет.
                query.append("' ");
            }
            query.append(") ");
            shouldAddAnd = true;
        }

        // Категории
        if(categories != null && !categories.isEmpty()) {
            if (shouldAddAnd) {
                query.append("AND ");
            }

            query.append("(");
            for (int i = 0; i < categories.size(); i++) {
                if(i > 0) {
                    query.append("OR ");
                }
                query.append("category_id = ");
                query.append(categories.get(i));
                query.append(" ");
            }
            query.append(") ");
            shouldAddAnd = true;
        }

        // Даты
        if (rangeStart != null) {
            if (shouldAddAnd) {
                query.append("AND ");
            }

            query.append("event_date >= '");
            query.append(rangeStart);
            query.append("' ");
            shouldAddAnd = true;
        }

        if (rangeEnd != null) {
            if (shouldAddAnd) {
                query.append("AND ");
            }

            query.append("event_date <= '");
            query.append(rangeEnd);
            query.append("' ");
        }

        return database.getEvents(from, size, query.toString());
    }
}


package ru.practicum.main_service.server.database;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.practicum.main_service.server.dto.*;
import ru.practicum.main_service.server.services.EventService;
import ru.practicum.main_service.server.utility.StatsClient;
import ru.practicum.main_service.server.utility.errors.NotFoundError;
import ru.practicum.stats.dto.StatsGroupData;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class EventDatabase {

    private final DateTimeFormatter formatter = MainServiceDtoConstants.DATE_TIME_FORMATTER;
    private final JdbcTemplate jdbcTemplate;
    private final UserDatabase userDatabase;
    private final CategoryDatabase categoryDatabase;
    private final StatsClient statsClient;
    private final ParticipationRequestsDatabase requestsDatabase;

    @Autowired
    public EventDatabase(JdbcTemplate jdbcTemplate,
                         UserDatabase userDatabase,
                         CategoryDatabase categoryDatabase,
                         StatsClient statsClient,
                         ParticipationRequestsDatabase participationRequestsDatabase) {
        this.jdbcTemplate = jdbcTemplate;
        this.userDatabase = userDatabase;
        this.categoryDatabase = categoryDatabase;
        this.statsClient = statsClient;
        this.requestsDatabase = participationRequestsDatabase;
    }

    public List<EventDtoResponse> mapEvents(SqlRowSet rs) {
        List<EventDtoResponse> events = new ArrayList<>();
        List<Integer> userIds = new ArrayList<>();
        List<Integer> categoryIds = new ArrayList<>();
        List<Integer> eventIds = new ArrayList<>();
        while (rs.next()) {
            EventDtoResponse event = new EventDtoResponse();
            event.setAnnotation(rs.getString("annotation"));

            event.setCreatedOn(formatter.format(rs.getTimestamp("created_on").toLocalDateTime()));
            event.setDescription(rs.getString("description"));
            event.setEventDate(formatter.format(rs.getTimestamp("event_date").toLocalDateTime()));
            event.setId(rs.getInt("id"));

            eventIds.add(event.getId());
            categoryIds.add(rs.getInt("category_id"));
            userIds.add(rs.getInt("initiator_id"));

            LocationDto location = new LocationDto();
            location.setLat(rs.getDouble("location_lat"));
            location.setLon(rs.getDouble("location_lon"));

            event.setLocation(location);
            event.setPaid(rs.getBoolean("paid"));
            event.setParticipantLimit(rs.getInt("participant_limit"));

            if (rs.getObject("published_date") != null) {
                event.setPublishedOn(formatter.format(rs.getTimestamp("published_date").toLocalDateTime()));
            }

            event.setRequestModeration(rs.getBoolean("request_moderation"));
            event.setState(rs.getString("state"));
            event.setTitle(rs.getString("title"));

            events.add(event);
        }
        Map<Integer, UserDto> usersMap = userDatabase.getUsersMap(userIds);
        Map<Integer, CategoryDto> categoriesMap = categoryDatabase.getCategoriesMap(categoryIds);
        Map<Integer, Integer> viewsMap = getViewsMap(eventIds);
        Map<Integer, Integer> requestsCountMap = requestsDatabase.getConfirmedRequestsCountForEventMap(eventIds);
        for (int i = 0; i < events.size(); i++) {
            events.get(i).setCategory(categoriesMap.get(categoryIds.get(i)));
            events.get(i).setConfirmedRequests(requestsCountMap.get(eventIds.get(i)));
            events.get(i).setInitiator(usersMap.get(userIds.get(i)));
            events.get(i).setViews(viewsMap.get(eventIds.get(i)));
        }

        return events;
    }

    public EventDtoResponse getEvent(Integer id) {
        String sqlQuery =
                "SELECT * FROM events WHERE id = ?;";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sqlQuery, id);
        List<EventDtoResponse> output = mapEvents(rs);
        if (output.isEmpty()) {
            throw new NotFoundError("Не найдено событие.", id);
        }
        return output.get(0);
    }

    public List<EventDtoResponse> getEvents(int from, int size) {
        String sqlQuery =
                "SELECT * FROM events LIMIT ? OFFSET ?;";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sqlQuery, size, from);

        return mapEvents(rs);
    }

    public Map<Integer, EventDtoResponse> getEventsMap(List<Integer> ids) {
        if (ids.isEmpty()) {
            return new HashMap<>();
        }
        StringBuilder sqlQuery = new StringBuilder("SELECT * FROM events WHERE id IN (");
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) sqlQuery.append(", ");
            sqlQuery.append(ids.get(i));
        }
        sqlQuery.append(");");

        SqlRowSet rs = jdbcTemplate.queryForRowSet(sqlQuery.toString());

        Map<Integer, EventDtoResponse> EventsMap = new HashMap<>();
        for (EventDtoResponse event : mapEvents(rs)) {
            EventsMap.put(event.getId(), event);
        }
        return EventsMap;
    }

    public List<EventDtoResponse> getEvents(int from, int size, String query) {
        if (query == null || query.isBlank()) {
            return getEvents(from, size);
        }

        String sqlQuery =
                "SELECT * FROM events WHERE " + query + " LIMIT ? OFFSET ?;";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sqlQuery, size, from);

        return mapEvents(rs);
    }

    public List<EventDtoResponse> getEventsWithFilter(String text, Boolean paid, List<Integer> categories,
                                                      String rangeStart, String rangeEnd, boolean onlyAvailable,
                                                      EventService.Sort sort, int from, int size) {
        // Сам поиск
        StringBuilder query = new StringBuilder();
        boolean shouldAddAnd = false;

        if (text != null) {
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

        if (paid != null) {
            if (shouldAddAnd) {
                query.append("AND ");
            }

            query.append("paid = ");
            query.append(paid);
            query.append(" ");

            shouldAddAnd = true;
        }

        // Категории
        if (categories != null && !categories.isEmpty()) {
            if (shouldAddAnd) {
                query.append("AND ");
            }

            query.append("(");
            for (int i = 0; i < categories.size(); i++) {
                if (i > 0) {
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

        List<EventDtoResponse> output = getEvents(from, size, query.toString());
        if (onlyAvailable) {
            return output.stream().filter(event -> event.getConfirmedRequests() < event.getParticipantLimit())
                    .collect(Collectors.toList());
        }
        return output;
    }

    public List<EventDtoResponse> getEventsWithAdminFilters(List<Integer> users, List<String> states, List<Integer> categories,
                                                            String rangeStart, String rangeEnd,
                                                            int from, int size) {
        // Сам поиск
        StringBuilder query = new StringBuilder();
        boolean shouldAddAnd = false;

        // Юзеры

        if (users != null && !users.isEmpty()) {
            query.append("(");
            for (int i = 0; i < users.size(); i++) {
                if (i > 0) {
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

        if (states != null && !states.isEmpty()) {
            if (shouldAddAnd) {
                query.append("AND ");
            }

            query.append("(");
            for (int i = 0; i < states.size(); i++) {
                if (i > 0) {
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
        if (categories != null && !categories.isEmpty()) {
            if (shouldAddAnd) {
                query.append("AND ");
            }

            query.append("(");
            for (int i = 0; i < categories.size(); i++) {
                if (i > 0) {
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

        return getEvents(from, size, query.toString());
    }

    public void deleteEvent(int id) {
        String sqlQuery = "DELETE FROM events " +
                "WHERE id = ?";
        jdbcTemplate.update(sqlQuery, id);
    }

    public EventDtoResponse createEvent(EventDto event) {
        String sqlQuery = "INSERT INTO events " +
                "(annotation, " +
                "category_id, " +
                "created_on, " +
                "description, " +
                "event_date, " +
                "initiator_id, " +
                "location_lat, " +
                "location_lon, " +
                "paid, " +
                "participant_limit, " +
                "request_moderation, " +
                "state, " +
                "title) " +
                "VALUES (?, " + // annotation
                "?, " + // category_id
                "?, " + // created_on
                "?, " + // description
                "?, " + // event_date
                "?, " + // initiator_id
                "?, " + // location_lat
                "?, " + // location_lon
                "?, " + // paid
                "?, " + // participant_limit
                "?, " + // request_moderation
                "?, " + // state
                "?) " + // title
                "RETURNING *";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sqlQuery,
                event.getAnnotation(),
                event.getCategory(),
                Timestamp.valueOf(LocalDateTime.parse(event.getCreatedOn(), formatter)),
                event.getDescription(),
                Timestamp.valueOf(LocalDateTime.parse(event.getEventDate(), formatter)),
                event.getInitiator(),
                event.getLocation().getLat(),
                event.getLocation().getLon(),
                event.getPaid(),
                event.getParticipantLimit(),
                event.getRequestModeration(),
                event.getState(),
                event.getTitle());
        return mapEvents(rs).get(0);
    }

    public EventDtoResponse updateEvent(EventDto event) {
        if (event.getAnnotation() != null) {
            String sqlQuery =
                    "UPDATE events SET annotation = ? WHERE id = ?;";
            jdbcTemplate.update(sqlQuery, event.getAnnotation(), event.getId());
        }
        if (event.getCategory() != null) {
            String sqlQuery =
                    "UPDATE events SET category_id = ? WHERE id = ?;";
            jdbcTemplate.update(sqlQuery, event.getCategory(), event.getId());
        }
        if (event.getDescription() != null) {
            String sqlQuery =
                    "UPDATE events SET description = ? WHERE id = ?;";
            jdbcTemplate.update(sqlQuery, event.getDescription(), event.getId());
        }
        if (event.getEventDate() != null) {
            String sqlQuery =
                    "UPDATE events SET event_date = ? WHERE id = ?;";
            jdbcTemplate.update(sqlQuery,
                    Timestamp.valueOf(LocalDateTime.parse(event.getEventDate(), formatter)),
                    event.getId());
        }
        if (event.getLocation() != null) {
            String sqlQuery =
                    "UPDATE events SET location_lat = ?, location_lon = ? WHERE id = ?;";
            jdbcTemplate.update(sqlQuery, event.getLocation().getLat(), event.getLocation().getLon(), event.getId());
        }
        if (event.getPaid() != null) {
            String sqlQuery =
                    "UPDATE events SET paid = ? WHERE id = ?;";
            jdbcTemplate.update(sqlQuery, event.getPaid(), event.getId());
        }
        if (event.getParticipantLimit() != null) {
            String sqlQuery =
                    "UPDATE events SET participant_limit = ? WHERE id = ?;";
            jdbcTemplate.update(sqlQuery, event.getParticipantLimit(), event.getId());
        }
        if (event.getRequestModeration() != null) {
            String sqlQuery =
                    "UPDATE events SET request_moderation = ? WHERE id = ?;";
            jdbcTemplate.update(sqlQuery, event.getRequestModeration(), event.getId());
        }
        if (event.getTitle() != null) {
            String sqlQuery =
                    "UPDATE events SET title = ? WHERE id = ?;";
            jdbcTemplate.update(sqlQuery, event.getTitle(), event.getId());
        }

        if (event.getState() != null) {
            String sqlQuery =
                    "UPDATE events SET state = ? WHERE id = ?;";
            jdbcTemplate.update(sqlQuery, event.getState(), event.getId());
        }
        if (event.getPublishedOn() != null) {
            String sqlQuery =
                    "UPDATE events SET published_date = ? WHERE id = ?;";
            jdbcTemplate.update(sqlQuery,
                    Timestamp.valueOf(LocalDateTime.parse(event.getPublishedOn(), formatter)),
                    event.getId());
        }
        return getEvent(event.getId());
    }

    public int getViews(int eventId) {
        String[] uris = new String[1];
        uris[0] = "/events/" + eventId;
        StatsGroupData[] stats = statsClient.get(LocalDateTime.now().minusYears(100),
                LocalDateTime.now().plusYears(100), uris, true);

        if (!(stats != null && stats.length > 0)) {
            return 0;
        }

        return stats[0].getHits();
    }

    public Map<Integer, Integer> getViewsMap(List<Integer> ids) {
        if (ids.isEmpty()) {
            return new HashMap<>();
        }
        String[] uris = new String[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            uris[i] = "/events/" + ids.get(i);
        }
        StatsGroupData[] stats = statsClient.get(LocalDateTime.now().minusYears(100),
                LocalDateTime.now().plusYears(100), uris, true);

        Map<Integer, Integer> statsMap = new HashMap<>();

        for (StatsGroupData stat : stats) {
            statsMap.put(Integer.parseInt(stat.getUri().substring(8)), stat.getHits());
        }

        for (int id : ids) {
            if (!statsMap.containsKey(id)) {
                statsMap.put(id, 0);
            }
        }
        return statsMap;
    }
}

package ru.practicum.main_service.server.database;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.practicum.main_service.server.dto.*;
import ru.practicum.main_service.server.utility.errors.NotFoundError;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class EventDatabase {

    private final DateTimeFormatter formatter = MainServiceDtoConstants.DATE_TIME_FORMATTER;
    private final JdbcTemplate jdbcTemplate;
    private final UserDatabase userDatabase;
    private final CategoryDatabase categoryDatabase;
    private final ParticipationRequestsDatabase requestsDatabase;

    @Autowired
    public EventDatabase(JdbcTemplate jdbcTemplate,
                         UserDatabase userDatabase,
                         CategoryDatabase categoryDatabase,
                         ParticipationRequestsDatabase participationRequestsDatabase) {
        this.jdbcTemplate = jdbcTemplate;
        this.userDatabase = userDatabase;
        this.categoryDatabase = categoryDatabase;
        this.requestsDatabase = participationRequestsDatabase;
    }

    public List<EventDtoResponse> mapEvents(SqlRowSet rs) {
        List<EventDtoResponse> output = new ArrayList<>();
        while (rs.next()) {
            EventDtoResponse event = new EventDtoResponse();
            event.setAnnotation(rs.getString("annotation"));
            event.setCategory(categoryDatabase.getCategory(rs.getInt("category_id")));

            event.setConfirmedRequests(requestsDatabase.getConfirmedRequestsCountForEvent(rs.getInt("id")));

            event.setCreatedOn(formatter.format(rs.getTimestamp("created_on").toLocalDateTime()));
            event.setDescription(rs.getString("description"));
            event.setEventDate(formatter.format(rs.getTimestamp("event_date").toLocalDateTime()));
            event.setId(rs.getInt("id"));
            event.setInitiator(userDatabase.getUser(rs.getInt("initiator_id")));

            LocationDto location = new LocationDto();
            location.setLat(rs.getDouble("location_lat"));
            location.setLon(rs.getDouble("location_lon"));

            event.setLocation(location);
            event.setPaid(rs.getBoolean("paid"));
            event.setParticipantLimit(rs.getInt("participant_limit"));

            if(rs.getObject("published_date") != null) {
                event.setPublishedOn(formatter.format(rs.getTimestamp("published_date").toLocalDateTime()));
            }

            event.setRequestModeration(rs.getBoolean("request_moderation"));
            event.setState(rs.getString("state"));
            event.setTitle(rs.getString("title"));
            event.setViews(getViews(rs.getInt("id")));

            output.add(event);
        }
        return output;
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
    public List<EventDtoResponse> getEvents(int from, int size, String query) {
        if(query == null || query.isBlank()) {
            return getEvents(from, size);
        }

        String sqlQuery =
                "SELECT * FROM events WHERE " + query + " LIMIT ? OFFSET ?;";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sqlQuery, size, from);

        return mapEvents(rs);
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
        if(event.getAnnotation() != null) {
            String sqlQuery =
                    "UPDATE events SET annotation = ? WHERE id = ?;";
            jdbcTemplate.update(sqlQuery, event.getAnnotation(), event.getId());
        }
        if(event.getCategory() != null) {
            String sqlQuery =
                    "UPDATE events SET category_id = ? WHERE id = ?;";
            jdbcTemplate.update(sqlQuery, event.getCategory(), event.getId());
        }
        if(event.getDescription() != null) {
            String sqlQuery =
                    "UPDATE events SET description = ? WHERE id = ?;";
            jdbcTemplate.update(sqlQuery, event.getDescription(), event.getId());
        }
        if(event.getEventDate() != null) {
            String sqlQuery =
                    "UPDATE events SET event_date = ? WHERE id = ?;";
            jdbcTemplate.update(sqlQuery,
                    Timestamp.valueOf(LocalDateTime.parse(event.getEventDate(), formatter)),
                    event.getId());
        }
        if(event.getLocation() != null) {
            String sqlQuery =
                    "UPDATE events SET location_lat = ?, location_lon = ? WHERE id = ?;";
            jdbcTemplate.update(sqlQuery, event.getLocation().getLat(), event.getLocation().getLon(), event.getId());
        }
        if(event.getPaid() != null) {
            String sqlQuery =
                    "UPDATE events SET paid = ? WHERE id = ?;";
            jdbcTemplate.update(sqlQuery, event.getPaid(), event.getId());
        }
        if(event.getParticipantLimit() != null) {
            String sqlQuery =
                    "UPDATE events SET participant_limit = ? WHERE id = ?;";
            jdbcTemplate.update(sqlQuery, event.getParticipantLimit(), event.getId());
        }
        if(event.getRequestModeration() != null) {
            String sqlQuery =
                    "UPDATE events SET request_moderation = ? WHERE id = ?;";
            jdbcTemplate.update(sqlQuery, event.getRequestModeration(), event.getId());
        }
        if(event.getTitle() != null) {
            String sqlQuery =
                    "UPDATE events SET title = ? WHERE id = ?;";
            jdbcTemplate.update(sqlQuery, event.getTitle(), event.getId());
        }

        if(event.getState() != null) {
            String sqlQuery =
                    "UPDATE events SET state = ? WHERE id = ?;";
            jdbcTemplate.update(sqlQuery, event.getState(), event.getId());
        }
        if(event.getPublishedOn() != null) {
            String sqlQuery =
                    "UPDATE events SET published_date = ? WHERE id = ?;";
            jdbcTemplate.update(sqlQuery,
                    Timestamp.valueOf(LocalDateTime.parse(event.getPublishedOn(), formatter)),
                    event.getId());
        }
        return getEvent(event.getId());
    }

    public void incrementViews(int eventId, String ip) {
        String ipCheckSqlQuery =
                "SELECT * FROM views_to_ips WHERE event_id = ? AND ip = ?;";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(ipCheckSqlQuery, eventId, ip);
        if(rs.next()) {
            return;
        }
        String sqlQuery =
                "INSERT INTO views_to_ips (event_id, ip) VALUES (? , ?);";

        jdbcTemplate.update(sqlQuery, eventId, ip);
    }

    public int getViews(int eventId) {
        String ipCheckSqlQuery =
                "SELECT count(*) AS count_views FROM views_to_ips WHERE event_id = ?;";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(ipCheckSqlQuery, eventId);
        rs.next();
        return rs.getInt("count_views");
    }
}

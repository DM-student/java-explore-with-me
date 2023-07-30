package ru.practicum.main_service.server.database;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.practicum.main_service.server.dto.MainServiceDtoConstants;
import ru.practicum.main_service.server.dto.ParticipationRequestDto;
import ru.practicum.main_service.server.dto.UserDto;
import ru.practicum.main_service.server.utility.errors.NotFoundError;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class ParticipationRequestsDatabase {
    private final DateTimeFormatter formatter = MainServiceDtoConstants.DATE_TIME_FORMATTER;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ParticipationRequestsDatabase(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private List<ParticipationRequestDto> mapRequests(SqlRowSet rs) {
        List<ParticipationRequestDto> output = new ArrayList<>();
        while (rs.next()) {
            ParticipationRequestDto requestDto = new ParticipationRequestDto();
            requestDto.setId(rs.getInt("id"));
            requestDto.setCreated(rs.getTimestamp("created_on").toLocalDateTime().format(formatter));
            requestDto.setEvent(rs.getInt("event_id"));
            requestDto.setRequester(rs.getInt("requester_id"));
            requestDto.setStatus("status");
            output.add(requestDto);
        }
        return output;
    }

    public ParticipationRequestDto getRequest(Integer id) {
        String sqlQuery =
                "SELECT * FROM participation_requests WHERE id = ?;";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sqlQuery, id);
        List<ParticipationRequestDto> output = mapRequests(rs);
        if (output.isEmpty()) {
            throw new NotFoundError("Не найден запрос на участие.", id);
        }
        return output.get(0);
    }

    public List<ParticipationRequestDto> getRequestsForUser(int id) {
        String sqlQuery = "SELECT * " +
                "FROM participation_requests " +
                "WHERE requester_id = ?;";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sqlQuery, id);
        return mapRequests(rs);
    }

    public List<ParticipationRequestDto> getRequestsForEvent(int id) {
        String sqlQuery = "SELECT * " +
                "FROM participation_requests " +
                "WHERE event_id = ?;";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sqlQuery, id);
        return mapRequests(rs);
    }

    public List<ParticipationRequestDto> getRequestsForEvent(int id, String status) {
        String sqlQuery = "SELECT * " +
                "FROM participation_requests " +
                "WHERE event_id = ? AND status = ?;";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sqlQuery, id, status);
        return mapRequests(rs);
    }

    public void deleteRequest(int id) {
        String sqlQuery = "DELETE FROM participation_requests " +
                "WHERE id = ?";
        jdbcTemplate.update(sqlQuery, id);
    }

    public ParticipationRequestDto createRequest(ParticipationRequestDto request) {
        String sqlQuery = "INSERT INTO participation_requests " +
                "(created_on, " +
                "event_id, " +
                "requester_id" +
                "status) " +
                "VALUES (?, ?) " +
                "RETURNING *";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sqlQuery, request.getCreated(),
                request.getEvent(), request.getRequester(), request.getStatus());
        return mapRequests(rs).get(0);
    }

    public ParticipationRequestDto updateRequestStatus(int id, String status) {
        String sqlQuery =
                "UPDATE participation_requests SET status = ? WHERE id = ?;";
        jdbcTemplate.update(sqlQuery, status, id);
        return getRequest(id);
    }

    public int getConfirmedRequestsCountForEvent(int id) {
        final String confirmed = "confirmed";

        String sqlQuery = "SELECT COUNT(*) AS amount " +
                "FROM participation_requests " +
                "WHERE status like ? AND id = ?;";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sqlQuery, confirmed, id);
        return rs.getInt("amount");
    }


}

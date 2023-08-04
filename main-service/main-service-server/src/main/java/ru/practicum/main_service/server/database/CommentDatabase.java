package ru.practicum.main_service.server.database;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.practicum.main_service.server.dto.CommentDto;
import ru.practicum.main_service.server.dto.CommentResponseDto;
import ru.practicum.main_service.server.dto.MainServiceDtoConstants;
import ru.practicum.main_service.server.dto.UserDto;
import ru.practicum.main_service.server.utility.errors.NotFoundError;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class CommentDatabase {
    private static final DateTimeFormatter formatter = MainServiceDtoConstants.DATE_TIME_FORMATTER;
    private final JdbcTemplate jdbcTemplate;
    private final UserDatabase userDatabase;

    @Autowired
    public CommentDatabase(JdbcTemplate jdbcTemplate, UserDatabase userDatabase) {
        this.jdbcTemplate = jdbcTemplate;
        this.userDatabase = userDatabase;
    }

    private List<CommentResponseDto> mapComments(SqlRowSet rs) {
        List<CommentResponseDto> comments = new ArrayList<>();
        List<Integer> userIds = new ArrayList<>();
        while (rs.next()) {
            CommentResponseDto comment = new CommentResponseDto();
            comment.setId(rs.getInt("id"));
            comment.setText(rs.getString("comment_text"));
            comment.setEventId(rs.getInt("event_id"));

            userIds.add(rs.getInt("user_id"));

            comment.setCreatedOn(rs.getTimestamp("created_on").toLocalDateTime().format(formatter));
            comment.setEdited(rs.getBoolean("edited"));

            comments.add(comment);
        }
        Map<Integer, UserDto> userMap = userDatabase.getUsersMap(userIds);
        for(int i = 0; i < comments.size(); i++) {
            comments.get(i).setUser(userMap.get(userIds.get(i)));
        }
        return comments;
    }

    public List<CommentResponseDto> getCommentsForAnEvent(int eventId, int from, int size) {
        String sqlQuery = "SELECT * " +
                "FROM comments " +
                "WHERE event_id = ? " +
                "LIMIT ? " +
                "OFFSET ?" +
                "ORDER BY created_on DESC;";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sqlQuery, eventId, size, from);
        return mapComments(rs);
    }

    public List<CommentResponseDto> getCommentsForAnUser(int userId, int from, int size) {
        String sqlQuery = "SELECT * " +
                "FROM comments " +
                "WHERE user_id = ? " +
                "LIMIT ? " +
                "OFFSET ? " +
                "ORDER BY created_on DESC;";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sqlQuery, userId, size, from);
        return mapComments(rs);
    }

    public CommentResponseDto getComment(Integer id) {
        String sqlQuery =
                "SELECT * FROM comments WHERE id = ? ORDER BY created_on DESC;";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sqlQuery, id);
        List<CommentResponseDto> output = mapComments(rs);
        if (output.isEmpty()) {
            throw new NotFoundError("Не найден комментарий.", id);
        }
        return output.get(0);
    }

    public CommentResponseDto createComment(CommentDto comment) {
        String sqlQuery = "INSERT INTO comments " +
                "(event_id, user_id, created_on, comment_text, edited) " +
                "VALUES (?, ?, ?, ?, ?) " +
                "RETURNING *";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sqlQuery,
                comment.getEventId(), comment.getUserId(),
                LocalDateTime.parse(comment.getCreation_date(), formatter), comment.getText(), comment.getEdited());
        return mapComments(rs).get(0);
    }

    public void deleteComment(int id) {
        String sqlQuery = "DELETE FROM comments " +
                "WHERE id = ?;";
        jdbcTemplate.update(sqlQuery, id);
    }

    public CommentResponseDto editComment(CommentDto comment) {
        String sqlQuery =
                "UPDATE comments SET comment_text = ?, edited = ? WHERE id = ?;";
        jdbcTemplate.update(sqlQuery, comment.getText(), comment.getEdited(), comment.getId());
        return getComment(comment.getId());
    }
}

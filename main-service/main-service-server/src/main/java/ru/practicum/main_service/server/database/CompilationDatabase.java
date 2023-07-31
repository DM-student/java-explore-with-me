package ru.practicum.main_service.server.database;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.practicum.main_service.server.dto.CompilationDto;
import ru.practicum.main_service.server.dto.CompilationDtoResponse;
import ru.practicum.main_service.server.dto.EventDtoResponse;
import ru.practicum.main_service.server.utility.errors.NotFoundError;

import java.util.ArrayList;
import java.util.List;

@Component
public class CompilationDatabase {

    private final JdbcTemplate jdbcTemplate;
    private final EventDatabase eventDatabase;

    @Autowired
    public CompilationDatabase(JdbcTemplate jdbcTemplate,
                               EventDatabase eventDatabase) {
        this.jdbcTemplate = jdbcTemplate;
        this.eventDatabase = eventDatabase;
    }

    public List<CompilationDtoResponse> mapCompilations(SqlRowSet rs) {
        List<CompilationDtoResponse> output = new ArrayList<>();
        while (rs.next()) {
            CompilationDtoResponse compilation = new CompilationDtoResponse();

            compilation.setId(rs.getInt("id"));
            compilation.setTitle(rs.getString("title"));
            compilation.setPinned(rs.getBoolean("pinned"));

            String sqlQuery =
                    "SELECT * FROM events_to_compilations WHERE compilation_id = ?;";
            SqlRowSet subrs = jdbcTemplate.queryForRowSet(sqlQuery, compilation.getId());

            List<EventDtoResponse> events = new ArrayList<>();
            while (subrs.next()) {
                events.add(eventDatabase.getEvent(subrs.getInt("event_id")));
            }
            compilation.setEvents(events);

            output.add(compilation);
        }
        return output;
    }

    public CompilationDtoResponse getCompilation(Integer id) {
        String sqlQuery =
                "SELECT * FROM compilations WHERE id = ?;";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sqlQuery, id);
        List<CompilationDtoResponse> output = mapCompilations(rs);
        if (output.isEmpty()) {
            throw new NotFoundError("Не найдена категория.", id);
        }
        return output.get(0);
    }

    public List<CompilationDtoResponse> getByTitle(String title) {
        String sqlQuery =
                "SELECT * FROM compilations WHERE title = ?;";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sqlQuery, title);
        List<CompilationDtoResponse> output = mapCompilations(rs);
        return output;
    }

    public List<CompilationDtoResponse> getAllCompilations(int from, int size) {
        String sqlQuery =
                "SELECT * FROM compilations LIMIT ? OFFSET ?;";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sqlQuery, size, from);

        return mapCompilations(rs);
    }

    public List<CompilationDtoResponse> getAllCompilations(int from, int size, boolean pinned) {
        String sqlQuery =
                "SELECT * " +
                        "FROM compilations " +
                        "WHERE pinned = 'true' " +
                        "LIMIT ? OFFSET ?;";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sqlQuery, size, from);

        return mapCompilations(rs);
    }

    public void deleteCompilation(int id) {
        String sqlQuery = "DELETE FROM compilations " +
                "WHERE id = ?";
        jdbcTemplate.update(sqlQuery, id);
    }

    private void linkEvents(int compilationId, List<Integer> events) {
        if (events == null) return;

        String sqlQuery = "DELETE FROM events_to_compilations " +
                "WHERE compilation_id = ?";
        jdbcTemplate.update(sqlQuery, compilationId);

        for (Integer eventId : events) {
            String sqlSubQuery = "INSERT INTO events_to_compilations (compilation_id, event_id) " +
                    "VALUES (?, ?);";
            jdbcTemplate.update(sqlSubQuery, compilationId, eventId);
        }
    }

    public CompilationDtoResponse createCompilation(CompilationDto compilation) {
        String sqlQuery = "INSERT INTO compilations (title, pinned) " +
                "VALUES (?, ?) " +
                "RETURNING *;";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sqlQuery, compilation.getTitle(), compilation.getPinned());
        int postedId = mapCompilations(rs).get(0).getId();
        linkEvents(postedId, compilation.getEvents());
        return getCompilation(postedId);
    }

    public CompilationDtoResponse patchCompilation(CompilationDto compilation) {
        if (compilation.getTitle() != null) {
            String sqlQuery =
                    "UPDATE compilations SET title = ? WHERE id = ?;";
            jdbcTemplate.update(sqlQuery, compilation.getTitle(), compilation.getId());
        }
        if (compilation.getPinned() != null) {
            String sqlQuery =
                    "UPDATE compilations SET pinned = ? WHERE id = ?;";
            jdbcTemplate.update(sqlQuery, compilation.getPinned(), compilation.getId());
        }
        linkEvents(compilation.getId(), compilation.getEvents());
        return getCompilation(compilation.getId());
    }
}

package ru.practicum.main_service.server.database;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.practicum.main_service.server.dto.UserDto;
import ru.practicum.main_service.server.utility.errors.NotFoundError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class UserDatabase {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserDatabase(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private List<UserDto> mapUsers(SqlRowSet rs) {
        List<UserDto> output = new ArrayList<>();
        while (rs.next()) {
            UserDto user = new UserDto();
            user.setId(rs.getInt("id"));
            user.setEmail(rs.getString("email"));
            user.setName(rs.getString("name"));
            output.add(user);
        }
        return output;
    }

    public UserDto getUser(Integer id) {
        String sqlQuery =
                "SELECT * FROM users WHERE id = ?;";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sqlQuery, id);
        List<UserDto> output = mapUsers(rs);
        if (output.isEmpty()) {
            throw new NotFoundError("Не найден пользователь.", id);
        }
        return output.get(0);
    }

    public List<UserDto> getUsers(int from, int size) {
        String sqlQuery = "SELECT * " +
                "FROM users " +
                "LIMIT ? " +
                "OFFSET ?;";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sqlQuery, size, from);
        return mapUsers(rs);
    }

    public Map<Integer, UserDto> getUsersMap(List<Integer> ids) {
        if (ids.isEmpty()) {
            return new HashMap<>();
        }
        StringBuilder sqlQuery = new StringBuilder("SELECT * FROM users WHERE id IN (");
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) sqlQuery.append(", ");
            sqlQuery.append(ids.get(i));
        }
        sqlQuery.append(");");

        SqlRowSet rs = jdbcTemplate.queryForRowSet(sqlQuery.toString());

        Map<Integer, UserDto> usersMap = new HashMap<>();
        for (UserDto user : mapUsers(rs)) {
            usersMap.put(user.getId(), user);
        }
        return usersMap;
    }

    public List<UserDto> getUsersByName(String name) {
        String sqlQuery = "SELECT * " +
                "FROM users " +
                "WHERE name = ?;";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sqlQuery, name);
        return mapUsers(rs);
    }

    public List<UserDto> getUsersByEmail(String email) {
        String sqlQuery = "SELECT * " +
                "FROM users " +
                "WHERE email = ?;";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sqlQuery, email);
        return mapUsers(rs);
    }

    public void deleteUser(int id) {
        String sqlQuery = "DELETE FROM users " +
                "WHERE id = ?";
        jdbcTemplate.update(sqlQuery, id);
    }

    public UserDto createUser(UserDto user) {
        String sqlQuery = "INSERT INTO users (email, name) " +
                "VALUES (?, ?) " +
                "RETURNING *";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sqlQuery, user.getEmail(), user.getName());
        return mapUsers(rs).get(0);
    }
}

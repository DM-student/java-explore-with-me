package ru.practicum.stats.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.practicum.stats.data_entity.StatRecord;

@Component
public class StatsDatabase {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public StatsDatabase(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void saveStat(StatRecord stat) {
        jdbcTemplate.update("INSERT INTO stat-record VALUES (?, ?, ?, ?);",
                stat.getApp(), stat.getUri(), stat.getIp(), stat.getTimestamp());
    }

}

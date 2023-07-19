package ru.practicum.stats.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.practicum.stats.data_entity.StatsGroupData;
import ru.practicum.stats.data_entity.StatsRecord;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class StatsDatabase {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public StatsDatabase(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void saveStat(StatsRecord stat) {
        jdbcTemplate.update("INSERT INTO stats_records (app, uri, ip, record_timestamp) VALUES (?, ?, ?, ?);",
                stat.getApp(), stat.getUri(), stat.getIp(), stat.getTimestamp());
    }

    public List<StatsRecord> getStats(LocalDateTime start, LocalDateTime end, boolean unique) {
        List<StatsRecord> result = new ArrayList<>();
        String sql = "SELECT * " +
                "FROM stats_records " +
                "WHERE record_timestamp >= ? AND record_timestamp <= ?;";
        if (unique) {
            sql = "SELECT DISTINCT ON (ip) * " +
                    "FROM stats_records " +
                    "WHERE record_timestamp >= ? AND record_timestamp <= ?;";

        }
        jdbcTemplate.query(sql, (rs) -> {
            StatsRecord stat = new StatsRecord();
            stat.setId(rs.getInt("id"));
            stat.setApp(rs.getString("app"));
            stat.setUri(rs.getString("uri"));
            stat.setIp(rs.getString("ip"));
            stat.setTimestamp(rs.getTimestamp("record_timestamp").toLocalDateTime());

            result.add(stat);
        }, start, end);

        return result;
    }

    public List<StatsGroupData> getStatsForUris(LocalDateTime start, LocalDateTime end, boolean unique, List<String> uris) {
        List<StatsGroupData> result = new ArrayList<>();
        for (String uri : uris) {
            String sql = "SELECT app, uri, count(*) AS hits " +
                    "FROM stats_records " +
                    "WHERE record_timestamp >= ? AND record_timestamp <= ? AND uri LIKE ? " +
                    "GROUP BY app, uri;";
            if (unique) {
                sql = "SELECT DISTINCT ON (ip) app, uri, count(*) AS hits " +
                        "FROM stats_records " +
                        "WHERE record_timestamp >= ? AND record_timestamp <= ? AND uri LIKE ? " +
                        "GROUP BY app, uri;";

            }
            jdbcTemplate.query(sql, (rs) -> {
                StatsGroupData stat = new StatsGroupData();
                stat.setApp(rs.getString("app"));
                stat.setUri(rs.getString("uri"));
                stat.setHits(rs.getInt("hits"));

                result.add(stat);
            }, start, end, uri);
        }

        return result;
    }
}

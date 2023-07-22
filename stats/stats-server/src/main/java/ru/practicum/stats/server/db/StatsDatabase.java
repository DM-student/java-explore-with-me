package ru.practicum.stats.server.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.practicum.stats.dto.StatsGroupData;
import ru.practicum.stats.dto.StatsRecord;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
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
                stat.getApp(), stat.getUri(), stat.getIp(), stat.getTimestampLocalDateTime());
    }

    public List<StatsGroupData> getStats(LocalDateTime start, LocalDateTime end, boolean unique) {
        List<StatsGroupData> result = new ArrayList<>();
        String sql = "SELECT app, uri, count(*) AS hits " +
                "FROM stats_records " +
                "WHERE record_timestamp >= ? AND record_timestamp <= ? " +
                "GROUP BY app, uri;";
        if (unique) {
            sql = "SELECT s.app, s.uri, count(s.id) AS hits " +
                    "FROM (SELECT DISTINCT ON (uri, ip) * FROM stats_records) AS s " +
                    "WHERE record_timestamp >= ? AND record_timestamp <= ? " +
                    "GROUP BY app, uri;";

        }
        jdbcTemplate.query(sql, (rs) -> {
            StatsGroupData stat = new StatsGroupData();
            stat.setApp(rs.getString("app"));
            stat.setUri(rs.getString("uri"));
            stat.setHits(rs.getInt("hits"));

            result.add(stat);
        }, start, end);
        result.sort(Comparator.comparingInt(StatsGroupData::getHits));
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
                sql = "SELECT s.app, s.uri, count(s.id) AS hits " +
                        "FROM (SELECT DISTINCT ON (uri, ip) * FROM stats_records) AS s " +
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
        // Сортировка тут, ибо для каждого эндпоинта обработка идёт отдельным SQL запросом.
        result.sort((data1, data2) -> data2.getHits() - data1.getHits()); // обратная сортировка
        return result;
    }
}

package ru.practicum.stats.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class StatsRecord {
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Integer id;
    private String app;
    private String uri;
    private String ip;
    private String timestamp;

    public LocalDateTime getTimestampLocalDateTime() {
        return LocalDateTime.parse(timestamp, DATE_TIME_FORMATTER);
    }

    public void setTimestamp(String dateTime) {
        timestamp = dateTime;
    }

    public void setTimestamp(LocalDateTime dateTime) {
        timestamp = dateTime.format(DATE_TIME_FORMATTER);
    }
}

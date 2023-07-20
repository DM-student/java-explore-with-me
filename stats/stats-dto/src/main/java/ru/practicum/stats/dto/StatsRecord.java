package ru.practicum.stats.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class StatsRecord {
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Integer id;
    private String app;
    private String uri;
    private String ip;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String timestamp;

    public LocalDateTime getTimestamp() {
        return LocalDateTime.parse(timestamp, formatter);
    }

    public void setTimestamp(LocalDateTime dateTime) {
        timestamp = dateTime.format(formatter);
    }
}

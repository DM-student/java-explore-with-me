package ru.practicum.stats.dto;

import lombok.Data;

import java.time.format.DateTimeFormatter;

@Data
public class StatsGroupData {
    // Чтобы можно было получить формат не только в StatsRecord
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private String app;
    private String uri;
    private Integer hits;
}

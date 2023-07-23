package ru.practicum.stats.dto;

import lombok.Data;

import java.time.format.DateTimeFormatter;

@Data
public class StatsGroupData {
    private String app;
    private String uri;
    private Integer hits;
}

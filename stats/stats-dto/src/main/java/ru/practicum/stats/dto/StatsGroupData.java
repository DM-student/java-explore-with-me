package ru.practicum.stats.dto;

import lombok.Data;

@Data
public class StatsGroupData {
    private String app;
    private String uri;
    private Integer hits;
}

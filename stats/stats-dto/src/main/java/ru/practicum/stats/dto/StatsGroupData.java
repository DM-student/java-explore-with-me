package ru.practicum.stats.dto;

import lombok.Data;

@Data
public class StatsGroupData {
    String app;
    String uri;
    Integer hits;
}

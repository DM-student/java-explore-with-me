package ru.practicum.stats.data_entity;

import lombok.Data;

@Data
public class StatsGroupData {
    String app;
    String uri;
    Integer hits;
}

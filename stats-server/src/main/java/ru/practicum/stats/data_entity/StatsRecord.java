package ru.practicum.stats.data_entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StatsRecord {
    // Не вижу смысла в валидации и проверке, ввиду того, что передавать информацию по-хорошему
    // будет не пользователь, а гейтвей.
    Integer id;
    String app;
    String uri;
    String ip;
    LocalDateTime timestamp;
}

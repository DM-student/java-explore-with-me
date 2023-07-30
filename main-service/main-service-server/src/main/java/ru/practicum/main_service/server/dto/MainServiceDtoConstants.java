package ru.practicum.main_service.server.dto;

import java.time.format.DateTimeFormatter;

public class MainServiceDtoConstants {
    // Несмотря на то, что этот форматер идентичен форматеру статистики - это два разных форматера для разных сервисов.
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
}

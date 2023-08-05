package ru.practicum.main_service.server.dto;

import lombok.Data;
import ru.practicum.main_service.server.utility.Helpers;

import java.time.format.DateTimeFormatter;

@Data
public class CommentDto {
    private static final DateTimeFormatter formatter = MainServiceDtoConstants.DATE_TIME_FORMATTER;

    private Integer id;
    private Integer eventId;
    private Integer userId;
    private String creationDate;
    private String text;
    private Boolean edited;


    public boolean isValidToPost() {
        if (eventId == null) return false;
        if (userId == null) return false;
        if (creationDate == null) return false;
        if (edited == null) return false;
        if (!Helpers.validateDateTimeFormat(creationDate, formatter)) return false;
        // if (edited == null) return false; Ругается код-стайл проверка.
        if (text == null || text.isBlank()) return false;
        return true;
    }

    // Тут по сути чуть иная версия валидации, ибо
    // по-хорошему ничего кроме текста не должно быть возможности менять.
    public boolean isValidToEdit() {
        if (id == null) return false;
        if (edited == null) return false;
        if (text == null || text.isBlank()) return false;
        return true;
    }
}

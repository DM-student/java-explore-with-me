package ru.practicum.main_service.server.dto;

import lombok.Data;
import ru.practicum.main_service.server.utility.Helpers;

import java.time.format.DateTimeFormatter;

@Data
public class EventDto {
    private String annotation;
    private Integer category;
    private String createdOn;
    private String description;
    private String eventDate;
    private Integer id;
    private Integer initiator;
    private LocationDto location;
    private Boolean paid;
    private Integer participantLimit;
    private Boolean requestModeration;
    private String state;
    private String title;

    private String stateAction;

    public void setDefaultValues() {
        if (participantLimit == null) participantLimit = 0;
        if (requestModeration == null) requestModeration = true;
    }

    /**
     * Служит для валидации объекта, перед его публикацией.
     * Не проверяет ничего на стороне БД!
     *
     * @return Ответ в виде булева значения.
     */
    public boolean isValid() {
        if (annotation == null) return false;
        if (category == null) return false;
        if (createdOn == null) return false;
        if (description == null) return false;
        if (eventDate == null) return false;
        if (initiator == null) return false;
        if (location == null) return false;
        if (paid == null) return false;
        if (participantLimit == null) return false;
        if (title == null) return false;
        return isValidSkipNulls();
    }

    /**
     * Служит для валидации объекта, но игнорирует отсутствующие переменные.
     * Не проверяет ничего на стороне БД!
     *
     * @return Ответ в виде булева значения.
     */
    public boolean isValidSkipNulls() {
        DateTimeFormatter formatter = MainServiceDtoConstants.DATE_TIME_FORMATTER;

        if (annotation != null) {
            if(annotation.isBlank()) return false;
        }
        if (category != null) {
            if (category < 0) return false;
        }
        if (createdOn != null) {
            if (!Helpers.validateDateTimeFormat(createdOn, formatter)) return false;
        }
        if (description != null) {
            if(description.isBlank()) return false;
        }
        if (eventDate != null) {
            if (!Helpers.validateDateTimeFormat(eventDate, formatter)) return false;
        }
        if (initiator != null) {
            if (initiator < 0) return false;
        }
        if (location != null) {
            if (!location.isValid()) return false;
        }
        if (participantLimit != null) {
            if (participantLimit < 0) return false;
        }
        if (title != null) {
            if(title.isBlank()) return false;
        }
        return true;
    }
}

package ru.practicum.main_service.server.dto;

import lombok.Data;
import ru.practicum.main_service.server.utility.Helpers;

import java.time.format.DateTimeFormatter;

@Data
public class ParticipationRequestDto {
    private Integer id;
    private String created;
    private Integer event;
    private Integer requester;
    private String status;

    /**
     * Служит для валидации объекта, перед его публикацией.
     * Не проверяет ничего на стороне БД!
     *
     * @return Ответ в виде булева значения.
     */
    public boolean isValid() {
        if(created == null) return false;
        if(event == null) return false;
        if(requester == null) return false;

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

        if (created != null) {
            if(Helpers.validateDateTimeFormat(created, formatter))  return false;
        }
        if (event != null) {
            if (event < 0) return false;
        }
        if (requester != null) {
            if (requester < 0) return false;
        }

        return true;
    }
}

package ru.practicum.main_service.server.dto;

import lombok.Data;

@Data
public class LocationDto {
    private Double lat;
    private Double lon;

    /**
     * Служит для валидации объекта, перед его публикацией.
     * Не проверяет ничего на стороне БД!
     *
     * @return Ответ в виде булева значения.
     */
    public boolean isValid() {
        return lat != null && lon != null;
    }
}

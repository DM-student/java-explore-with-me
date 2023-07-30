package ru.practicum.main_service.server.dto;

import lombok.Data;

@Data
public class CategoryDto {
    private Integer id;
    private String name;

    /**
     * Служит для валидации объекта, перед его публикацией.
     * Не проверяет ничего на стороне БД!
     *
     * @return Ответ в виде булева значения.
     */
    public boolean isValid() {
        if (name == null) return false;
        if (name.isBlank()) return false;
        if (name.length() > 64) return false;

        return true;
    }
}

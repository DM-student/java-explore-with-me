package ru.practicum.main_service.server.dto;

import lombok.Data;

import java.util.List;

@Data
public class CompilationDto {
    private Integer id;
    private List<Integer> events;
    private String title;
    private Boolean pinned;

    /**
     * Служит для валидации объекта, перед его публикацией.
     * Не проверяет ничего на стороне БД!
     *
     * @return Ответ в виде булева значения.
     */
    public boolean isValid() {
        if (title == null) return false;
        if (pinned == null) return false;

        return isValidSkipNulls();
    }

    /**
     * Служит для валидации объекта, но игнорирует отсутствующие переменные.
     * Не проверяет ничего на стороне БД!
     *
     * @return Ответ в виде булева значения.
     */
    public boolean isValidSkipNulls() {
        if (title != null) {
            if (title.isBlank()) return false;
            if (title.length() > 50) return false;
            if (title.length() < 2) return false;
        }

        return true;
    }


}

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
        return true;
    }
}

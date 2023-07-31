package ru.practicum.main_service.server.dto;

import lombok.Data;
import ru.practicum.main_service.server.utility.Helpers;


// В некоторых случаях я не создавал "краткую" версию ДТОшки. Краткие ДТОшки у меня только для ситуаций,
// где нужно избегать "зацикленность".
@Data
public class UserDto {
    private Integer id;
    private String name;
    private String email;

    /**
     * Служит для валидации объекта, перед его публикацией.
     * Не проверяет ничего на стороне БД!
     *
     * @return Ответ в виде булева значения.
     */
    public boolean isValid() {
        if (name == null) return false;
        if (email == null) return false;

        return isValidSkipNulls();
    }

    /**
     * Служит для валидации объекта, но игнорирует отсутствующие переменные.
     * Не проверяет ничего на стороне БД!
     *
     * @return Ответ в виде булева значения.
     */
    public boolean isValidSkipNulls() {
        if (name != null) {
            if (name.isBlank()) return false;
            if (name.length() > 250) return false;
            if (name.length() < 2) return false;
        }

        if (email != null) {
            if (!Helpers.validateEmail(email)) return false;
        }

        return true;
    }
}

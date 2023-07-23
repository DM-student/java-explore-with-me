package ru.practicum.main_service.server.dto;

import lombok.Data;

@Data
public class UserDto {
    private Integer id;
    private String name;
    private String email;

    public boolean isValid() {
        if(name == null) return false;
        if(name.isBlank()) return false;
        if(name.length() > 64) return false;

        if(email == null) return false;
        if(email.isBlank()) return false;
        if(email.length() > 256) return false;

        return true;
    }

    public boolean isValidSkipNulls() {
        if(name != null) {
            if (name.isBlank()) return false;
            if (name.length() > 64) return false;
        }

        if(email != null) {
            if (email.isBlank()) return false;
            if (email.length() > 256) return false;
        }

        return true;
    }
}

package ru.practicum.main_service.server.dto;

import lombok.Data;
import ru.practicum.main_service.server.dto.UserDto;

@Data
public class CommentResponseDto {
    private Integer id;
    private UserDto user;
    private Integer eventId;
    private String createdOn;
    private String text;
    private Boolean edited;
}

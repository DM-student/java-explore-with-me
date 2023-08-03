package ru.practicum.main_service.server.dto;

import lombok.Data;

import java.util.List;

@Data
public class CompilationDtoResponse {
    private Integer id;
    private List<EventDtoResponse> events;
    private String title;
    private Boolean pinned;
}

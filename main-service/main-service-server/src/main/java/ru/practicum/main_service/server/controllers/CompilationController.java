package ru.practicum.main_service.server.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main_service.server.dto.CategoryDto;
import ru.practicum.main_service.server.dto.CompilationDto;
import ru.practicum.main_service.server.dto.CompilationDtoResponse;
import ru.practicum.main_service.server.services.CategoryService;
import ru.practicum.main_service.server.services.CompilationService;
import ru.practicum.stats.httpclient.StatsHttpClientHitException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class CompilationController {
    @Autowired
    private CompilationService service;

    @Autowired
    private EarlyRequestHandler earlyRequestHandler;

    @GetMapping("/compilations")
    public List<CompilationDtoResponse> getAll(HttpServletRequest servletRequest,
                                               @RequestParam(required = false) Boolean pinned,
                                               @RequestParam(defaultValue = "0") int from,
                                               @RequestParam(defaultValue = "10") int size) throws StatsHttpClientHitException {
        earlyRequestHandler.handle(servletRequest);

        if(pinned != null) {
            return service.getAll(from, size, pinned);
        }
        return service.getAll(from, size);
    }

    @GetMapping("/compilations/{compId}")
    public CompilationDtoResponse get(HttpServletRequest servletRequest,
                                    @PathVariable int catId) throws StatsHttpClientHitException {
        earlyRequestHandler.handle(servletRequest);

        return service.getById(catId);
    }

    @PostMapping("/admin/compilations")
    public ResponseEntity<CompilationDtoResponse> post(HttpServletRequest servletRequest,
                                                       @RequestBody CompilationDto compilationDto) throws StatsHttpClientHitException {
        earlyRequestHandler.handle(servletRequest);

        return new ResponseEntity<>(service.post(compilationDto), HttpStatus.CREATED);
    }

    @PatchMapping("/admin/compilations/{compId}")
    public CompilationDtoResponse patch(HttpServletRequest servletRequest,
                              @RequestBody CompilationDto compilationDto,
                              @PathVariable int compId) throws StatsHttpClientHitException {
        earlyRequestHandler.handle(servletRequest);

        compilationDto.setId(compId);
        return service.update(compilationDto);
    }

    @DeleteMapping("/admin/compilations/{compId}")
    public CompilationDtoResponse delete(HttpServletRequest servletRequest,
                                 @PathVariable int compId) throws StatsHttpClientHitException {
        earlyRequestHandler.handle(servletRequest);

        return service.delete(compId);
    }
}

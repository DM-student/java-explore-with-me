package ru.practicum.main_service.server.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main_service.server.dto.CategoryDto;
import ru.practicum.main_service.server.services.CategoryService;
import ru.practicum.stats.httpclient.StatsHttpClientHitException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class CategoryController {
    @Autowired
    private CategoryService service;

    @Autowired
    private EarlyRequestHandler earlyRequestHandler;

    @GetMapping("/categories")
    public List<CategoryDto> getAll(HttpServletRequest servletRequest,
                                    @RequestParam(defaultValue = "0") int from,
                                    @RequestParam(defaultValue = "10") int size) throws StatsHttpClientHitException {
        earlyRequestHandler.handle(servletRequest);

        return service.getAll(from, size);
    }

    @GetMapping("/categories/{catId}")
    public CategoryDto get(HttpServletRequest servletRequest,
                                    @PathVariable int catId) throws StatsHttpClientHitException {
        earlyRequestHandler.handle(servletRequest);

        return service.getById(catId);
    }

    @PostMapping("/admin/categories")
    public ResponseEntity<CategoryDto> post(HttpServletRequest servletRequest,
                                            @RequestBody CategoryDto category) throws StatsHttpClientHitException {
        earlyRequestHandler.handle(servletRequest);

        return new ResponseEntity<>(service.post(category), HttpStatus.CREATED);
    }

    @PatchMapping("/admin/categories/{catId}")
    public CategoryDto patch(HttpServletRequest servletRequest,
                              @RequestBody CategoryDto category,
                              @PathVariable int catId) throws StatsHttpClientHitException {
        earlyRequestHandler.handle(servletRequest);

        category.setId(catId);
        return service.update(category);
    }

    @DeleteMapping("/admin/categories/{catId}")
    public CategoryDto delete(HttpServletRequest servletRequest,
                              @PathVariable int catId) throws StatsHttpClientHitException {
        earlyRequestHandler.handle(servletRequest);

        return service.delete(catId);
    }
}

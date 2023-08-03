package ru.practicum.main_service.server.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main_service.server.dto.UserDto;
import ru.practicum.main_service.server.services.UserService;
import ru.practicum.stats.httpclient.StatsHttpClientHitException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class UserController {
    @Autowired
    private EarlyRequestHandler earlyRequestHandler;

    @Autowired
    private UserService userService;

    @GetMapping("/admin/users")
    public ResponseEntity<List<UserDto>> getUsers(
            HttpServletRequest servletRequest,
            @RequestParam(required = false) List<Integer> ids,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size
    ) throws StatsHttpClientHitException {
        earlyRequestHandler.handle(servletRequest);

        List<UserDto> users = userService.getUsers(ids, from, size);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }


    @PostMapping("/admin/users")
    public ResponseEntity<UserDto> createUser(
            HttpServletRequest servletRequest,
            RequestEntity<UserDto> request
    ) throws StatsHttpClientHitException {
        earlyRequestHandler.handle(servletRequest);

        UserDto user = userService.createUser(request.getBody());
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @DeleteMapping("/admin/users/{userId}")
    public ResponseEntity<UserDto> deleteUser(
            HttpServletRequest servletRequest,
            @PathVariable int userId
    ) throws StatsHttpClientHitException {
        earlyRequestHandler.handle(servletRequest);

        UserDto user = userService.deleteUser(userId);
        return new ResponseEntity<>(user, HttpStatus.NO_CONTENT);
    }
}

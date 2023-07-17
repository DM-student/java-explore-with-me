package ru.practicum.gateway.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.gateway.stats.Stats;

@RestController
public class MainController {
    @Autowired
    private Stats stats;

    @RequestMapping
    public String handleRequest() {
        return "Разработка в процессе";
    }
}

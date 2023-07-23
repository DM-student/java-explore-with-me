package ru.practicum.stats.server.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.stats.dto.StatsConstants;
import ru.practicum.stats.dto.StatsGroupData;
import ru.practicum.stats.dto.StatsRecord;
import ru.practicum.stats.server.db.StatsDatabase;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@Slf4j
public class MainController {
    // Мне не хочется плодить сущности, когда сам сервер статистики настолько прост.
    @Autowired
    private StatsDatabase stats;

    private static final DateTimeFormatter formatter = StatsConstants.DATE_TIME_FORMATTER;

    @PostMapping("/hit")
    public ResponseEntity<StatsRecord> statsHit(RequestEntity<StatsRecord> request) {
        StatsRecord statsRecord = request.getBody();

        stats.saveStat(statsRecord);
        return new ResponseEntity<>(statsRecord, HttpStatus.CREATED);
    }

    @GetMapping("/stats")
    public List<StatsGroupData> statsGet(RequestEntity<String> request, @RequestParam String start,
                                         @RequestParam String end, @RequestParam(required = false) List<String> uris,
                                         @RequestParam(defaultValue = "false") Boolean unique) {
        // Тут тоже немного логики в контроллер попало, но если
        // понадобиться расширить функционал - я выведу всю логику в сервис.

        List<StatsGroupData> groupStats;
        if (uris != null) {
            groupStats = stats.getStatsForUris(LocalDateTime.parse(start, formatter),
                    LocalDateTime.parse(end, formatter),
                    unique,
                    uris);
        } else {
            groupStats = stats.getStats(LocalDateTime.parse(start, formatter),
                    LocalDateTime.parse(end, formatter),
                    unique);
        }
        return groupStats;
    }
}

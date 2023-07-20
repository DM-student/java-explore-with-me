package ru.practicum.stats.server.controllers;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.stats.dto.StatsGroupData;
import ru.practicum.stats.dto.StatsRecord;
import ru.practicum.stats.server.db.StatsDatabase;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@Slf4j
public class MainController {
    @Autowired
    StatsDatabase stats;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @PostMapping("/hit")
    public ResponseEntity<String> statsHit(RequestEntity<String> request) {
        // Мне ну очень не хочется ради 5-10 строчек кода создавать отдельный сервис.
        // А так же я в этом проекте предпочту собственные JSONы вместо ДТОшек, мне так удобнее.
        JSONObject requestJson = new JSONObject(request.getBody());
        StatsRecord statsRecord = new StatsRecord();

        statsRecord.setApp(requestJson.getString("app"));
        statsRecord.setUri(requestJson.getString("uri"));
        statsRecord.setIp(requestJson.getString("ip"));
        statsRecord.setTimestamp(LocalDateTime.parse(requestJson.getString("timestamp"), formatter));

        stats.saveStat(statsRecord);
        return new ResponseEntity<>(requestJson.toString(), HttpStatus.CREATED);
    }

    @GetMapping("/stats")
    public ResponseEntity<String> statsGet(RequestEntity<String> request, @RequestParam String start,
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
        JSONArray json = new JSONArray();
        for (StatsGroupData stat : groupStats) {
            JSONObject statJson = new JSONObject();

            statJson.put("app", stat.getApp());
            statJson.put("uri", stat.getUri());
            statJson.put("hits", stat.getHits());

            json.put(statJson);
        }
        return new ResponseEntity<>(json.toString(), HttpStatus.OK);
    }
}
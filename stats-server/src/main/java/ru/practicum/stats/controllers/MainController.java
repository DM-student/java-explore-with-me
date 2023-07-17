package ru.practicum.stats.controllers;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.stats.data_entity.StatRecord;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
public class MainController {
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @PostMapping("/hit")
    public ResponseEntity<String> statsHit(RequestEntity<String> request) {
        // Мне ну очень не хочется ради 5-10 строчек кода создавать отдельный сервис.
        JSONObject requestJson = new JSONObject(request.getBody());
        StatRecord statRecord = new StatRecord();
        statRecord.setApp(requestJson.getString("app"));
        statRecord.setUri(requestJson.getString("uri"));
        statRecord.setIp(requestJson.getString("ip"));
        statRecord.setTimestamp(LocalDateTime.parse(requestJson.getString("timestamp"), formatter));

        return new ResponseEntity<>(requestJson.toString(), HttpStatus.OK);
    }
}

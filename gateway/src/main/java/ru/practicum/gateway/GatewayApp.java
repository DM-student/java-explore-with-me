package ru.practicum.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GatewayApp {
    public static void main(String[] args) {
        if(true) {
            return; // Я так "деактивировал" задел на будущее.
        }
        SpringApplication.run(GatewayApp.class, args);
    }
}
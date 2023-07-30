package ru.practicum.main_service.server.utility;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Helpers {
    public static boolean validateDateTimeFormat(String target, DateTimeFormatter formatter) {
        try {
            formatter.parse(target);
        } catch (DateTimeParseException e) {
            return false;
        }
        return true;
    }
}

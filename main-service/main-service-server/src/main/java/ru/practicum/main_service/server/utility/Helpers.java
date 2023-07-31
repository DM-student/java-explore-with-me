package ru.practicum.main_service.server.utility;

import lombok.extern.slf4j.Slf4j;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
public class Helpers {
    public static boolean validateDateTimeFormat(String target, DateTimeFormatter formatter) {
        try {
            formatter.parse(target);
        } catch (DateTimeParseException e) {
            return false;
        }
        return true;
    }

    public static boolean validateEmail(String emailAddress) {
        // Регулярные выражения - это боль.

        /*
        Заметка:

        Я пытался реализовать это через регулярные выражения, составил не один и не два регекса,
        которые работали в IDEA, работали на сайтах для проверки, причём с настройками на систему java,
        мои регексы работали везде кроме самой программы!

        Я потратил на это не мало часов и не хочу их больше трогать.
         */
        try {
            if (emailAddress.length() > 254) return false;
            String localPart = emailAddress.split("@")[0];
            String domainPart = emailAddress.split("@")[1];
            String[] domainSeparated = domainPart.split("\\.");

            if (localPart.length() > 64) return false;
            if (domainSeparated[domainSeparated.length - 2].length() > 63) return false;
            if (domainSeparated[domainSeparated.length - 2].length() < 1) return false;
            if (domainSeparated[domainSeparated.length - 1].length() < 2) return false;

            return true;
        } catch (Throwable e) {
            return false;
        }
    }
}

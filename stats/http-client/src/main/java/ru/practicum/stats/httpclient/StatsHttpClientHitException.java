package ru.practicum.stats.httpclient;

public class StatsHttpClientHitException extends Throwable {
    public StatsHttpClientHitException() {
        super("Stats server didn't accept new stats record.");
    }
}

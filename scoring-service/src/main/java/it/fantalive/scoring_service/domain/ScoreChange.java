package it.fantalive.match_event_producer.domain;

public record ScoreChange(int goals, int assists, int yellowCards, int redCards,
                          double bonus, Double rating) {}
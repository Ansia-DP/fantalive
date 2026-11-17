package it.fantalive.standings_service.event;

import java.time.Instant;

public record PlayerScoreUpdated(String eventId, int matchday, String matchId, String playerId,
                                 String playerName, Double rating, double bonus, Double fantavoto,
                                 int version, Instant updatedAt) {}
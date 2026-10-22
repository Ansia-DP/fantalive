package it.fantalive.match_event_producer.event;

import java.time.Instant;

public record PlayerRated(String eventId, String matchId, String playerId, String playerName,
                          double rating, Instant occurredAt) implements MatchEvent {}
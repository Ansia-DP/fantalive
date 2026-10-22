package it.fantalive.match_event_producer.event;

import java.time.Instant;

public record GoalScored(String eventId, String matchId, String playerId, String playerName,
                         int minute, Instant occurredAt) implements MatchEvent {}
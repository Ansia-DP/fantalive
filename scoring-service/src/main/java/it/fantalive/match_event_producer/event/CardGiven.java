package it.fantalive.match_event_producer.event;

import java.time.Instant;

public record CardGiven(String eventId, String matchId, String playerId, String playerName,
                        Card card, int minute, Instant occurredAt) implements MatchEvent {

    public enum Card { YELLOW, RED }
}
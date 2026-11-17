package it.fantalive.scoring_service.event;

import java.time.Instant;

public record CardGiven(String eventId, String matchId, int matchday, String playerId, String playerName,
                        Card card, int minute, Instant occurredAt) implements MatchEvent {

    public enum Card { YELLOW, RED }
}
package it.fantalive.scoring_service.event;

import java.time.Instant;

/** Stato completo del punteggio di un giocatore in una giornata (evento snapshot). */
public record PlayerScoreUpdated(
        String eventId,
        int matchday,
        String matchId,
        String playerId,
        String playerName,
        Double rating,
        double bonus,
        Double fantavoto,
        int version,
        Instant updatedAt
) {}
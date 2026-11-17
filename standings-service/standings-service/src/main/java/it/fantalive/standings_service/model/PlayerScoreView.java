package it.fantalive.standings_service.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("player_scores")
public record PlayerScoreView(
        @Id String id,          // matchday:playerId
        int matchday,
        String playerId,
        String playerName,
        Double fantavoto,
        int version,
        Instant updatedAt
) {}
package it.fantalive.scoring_service.model;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.Set;

@Document("player_match_scores")
@Getter
public class PlayerMatchScore {

    @Id
    private String id;          // matchday:matchId:playerId
    private String matchId;
    private String playerId;
    private String playerName;
    private int goals;
    private int assists;
    private int yellowCards;
    private int redCards;
    private double bonus;
    private Double rating;      // null finché non arriva il voto ("senza voto")
    private Set<String> processedEventIds = new HashSet<>();
    private int matchday;

    public PlayerMatchScore() {}

    /** Fantavoto = voto + bonus/malus. Calcolato, non salvato su Mongo. */
    public Double getFantavoto() {
        return rating == null ? null : rating + bonus;
    }
}
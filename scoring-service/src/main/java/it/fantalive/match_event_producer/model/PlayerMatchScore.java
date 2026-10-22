package it.fantalive.match_event_producer.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.Set;

@Document("player_match_scores")
public class PlayerMatchScore {

    @Id
    private String id;          // matchId:playerId
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

    public PlayerMatchScore() {}

    /** Fantavoto = voto + bonus/malus. Calcolato, non salvato su Mongo. */
    public Double getFantavoto() {
        return rating == null ? null : rating + bonus;
    }

    public String getId() { return id; }
    public String getMatchId() { return matchId; }
    public String getPlayerId() { return playerId; }
    public String getPlayerName() { return playerName; }
    public int getGoals() { return goals; }
    public int getAssists() { return assists; }
    public int getYellowCards() { return yellowCards; }
    public int getRedCards() { return redCards; }
    public double getBonus() { return bonus; }
    public Double getRating() { return rating; }
    public Set<String> getProcessedEventIds() { return processedEventIds; }
}
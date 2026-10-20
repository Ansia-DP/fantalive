package it.fantalive.model;

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
    private double bonus;
    private Set<String> processedEventIds = new HashSet<>();

    public PlayerMatchScore() {}

    public String getId() { return id; }
    public String getMatchId() { return matchId; }
    public String getPlayerId() { return playerId; }
    public String getPlayerName() { return playerName; }
    public int getGoals() { return goals; }
    public double getBonus() { return bonus; }
    public Set<String> getProcessedEventIds() { return processedEventIds; }
}
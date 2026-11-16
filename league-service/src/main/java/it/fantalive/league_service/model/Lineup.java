package it.fantalive.league_service.model;

import it.fantalive.league_service.exception.RuleViolationException;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Document("lineups")
public class Lineup {

    public static final int STARTERS = 11;
    public static final int MATCHDAYS = 38;

    @Id
    private String id;          // leagueId:teamId:matchday
    private String leagueId;
    private String teamId;
    private int matchday;
    private List<String> starters;
    private List<String> bench;
    private Instant submittedAt;

    public Lineup() {}

    /** Crea una formazione valida, o lancia RuleViolationException. */
    public static Lineup create(String leagueId, FantaTeam team, int matchday,
                                List<String> starters, List<String> bench) {
        if (matchday < 1 || matchday > MATCHDAYS) {
            throw new RuleViolationException("La giornata deve essere tra 1 e " + MATCHDAYS);
        }
        if (starters.size() != STARTERS) {
            throw new RuleViolationException("Servono esattamente " + STARTERS + " titolari");
        }

        var all = new ArrayList<String>(starters);
        all.addAll(bench);
        if (new HashSet<>(all).size() != all.size()) {
            throw new RuleViolationException("Un giocatore compare più di una volta");
        }

        Map<String, Role> roster = team.roster().stream()
                .collect(Collectors.toMap(RosterPlayer::playerId, RosterPlayer::role));
        for (String playerId : all) {
            if (!roster.containsKey(playerId)) {
                throw new RuleViolationException("Il giocatore " + playerId + " non è nella rosa");
            }
        }

        long goalkeepers = starters.stream().filter(p -> roster.get(p) == Role.P).count();
        if (goalkeepers != 1) {
            throw new RuleViolationException("Tra i titolari serve esattamente un portiere");
        }

        var lineup = new Lineup();
        lineup.id = leagueId + ":" + team.teamId() + ":" + matchday;
        lineup.leagueId = leagueId;
        lineup.teamId = team.teamId();
        lineup.matchday = matchday;
        lineup.starters = List.copyOf(starters);
        lineup.bench = List.copyOf(bench);
        lineup.submittedAt = Instant.now();
        return lineup;
    }

    public String getId() { return id; }
    public String getLeagueId() { return leagueId; }
    public String getTeamId() { return teamId; }
    public int getMatchday() { return matchday; }
    public List<String> getStarters() { return starters; }
    public List<String> getBench() { return bench; }
    public Instant getSubmittedAt() { return submittedAt; }
}
package it.fantalive.league_service.model;

import it.fantalive.league_service.exception.RuleViolationException;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Document("leagues")
public class League {

    public static final int MAX_TEAMS = 10;

    @Id
    private String id;
    private String name;
    private List<FantaTeam> teams = new ArrayList<>();

    @Version
    private Long version;

    public League() {}

    public League(String name) {
        this.name = name;
    }

    /** Aggiunge una squadra verificando le regole della lega. */
    public FantaTeam addTeam(String teamName, String owner, List<RosterPlayer> roster) {
        if (teams.size() >= MAX_TEAMS) {
            throw new RuleViolationException("La lega ha già " + MAX_TEAMS + " squadre");
        }
        if (teams.stream().anyMatch(t -> t.name().equalsIgnoreCase(teamName))) {
            throw new RuleViolationException("Esiste già una squadra chiamata " + teamName);
        }
        long distinct = roster.stream().map(RosterPlayer::playerId).distinct().count();
        if (distinct != roster.size()) {
            throw new RuleViolationException("La rosa contiene giocatori duplicati");
        }
        var team = new FantaTeam(UUID.randomUUID().toString(), teamName, owner, List.copyOf(roster));
        teams.add(team);
        return team;
    }

    public Optional<FantaTeam> findTeam(String teamId) {
        return teams.stream().filter(t -> t.teamId().equals(teamId)).findFirst();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public List<FantaTeam> getTeams() { return teams; }
    public Long getVersion() { return version; }
}
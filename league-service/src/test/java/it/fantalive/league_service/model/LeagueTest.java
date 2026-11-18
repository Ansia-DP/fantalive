package it.fantalive.league_service.model;

import it.fantalive.league_service.exception.RuleViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LeagueTest {

    private static List<RosterPlayer> roster() {
        return List.of(
                new RosterPlayer("p1", "Rossi", Role.P),
                new RosterPlayer("p2", "Bianchi", Role.D));
    }

    @Test
    @DisplayName("Aggiunge una squadra e la rende recuperabile per id")
    void addsTeam() {
        var league = new League("Lega Amici");

        var team = league.addTeam("Real Pizzeria", "Mario", roster());

        assertThat(team.teamId()).isNotBlank();
        assertThat(league.getTeams()).containsExactly(team);
        assertThat(league.findTeam(team.teamId())).contains(team);
    }

    @Test
    @DisplayName("Assegna id diversi a squadre diverse")
    void generatesUniqueTeamIds() {
        var league = new League("Lega Amici");

        var first = league.addTeam("Squadra A", "Mario", roster());
        var second = league.addTeam("Squadra B", "Luigi", roster());

        assertThat(first.teamId()).isNotEqualTo(second.teamId());
    }

    @Test
    @DisplayName("Rifiuta un nome di squadra già usato, ignorando maiuscole e minuscole")
    void rejectsDuplicateTeamName() {
        var league = new League("Lega Amici");
        league.addTeam("Real Pizzeria", "Mario", roster());

        assertThatThrownBy(() -> league.addTeam("real PIZZERIA", "Luigi", roster()))
                .isInstanceOf(RuleViolationException.class)
                .hasMessageContaining("Esiste già");
    }

    @Test
    @DisplayName("Rifiuta l'undicesima squadra")
    void rejectsMoreThanMaxTeams() {
        var league = new League("Lega Amici");
        IntStream.rangeClosed(1, League.MAX_TEAMS)
                .forEach(i -> league.addTeam("Squadra " + i, "Owner " + i, roster()));

        assertThatThrownBy(() -> league.addTeam("Squadra in più", "Owner", roster()))
                .isInstanceOf(RuleViolationException.class)
                .hasMessageContaining(String.valueOf(League.MAX_TEAMS));
        assertThat(league.getTeams()).hasSize(League.MAX_TEAMS);
    }

    @Test
    @DisplayName("Rifiuta una rosa con giocatori duplicati")
    void rejectsDuplicatePlayersInRoster() {
        var league = new League("Lega Amici");
        var roster = List.of(
                new RosterPlayer("p1", "Rossi", Role.P),
                new RosterPlayer("p1", "Rossi di nuovo", Role.D));

        assertThatThrownBy(() -> league.addTeam("Real Pizzeria", "Mario", roster))
                .isInstanceOf(RuleViolationException.class)
                .hasMessageContaining("duplicati");
    }

    @Test
    @DisplayName("Una squadra rifiutata non viene aggiunta alla lega")
    void rejectedTeamIsNotAdded() {
        var league = new League("Lega Amici");
        league.addTeam("Real Pizzeria", "Mario", roster());

        try {
            league.addTeam("Real Pizzeria", "Luigi", roster());
        } catch (RuleViolationException ignored) {
            // atteso
        }

        assertThat(league.getTeams()).hasSize(1);
    }

    @Test
    @DisplayName("Restituisce vuoto per una squadra inesistente")
    void findTeamReturnsEmptyForUnknownId() {
        var league = new League("Lega Amici");

        assertThat(league.findTeam("non-esiste")).isEmpty();
    }

    @Test
    @DisplayName("La rosa salvata non cambia se la lista originale viene modificata")
    void rosterIsCopied() {
        var league = new League("Lega Amici");
        var roster = new ArrayList<>(roster());
        var team = league.addTeam("Real Pizzeria", "Mario", roster);

        roster.add(new RosterPlayer("p3", "Verdi", Role.C));

        assertThat(team.roster()).hasSize(2);
    }
}
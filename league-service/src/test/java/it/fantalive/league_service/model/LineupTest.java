package it.fantalive.league_service.model;

import it.fantalive.league_service.exception.RuleViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LineupTest {

    private static final String LEAGUE_ID = "league-1";

    /**
     * Rosa di prova: p1 portiere, p2-p5 difensori, p6-p9 centrocampisti,
     * p10-p12 attaccanti, p13 secondo portiere.
     */
    private static FantaTeam team() {
        var roster = new ArrayList<RosterPlayer>();
        roster.add(new RosterPlayer("p1", "Portiere 1", Role.P));
        IntStream.rangeClosed(2, 5).forEach(i -> roster.add(new RosterPlayer("p" + i, "Difensore " + i, Role.D)));
        IntStream.rangeClosed(6, 9).forEach(i -> roster.add(new RosterPlayer("p" + i, "Centrocampista " + i, Role.C)));
        IntStream.rangeClosed(10, 12).forEach(i -> roster.add(new RosterPlayer("p" + i, "Attaccante " + i, Role.A)));
        roster.add(new RosterPlayer("p13", "Portiere 2", Role.P));
        return new FantaTeam("team-1", "Real Pizzeria", "Mario", roster);
    }

    /** Genera ["pFrom", ..., "pTo"]. */
    private static List<String> players(int from, int to) {
        return IntStream.rangeClosed(from, to).mapToObj(i -> "p" + i).toList();
    }

    private static final List<String> VALID_STARTERS = players(1, 11);

    @Test
    @DisplayName("Una formazione valida viene creata con id e dati corretti")
    void createsValidLineup() {
        // Act
        var lineup = Lineup.create(LEAGUE_ID, team(), 3, VALID_STARTERS, List.of("p12"));

        // Assert
        assertThat(lineup.getId()).isEqualTo("league-1:team-1:3");
        assertThat(lineup.getLeagueId()).isEqualTo(LEAGUE_ID);
        assertThat(lineup.getTeamId()).isEqualTo("team-1");
        assertThat(lineup.getMatchday()).isEqualTo(3);
        assertThat(lineup.getStarters()).containsExactlyElementsOf(VALID_STARTERS);
        assertThat(lineup.getBench()).containsExactly("p12");
        assertThat(lineup.getSubmittedAt()).isNotNull();
    }

    @Test
    @DisplayName("Rifiuta una formazione con meno di 11 titolari")
    void rejectsTooFewStarters() {
        assertThatThrownBy(() -> Lineup.create(LEAGUE_ID, team(), 1, players(1, 10), List.of()))
                .isInstanceOf(RuleViolationException.class)
                .hasMessageContaining("11 titolari");
    }

    @Test
    @DisplayName("Rifiuta un giocatore che non è nella rosa")
    void rejectsPlayerNotInRoster() {
        var starters = new ArrayList<>(players(1, 10));
        starters.add("p99");

        assertThatThrownBy(() -> Lineup.create(LEAGUE_ID, team(), 1, starters, List.of()))
                .isInstanceOf(RuleViolationException.class)
                .hasMessageContaining("p99");
    }

    @Test
    @DisplayName("Rifiuta un giocatore presente sia tra i titolari sia in panchina")
    void rejectsDuplicatePlayer() {
        assertThatThrownBy(() -> Lineup.create(LEAGUE_ID, team(), 1, VALID_STARTERS, List.of("p2")))
                .isInstanceOf(RuleViolationException.class)
                .hasMessageContaining("più di una volta");
    }

    @Test
    @DisplayName("Rifiuta una formazione senza portiere tra i titolari")
    void rejectsNoGoalkeeper() {
        assertThatThrownBy(() -> Lineup.create(LEAGUE_ID, team(), 1, players(2, 12), List.of()))
                .isInstanceOf(RuleViolationException.class)
                .hasMessageContaining("portiere");
    }

    @Test
    @DisplayName("Rifiuta una formazione con due portieri tra i titolari")
    void rejectsTwoGoalkeepers() {
        var starters = new ArrayList<>(players(1, 10));
        starters.add("p13");

        assertThatThrownBy(() -> Lineup.create(LEAGUE_ID, team(), 1, starters, List.of()))
                .isInstanceOf(RuleViolationException.class)
                .hasMessageContaining("portiere");
    }

    @ParameterizedTest(name = "giornata {0} rifiutata")
    @ValueSource(ints = {-1, 0, 39, 100})
    @DisplayName("Rifiuta giornate fuori dall'intervallo 1-38")
    void rejectsInvalidMatchday(int matchday) {
        assertThatThrownBy(() -> Lineup.create(LEAGUE_ID, team(), matchday, VALID_STARTERS, List.of()))
                .isInstanceOf(RuleViolationException.class)
                .hasMessageContaining("giornata");
    }

    @ParameterizedTest(name = "giornata {0} accettata")
    @ValueSource(ints = {1, 38})
    @DisplayName("Accetta la prima e l'ultima giornata")
    void acceptsBoundaryMatchdays(int matchday) {
        var lineup = Lineup.create(LEAGUE_ID, team(), matchday, VALID_STARTERS, List.of());

        assertThat(lineup.getMatchday()).isEqualTo(matchday);
    }

    @Test
    @DisplayName("La formazione non cambia se la lista originale viene modificata dopo la creazione")
    void isImmutable() {
        var starters = new ArrayList<>(VALID_STARTERS);
        var lineup = Lineup.create(LEAGUE_ID, team(), 1, starters, List.of());

        starters.set(0, "p99");   // modifico la lista passata in ingresso

        assertThat(lineup.getStarters()).first().isEqualTo("p1");
        assertThatThrownBy(() -> lineup.getStarters().add("p12"))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
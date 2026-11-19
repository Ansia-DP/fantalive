package it.fantalive.match_event_producer.service;

import it.fantalive.match_event_producer.event.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Il simulatore è casuale: invece di verificare UNA partita precisa,
 * verifichiamo le proprietà che devono valere per QUALSIASI partita,
 * ripetendo i test molte volte (@RepeatedTest).
 */
@ExtendWith(MockitoExtension.class)
class MatchSimulatorTest {

    private static final String MATCH_ID = "NAP-JUV";
    private static final int MATCHDAY = 7;

    private static final List<MatchSimulator.Player> PLAYERS = List.of(
            new MatchSimulator.Player("p1", "Rossi"),
            new MatchSimulator.Player("p2", "Bianchi"),
            new MatchSimulator.Player("p3", "Verdi"),
            new MatchSimulator.Player("p4", "Neri"),
            new MatchSimulator.Player("p5", "Russo"));

    /** Mock: un publisher finto che non pubblica nulla ma registra ogni chiamata. */
    @Mock
    private MatchEventPublisher publisher;

    private MatchSimulator simulator;

    @BeforeEach
    void setUp() {
        simulator = new MatchSimulator(publisher);
    }

    // --- Helper ---

    private static <T extends MatchEvent> List<T> ofType(List<MatchEvent> events, Class<T> type) {
        return events.stream().filter(type::isInstance).map(type::cast).toList();
    }

    /** Minuto dell'evento, o null per i voti (che non hanno minuto). */
    private static Integer minuteOf(MatchEvent event) {
        return switch (event) {
            case GoalScored g -> g.minute();
            case AssistMade a -> a.minute();
            case CardGiven c -> c.minute();
            case PlayerRated r -> null;
        };
    }

    // --- Proprietà della partita simulata ---

    @RepeatedTest(50)
    @DisplayName("Ogni giocatore riceve esattamente un voto, e i voti arrivano a fine partita")
    void everyPlayerGetsOneRatingAtTheEnd() {
        var events = simulator.simulate(MATCH_ID, MATCHDAY, PLAYERS);

        var ratings = ofType(events, PlayerRated.class);
        assertThat(ratings)
                .extracting(PlayerRated::playerId)
                .containsExactlyInAnyOrder("p1", "p2", "p3", "p4", "p5");

        var lastEvents = events.subList(events.size() - PLAYERS.size(), events.size());
        assertThat(lastEvents).allMatch(PlayerRated.class::isInstance);
    }

    @RepeatedTest(50)
    @DisplayName("Gli eventi di gioco sono in ordine cronologico, tra il minuto 1 e il 90")
    void matchEventsAreChronological() {
        var events = simulator.simulate(MATCH_ID, MATCHDAY, PLAYERS);

        var minutes = events.stream()
                .map(MatchSimulatorTest::minuteOf)
                .filter(Objects::nonNull)
                .toList();

        assertThat(minutes).isSorted();
        assertThat(minutes).allSatisfy(m -> assertThat(m).isBetween(1, 90));
    }

    @RepeatedTest(50)
    @DisplayName("Tutti gli eventi hanno la partita e la giornata richieste, e id univoci")
    void allEventsCarryMatchAndMatchday() {
        var events = simulator.simulate(MATCH_ID, MATCHDAY, PLAYERS);

        assertThat(events).allSatisfy(e -> {
            assertThat(e.matchId()).isEqualTo(MATCH_ID);
            assertThat(e.matchday()).isEqualTo(MATCHDAY);
        });
        assertThat(events).extracting(MatchEvent::eventId).doesNotHaveDuplicates();
    }

    @RepeatedTest(50)
    @DisplayName("I voti sono tra 5 e 10, a passi di mezzo punto, e premiano chi segna")
    void ratingsAreRealistic() {
        var events = simulator.simulate(MATCH_ID, MATCHDAY, PLAYERS);

        Map<String, Long> goalsByPlayer = ofType(events, GoalScored.class).stream()
                .collect(Collectors.groupingBy(GoalScored::playerId, Collectors.counting()));

        assertThat(ofType(events, PlayerRated.class)).allSatisfy(r -> {
            assertThat(r.rating()).isBetween(5.0, 10.0);
            assertThat(r.rating() * 2).isEqualTo(Math.rint(r.rating() * 2));   // multiplo di 0.5

            long goals = goalsByPlayer.getOrDefault(r.playerId(), 0L);
            assertThat(r.rating()).isGreaterThanOrEqualTo(Math.min(10.0, 5.0 + goals));
        });
    }

    @RepeatedTest(50)
    @DisplayName("Ogni assist accompagna un gol dello stesso minuto, segnato da un altro giocatore")
    void assistsGoToSomeoneElse() {
        var events = simulator.simulate(MATCH_ID, MATCHDAY, PLAYERS);
        var goals = ofType(events, GoalScored.class);

        assertThat(ofType(events, AssistMade.class)).allSatisfy(a ->
                assertThat(goals).anySatisfy(g -> {
                    assertThat(g.minute()).isEqualTo(a.minute());
                    assertThat(g.playerId()).isNotEqualTo(a.playerId());
                }));
    }

    @RepeatedTest(20)
    @DisplayName("Con un solo giocatore non ci sono mai assist")
    void singlePlayerNeverGetsAssists() {
        var events = simulator.simulate(MATCH_ID, MATCHDAY, List.of(PLAYERS.getFirst()));

        assertThat(ofType(events, AssistMade.class)).isEmpty();
    }

    // --- Interazione con il publisher (il mock) ---

    @Test
    @DisplayName("Pubblica ogni evento una sola volta, nello stesso ordine restituito")
    void publishesEveryEventInOrder() {
        var events = simulator.simulate(MATCH_ID, MATCHDAY, PLAYERS);

        // ArgumentCaptor "cattura" gli argomenti con cui il mock è stato chiamato
        var captor = ArgumentCaptor.forClass(MatchEvent.class);
        verify(publisher, times(events.size())).publish(captor.capture());

        assertThat(captor.getAllValues()).containsExactlyElementsOf(events);
    }

    // --- Input non validi ---

    @Test
    @DisplayName("Rifiuta una partita senza giocatori, senza pubblicare nulla")
    void rejectsEmptyPlayerList() {
        assertThatThrownBy(() -> simulator.simulate(MATCH_ID, MATCHDAY, List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("almeno un giocatore");

        verifyNoInteractions(publisher);
    }

    @ParameterizedTest(name = "giornata {0} rifiutata")
    @ValueSource(ints = {0, 39})
    @DisplayName("Rifiuta giornate fuori dall'intervallo 1-38, senza pubblicare nulla")
    void rejectsInvalidMatchday(int matchday) {
        assertThatThrownBy(() -> simulator.simulate(MATCH_ID, matchday, PLAYERS))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("giornata");

        verifyNoInteractions(publisher);
    }
}
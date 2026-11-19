package it.fantalive.match_event_producer.domain;

import it.fantalive.match_event_producer.event.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Nei test delle regole di business i valori attesi sono scritti come letterali
 * (3.0, -0.5, ...) e NON presi dalle costanti di ScoringRules: il test deve
 * proteggere la specifica ("il giallo vale -0.5"), non ricopiarla dal codice.
 */
class ScoringRulesTest {

    // --- Helper per creare gli eventi senza ripetere tutti i campi ---

    private static GoalScored goal() {
        return new GoalScored("e1", "NAP-JUV", 1, "p9", "Colombo", 23, Instant.now());
    }

    private static AssistMade assist() {
        return new AssistMade("e2", "NAP-JUV", 1, "p9", "Colombo", 23, Instant.now());
    }

    private static CardGiven card(CardGiven.Card card) {
        return new CardGiven("e3", "NAP-JUV", 1, "p9", "Colombo", card, 40, Instant.now());
    }

    private static PlayerRated rating(double value) {
        return new PlayerRated("e4", "NAP-JUV", 1, "p9", "Colombo", value, Instant.now());
    }

    // --- Un test per regola ---

    @Test
    @DisplayName("Un gol aggiunge un gol e 3 punti di bonus")
    void goal_addsGoalAndBonus() {
        var change = ScoringRules.changeFor(goal());

        assertThat(change).isEqualTo(new ScoreChange(1, 0, 0, 0, 3.0, null));
    }

    @Test
    @DisplayName("Un assist aggiunge un assist e 1 punto di bonus")
    void assist_addsAssistAndBonus() {
        var change = ScoringRules.changeFor(assist());

        assertThat(change).isEqualTo(new ScoreChange(0, 1, 0, 0, 1.0, null));
    }

    @Test
    @DisplayName("Un giallo aggiunge un'ammonizione e mezzo punto di malus")
    void yellowCard_addsMalus() {
        var change = ScoringRules.changeFor(card(CardGiven.Card.YELLOW));

        assertThat(change).isEqualTo(new ScoreChange(0, 0, 1, 0, -0.5, null));
    }

    @Test
    @DisplayName("Un rosso aggiunge un'espulsione e 1 punto di malus")
    void redCard_addsMalus() {
        var change = ScoringRules.changeFor(card(CardGiven.Card.RED));

        assertThat(change).isEqualTo(new ScoreChange(0, 0, 0, 1, -1.0, null));
    }

    @Test
    @DisplayName("Il voto imposta il rating senza toccare il bonus")
    void rating_setsRatingOnly() {
        var change = ScoringRules.changeFor(rating(6.5));

        assertThat(change).isEqualTo(new ScoreChange(0, 0, 0, 0, 0.0, 6.5));
    }

    @ParameterizedTest(name = "voto {0} rifiutato")
    @ValueSource(doubles = {-1.0, 0.0, 0.5, 10.5, 15.0})
    @DisplayName("Rifiuta voti fuori dall'intervallo 1-10")
    void rating_outOfRange_isRejected(double value) {
        assertThatThrownBy(() -> ScoringRules.changeFor(rating(value)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Voto non valido");
    }

    @ParameterizedTest(name = "voto {0} accettato")
    @ValueSource(doubles = {1.0, 10.0})
    @DisplayName("Accetta i voti limite 1 e 10")
    void rating_boundaries_areAccepted(double value) {
        assertThat(ScoringRules.changeFor(rating(value)).rating()).isEqualTo(value);
    }

    // --- Fantavoto ---

    @Test
    @DisplayName("Senza voto il fantavoto è null, anche con dei bonus")
    void fantavoto_withoutRating_isNull() {
        assertThat(ScoringRules.fantavoto(null, 3.0)).isNull();
    }

    @ParameterizedTest(name = "voto {0} + bonus {1} = {2}")
    @CsvSource({
            "6.0,  0.0,  6.0",
            "6.0,  3.0,  9.0",
            "6.5, -0.5,  6.0",
            "5.0, -1.0,  4.0",
            "7.5,  4.0, 11.5"
    })
    @DisplayName("Il fantavoto è la somma di voto e bonus")
    void fantavoto_isRatingPlusBonus(double rating, double bonus, double expected) {
        assertThat(ScoringRules.fantavoto(rating, bonus)).isEqualTo(expected);
    }

    @Test
    @DisplayName("Partita completa: gol, assist e giallo con voto 7 danno fantavoto 10.5")
    void fullMatch_scenario() {
        List<MatchEvent> events = List.of(goal(), assist(), card(CardGiven.Card.YELLOW), rating(7.0));

        double bonus = events.stream()
                .map(ScoringRules::changeFor)
                .mapToDouble(ScoreChange::bonus)
                .sum();

        assertThat(bonus).isEqualTo(3.5);                          // +3 +1 -0.5
        assertThat(ScoringRules.fantavoto(7.0, bonus)).isEqualTo(10.5);
    }
}
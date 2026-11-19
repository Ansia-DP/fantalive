package it.fantalive.match_event_producer.domain;

import it.fantalive.match_event_producer.event.*;

public final class ScoringRules {

    public static final double GOAL_BONUS = 3.0;
    public static final double ASSIST_BONUS = 1.0;
    public static final double YELLOW_CARD_MALUS = -0.5;
    public static final double RED_CARD_MALUS = -1.0;
    public static final double MIN_RATING = 1.0;
    public static final double MAX_RATING = 10.0;

    private ScoringRules() {}

    /** Calcola cosa cambia nel punteggio per effetto dell'evento. */
    public static ScoreChange changeFor(MatchEvent event) {
        return switch (event) {
            case GoalScored g -> new ScoreChange(1, 0, 0, 0, GOAL_BONUS, null);
            case AssistMade a -> new ScoreChange(0, 1, 0, 0, ASSIST_BONUS, null);
            case CardGiven c when c.card() == CardGiven.Card.YELLOW ->
                    new ScoreChange(0, 0, 1, 0, YELLOW_CARD_MALUS, null);
            case CardGiven c -> new ScoreChange(0, 0, 0, 1, RED_CARD_MALUS, null);
            case PlayerRated r -> {
                if (r.rating() < MIN_RATING || r.rating() > MAX_RATING) {
                    throw new IllegalArgumentException(
                            "Voto non valido per " + r.playerName() + ": " + r.rating());
                }
                yield new ScoreChange(0, 0, 0, 0, 0.0, r.rating());
            }
        };
    }

    /** Fantavoto = voto + bonus/malus; null se il giocatore è senza voto. */
    public static Double fantavoto(Double rating, double bonus) {
        return rating == null ? null : rating + bonus;
    }
}
package it.fantalive.scoring_service.listener;

import it.fantalive.scoring_service.event.MatchEvent;
import it.fantalive.scoring_service.service.ScorePublisher;
import it.fantalive.scoring_service.service.ScoringService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class MatchEventListener {

    private static final Logger log = LoggerFactory.getLogger(MatchEventListener.class);
    private final ScoringService scoringService;
    private final ScorePublisher scorePublisher;

    public MatchEventListener(ScoringService scoringService, ScorePublisher scorePublisher) {
        this.scoringService = scoringService;
        this.scorePublisher = scorePublisher;
    }

    @KafkaListener(topics = "match-events")
    public void onEvent(MatchEvent event) {
        var result = scoringService.apply(event);

        if (result.applied()) {
            log.info("{} applicato: {} (giornata {}, evento {})", event.getClass().getSimpleName(),
                    event.playerName(), event.matchday(), event.eventId());
        } else {
            log.warn("Evento {} già elaborato: ripubblico lo stato attuale", event.eventId());
        }

        // Pubblichiamo SEMPRE lo snapshot, anche per i duplicati:
        // se una pubblicazione precedente era fallita, così il buco si richiude.
        scorePublisher.publish(result.score());
    }
}
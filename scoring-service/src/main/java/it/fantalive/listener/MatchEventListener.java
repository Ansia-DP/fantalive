package it.fantalive.listener;

import it.fantalive.event.GoalScored;
import it.fantalive.service.ScoringService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class MatchEventListener {

    private static final Logger log = LoggerFactory.getLogger(MatchEventListener.class);
    private final ScoringService scoringService;

    public MatchEventListener(ScoringService scoringService) {
        this.scoringService = scoringService;
    }

    @KafkaListener(topics = "match-events")
    public void onGoal(GoalScored event) {
        if (scoringService.applyGoal(event)) {
            log.info("Gol di {} al {}' applicato (evento {})",
                    event.playerName(), event.minute(), event.eventId());
        } else {
            log.warn("Evento {} già elaborato: duplicato ignorato", event.eventId());
        }
    }
}
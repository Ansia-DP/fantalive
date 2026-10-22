package it.fantalive.match_event_producer.listener;

import it.fantalive.match_event_producer.event.MatchEvent;
import it.fantalive.match_event_producer.service.ScoringService;
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
    public void onEvent(MatchEvent event) {
        if (scoringService.apply(event)) {
            log.info("{} applicato: {} (evento {})",
                    event.getClass().getSimpleName(), event.playerName(), event.eventId());
        } else {
            log.warn("Evento {} già elaborato: duplicato ignorato", event.eventId());
        }
    }
}
package it.fantalive.standings_service.listener;

import it.fantalive.standings_service.event.LineupSubmitted;
import it.fantalive.standings_service.event.PlayerScoreUpdated;
import it.fantalive.standings_service.service.ProjectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class StandingsListener {

    private static final Logger log = LoggerFactory.getLogger(StandingsListener.class);
    private final ProjectionService projection;

    public StandingsListener(ProjectionService projection) {
        this.projection = projection;
    }

    @KafkaListener(topics = "lineup-events", properties =
            "spring.json.value.default.type=it.fantalive.standings_service.event.LineupSubmitted")
    public void onLineup(LineupSubmitted event) {
        projection.onLineup(event);
        log.info("Formazione registrata: {} (giornata {})", event.teamName(), event.matchday());
    }

    @KafkaListener(topics = "player-scores", properties =
            "spring.json.value.default.type=it.fantalive.standings_service.event.PlayerScoreUpdated")
    public void onPlayerScore(PlayerScoreUpdated event) {
        if (projection.onPlayerScore(event)) {
            log.info("Fantavoto aggiornato: {} giornata {} -> {} (v{})", event.playerName(),
                    event.matchday(), event.fantavoto(), event.version());
        } else {
            log.debug("Snapshot ignorato (vecchio o già visto): {} v{}",
                    event.playerName(), event.version());
        }
    }
}
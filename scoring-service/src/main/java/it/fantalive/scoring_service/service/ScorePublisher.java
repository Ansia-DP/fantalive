package it.fantalive.scoring_service.service;

import it.fantalive.scoring_service.config.KafkaTopicConfig;
import it.fantalive.scoring_service.event.PlayerScoreUpdated;
import it.fantalive.scoring_service.model.PlayerMatchScore;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class ScorePublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ScorePublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Pubblica lo stato completo del giocatore e ATTENDE la conferma di Kafka.
     * Se fallisce, l'eccezione risale al listener: l'evento in ingresso non viene
     * confermato e Kafka lo riconsegnerà.
     */
    public void publish(PlayerMatchScore s) {
        var event = new PlayerScoreUpdated(UUID.randomUUID().toString(), s.getMatchday(),
                s.getMatchId(), s.getPlayerId(), s.getPlayerName(), s.getRating(),
                s.getBonus(), s.getFantavoto(), s.getProcessedEventIds().size(), Instant.now());
        try {
            kafkaTemplate.send(KafkaTopicConfig.PLAYER_SCORES, s.getPlayerId(), event)
                    .get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new IllegalStateException("Pubblicazione del punteggio fallita", e);
        }
    }
}
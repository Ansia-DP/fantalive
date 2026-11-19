package it.fantalive.match_event_producer.service;

import it.fantalive.match_event_producer.config.KafkaTopicConfig;
import it.fantalive.match_event_producer.event.PlayerScoreUpdated;
import it.fantalive.match_event_producer.model.PlayerMatchScore;
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
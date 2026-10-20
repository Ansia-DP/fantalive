package it.fantalive.service;

import it.fantalive.config.KafkaTopicConfig;
import it.fantalive.event.GoalScored;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class MatchEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(MatchEventPublisher.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public MatchEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(GoalScored event) {
        // chiave = matchId: tutti gli eventi della stessa partita finiscono
        // nella stessa partizione, quindi restano in ordine
        kafkaTemplate.send(KafkaTopicConfig.MATCH_EVENTS, event.matchId(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Invio fallito per evento {}", event.eventId(), ex);
                    } else {
                        log.info("Evento {} inviato su partizione {} offset {}",
                                event.eventId(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
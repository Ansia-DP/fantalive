package it.fantalive.match_event_producer.service;

import it.fantalive.match_event_producer.config.KafkaTopicConfig;
import it.fantalive.match_event_producer.event.MatchEvent;
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

    public void publish(MatchEvent event) {
        kafkaTemplate.send(KafkaTopicConfig.MATCH_EVENTS, event.matchId(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Invio fallito per evento {}", event.eventId(), ex);
                    } else {
                        log.info("{} {} inviato su partizione {} offset {}",
                                event.getClass().getSimpleName(), event.eventId(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
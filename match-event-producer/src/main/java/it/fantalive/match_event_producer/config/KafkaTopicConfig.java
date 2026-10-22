package it.fantalive.match_event_producer.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String MATCH_EVENTS = "match-events";

    @Bean
    NewTopic matchEventsTopic() {
        return TopicBuilder.name(MATCH_EVENTS).partitions(3).replicas(1).build();
    }
}
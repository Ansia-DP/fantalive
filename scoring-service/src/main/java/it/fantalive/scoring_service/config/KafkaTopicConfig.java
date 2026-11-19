package it.fantalive.match_event_producer.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String PLAYER_SCORES = "player-scores";

    @Bean
    NewTopic playerScoresTopic() {
        return TopicBuilder.name(PLAYER_SCORES).partitions(3).replicas(1).build();
    }
}
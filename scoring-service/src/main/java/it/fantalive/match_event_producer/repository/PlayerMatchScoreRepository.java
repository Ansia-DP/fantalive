package it.fantalive.match_event_producer.repository;

import it.fantalive.match_event_producer.model.PlayerMatchScore;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PlayerMatchScoreRepository extends MongoRepository<PlayerMatchScore, String> {}
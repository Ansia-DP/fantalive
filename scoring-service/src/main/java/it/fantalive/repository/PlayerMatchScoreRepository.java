package it.fantalive.repository;

import it.fantalive.model.PlayerMatchScore;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PlayerMatchScoreRepository extends MongoRepository<PlayerMatchScore, String> {}
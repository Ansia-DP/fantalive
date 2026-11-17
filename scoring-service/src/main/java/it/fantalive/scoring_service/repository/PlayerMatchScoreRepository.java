package it.fantalive.scoring_service.repository;

import it.fantalive.scoring_service.model.PlayerMatchScore;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PlayerMatchScoreRepository extends MongoRepository<PlayerMatchScore, String> {}
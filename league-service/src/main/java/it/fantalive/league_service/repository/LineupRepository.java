package it.fantalive.league_service.repository;

import it.fantalive.league_service.model.Lineup;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LineupRepository extends MongoRepository<Lineup, String> {}
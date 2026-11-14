package it.fantalive.league_service.repository;

import it.fantalive.league_service.model.League;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LeagueRepository extends MongoRepository<League, String> {}
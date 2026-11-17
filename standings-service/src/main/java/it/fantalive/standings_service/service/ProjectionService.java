package it.fantalive.standings_service.service;

import it.fantalive.standings_service.event.LineupSubmitted;
import it.fantalive.standings_service.event.PlayerScoreUpdated;
import it.fantalive.standings_service.model.LineupView;
import it.fantalive.standings_service.model.PlayerScoreView;
import it.fantalive.standings_service.repository.LineupViewRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
public class ProjectionService {

    private final LineupViewRepository lineupRepository;
    private final MongoTemplate mongoTemplate;

    public ProjectionService(LineupViewRepository lineupRepository, MongoTemplate mongoTemplate) {
        this.lineupRepository = lineupRepository;
        this.mongoTemplate = mongoTemplate;
    }

    /** La formazione più recente sostituisce la precedente: save è già idempotente. */
    public void onLineup(LineupSubmitted event) {
        lineupRepository.save(LineupView.from(event));
    }

    /**
     * Salva lo snapshot solo se è più recente di quello già presente.
     * @return true se salvato, false se era vecchio o già visto
     */
    public boolean onPlayerScore(PlayerScoreUpdated e) {
        String id = e.matchday() + ":" + e.playerId();
        try {
            mongoTemplate.upsert(
                    Query.query(Criteria.where("_id").is(id).and("version").lt(e.version())),
                    new Update()
                            .set("matchday", e.matchday())
                            .set("playerId", e.playerId())
                            .set("playerName", e.playerName())
                            .set("fantavoto", e.fantavoto())
                            .set("version", e.version())
                            .set("updatedAt", e.updatedAt()),
                    PlayerScoreView.class);
            return true;
        } catch (DuplicateKeyException ex) {
            return false;
        }
    }
}
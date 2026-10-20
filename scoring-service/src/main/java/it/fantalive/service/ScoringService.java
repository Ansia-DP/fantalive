package it.fantalive.service;

import com.mongodb.client.result.UpdateResult;
import it.fantalive.event.GoalScored;
import it.fantalive.model.PlayerMatchScore;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
public class ScoringService {

    private final MongoTemplate mongoTemplate;

    public ScoringService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Applica il gol se l'evento non è già stato elaborato.
     * @return true se applicato, false se era un duplicato
     */
    public boolean applyGoal(GoalScored event) {
        String id = event.matchId() + ":" + event.playerId();

        // 1. Crea il documento se non esiste ancora (innocuo se esiste già)
        mongoTemplate.upsert(
                Query.query(Criteria.where("_id").is(id)),
                new Update()
                        .setOnInsert("matchId", event.matchId())
                        .setOnInsert("playerId", event.playerId())
                        .setOnInsert("playerName", event.playerName())
                        .setOnInsert("goals", 0)
                        .setOnInsert("bonus", 0.0),
                PlayerMatchScore.class);

        // 2. Applica il gol SOLO se l'eventId non è già nell'elenco (operazione atomica)
        UpdateResult result = mongoTemplate.updateFirst(
                Query.query(Criteria.where("_id").is(id)
                        .and("processedEventIds").ne(event.eventId())),
                new Update()
                        .inc("goals", 1)
                        .inc("bonus", 3.0)
                        .addToSet("processedEventIds", event.eventId()),
                PlayerMatchScore.class);

        return result.getModifiedCount() == 1;
    }
}
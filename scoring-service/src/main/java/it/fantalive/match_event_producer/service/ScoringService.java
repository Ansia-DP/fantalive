package it.fantalive.match_event_producer.service;

import com.mongodb.client.result.UpdateResult;
import it.fantalive.match_event_producer.event.*;
import it.fantalive.match_event_producer.model.PlayerMatchScore;
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
     * Applica l'evento se non è già stato elaborato.
     * @return true se applicato, false se era un duplicato
     */
    public boolean apply(MatchEvent event) {
        String id = event.matchId() + ":" + event.playerId();

        // 1. Crea il documento se non esiste (i contatori non servono:
        //    $inc su un campo mancante parte da 0)
        mongoTemplate.upsert(
                Query.query(Criteria.where("_id").is(id)),
                new Update()
                        .setOnInsert("matchId", event.matchId())
                        .setOnInsert("playerId", event.playerId())
                        .setOnInsert("playerName", event.playerName()),
                PlayerMatchScore.class);

        // 2. Cosa cambia, in base al tipo di evento
        Update update = switch (event) {
            case GoalScored g -> new Update().inc("goals", 1).inc("bonus", 3.0);
            case AssistMade a -> new Update().inc("assists", 1).inc("bonus", 1.0);
            case CardGiven c when c.card() == CardGiven.Card.YELLOW ->
                    new Update().inc("yellowCards", 1).inc("bonus", -0.5);
            case CardGiven c -> new Update().inc("redCards", 1).inc("bonus", -1.0);
            case PlayerRated r -> new Update().set("rating", r.rating());
        };
        update.addToSet("processedEventIds", event.eventId());

        // 3. Applica solo se l'evento non è già stato elaborato (atomico)
        UpdateResult result = mongoTemplate.updateFirst(
                Query.query(Criteria.where("_id").is(id)
                        .and("processedEventIds").ne(event.eventId())),
                update,
                PlayerMatchScore.class);

        return result.getModifiedCount() == 1;
    }
}
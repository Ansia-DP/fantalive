package it.fantalive.scoring_service.service;

import it.fantalive.scoring_service.event.*;
import it.fantalive.scoring_service.model.PlayerMatchScore;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
public class ScoringService {

    /** Esito: se l'evento è stato applicato, e lo stato attuale del giocatore. */
    public record ApplyResult(boolean applied, PlayerMatchScore score) {}

    private final MongoTemplate mongoTemplate;

    public ScoringService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public ApplyResult apply(MatchEvent event) {
        String id = event.matchday() + ":" + event.matchId() + ":" + event.playerId();

        // 1. Crea il documento se non esiste
        mongoTemplate.upsert(
                Query.query(Criteria.where("_id").is(id)),
                new Update()
                        .setOnInsert("matchday", event.matchday())
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

        // 3. Applica solo se l'evento è nuovo, e restituisci il documento GIÀ aggiornato
        PlayerMatchScore updated = mongoTemplate.findAndModify(
                Query.query(Criteria.where("_id").is(id)
                        .and("processedEventIds").ne(event.eventId())),
                update,
                FindAndModifyOptions.options().returnNew(true),
                PlayerMatchScore.class);

        if (updated != null) {
            return new ApplyResult(true, updated);
        }
        // Duplicato: niente da applicare, ma restituiamo lo stato attuale
        return new ApplyResult(false, mongoTemplate.findById(id, PlayerMatchScore.class));
    }
}
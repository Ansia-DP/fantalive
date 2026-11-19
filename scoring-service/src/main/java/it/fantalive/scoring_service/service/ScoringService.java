package it.fantalive.match_event_producer.service;

import it.fantalive.match_event_producer.domain.ScoreChange;
import it.fantalive.match_event_producer.domain.ScoringRules;
import it.fantalive.match_event_producer.event.*;
import it.fantalive.match_event_producer.model.PlayerMatchScore;
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

        Update update = toUpdate(ScoringRules.changeFor(event));
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

    private static Update toUpdate(ScoreChange change) {
        var update = new Update();
        if (change.goals() != 0)       update.inc("goals", change.goals());
        if (change.assists() != 0)     update.inc("assists", change.assists());
        if (change.yellowCards() != 0) update.inc("yellowCards", change.yellowCards());
        if (change.redCards() != 0)    update.inc("redCards", change.redCards());
        if (change.bonus() != 0)       update.inc("bonus", change.bonus());
        if (change.rating() != null)   update.set("rating", change.rating());
        return update;
    }
}
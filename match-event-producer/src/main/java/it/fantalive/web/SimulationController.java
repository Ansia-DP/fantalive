package it.fantalive.web;

import it.fantalive.event.GoalScored;
import it.fantalive.service.MatchEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/simulate")
public class SimulationController {

    private final MatchEventPublisher publisher;

    public SimulationController(MatchEventPublisher publisher) {
        this.publisher = publisher;
    }

    // eventId opzionale: se lo passi puoi simulare un evento duplicato
    public record GoalRequest(String eventId, String playerId, String playerName, int minute) {}

    @PostMapping("/{matchId}/goal")
    public ResponseEntity<GoalScored> goal(@PathVariable String matchId,
                                           @RequestBody GoalRequest req) {
        var eventId = req.eventId() != null ? req.eventId() : UUID.randomUUID().toString();
        var event = new GoalScored(eventId, matchId,
                req.playerId(), req.playerName(), req.minute(), Instant.now());
        publisher.publish(event);
        return ResponseEntity.accepted().body(event);
    }
}
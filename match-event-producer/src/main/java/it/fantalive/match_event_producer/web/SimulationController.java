package it.fantalive.match_event_producer.web;

import it.fantalive.match_event_producer.event.GoalScored;
import it.fantalive.match_event_producer.event.MatchEvent;
import it.fantalive.match_event_producer.service.MatchEventPublisher;
import it.fantalive.match_event_producer.service.MatchSimulator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/simulate")
public class SimulationController {

    private final MatchEventPublisher publisher;
    private final MatchSimulator simulator;

    public SimulationController(MatchEventPublisher publisher, MatchSimulator simulator) {
        this.publisher = publisher;
        this.simulator = simulator;
    }

    // Gol singolo, con eventId opzionale per simulare duplicati
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

    // Partita intera simulata
    public record SimulationRequest(List<MatchSimulator.Player> players) {}

    @PostMapping("/{matchId}/match")
    public ResponseEntity<List<MatchEvent>> match(@PathVariable String matchId,
                                                  @RequestBody SimulationRequest req) {
        return ResponseEntity.accepted().body(simulator.simulate(matchId, req.players()));
    }
}
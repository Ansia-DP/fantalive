package it.fantalive.scoring_service.web;

import it.fantalive.scoring_service.event.GoalScored;
import it.fantalive.scoring_service.event.MatchEvent;
import it.fantalive.scoring_service.service.MatchEventPublisher;
import it.fantalive.scoring_service.service.MatchSimulator;
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

    public record GoalRequest(String eventId, int matchday, String playerId, String playerName, int minute) {}

    @PostMapping("/{matchId}/goal")
    public ResponseEntity<GoalScored> goal(@PathVariable String matchId,
                                           @RequestBody GoalRequest req) {
        var eventId = req.eventId() != null ? req.eventId() : UUID.randomUUID().toString();
        var event = new GoalScored(eventId, matchId, req.matchday(),
                req.playerId(), req.playerName(), req.minute(), Instant.now());
        publisher.publish(event);
        return ResponseEntity.accepted().body(event);
    }

    public record SimulationRequest(int matchday, List<MatchSimulator.Player> players) {}

    @PostMapping("/{matchId}/match")
    public ResponseEntity<List<MatchEvent>> match(@PathVariable String matchId,
                                                  @RequestBody SimulationRequest req) {
        return ResponseEntity.accepted().body(simulator.simulate(matchId, req.matchday(), req.players()));
    }
}
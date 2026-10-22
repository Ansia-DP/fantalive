package it.fantalive.match_event_producer.service;

import it.fantalive.match_event_producer.event.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class MatchSimulator {

    public record Player(String id, String name) {}

    private record Timed(int minute, MatchEvent event) {}

    private final MatchEventPublisher publisher;

    public MatchSimulator(MatchEventPublisher publisher) {
        this.publisher = publisher;
    }

    public List<MatchEvent> simulate(String matchId, List<Player> players) {
        var rnd = ThreadLocalRandom.current();
        var timeline = new ArrayList<Timed>();
        var goalsByPlayer = new HashMap<String, Integer>();

        // Gol e assist
        int goals = rnd.nextInt(0, 5);
        for (int i = 0; i < goals; i++) {
            var scorer = pick(players, rnd);
            int minute = rnd.nextInt(1, 91);
            timeline.add(new Timed(minute, new GoalScored(newId(), matchId,
                    scorer.id(), scorer.name(), minute, Instant.now())));
            goalsByPlayer.merge(scorer.id(), 1, Integer::sum);

            if (players.size() > 1 && rnd.nextDouble() < 0.6) {
                Player assistman;
                do { assistman = pick(players, rnd); } while (assistman.equals(scorer));
                timeline.add(new Timed(minute, new AssistMade(newId(), matchId,
                        assistman.id(), assistman.name(), minute, Instant.now())));
            }
        }

        // Cartellini
        int yellows = rnd.nextInt(0, 5);
        for (int i = 0; i < yellows; i++) {
            var p = pick(players, rnd);
            int minute = rnd.nextInt(1, 91);
            timeline.add(new Timed(minute, new CardGiven(newId(), matchId,
                    p.id(), p.name(), CardGiven.Card.YELLOW, minute, Instant.now())));
        }
        if (rnd.nextDouble() < 0.1) {
            var p = pick(players, rnd);
            int minute = rnd.nextInt(1, 91);
            timeline.add(new Timed(minute, new CardGiven(newId(), matchId,
                    p.id(), p.name(), CardGiven.Card.RED, minute, Instant.now())));
        }

        // Ordine cronologico
        timeline.sort(Comparator.comparingInt(Timed::minute));
        var events = new ArrayList<MatchEvent>(timeline.stream().map(Timed::event).toList());

        // Voti in pagella a fine partita: da 5 a 7, +1 per ogni gol, massimo 10
        for (var p : players) {
            double base = 5.0 + 0.5 * rnd.nextInt(0, 5);
            double rating = Math.min(10.0, base + goalsByPlayer.getOrDefault(p.id(), 0));
            events.add(new PlayerRated(newId(), matchId, p.id(), p.name(), rating, Instant.now()));
        }

        events.forEach(publisher::publish);
        return events;
    }

    private static Player pick(List<Player> players, ThreadLocalRandom rnd) {
        return players.get(rnd.nextInt(players.size()));
    }

    private static String newId() {
        return UUID.randomUUID().toString();
    }
}
package it.fantalive.standings_service.web;

import it.fantalive.standings_service.repository.LineupViewRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/leagues/{leagueId}")
public class StandingsController {

    private final LineupViewRepository repository;

    public StandingsController(LineupViewRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/standings")
    public List<TeamStanding> standings(@PathVariable String leagueId) {
        return repository.standings(leagueId);
    }

    @GetMapping("/matchdays/{matchday}")
    public List<MatchdayResult> matchday(@PathVariable String leagueId, @PathVariable int matchday) {
        return repository.matchday(leagueId, matchday);
    }
}
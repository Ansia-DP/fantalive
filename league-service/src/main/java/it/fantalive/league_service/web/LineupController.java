package it.fantalive.league_service.web;

import it.fantalive.league_service.model.Lineup;
import it.fantalive.league_service.service.LineupService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/leagues/{leagueId}/teams/{teamId}/lineups")
public class LineupController {

    private final LineupService service;

    public LineupController(LineupService service) {
        this.service = service;
    }

    public record SubmitLineupRequest(@NotNull List<@NotBlank String> starters,
                                      List<@NotBlank String> bench) {}

    @PutMapping("/{matchday}")
    public Lineup submit(@PathVariable String leagueId, @PathVariable String teamId,
                         @PathVariable int matchday, @Valid @RequestBody SubmitLineupRequest req) {
        var bench = req.bench() != null ? req.bench() : List.<String>of();
        return service.submitLineup(leagueId, teamId, matchday, req.starters(), bench);
    }

    @GetMapping("/{matchday}")
    public Lineup get(@PathVariable String leagueId, @PathVariable String teamId,
                      @PathVariable int matchday) {
        return service.getLineup(leagueId, teamId, matchday);
    }
}
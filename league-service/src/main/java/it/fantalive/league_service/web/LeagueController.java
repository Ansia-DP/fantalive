package it.fantalive.league_service.web;

import it.fantalive.league_service.model.FantaTeam;
import it.fantalive.league_service.model.League;
import it.fantalive.league_service.model.Role;
import it.fantalive.league_service.model.RosterPlayer;
import it.fantalive.league_service.service.LeagueService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/leagues")
public class LeagueController {

    private final LeagueService service;

    public LeagueController(LeagueService service) {
        this.service = service;
    }

    public record CreateLeagueRequest(@NotBlank String name) {}

    public record RosterPlayerRequest(@NotBlank String playerId,
                                      @NotBlank String name,
                                      @NotNull Role role) {}

    public record AddTeamRequest(@NotBlank String name,
                                 @NotBlank String owner,
                                 @NotEmpty List<@Valid RosterPlayerRequest> roster) {}

    @PostMapping
    public ResponseEntity<League> create(@Valid @RequestBody CreateLeagueRequest req) {
        League league = service.createLeague(req.name());
        return ResponseEntity.created(URI.create("/leagues/" + league.getId())).body(league);
    }

    @GetMapping("/{leagueId}")
    public League get(@PathVariable String leagueId) {
        return service.getLeague(leagueId);
    }

    @PostMapping("/{leagueId}/teams")
    public ResponseEntity<FantaTeam> addTeam(@PathVariable String leagueId,
                                             @Valid @RequestBody AddTeamRequest req) {
        var roster = req.roster().stream()
                .map(p -> new RosterPlayer(p.playerId(), p.name(), p.role()))
                .toList();
        FantaTeam team = service.addTeam(leagueId, req.name(), req.owner(), roster);
        return ResponseEntity
                .created(URI.create("/leagues/" + leagueId + "/teams/" + team.teamId()))
                .body(team);
    }
}
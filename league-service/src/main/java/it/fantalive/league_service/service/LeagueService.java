package it.fantalive.league_service.service;

import it.fantalive.league_service.exception.LeagueNotFoundException;
import it.fantalive.league_service.model.FantaTeam;
import it.fantalive.league_service.model.League;
import it.fantalive.league_service.model.RosterPlayer;
import it.fantalive.league_service.repository.LeagueRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LeagueService {

    private final LeagueRepository repository;

    public LeagueService(LeagueRepository repository) {
        this.repository = repository;
    }

    public League createLeague(String name) {
        return repository.save(new League(name));
    }

    public League getLeague(String leagueId) {
        return repository.findById(leagueId)
                .orElseThrow(() -> new LeagueNotFoundException(leagueId));
    }

    public FantaTeam addTeam(String leagueId, String name, String owner, List<RosterPlayer> roster) {
        League league = getLeague(leagueId);
        FantaTeam team = league.addTeam(name, owner, roster);
        repository.save(league);   // fallisce se qualcuno ha modificato la lega nel frattempo (@Version)
        return team;
    }
}
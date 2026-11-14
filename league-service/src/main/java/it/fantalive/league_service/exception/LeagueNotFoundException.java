package it.fantalive.league_service.exception;

public class LeagueNotFoundException extends RuntimeException {
    public LeagueNotFoundException(String leagueId) {
        super("Lega non trovata: " + leagueId);
    }
}
package it.fantalive.league_service.model;

import java.util.List;

public record FantaTeam(String teamId, String name, String owner, List<RosterPlayer> roster) {}
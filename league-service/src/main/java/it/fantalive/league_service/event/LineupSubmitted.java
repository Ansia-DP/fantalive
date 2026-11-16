package it.fantalive.league_service.event;

import java.time.Instant;
import java.util.List;

public record LineupSubmitted(String eventId, String leagueId, String teamId, String teamName,
                              int matchday, List<String> starters, Instant submittedAt) {}
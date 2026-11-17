package it.fantalive.standings_service.web;

public record MatchdayResult(String teamId, String teamName, double points, int playersWithVote) {}
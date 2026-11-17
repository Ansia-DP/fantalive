package it.fantalive.standings_service.web;

public record TeamStanding(String teamId, String teamName, double totalPoints, int matchdaysPlayed) {}
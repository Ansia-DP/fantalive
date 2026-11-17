package it.fantalive.standings_service.model;

import it.fantalive.standings_service.event.LineupSubmitted;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document("lineups")
public record LineupView(
        @Id String id,          // leagueId:teamId:matchday
        String leagueId,
        String teamId,
        String teamName,
        int matchday,
        List<String> starters,
        Instant submittedAt
) {
    public static LineupView from(LineupSubmitted e) {
        return new LineupView(e.leagueId() + ":" + e.teamId() + ":" + e.matchday(),
                e.leagueId(), e.teamId(), e.teamName(), e.matchday(), e.starters(), e.submittedAt());
    }
}
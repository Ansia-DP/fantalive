package it.fantalive.standings_service.repository;

import it.fantalive.standings_service.model.LineupView;
import it.fantalive.standings_service.web.MatchdayResult;
import it.fantalive.standings_service.web.TeamStanding;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface LineupViewRepository extends MongoRepository<LineupView, String> {

    /** Classifica generale: somma dei fantavoti dei titolari, giornata per giornata. */
    @Aggregation(pipeline = {
            "{ $match: { leagueId: ?0 } }",
            "{ $lookup: { from: 'player_scores', let: { starters: '$starters', md: '$matchday' }, "
                    + "pipeline: [ { $match: { $expr: { $and: [ "
                    + "{ $eq: ['$matchday', '$$md'] }, { $in: ['$playerId', '$$starters'] } ] } } } ], "
                    + "as: 'scores' } }",
            "{ $project: { teamId: 1, teamName: 1, points: { $sum: '$scores.fantavoto' } } }",
            "{ $group: { _id: '$teamId', teamName: { $last: '$teamName' }, "
                    + "totalPoints: { $sum: '$points' }, matchdaysPlayed: { $sum: 1 } } }",
            "{ $project: { _id: 0, teamId: '$_id', teamName: 1, totalPoints: 1, matchdaysPlayed: 1 } }",
            "{ $sort: { totalPoints: -1 } }"
    })
    List<TeamStanding> standings(String leagueId);

    /** Risultati di una singola giornata. */
    @Aggregation(pipeline = {
            "{ $match: { leagueId: ?0, matchday: ?1 } }",
            "{ $lookup: { from: 'player_scores', let: { starters: '$starters', md: '$matchday' }, "
                    + "pipeline: [ { $match: { $expr: { $and: [ "
                    + "{ $eq: ['$matchday', '$$md'] }, { $in: ['$playerId', '$$starters'] } ] } } } ], "
                    + "as: 'scores' } }",
            "{ $project: { _id: 0, teamId: 1, teamName: 1, "
                    + "points: { $sum: '$scores.fantavoto' }, "
                    + "playersWithVote: { $size: { $filter: { input: '$scores', "
                    + "cond: { $ne: ['$$this.fantavoto', null] } } } } } }",
            "{ $sort: { points: -1 } }"
    })
    List<MatchdayResult> matchday(String leagueId, int matchday);
}
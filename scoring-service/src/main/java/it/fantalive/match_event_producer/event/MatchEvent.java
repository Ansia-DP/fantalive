package it.fantalive.match_event_producer.event;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = GoalScored.class, name = "GOAL_SCORED"),
        @JsonSubTypes.Type(value = AssistMade.class, name = "ASSIST_MADE"),
        @JsonSubTypes.Type(value = CardGiven.class, name = "CARD_GIVEN"),
        @JsonSubTypes.Type(value = PlayerRated.class, name = "PLAYER_RATED")
})
public sealed interface MatchEvent permits AssistMade, CardGiven, GoalScored, PlayerRated {
    String eventId();
    String matchId();
    String playerId();
    String playerName();
}
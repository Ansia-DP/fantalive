package it.fantalive.league_service.service;

import it.fantalive.league_service.config.KafkaTopicConfig;
import it.fantalive.league_service.event.LineupSubmitted;
import it.fantalive.league_service.exception.EventPublishingException;
import it.fantalive.league_service.exception.NotFoundException;
import it.fantalive.league_service.model.FantaTeam;
import it.fantalive.league_service.model.League;
import it.fantalive.league_service.model.Lineup;
import it.fantalive.league_service.repository.LineupRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class LineupService {

    private final LeagueService leagueService;
    private final LineupRepository repository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public LineupService(LeagueService leagueService, LineupRepository repository,
                         KafkaTemplate<String, Object> kafkaTemplate) {
        this.leagueService = leagueService;
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public Lineup submitLineup(String leagueId, String teamId, int matchday,
                               List<String> starters, List<String> bench) {
        League league = leagueService.getLeague(leagueId);
        FantaTeam team = league.findTeam(teamId)
                .orElseThrow(() -> new NotFoundException("Squadra non trovata: " + teamId));

        Lineup lineup = Lineup.create(leagueId, team, matchday, starters, bench);

        // 1. Scrittura su MongoDB
        repository.save(lineup);

        // 2. Scrittura su Kafka
        //    ATTENZIONE: due scritture su due sistemi diversi, senza transazione comune.
        //    Se la seconda fallisce, la prima resta. È il problema del "dual write".
        var event = new LineupSubmitted(UUID.randomUUID().toString(), leagueId, teamId,
                team.name(), matchday, lineup.getStarters(), lineup.getSubmittedAt());
        try {
            kafkaTemplate.send(KafkaTopicConfig.LINEUP_EVENTS, leagueId, event)
                    .get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new EventPublishingException("Formazione salvata ma evento non pubblicato", e);
        }

        return lineup;
    }

    public Lineup getLineup(String leagueId, String teamId, int matchday) {
        return repository.findById(leagueId + ":" + teamId + ":" + matchday)
                .orElseThrow(() -> new NotFoundException(
                        "Nessuna formazione per la giornata " + matchday));
    }
}
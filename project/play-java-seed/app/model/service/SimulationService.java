package model.service;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.Table;
import model.DTO.fighting.FightingResultDTO;
import model.entities.Team;
import model.service.fightingService.FightingService;
import org.apache.pekko.actor.ActorSystem;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

@Singleton
public class SimulationService {

    private final Executor combatExecutor;

    @Inject
    public SimulationService(ActorSystem actorSystem) {
        this.combatExecutor = actorSystem.dispatchers().lookup("combat-simulation-dispatcher");
    }

    public CompletionStage<FightingResultDTO> simulateAsync(Team teamA, Team teamB, long fightingSeed) {
        return CompletableFuture.supplyAsync(() -> {
            FightingService engine = new FightingService(fightingSeed, teamA, teamB);
            return engine.simulate();
        }, combatExecutor);
    }
}

package model.service;

import model.actor.ConnexionActor;
import model.actor.GameActor;
import model.monitor.ConnexionMonitor;
import model.repositories.GameCreationRepository;
import model.repositories.GameRepository;
import org.apache.pekko.actor.ActorSystem;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.javadsl.Adapter;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MatchmakingService {

    private final Queue<ActorRef<ConnexionActor.Message>> waitingPlayers = new LinkedList<>();
    private final ActorSystem actorSystem;
    private final SeedMakerService seedGenerator;
    private final String version;
    private final GameCreationRepository creationRepo;
    private final ConnexionMonitor connexions;
    private final GameRepository gameRepo;
    private final GameLevelService gameLevelService;


    @Inject
    public MatchmakingService(ActorSystem actorSystem,
                              SeedMakerService seedGenerator,
                              com.typesafe.config.Config config,
                              GameCreationRepository creationRepo,
                              ConnexionMonitor connexions,
                              GameRepository gameRepo,
                              GameLevelService gameLevelService){
        this.actorSystem = actorSystem;
        this.seedGenerator = seedGenerator;
        this.version = config.getString("version");
        this.creationRepo = creationRepo;
        this.connexions = connexions;
        this.gameRepo = gameRepo;
        this.gameLevelService = gameLevelService;
    }

    public synchronized CompletionStage<ActorRef<GameActor.Message>> addPlayer(String userId) {
        waitingPlayers.add(connexions.getActorFromId(userId));
        return tryCreateGame();
    }

    public synchronized CompletionStage<ActorRef<GameActor.Message>> tryCreateGame() {
        if (waitingPlayers.size() >= 8) {

            List<ActorRef<ConnexionActor.Message>> players = new ArrayList<>();
            List<Long> playerId = new ArrayList<>();

            for (int i = 0; i < 8; i++) {
                ActorRef<ConnexionActor.Message> player = waitingPlayers.poll();
                playerId.add(Long.parseLong(connexions.getIdFromActor(player)));
                players.add(player);
            }


            if (players.isEmpty()){
                return null;
            }

            return creationRepo.createGame(version, seedGenerator.createGameSeed(), playerId).thenApply(game -> {
                return Adapter.spawn(actorSystem, GameActor.create(players, game, gameRepo, seedGenerator, gameLevelService), "game-" + game.getId());
            });
        }
        return null;
    }

}

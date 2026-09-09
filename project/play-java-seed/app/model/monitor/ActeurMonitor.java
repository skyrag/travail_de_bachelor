package model.monitor;

import model.actor.ConnexionActor;
import model.actor.GameActor;
import model.entities.game.Game;
import model.repositories.GameCreationRepository;
import model.repositories.GameRepository;
import model.service.GameLevelService;
import model.service.MatchmakingService;
import model.service.SeedMakerService;
import model.service.SimulationService;
import org.apache.pekko.actor.ActorSystem;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.javadsl.Adapter;
import org.apache.pekko.japi.Pair;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class ActeurMonitor {

    private final ActorSystem actorSystem;
    private final GameRepository gameRepo;
    private final GameCreationRepository creationRepo;
    private final MatchmakingService matchmakingService;
    private final SeedMakerService seedGenerator;
    private final GameLevelService gameLevelService;
    private final SimulationService simulationService;

    private List<String> idList = new ArrayList<>();
    private List<ActorRef<ConnexionActor.Message>> connexionList = new ArrayList<>();

    private List<ActorRef<GameActor.Message>> gameList = new ArrayList<>();

    @Inject
    public ActeurMonitor(ActorSystem actorSystem,
                         GameRepository gameRepository,
                         SeedMakerService seedGenerator,
                         GameCreationRepository creationRepo,
                         GameLevelService gameLevelService,
                         SimulationService simulationService,
                         MatchmakingService matchmakingService) {
        this.actorSystem = actorSystem;
        this.gameRepo = gameRepository;
        this.seedGenerator = seedGenerator;
        this.creationRepo = creationRepo;
        this.gameLevelService = gameLevelService;
        this.simulationService = simulationService;
        this.matchmakingService = matchmakingService;

        this.matchmakingService.setMonitor(this);

    }

    public synchronized ActorRef<ConnexionActor.Message> getOrCreateActorFromId (String userId, org.apache.pekko.actor.ActorRef ws) {
        int index = idList.indexOf(userId);
        if (index == -1){
            ActorRef<ConnexionActor.Message> actor = Adapter.spawn(
                    actorSystem,
                    ConnexionActor.create(ws, Long.parseLong(userId), this, matchmakingService),
                    userId
            );
            idList.add(userId);
            connexionList.add(actor);
            return actor;
        }
        return connexionList.get(index);
    }

    public synchronized ActorRef<ConnexionActor.Message> getActorFromId(String userId){
        return connexionList.get(idList.indexOf(userId));
    }



    public synchronized void removeByActor (ActorRef<ConnexionActor.Message> actor) {
        int index = connexionList.indexOf(actor);
        idList.remove(index);
        connexionList.remove(index);
    }

    public synchronized ActorRef<GameActor.Message> createGame(List<Pair<ActorRef<ConnexionActor.Message>,Long>> players, String version, List<Long> playerId){
        Game game =  creationRepo.createGame(version, seedGenerator.createGameSeed(), playerId).toCompletableFuture().join();
        ActorRef<GameActor.Message> gameActor =  Adapter.spawn(actorSystem, GameActor.create(players, game, gameRepo, seedGenerator, gameLevelService,simulationService), "game-" + game.getId());
        gameList.add(gameActor);
        return gameActor;
    }

}

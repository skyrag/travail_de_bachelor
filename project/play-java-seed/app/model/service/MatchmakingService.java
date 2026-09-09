package model.service;

import model.actor.ConnexionActor;
import model.actor.GameActor;
import model.monitor.ActeurMonitor;
import model.repositories.GameCreationRepository;
import model.repositories.GameRepository;
import org.apache.pekko.actor.ActorSystem;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.javadsl.Adapter;
import org.apache.pekko.japi.Pair;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletionStage;

@Singleton
public class MatchmakingService {

    private static final int PLAYERS_PER_GAME = 8; // ou injecté depuis application.conf

    private final Queue<Pair<ActorRef<ConnexionActor.Message>,Long>> waitingPlayers = new LinkedList<>();
    private final String version;
    private ActeurMonitor monitor;


    @Inject
    public MatchmakingService(com.typesafe.config.Config config){
        this.version = config.getString("version");
    }

    public void setMonitor(ActeurMonitor monitor){
        if (this.monitor == null){
            this.monitor = monitor;
        }
    }

    public synchronized ActorRef<GameActor.Message> addPlayer(String userId) {
        waitingPlayers.add(new Pair<>(monitor.getActorFromId(userId), Long.valueOf(userId)));
        if (waitingPlayers.size() >= PLAYERS_PER_GAME) {

            List<Pair<ActorRef<ConnexionActor.Message>,Long>> players = new ArrayList<>();
            List<Long> playerId = new ArrayList<>();

            for (int i = 0; i < PLAYERS_PER_GAME; i++) {
                Pair<ActorRef<ConnexionActor.Message>, Long> player = waitingPlayers.poll();
                playerId.add(player.second());
                players.add(player);
            }


            if (players.isEmpty()){
                return null;
            }

            return monitor.createGame(players, version, playerId);
        }
        return null;
    }

}

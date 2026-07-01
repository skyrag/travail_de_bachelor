package model.service;

import model.actor.ConnexionActor;
import model.actor.GameActor;
import org.apache.pekko.actor.ActorSystem;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.javadsl.Adapter;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MatchmakingService {

    private final Queue<ActorRef<ConnexionActor.Message>> waitingPlayers = new ConcurrentLinkedQueue<>();
    private final ActorSystem actorSystem;

    @Inject
    public MatchmakingService(ActorSystem actorSystem){
        this.actorSystem = actorSystem;
    }

    public synchronized ActorRef<GameActor.Message> addPlayer(ActorRef<ConnexionActor.Message> player) {
        waitingPlayers.add(player);
        return tryCreateGame();
    }

    public synchronized ActorRef<GameActor.Message> tryCreateGame() {
        if (waitingPlayers.size() >= 8) {
            List<ActorRef<ConnexionActor.Message>> users = new ArrayList<>();
            for (int i = 0 ; i < 8 ; i++){
                users.add(waitingPlayers.poll());
            }
            // il faudrait renvoyer un future pour que l'on ait le temps de faire un accès a la Db pour enregistrer la partie et ensuite chopper son id et enfin retourner le nouvelle acteur
            // TODO revenir lorsque l'on aura implémenter la Db complétement et les partie entities
            return Adapter.spawn(
                    actorSystem,
                    GameActor.create(users),
                    "aaaa"
            );
        }
        return null;
    }

}

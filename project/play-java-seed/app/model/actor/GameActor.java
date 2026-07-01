package model.actor;

import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.apache.pekko.actor.typed.javadsl.Receive;

import java.util.ArrayList;
import java.util.List;

public class GameActor extends AbstractBehavior<GameActor.Message> {

    public interface Message {}

    public static Behavior<GameActor.Message> create(List<ActorRef<ConnexionActor.Message>> users) {
        return Behaviors.setup(ctx -> new GameActor(ctx, users));
    }

    private final List<ActorRef<ConnexionActor.Message>> users;
    // TODO mettre le repo pour la game

    private GameActor(ActorContext<Message> context, List<ActorRef<ConnexionActor.Message>> users) {
        super(context);
        this.users = users;
    }

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .build();
    }



}

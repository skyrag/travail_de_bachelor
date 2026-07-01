package model.actor;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.pekko.actor.AbstractActor;
import org.apache.pekko.actor.Props;
import org.apache.pekko.actor.typed.ActorRef;

public class BridgeActor extends AbstractActor {
    private final ActorRef<ConnexionActor.Message> userActor;
    private final org.apache.pekko.actor.ActorRef out;

    public static Props create(org.apache.pekko.actor.ActorRef out, ActorRef<ConnexionActor.Message> actor) {
        return Props.create(BridgeActor.class, () -> new BridgeActor(out, actor));
    }

    public BridgeActor(org.apache.pekko.actor.ActorRef out, ActorRef<ConnexionActor.Message> userActor) {
        this.userActor = userActor;
        this.out = out;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(JsonNode.class, msg ->
                        userActor.tell(new ConnexionActor.IncomingMessage(msg))
                )

                .build();
    }

    @Override
    public void postStop() {
        userActor.tell(ConnexionActor.ConnectionClosed.INSTANCE);
    }
}

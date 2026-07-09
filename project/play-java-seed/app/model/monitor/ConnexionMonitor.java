package model.monitor;

import model.actor.ConnexionActor;
import org.apache.pekko.actor.typed.ActorRef;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class ConnexionMonitor {

    private List<String> idList = new ArrayList<>();
    private List<ActorRef<ConnexionActor.Message>> connexionList = new ArrayList<>();

    public ConnexionMonitor() {

    }

    public synchronized ActorRef<ConnexionActor.Message> getActorFromId (String userId) {
        int index = idList.indexOf(userId);
        return index == -1 ? null : connexionList.get(index);
    }

    public synchronized String getIdFromActor (ActorRef<ConnexionActor.Message> actor) {
        int index = connexionList.indexOf(actor);
        return index == -1 ? null : idList.get(index);
    }

    public synchronized void addConnexion (String userId, ActorRef<ConnexionActor.Message> actor){
        idList.add(userId);
        connexionList.add(actor);
    }

    public synchronized void removeById (String userid) {
        removeByIndex(idList.indexOf(userid));
    }

    public synchronized void removeByActor (ActorRef<ConnexionActor.Message> actor) {
        removeByIndex(connexionList.indexOf(actor));
    }

    private synchronized void removeByIndex(int index) {
        idList.remove(index);
        connexionList.remove(index);
    }

}

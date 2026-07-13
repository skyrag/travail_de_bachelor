package model.actor;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Id;
import model.entities.Fight;
import model.entities.Round;
import model.entities.Team;
import model.entities.game.Game;
import model.entities.game.Pool;
import model.entities.game.PoolEntry;
import model.entities.unit.InstanceUnit;
import model.entities.unit.Unit;
import model.fightingDTO.UnitDTO;
import model.repositories.GameRepository;
import model.service.GameLevelService;
import model.service.SeedMakerService;
import model.utils.Tuple;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.apache.pekko.actor.typed.javadsl.Receive;
import org.apache.pekko.japi.Pair;
import play.libs.Json;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static model.actor.JsonConstantes.*;
import static model.service.fightingService.UnitDTOMapper.unitToDTO;
import static model.utils.Constante.*;

public class GameActor extends AbstractBehavior<GameActor.Message> {

    public interface Message {}

    public abstract static class ValidationMessage implements Message {
        protected long userId;
        protected long messageId;
        protected ActorRef<ConnexionActor.Message> respondTo;
        public ValidationMessage(long userId, long messageId, ActorRef<ConnexionActor.Message> respondTo) {
            this.userId = userId;
            this.messageId = messageId;
            this.respondTo = respondTo;
        }
    }

    public static final class BuyingUnitMessage extends ValidationMessage {
        private long unitId;
        public BuyingUnitMessage(long userId, long unitId, long messageId, ActorRef<ConnexionActor.Message> respondTo) {
            super(userId, messageId, respondTo);
            this.unitId = unitId;
        }
    }

    public static final class SellingUnitMessage extends ValidationMessage {
        private long unitId;
        public SellingUnitMessage(long userId, long unitId, long messageId, ActorRef<ConnexionActor.Message> respondTo){
            super(userId, messageId, respondTo);
            this.unitId = unitId;
        }
    }

    public static final class MovingUnitMessage extends ValidationMessage {
        private long unitId;
        private Tuple newPosition;
        public MovingUnitMessage(long userId, long messageId, long unitId, Tuple newPosition, ActorRef<ConnexionActor.Message> respondTo) {
            super(userId, messageId, respondTo);
            this.unitId = unitId;
            this.newPosition = newPosition;
        }
    }

    public static final class GivingUnitObjectMessage extends ValidationMessage {
        private long unitId;
        private long itemId;
        public GivingUnitObjectMessage(long userId, long messageId,long unitId, long itemId, ActorRef<ConnexionActor.Message> respondTo) {
            super(userId, messageId, respondTo);
            this.unitId = unitId;
            this.itemId = itemId;
        }
    }

    public static final class RerollShopMessage extends ValidationMessage {
        public RerollShopMessage(long userId, long messageId, ActorRef<ConnexionActor.Message> respondTo) {
            super(userId, messageId, respondTo);
        }
    }

    public static final class BuyingExpMessage extends ValidationMessage {
        public BuyingExpMessage(long userId, long messageId, ActorRef<ConnexionActor.Message> respondTo) {
            super(userId, messageId, respondTo);
        }
    }

    public static final class EndOfRoundMessage implements Message {
        public EndOfRoundMessage() {}
    }

    public static final class EndOfFightMessage implements Message {
        private long winnerId;
        private long loserId;
        private int pvLost;
        public EndOfFightMessage(long winnerId, long loserId, int pvLost) {
            this.winnerId = winnerId;
            this.loserId = loserId;
            this.pvLost = pvLost;
        }
    }

    public static final class SetupMessage implements Message {
        public SetupMessage() {}
    }


    private final List<Pair<ActorRef<ConnexionActor.Message>, Long>> users;
    private final Game game;
    private final List<Team> teams;
    private final List<Pool> pools;
    private final GameRepository repo;
    private final SeedMakerService seedGenerator;
    private final GameLevelService gameLevelService;
    private int nbFight;
    private int maxFights;
    private boolean gameOver;
    private long UUID = 500000L;

    public static Behavior<GameActor.Message> create(List<Pair<ActorRef<ConnexionActor.Message>, Long>> users, Game game, GameRepository repo, SeedMakerService seedGenerator, GameLevelService gameLevelService) {
        return Behaviors.setup(ctx -> new GameActor(ctx, users, game, repo, seedGenerator, gameLevelService));
    }

    private GameActor(ActorContext<Message> context, List<Pair<ActorRef<ConnexionActor.Message>, Long>> users, Game game, GameRepository repo, SeedMakerService seedGenerator, GameLevelService gameLevelService) {
        super(context);
        this.users = users;
        this.game = game;
        this.repo = repo;
        this.teams = game.getTeams();
        this.pools = game.getPools();
        this.seedGenerator = seedGenerator;
        this.gameLevelService = gameLevelService;
        this.nbFight = 0;
        this.gameOver = false;
    }

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .build();
        //TODO implémenter tout les messages
    }

    private Behavior<Message> onSetupMessage(SetupMessage msg){
        long baseSeed = game.getSeed();
        for (Team team : teams){
            team.setSeed(seedGenerator.createFromSeed(baseSeed, "player:" + team.getUser().getId()));
            Unit unit = randomUnitFromPool(pools.getFirst(), team.getSeed());
            InstanceUnit instanceUnit = new InstanceUnit(1, new Tuple(0,0), unit, team);
            team.addUnit(instanceUnit);
        }

        //TODO envoyer l'état de la game a l'utilisateur. et lui envoyer toute les unité du pool pour qu'on ait juste besoin de parler en unitId ?
        repo.merge(game);
        return Behaviors.same();
    }

    private Behavior<Message> onBuyingUnitMessage(BuyingUnitMessage msg){
        Team team = game.getTeam(msg.userId);

        Unit unit = game.getUnitById(msg.unitId);

        if (team == null || unit == null || !team.canBuyUnit(unit) || game.canRemoveUnitToPool(msg.unitId)){
            msg.respondTo.tell(new ConnexionActor.FeedbackInput(Json.newObject().put(ID, msg.messageId).put(TYPE, ERROR).put(LOG, "cannot buy this unit")));
            return Behaviors.same();
        }

        JsonNode respond = Json.newObject().put(ID, msg.messageId).put(TYPE, OK);
        JsonNode tell = Json.newObject().put(ID, msg.messageId).put(TYPE, BUY).set(PAYLOAD, Json.newObject().put(UNIT, msg.unitId));

        msg.respondTo.tell(new ConnexionActor.FeedbackInput(respond));

        tellOtherUsers(tell, msg.userId);

        repo.merge(game).exceptionally(err -> {
            getContext().getLog().error("Failed to merge game: {} from message {}", err.getMessage(), msg);
            return null;
        });
        return Behaviors.same();
    }

    private Behavior<Message> onSellUnitMessage(SellingUnitMessage msg) {
        Team team = game.getTeam(msg.userId);
        if (team == null || !team.canSellUnit(msg.unitId) || game.canAddUnitToPool(msg.unitId)){
            msg.respondTo.tell(new ConnexionActor.FeedbackInput(Json.newObject().put(ID, msg.messageId).put(TYPE , ERROR).put(LOG, "cannot sell this unit")));
            return Behaviors.same();
        }

        msg.respondTo.tell(new ConnexionActor.FeedbackInput(Json.newObject().put(ID, msg.messageId).put(TYPE, OK)));
        JsonNode payload = Json.newObject().put(ID, msg.messageId).put(TYPE , SELL).put(PAYLOAD, msg.unitId);
        tellOtherUsers(payload, msg.userId);

        repo.merge(game).exceptionally(err -> {
            getContext().getLog().error("Failed to merge game: {} from message {}", err.getMessage(), msg);
            return null;
        });
        return Behaviors.same();
    }

    private Behavior<Message> onMovingUnitMessage(MovingUnitMessage msg){
        Team team = game.getTeam(msg.userId);
        if (team == null || !team.canMoveUnit(msg.newPosition, msg.unitId)) {
            msg.respondTo.tell(new ConnexionActor.FeedbackInput(Json.newObject().put(ID, msg.messageId).put(TYPE, ERROR).put(LOG, "cannot move this unit")));
            return Behaviors.same();
        }

        msg.respondTo.tell(new ConnexionActor.FeedbackInput(Json.newObject().put(ID, msg.messageId).put(TYPE, OK)));
        JsonNode payload = Json.newObject().put(ID, msg.messageId).put(TYPE , MOVE)
                .set(PAYLOAD, Json.newObject()
                        .set(POSITION, Json.newObject()
                                .put("x" , msg.newPosition.x())
                                .put("y", msg.newPosition.y())));
        tellOtherUsers(payload, msg.userId);

        repo.merge(team).exceptionally(err -> {
            getContext().getLog().error("Failed to merge team: {} from message {}", err.getMessage(), msg);
            return null;
        });
        return Behaviors.same();
    }

    private Behavior<Message> onGivingUnitObjectMessage(GivingUnitObjectMessage msg) {
        Team team = game.getTeam(msg.userId);
        if (team == null || !team.canAddItemToUnit(msg.itemId, msg.unitId)) {
            msg.respondTo.tell(new ConnexionActor.FeedbackInput(Json.newObject().put(ID, msg.messageId).put(TYPE, ERROR).put(LOG, "cannot give this item to this unit")));
            return Behaviors.same();
        }

        msg.respondTo.tell(new ConnexionActor.FeedbackInput(Json.newObject().put(ID, msg.messageId).put(TYPE, OK)));
        JsonNode payload = Json.newObject().put(ID, msg.messageId).put(TYPE , GIVE).set(PAYLOAD, Json.newObject().put(UNIT, msg.unitId).put(ITEM, msg.itemId));
        tellOtherUsers(payload, msg.userId);

        repo.merge(team).exceptionally(err -> {
            getContext().getLog().error("Failed to merge team: {} from message {}", err.getMessage(), msg);
            return null;
        });
        return Behaviors.same();
    }

    private Behavior<Message> onRerollShopMessage(RerollShopMessage msg){
        Team team = game.getTeam(msg.userId);
        if (team == null) {
            msg.respondTo.tell(new ConnexionActor.FeedbackInput(Json.newObject().put(ID, msg.messageId).put(TYPE, ERROR).put(LOG, "no team found")));
            return Behaviors.same();
        }

        List<Unit> shop = new ArrayList<>();
        for (int i = 0; i < MAXNBSHOPUNIT; i++){
            Unit unit = randomUnitFromPool(randomRarityFromPools(team),team.getSeed());
            shop.add(unit);
        }

        if (!team.canReroll(shop)){
            msg.respondTo.tell(new ConnexionActor.FeedbackInput(Json.newObject().put(ID, msg.messageId).put(TYPE, ERROR).put(LOG, "team can't reroll")));
            return Behaviors.same();
        }

        JsonNode payload = Json.newObject().put("unit1", shop.get(0).getId())
                .put("unit2", shop.get(1).getId())
                .put("unit3", shop.get(2).getId())
                .put("unit4", shop.get(3).getId())
                .put("unit5", shop.get(4).getId());

        msg.respondTo.tell(new ConnexionActor.FeedbackInput(Json.newObject().put(ID, msg.messageId).put(TYPE, OK).set(PAYLOAD, payload)));

        repo.merge(team).exceptionally(err -> {
            getContext().getLog().error("Failed to merge team: {} from message {}", err.getMessage(), msg);
            return null;
        });
        return Behaviors.same();
    }

    private Behavior<Message> onBuyingExpMessage(BuyingExpMessage msg) {
        Team team = game.getTeam(msg.userId);
        if (team == null || !team.canBuyExp(gameLevelService.getExpRequired(team.getLvl()))) {
            msg.respondTo.tell(new ConnexionActor.FeedbackInput(Json.newObject().put(ID, msg.messageId).put(TYPE, ERROR).put(LOG, "cannot buy exp")));
            return Behaviors.same();
        }

        msg.respondTo.tell(new ConnexionActor.FeedbackInput(Json.newObject().put(ID, msg.messageId).put(TYPE, OK)));

        repo.merge(team).exceptionally(err -> {
            getContext().getLog().error("Failed to merge team: {} from message {}", err.getMessage(), msg);
            return null;
        });
        return Behaviors.same();
    }

    private Behavior<Message> onEndOfRoundMessage(EndOfRoundMessage msg) {

        //TODO lancer le service de combat sur pluesieurs thread, et ils sont sensé renvoyer un message au gameActor et websocketActor
        List<Team> remainingTeam = game.stillAlive();
        maxFights = remainingTeam.size()/2;

        return Behaviors.same();
    }

    private Behavior<Message> onEndOfFightMessage(EndOfFightMessage msg) {
        nbFight++;
        Team winner = game.getTeam(msg.winnerId);
        Team loser = game.getTeam(msg.loserId);
        if (winner == null || loser == null) {
            return Behaviors.same();
        }

        loser.lostFight(msg.pvLost);
        winner.wonFight();
        game.adjustRankings();

        JsonNode node = Json.newObject().


        if (loser.getHealth() <= 0) {
            //TODO envoyer un message de game over

            List<Team> remainingTeam = game.stillAlive();
            if (remainingTeam.size() == 1){
                gameOver = true;
                //TODO envoyer le message de fin de partie avec le winner;
            }
        }

        Round loserRound = loser.getRounds().getLast();
        Round winnerRound = winner.getRounds().getLast();
        Fight fight = new Fight(winnerRound, loserRound);

        if (nbFight == maxFights) {
            for (Team team: teams){
                team.endRound();
            }
        }

        repo.add(fight);
        repo.merge(game).exceptionally(err -> {
            getContext().getLog().error("Failed to merge game: {} from message {}", err.getMessage(), msg);
            return null;
        });        return Behaviors.same();
    }

    private Unit randomUnitFromPool(Pool pool, Random rand){
        int total = pool.getEntries().stream().mapToInt(PoolEntry::getNumber).sum();
        int r = rand.nextInt(total);
        int cumulative = 0;
        for (PoolEntry entry : pool.getEntries()) {
            cumulative += entry.getNumber();
            if (r < cumulative) {
                return entry.getUnit();
            }
        }
        throw new IllegalStateException("problème de cohérence dans le pool :" + pool.getPoolsRarity());
    }

    private Pool randomRarityFromPools(Team team) {
        int r = team.getSeed().nextInt(100);
        int cumulative = 0;
        for (Pool pool: pools){
            cumulative += gameLevelService.getProba(team.getLvl(), pool.getPoolsRarity());
            if (r < cumulative) {
                return pool;
            }
        }
        throw new IllegalStateException("problème de cohérence avec les proba des rareté de pool");
    }

    private void tellOtherUsers(JsonNode payload, long userId){
        for (Pair<ActorRef<ConnexionActor.Message>,Long> actor : users){
            if (actor.second() != userId){
                actor.first().tell(new ConnexionActor.ChangesFromOtherUser(payload));
            }
        }
    }

    private long getUUID (){
        return UUID++;
    }
}

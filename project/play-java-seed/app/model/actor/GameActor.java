package model.actor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import model.DTO.TeamDTO;
import model.DTO.fighting.FightingResultDTO;
import model.entities.Fight;
import model.entities.Round;
import model.entities.Team;
import model.entities.game.Game;
import model.entities.game.Pool;
import model.entities.game.PoolEntry;
import model.entities.unit.InstanceUnit;
import model.entities.unit.Item;
import model.entities.unit.Unit;
import model.DTO.UnitDTO;
import model.repositories.GameRepository;
import model.service.GameLevelService;
import model.service.SeedMakerService;
import model.DTO.ItemDTOMapper;
import model.DTO.UnitDTOMapper;
import model.service.SimulationService;
import model.service.fightingService.FightingService;
import model.utils.Tuple;
import org.apache.pekko.actor.Cancellable;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.DispatcherSelector;
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.apache.pekko.actor.typed.javadsl.Receive;
import org.apache.pekko.japi.Pair;
import org.apache.pekko.pattern.Patterns;
import play.libs.Json;

import org.apache.pekko.actor.typed.javadsl.TimerScheduler;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import java.time.Duration;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CompletionStage;

import static model.actor.JsonConstantes.*;
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

    public static final class StartOfRoundMessage implements Message{
        public static final StartOfRoundMessage INSTANCE = new StartOfRoundMessage();
        public StartOfRoundMessage() {}
    }

    public static final class EndOfRoundMessage implements Message {
        public EndOfRoundMessage() {}
    }

    public static final class EndOfFightMessage implements Message {
        private FightingResultDTO results;
        public EndOfFightMessage(FightingResultDTO results) {
            this.results = results;
        }
    }

    public static final class SetupMessage implements Message {
        public static final SetupMessage INSTANCE = new SetupMessage();
        public SetupMessage() {}
    }

    public static final class ConnexionSetupMessage implements Message {
        private long userId;
        private ActorRef<ConnexionActor.Message> respondTo;
        public ConnexionSetupMessage(long userId, ActorRef<ConnexionActor.Message> respondTo) {
            this.userId = userId;
            this.respondTo = respondTo;
        }
    }

    public static final class EndOfGame implements Message {
        public static final EndOfGame INSTANCE = new EndOfGame();
        public EndOfGame() {}
    }

    public static final class FailedCombatMessage implements Message {
        public final String throwable;
        public FailedCombatMessage(Throwable throwable) {
            this.throwable = throwable.getMessage();
        }
    }

    private Cancellable deathTimer;

    private final TimerScheduler<Message> timers;


    private final List<Pair<ActorRef<ConnexionActor.Message>, Long>> users;
    private final Game game;
    private final List<Team> teams;
    private final List<Pool> pools;
    private final GameRepository repo;
    private final SeedMakerService seedGenerator;
    private final GameLevelService gameLevelService;
    private final SimulationService simulationService;
    private int nbFight;
    private int maxFights;
    private boolean gameOver;
    private long UUID = 500000L;
    private final Random rand;

    private final int ROUNDTIMEMS = 90000;
    private static final String ROUND_TIMER_KEY = "round-timer";


    public static Behavior<Message> create(List<Pair<ActorRef<ConnexionActor.Message>, Long>> users, Game game, GameRepository repo, SeedMakerService seedGenerator, GameLevelService gameLevelService, SimulationService simulationService) {
        return Behaviors.withTimers(timers -> Behaviors.setup(ctx -> new GameActor(ctx, timers, users, game, repo, seedGenerator, gameLevelService, simulationService)));
    }

    private GameActor(ActorContext<Message> context, TimerScheduler<Message> timers, List<Pair<ActorRef<ConnexionActor.Message>, Long>> users, Game game, GameRepository repo, SeedMakerService seedGenerator, GameLevelService gameLevelService, SimulationService simulationService) {
        super(context);
        this.timers = timers;
        this.users = users;
        this.game = game;
        this.repo = repo;
        this.teams = game.getTeams();
        this.pools = game.getPools();
        this.seedGenerator = seedGenerator;
        this.gameLevelService = gameLevelService;
        this.simulationService = simulationService;
        this.nbFight = 0;
        this.gameOver = false;
        this.rand = new Random(game.getSeed());
        getContext().getSelf().tell(new SetupMessage());

        for (Pair<ActorRef<ConnexionActor.Message>, Long> pair : users){
            pair.first().tell(new ConnexionActor.StartGame(getContext().getSelf()));
        }
    }

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(ConnexionSetupMessage.class, this::onConnexionSetupMessage)
                .onMessage(BuyingUnitMessage.class, this::onBuyingUnitMessage)
                .onMessage(SellingUnitMessage.class, this::onSellUnitMessage)
                .onMessage(MovingUnitMessage.class, this::onMovingUnitMessage)
                .onMessage(GivingUnitObjectMessage.class, this::onGivingUnitObjectMessage)
                .onMessage(RerollShopMessage.class, this::onRerollShopMessage)
                .onMessage(BuyingExpMessage.class, this::onBuyingExpMessage)
                .onMessage(EndOfRoundMessage.class, this::onEndOfRoundMessage)
                .onMessage(EndOfFightMessage.class, this::onEndOfFightMessage)
                .onMessage(EndOfGame.class, this::onEndOfGame)
                .onMessage(StartOfRoundMessage.class, this::onStartOfRoundMessage)
                .onMessage(SetupMessage.class, this::onSetupMessage)
                .build();
    }

    private Behavior<Message> onSetupMessage(SetupMessage msg){
        long baseSeed = game.getSeed();
        for (Team team : teams){
            team.setSeed(seedGenerator.createFromSeed(baseSeed, "player:" + team.getUser().getId()));
            Unit unit = game.randomUnitFromPool(pools.getFirst(), team.getSeed());
            InstanceUnit instanceUnit = new InstanceUnit(1, new Tuple(0,0), unit, team);
            team.addUnit(instanceUnit);
        }

        repo.merge(game);
        return Behaviors.same();
    }

    private Behavior<Message> onBuyingUnitMessage(BuyingUnitMessage msg){
        if (gameOver) return Behaviors.same();
        Team team = game.getTeam(msg.userId);

        Unit unit = game.getUnitById(msg.unitId);

        if (team == null || unit == null || !team.canBuyUnit(unit) || game.canRemoveUnitToPool(msg.unitId)){
            msg.respondTo.tell(new ConnexionActor.FeedbackInput(Json.newObject().put(ID, msg.messageId).put(TYPE, ERROR).put(LOG, "cannot buy this unit")));
            return Behaviors.same();
        }

        JsonNode respond = Json.newObject().put(ID, msg.messageId).put(TYPE, OK);
        JsonNode tell = Json.newObject().put(ID, msg.messageId).put(TYPE, BUY).set(PAYLOAD, Json.newObject().put(USER, msg.userId).put(UNIT, msg.unitId));

        msg.respondTo.tell(new ConnexionActor.FeedbackInput(respond));

        tellOtherUsers(tell, msg.userId);

        repo.merge(game).exceptionally(err -> {
            getContext().getLog().error("Failed to merge game: {} from message {}", err.getMessage(), msg);
            return null;
        });
        return Behaviors.same();
    }

    private Behavior<Message> onSellUnitMessage(SellingUnitMessage msg) {
        if (gameOver) return Behaviors.same();

        Team team = game.getTeam(msg.userId);
        if (team == null || !team.canSellUnit(msg.unitId) || game.canAddUnitToPool(msg.unitId)){
            msg.respondTo.tell(new ConnexionActor.FeedbackInput(Json.newObject().put(ID, msg.messageId).put(TYPE , ERROR).put(LOG, "cannot sell this unit")));
            return Behaviors.same();
        }

        msg.respondTo.tell(new ConnexionActor.FeedbackInput(Json.newObject().put(ID, msg.messageId).put(TYPE, OK)));
        JsonNode payload = Json.newObject().put(ID, msg.messageId).put(TYPE , SELL).set(PAYLOAD, Json.newObject().put(USER, msg.userId).put(UNIT, msg.unitId));
        tellOtherUsers(payload, msg.userId);

        repo.merge(game).exceptionally(err -> {
            getContext().getLog().error("Failed to merge game: {} from message {}", err.getMessage(), msg);
            return null;
        });
        return Behaviors.same();
    }

    private Behavior<Message> onMovingUnitMessage(MovingUnitMessage msg){
        if (gameOver) return Behaviors.same();

        Team team = game.getTeam(msg.userId);
        if (team == null || !team.canMoveUnit(msg.newPosition, msg.unitId)) {
            msg.respondTo.tell(new ConnexionActor.FeedbackInput(Json.newObject().put(ID, msg.messageId).put(TYPE, ERROR).put(LOG, "cannot move this unit")));
            return Behaviors.same();
        }

        msg.respondTo.tell(new ConnexionActor.FeedbackInput(Json.newObject().put(ID, msg.messageId).put(TYPE, OK)));
        JsonNode payload = Json.newObject().put(ID, msg.messageId).put(TYPE , MOVE)
                .set(PAYLOAD, Json.newObject()
                        .put(USER, msg.userId)
                        .put(UNIT, msg.unitId)
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
        if (gameOver) return Behaviors.same();

        Team team = game.getTeam(msg.userId);
        if (team == null || !team.canAddItemToUnit(msg.itemId, msg.unitId)) {
            msg.respondTo.tell(new ConnexionActor.FeedbackInput(Json.newObject().put(ID, msg.messageId).put(TYPE, ERROR).put(LOG, "cannot give this item to this unit")));
            return Behaviors.same();
        }

        msg.respondTo.tell(new ConnexionActor.FeedbackInput(Json.newObject().put(ID, msg.messageId).put(TYPE, OK)));
        JsonNode payload = Json.newObject().put(ID, msg.messageId).put(TYPE , GIVE).set(PAYLOAD, Json.newObject().put(USER, msg.userId).put(UNIT, msg.unitId).put(ITEM, msg.itemId));
        tellOtherUsers(payload, msg.userId);

        repo.merge(team).exceptionally(err -> {
            getContext().getLog().error("Failed to merge team: {} from message {}", err.getMessage(), msg);
            return null;
        });
        return Behaviors.same();
    }

    private Behavior<Message> onRerollShopMessage(RerollShopMessage msg){
        if (gameOver) return Behaviors.same();

        Team team = game.getTeam(msg.userId);
        if (team == null) {
            msg.respondTo.tell(new ConnexionActor.FeedbackInput(Json.newObject().put(ID, msg.messageId).put(TYPE, ERROR).put(LOG, "no team found")));
            return Behaviors.same();
        }

        List<Unit> shop = new ArrayList<>();
        for (int i = 0; i < MAXNBSHOPUNIT; i++){
            Unit unit = game.randomUnitFromPool(randomRarityFromPools(team),team.getSeed());
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
        if (gameOver) return Behaviors.same();

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

    private Behavior<Message> onStartOfRoundMessage(StartOfRoundMessage msg) {

        for (Team team: teams){
            team.newRound();
        }

        long endTime = System.currentTimeMillis() + ROUNDTIMEMS;

        JsonNode payload = Json.newObject().put(ID, getUUID()).put(TYPE, ROUNDWINDOW).put(PAYLOAD, endTime);
        for (Pair<ActorRef<ConnexionActor.Message>,Long> pair: users) {
            pair.first().tell(new ConnexionActor.StartOfRound(payload));
        }

        timers.startSingleTimer(ROUND_TIMER_KEY, new EndOfRoundMessage(), Duration.ofMillis(ROUNDTIMEMS + INPUTBUFFER));

        return Behaviors.same();
    }

    private Behavior<Message> onEndOfRoundMessage(EndOfRoundMessage msg) {
        if (gameOver) return Behaviors.same();

        List<Team> remainingTeam = game.stillAlive();
        maxFights = remainingTeam.size()/2;



        if(remainingTeam.size() % 2 == 1) {
            maxFights++;
            remainingTeam.add(game.getLastDied());
        }

        for (int i = 0; i < maxFights; i++){
            Team teamA = remainingTeam.get(rand.nextInt(remainingTeam.size()));
            remainingTeam.remove(teamA);
            Team teamB = remainingTeam.getFirst();
            remainingTeam.remove(teamB);
            long seed = seedGenerator.createFromSeed(game.getSeed(), "combat:TeamAId="+ teamA.getId() + ":TeamBId=" + teamB.getId() + ":round=" + teamA.getRounds().getLast().getRoundNumber());

            CompletionStage<FightingResultDTO> future =
                    simulationService.simulateAsync(teamA, teamB, seed);

            future.thenAccept(result -> {
                // envoi direct au ConnectionActor, sans passer par le GameActor
                Team team1 = null;
                Team team2 = null;
                for (Team team: teams){
                    if (team.getId() == result.idTeamA()) team1 = team;
                    if (team.getId() == result.idTeamB()) team2 = team;
                }
                if (team2 == null || team1 == null) {
                    return;
                }
                for (Pair<ActorRef<ConnexionActor.Message>, Long> pair : users){
                    if (Objects.equals(pair.second(),team1.getUser().getId()) || Objects.equals(pair.second(), team2.getUser().getId())){
                        pair.first().tell(new ConnexionActor.SendFight(Json.toJson(result)));
                    }
                }
                getContext().getSelf().tell(new EndOfFightMessage(result));
            });
        }
        return Behaviors.same();
    }

    private Behavior<Message> onEndOfFightMessage(EndOfFightMessage msg) {
        if (gameOver) return Behaviors.same();

        nbFight++;
        Team teamA = game.getTeam(msg.results.idTeamA());
        Team teamB = game.getTeam(msg.results.idTeamB());

        Team loser = null;
        Team winner = null;

        if (teamA == null || teamB == null) {
            return Behaviors.same();
        }

        if (msg.results.pvLostTeamA() > 0){
            teamA.lostFight(msg.results.pvLostTeamA());
            loser = teamA;

        } else {
            teamA.wonFight();
            winner = teamA;
        }

        if (msg.results.pvLostTeamB() > 0){
            teamB.lostFight(msg.results.pvLostTeamB());
            loser = teamB;
        } else {
            teamB.wonFight();
            winner = teamB;
        }
        game.adjustRankings();

        ObjectNode payload = Json.newObject().set(TEAMA, Json.newObject()
                .put(USER, teamA.getId())
                .put(HEALTH, msg.results.pvLostTeamA()));
        payload.set(TEAMB, Json.newObject()
                .put(USER, teamB.getId())
                .put(HEALTH, msg.results.pvLostTeamB()));

        JsonNode node = Json.newObject()
                .put(ID, getUUID())
                .put(TYPE, FIGHTRESULT)
                .set(PAYLOAD, payload);

        tellOtherUsers(node, null); //null so that everyone knows that he lost hp even himself because it is not transmitted with the fight.

        checkHealth(teamA);
        checkHealth(teamB);

        if (winner == null || loser == null){
            loser = teamA;
            winner = teamB;
        }

        Round loserRound = loser.getRounds().getLast();
        Round winnerRound = winner.getRounds().getLast();
        Fight fight = new Fight(winnerRound, loserRound);

        if (nbFight == maxFights) {
            getContext().getSelf().tell(new StartOfRoundMessage());
        }

        repo.add(fight);
        repo.merge(game).exceptionally(err -> {
            getContext().getLog().error("Failed to merge game: {} from message {}", err.getMessage(), msg);
            return null;
        });        return Behaviors.same();
    }

    private Behavior<Message> onEndOfGame (EndOfGame msg){
        return Behaviors.stopped();
    }

    private Behavior<Message> onConnexionSetupMessage (ConnexionSetupMessage msg) {
        Team team = game.getTeam(msg.userId);
        if (team == null) {
            return Behaviors.same();
        }

        List<UnitDTO> unitDTOS = new ArrayList<>();
        for (Pool pool : game.getPools()){
            for (PoolEntry entry: pool.getEntries()){
                unitDTOS.add(UnitDTOMapper.unitToDTO(entry.getUnit()));
            }
        }

        List<Long> units = team.getUnits().stream().map(InstanceUnit::getId).toList();
        List<Long> items = team.getItems().stream().map(Item::getId).toList();
        List<Long> shop = team.getShop().stream().map(Unit::getId).toList();
        TeamDTO teamDTO = new TeamDTO(team.getStreak(),
                team.getHealth(),
                team.getLvl(),
                team.getExp(),
                team.getGold(),
                shop,
                items,
                units);

        repo.getAllItems().thenApply(listItems -> {

            ObjectNode response = Json.newObject().put(ID , getUUID()).put(TYPE, SETUP).set(UNITS, Json.toJson(unitDTOS));
            response.set(ITEMS, Json.toJson(listItems.stream().map(ItemDTOMapper::itemToDTO)));
            response.set(TEAM, Json.toJson(teamDTO));

            msg.respondTo.tell(new ConnexionActor.SetupMessage(response));
            return listItems;
        });


        return Behaviors.same();
    }

    private Behavior<Message> onFailedCombatMessage (FailedCombatMessage msg) {
        getContext().getLog().error("Failed to simulate combat: {}",msg.throwable);
        return Behaviors.same();
    }

    private void tellOtherUsers(JsonNode payload, Long userId){
        for (Pair<ActorRef<ConnexionActor.Message>,Long> actor : users){
            if (!Objects.equals(actor.second(), userId)){
                actor.first().tell(new ConnexionActor.ChangesFromOtherUser(payload));
            }
        }
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

    private void gameOver(JsonNode payload, Long userId) {
        for (Pair<ActorRef<ConnexionActor.Message>,Long> actor : users){
            if (Objects.equals(actor.second(), userId)){
                actor.first().tell(new ConnexionActor.EndGame(payload));
            }
        }
    }

    private long getUUID (){
        return UUID++;
    }

    private void checkHealth(Team team){
        if (team.getHealth() <= 0) {

            game.died(team);
            gameOver(Json.newObject().put(ID, getUUID()).put(TYPE, GAMELOST), team.getId());

            List<Team> remainingTeam = game.stillAlive();
            if (remainingTeam.size() == 1){
                gameOver = true;

                JsonNode finalMessage = Json.newObject().put(ID, getUUID()).put(TYPE, GAMEOVER).put(PAYLOAD, remainingTeam.getFirst().getId());
                for (Pair<ActorRef<ConnexionActor.Message>,Long> actor : users){
                    actor.first().tell(new ConnexionActor.EndGame(finalMessage));
                }

                deathTimer = getContext().scheduleOnce(
                        Duration.ofMinutes(5),
                        getContext().getSelf(),
                        EndOfGame.INSTANCE
                );
            }
        }
    }
}

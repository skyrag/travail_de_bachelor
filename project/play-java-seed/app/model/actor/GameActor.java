package model.actor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import model.DTO.*;
import model.DTO.fighting.FightingResultDTO;
import model.entities.Fight;
import model.entities.Round;
import model.entities.Team;
import model.entities.event.Event;
import model.entities.game.Game;
import model.entities.game.Pool;
import model.entities.game.PoolEntry;
import model.entities.unit.InstanceUnit;
import model.entities.unit.Item;
import model.entities.unit.Unit;
import model.repositories.GameRepository;
import model.service.GameLevelService;
import model.service.SeedMakerService;
import model.service.SimulationService;
import model.utils.SpriteMap;
import model.utils.Tuple;
import org.apache.pekko.actor.Cancellable;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.apache.pekko.actor.typed.javadsl.Receive;
import org.apache.pekko.japi.Pair;
import play.libs.Json;

import org.apache.pekko.actor.typed.javadsl.TimerScheduler;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CompletionStage;

import static model.actor.JsonConstantes.*;
import static model.utils.Constante.*;
import static model.utils.SpriteMap.getSingleton;

/**
 * Actor responsible for running a game.
 * <p>
 * It centralizes all game business logic: player action handling, round
 * progression, combat execution, synchronization with connection actors,
 * and data persistence.
 * <p>
 * All state changes are processed through this actor to ensure sequential
 * execution.
 */
public class GameActor extends AbstractBehavior<GameActor.Message> {

    /**
     * Common interface for all messages handled by {@code GameActor}.
     */
    public interface Message {}

    /**
     * Base class for messages representing a player action.
     * <p>
     * Each message contains the player ID, the client message ID, and the actor
     * to which the validation result should be sent.
     */
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


    /**
     * Request to buy a unit.
     */
    public static final class BuyingUnitMessage extends ValidationMessage {
        private int slot;
        public BuyingUnitMessage(long userId, int slot, long messageId, ActorRef<ConnexionActor.Message> respondTo) {
            super(userId, messageId, respondTo);
            this.slot = slot;
        }
    }

    /**
     * Request to sell a unit.
     */
    public static final class SellingUnitMessage extends ValidationMessage {
        private long unitId;
        public SellingUnitMessage(long userId, long unitId, long messageId, ActorRef<ConnexionActor.Message> respondTo){
            super(userId, messageId, respondTo);
            this.unitId = unitId;
        }
    }

    /**
     * Request to move a unit.
     */
    public static final class MovingUnitMessage extends ValidationMessage {
        private long unitId;
        private Tuple newPosition;
        public MovingUnitMessage(long userId, long messageId, long unitId, Tuple newPosition, ActorRef<ConnexionActor.Message> respondTo) {
            super(userId, messageId, respondTo);
            this.unitId = unitId;
            this.newPosition = newPosition;
        }
    }

    /**
     * Request to assign an item to a unit.
     */
    public static final class GivingUnitObjectMessage extends ValidationMessage {
        private long unitId;
        private String name;
        public GivingUnitObjectMessage(long userId, long messageId,long unitId, String itemName, ActorRef<ConnexionActor.Message> respondTo) {
            super(userId, messageId, respondTo);
            this.unitId = unitId;
            this.name = itemName;
        }
    }

    /**
     * Request to reroll the shop.
     */
    public static final class RerollShopMessage extends ValidationMessage {
        public RerollShopMessage(long userId, long messageId, ActorRef<ConnexionActor.Message> respondTo) {
            super(userId, messageId, respondTo);
        }
    }

    /**
     * Request to buy experience.
     */
    public static final class BuyingExpMessage extends ValidationMessage {
        public BuyingExpMessage(long userId, long messageId, ActorRef<ConnexionActor.Message> respondTo) {
            super(userId, messageId, respondTo);
        }
    }

    /**
     * Message that triggers the start of a new round.
     */
    public static final class StartOfRoundMessage implements Message{
        public static final StartOfRoundMessage INSTANCE = new StartOfRoundMessage();
        public StartOfRoundMessage() {}
    }

    /**
     * Message indicating the end of the preparation phase.
     */
    public static final class EndOfRoundMessage implements Message {
        public EndOfRoundMessage() {}
    }

    /**
     * Message containing the result of a simulated fight.
     */
    public static final class EndOfFightMessage implements Message {
        private FightingResultDTO results;
        public EndOfFightMessage(FightingResultDTO results) {
            this.results = results;
        }
    }

    /**
     * Message that initiates game setup.
     */
    public static final class SetupMessage implements Message {
        public static final SetupMessage INSTANCE = new SetupMessage();
        public SetupMessage() {}
    }


    /**
     * Message sent by a {@link ConnexionActor} to request the information
     * needed to initialize the client.
     */
    public static final class ConnexionSetupMessage implements Message {
        private long userId;
        private ActorRef<ConnexionActor.Message> respondTo;
        public ConnexionSetupMessage(long userId, ActorRef<ConnexionActor.Message> respondTo) {
            this.userId = userId;
            this.respondTo = respondTo;
        }
    }


    /**
     * Message requesting the game to end permanently.
     */
    public static final class EndOfGame implements Message {
        public static final EndOfGame INSTANCE = new EndOfGame();
        public EndOfGame() {}
    }

    public static final class InternalSetupDoneMessage implements Message {
        public final Game game;
        public InternalSetupDoneMessage(Game game) {
            this.game = game;
        }
    }

    public static final class RoundPersistedMessage implements Message {}

    private Cancellable deathTimer;

    private final TimerScheduler<Message> timers;


    private final List<Pair<ActorRef<ConnexionActor.Message>, Long>> users;
    private Game game;
    private List<Team> teams;
    private List<Pool> pools;
    private final GameRepository repo;
    private final SeedMakerService seedGenerator;
    private final GameLevelService gameLevelService;
    private final SimulationService simulationService;
    private int nbFight;
    private int maxFights;
    private boolean gameOver;
    private long UUID = 500000L;
    private final Random rand;
    private final SpriteMap spriteMap;

    private final int ROUNDTIMEMS = 90000;
    private static final String ROUND_TIMER_KEY = "round-timer";


    /**
     * Creates the initial behavior of a {@code GameActor}.
     *
     * @param users players participating in the game
     * @param game game being managed
     * @param repo repository used for persistence
     * @param seedGenerator random seed generator
     * @param gameLevelService service managing player levels
     * @param simulationService combat simulation service
     * @return the actor's initial behavior
     */
    public static Behavior<Message> create(List<Pair<ActorRef<ConnexionActor.Message>, Long>> users, Game game, GameRepository repo, SeedMakerService seedGenerator, GameLevelService gameLevelService, SimulationService simulationService) {
        return Behaviors.withTimers(timers -> Behaviors.setup(ctx -> new GameActor(ctx, timers, users, game, repo, seedGenerator, gameLevelService, simulationService)));
    }

    /**
     * Initializes a new actor representing a game.
     * <p>
     * Players are associated with the game, setup is scheduled, and each
     * { ConnexionActor} is informed of the game actor to which it should
     * forward player actions.
     */
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
        this.spriteMap = getSingleton();
        getContext().getSelf().tell(new SetupMessage());

    }

    /**
     * Defines the messages handled by {@code GameActor}.
     *
     * @return the receive behavior for messages
     */
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
                .onMessage(InternalSetupDoneMessage.class, this::onInternalSetupDoneMessage)
                .onMessage(RoundPersistedMessage.class, this::onRoundPersisted)
                .build();
    }

    /**
     * Initializes the game by assigning each team its random generator
     * and a starting unit, items and shopUnits.
     *
     * @param msg initialization message
     * @return the next behavior
     */
    private Behavior<Message> onSetupMessage(SetupMessage msg){
        long baseSeed = game.getSeed();

        for (Team team : teams){
            team.setSeed(seedGenerator.createFromSeed(baseSeed, "player:" + team.getUser().getId()));
            team.setItems(new ArrayList<>(game.getItems()));

            Unit startUnit = game.randomUnitFromPool(pools.getFirst(), team.getSeed());
            team.addUnit(new InstanceUnit(1, new Tuple(-1,-1), startUnit, team));

            //set shop units
            List<Unit> shop = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                shop.add(game.randomUnitFromPool(pools.getFirst(), team.getSeed()));
            }
            team.setShop(shop);

        }

        System.out.println("on est la");

        repo.persistGameSetup(game)
                .thenAccept(freshGame -> getContext().getSelf().tell(new InternalSetupDoneMessage(freshGame)))
                .exceptionally(err -> {
                    getContext().getLog().error("Failed to setup game: {}", err.toString());
                    return null;
                });


        return Behaviors.same();
    }


    private Behavior<Message> onInternalSetupDoneMessage(InternalSetupDoneMessage msg){
        System.out.println("on est dans setup done");

        for (Team fresh : msg.game.getTeams()) {
            for (Team old : teams) {
                if (Objects.equals(old.getUser().getId(),fresh.getUser().getId())) {
                    fresh.setRandom(old.getSeed());   // à ajouter dans Team
                }
            }
        }

        msg.game.setItems(this.game.getItems());
        msg.game.setLastDied(this.game.getLastDied());
        this.game = msg.game;
        this.teams = msg.game.getTeams();
        this.pools = msg.game.getPools();

        for (Pair<ActorRef<ConnexionActor.Message>, Long> pair : users){
            pair.first().tell(new ConnexionActor.StartGame(getContext().getSelf()));
        }

        getContext().getSelf().tell(new StartOfRoundMessage());
        return Behaviors.same();
    }

    /**
     * Handles a unit purchase request.
     * <p>
     * The request is validated, the response is sent to the player,
     * other players are notified, and the game state is persisted.
     *
     * @param msg purchase request
     * @return the next behavior
     */
    private Behavior<Message> onBuyingUnitMessage(BuyingUnitMessage msg){
        if (gameOver) return Behaviors.same();
        Team team = game.getTeam(msg.userId);
        System.out.println("user id " + msg.userId + " team " + team);


        if (team == null || msg.slot < 0 || msg.slot >= team.getShop().size()) {
            System.out.println(msg + "invalid slot");
            //ERROR
            return Behaviors.same();
        }

        Unit unit = team.getShop().get(msg.slot);

        System.out.println(unit.getName());

        if (unit == null){
            System.out.println("rip est le soucsi");

            msg.respondTo.tell(new ConnexionActor.FeedbackInput(Json.newObject().put(ID, msg.messageId).put(TYPE, ERROR).put(LOG, "cannot buy this unit")));
            return Behaviors.same();
        }

        System.out.println("avaant team.result");

        Team.Result bought = team.buyUnit(msg.slot);
        if (bought == null) {
            System.out.println("team.result est le soucsi");

            msg.respondTo.tell(new ConnexionActor.FeedbackInput(Json.newObject().put(ID, msg.messageId).put(TYPE, ERROR).put(LOG, "cannot buy this unit")));
            return Behaviors.same();
        }

        game.canRemoveUnitToPool(unit.getId());

        List<Unit> shop = team.getShop();
        int gold = team.getGold();

        repo.persistBuy(bought, team, shop, gold)
                .thenAccept(v -> {
                    long id = bought.instance().getId();
                    msg.respondTo.tell(new ConnexionActor.FeedbackInput(
                            Json.newObject().put(ID, msg.messageId).put(TYPE, OK).put(INSTANCEUNIT, id)));
                    tellOtherUsers(Json.newObject().put(ID, msg.messageId).put(TYPE, BUY)
                            .set(PAYLOAD, Json.newObject().put(USER, msg.userId).put(UNIT, bought.instance().getUnit().getId()).put(INSTANCEUNIT, id)), msg.userId);
                })
                .exceptionally(err -> { getContext().getLog().error("persistBuy: {}", err.toString()); return null; });
        return Behaviors.same();
    }

    /**
     * Handles a unit sale request.
     * <p>
     * If the action is valid, the sale is completed, other players are
     * notified, and the changes are persisted.
     *
     * @param msg sale request
     * @return the next behavior
     */
    private Behavior<Message> onSellUnitMessage(SellingUnitMessage msg) {
        if (gameOver) return Behaviors.same();

        Team team = game.getTeam(msg.userId);

        System.out.println("a");
        InstanceUnit sold = team.getUnitById(msg.unitId);
        if (sold == null) {
            System.out.println("aa");

            msg.respondTo.tell(new ConnexionActor.FeedbackInput(Json.newObject().put(ID, msg.messageId).put(TYPE , ERROR).put(LOG, "cannot sell this unit")));
            return Behaviors.same();
        }
        System.out.println("aaa");

        Team.Result results = team.sellUnit(msg.unitId);
        game.canAddUnitToPool(sold.getUnit().getId());
        System.out.println("aaaa");

        if (results == null) {
            System.out.println("aaaaa");

            msg.respondTo.tell(new ConnexionActor.FeedbackInput(Json.newObject().put(ID, msg.messageId).put(TYPE , ERROR).put(LOG, "cannot sell this unit")));
            return Behaviors.same();
        }

        List<Item> items = team.getItems();
        int gold = team.getGold();

        repo.persistSell(results, team, gold, items).thenAccept(v -> {
            msg.respondTo.tell(new ConnexionActor.FeedbackInput(Json.newObject().put(ID, msg.messageId).put(TYPE, OK)));
            JsonNode payload = Json.newObject().put(ID, msg.messageId).put(TYPE , SELL).set(PAYLOAD, Json.newObject().put(USER, msg.userId).put(UNIT, msg.unitId));
            tellOtherUsers(payload, msg.userId);
        }).exceptionally(err -> { getContext().getLog().error("persistSell: {}", err.toString()); return null; });
        return Behaviors.same();
    }

    /**
     * Handles a unit move request.
     * <p>
     * If the action is valid, the move is performed, other players are
     * notified, and the changes are persisted.
     *
     * @param msg move request
     * @return the next behavior
     */
    private Behavior<Message> onMovingUnitMessage(MovingUnitMessage msg){
        if (gameOver) return Behaviors.same();

        Team team = game.getTeam(msg.userId);
        if (team == null) {
            msg.respondTo.tell(new ConnexionActor.FeedbackInput(Json.newObject().put(ID, msg.messageId).put(TYPE, ERROR).put(LOG, "cannot move this unit")));
            return Behaviors.same();
        }

        System.out.println("on MovingUnit avant result");

        Team.Result result = team.moveUnit(msg.newPosition, msg.unitId);

        if (result == null) {
            msg.respondTo.tell(new ConnexionActor.FeedbackInput(
                    Json.newObject().put(ID, msg.messageId).put(TYPE, ERROR).put(LOG, "cannot move this unit")));
            return Behaviors.same();
        }

        Tuple newPos = result.instance().getPos();

        System.out.println("on moving unit avant persist");

        repo.persistMove(result, newPos).thenAccept(v -> {
            msg.respondTo.tell(new ConnexionActor.FeedbackInput(Json.newObject().put(ID, msg.messageId).put(TYPE, OK)));
            JsonNode payload = Json.newObject().put(ID, msg.messageId).put(TYPE , MOVE)
                    .set(PAYLOAD, Json.newObject()
                            .put(USER, msg.userId)
                            .put(UNIT, msg.unitId)
                            .set(POSITION, Json.newObject()
                                    .put("x" , msg.newPosition.x())
                                    .put("y", msg.newPosition.y())));
            tellOtherUsers(payload, msg.userId);
        }).exceptionally(err -> { getContext().getLog().error("persistMove: {}", err.toString()); return null; });

        return Behaviors.same();
    }

    /**
     * Handles a request to assign an item to a unit.
     *
     * @param msg item assignment request
     * @return the next behavior
     */
    private Behavior<Message> onGivingUnitObjectMessage(GivingUnitObjectMessage msg) {
        if (gameOver) return Behaviors.same();

        Team team = game.getTeam(msg.userId);
        if (team == null) {
            msg.respondTo.tell(new ConnexionActor.FeedbackInput(Json.newObject().put(ID, msg.messageId).put(TYPE, ERROR).put(LOG, "cannot give this item to this unit")));
            return Behaviors.same();
        }

        System.out.println("on giving avant result");


        Team.Result result = team.addItemToUnit(msg.name, msg.unitId);

        if (result == null) {
            msg.respondTo.tell(new ConnexionActor.FeedbackInput(
                    Json.newObject().put(ID, msg.messageId).put(TYPE, ERROR).put(LOG, "cannot give this item to this unit")));
            return Behaviors.same();
        }

        System.out.println("on giving avant persist");


        List<Item> instanceItems = result.instance().getItems();
        List<Item> teamItems = team.getItems();
        repo.persistGive(result, team, instanceItems, teamItems).thenAccept(v -> {
            msg.respondTo.tell(new ConnexionActor.FeedbackInput(Json.newObject().put(ID, msg.messageId).put(TYPE, OK)));
            JsonNode payload = Json.newObject().put(ID, msg.messageId).put(TYPE , GIVE).set(PAYLOAD, Json.newObject().put(USER, msg.userId).put(UNIT, msg.unitId).put(ITEMNAME, msg.name));
            tellOtherUsers(payload, msg.userId);
        }).exceptionally(err -> { getContext().getLog().error("persistGive: {}", err.toString()); return null; });

        return Behaviors.same();
    }

    /**
     * Handles a shop reroll request.
     * <p>
     * New units are generated according to the probabilities based on
     * the player's level and sent to the client.
     *
     * @param msg reroll request
     * @return the next behavior
     */
    private Behavior<Message> onRerollShopMessage(RerollShopMessage msg){
        if (gameOver) return Behaviors.same();

        Team team = game.getTeam(msg.userId);
        if (team == null || team.getGold() < REROLLCOST) {
            msg.respondTo.tell(new ConnexionActor.FeedbackInput(Json.newObject().put(ID, msg.messageId).put(TYPE, ERROR).put(LOG, "no team found")));
            return Behaviors.same();
        }

        List<Unit> shop = new ArrayList<>();
        for (int i = 0; i < MAXNBSHOPUNIT; i++){
            Unit unit = game.randomUnitFromPool(randomRarityFromPools(team),team.getSeed());
            shop.add(unit);
        }

        System.out.println("on reroll avant result");


        Event event = team.Reroll(shop);

        if (event == null){
            msg.respondTo.tell(new ConnexionActor.FeedbackInput(Json.newObject().put(ID, msg.messageId).put(TYPE, ERROR).put(LOG, "team can't reroll")));
            return Behaviors.same();
        }

        int gold = team.getGold();
        List<Unit> newShop = team.getShop();

        System.out.println("on reroll avant persist");


        repo.persistReroll(team, event, gold, newShop).thenAccept(v -> {
            ShopDTO units = ShopDTO.from(shop);

            msg.respondTo.tell(new ConnexionActor.FeedbackInput(Json.newObject().put(ID, msg.messageId).put(TYPE, OK).set(PAYLOAD, Json.toJson(units))));

        }).exceptionally(err -> { getContext().getLog().error("persistReroll: {}", err.toString()); return null; });
        return Behaviors.same();
    }

    /**
     * Handles an experience purchase request.
     *
     * @param msg exp purchase request
     * @return the next behavior
     */
    private Behavior<Message> onBuyingExpMessage(BuyingExpMessage msg) {
        if (gameOver) return Behaviors.same();

        Team team = game.getTeam(msg.userId);
        if (team == null || team.getGold() < EXPCOST) {
            msg.respondTo.tell(new ConnexionActor.FeedbackInput(Json.newObject().put(ID, msg.messageId).put(TYPE, ERROR).put(LOG, "cannot buy exp")));
            return Behaviors.same();
        }

        System.out.println("on exp avant result");


        Event event = team.canBuyExp(gameLevelService.getExpRequired(team.getLvl()));

        if (event == null) {
            msg.respondTo.tell(new ConnexionActor.FeedbackInput(
                    Json.newObject().put(ID, msg.messageId).put(TYPE, ERROR).put(LOG, "cannot buy exp")));
            return Behaviors.same();
        }

        int gold = team.getGold();
        int lvl = team.getLvl();
        int exp = team.getExp();

        System.out.println("on exp avant persist");


        repo.persistExp(team, event, gold, lvl, exp).thenAccept(v -> msg.respondTo.tell(new ConnexionActor.FeedbackInput(Json.newObject().put(ID, msg.messageId).put(TYPE, OK)))).exceptionally(err -> { getContext().getLog().error("persistExp: {}", err.toString()); return null; });
        return Behaviors.same();
    }

    /**
     * Starts a new round.
     * <p>
     * All teams are prepared for the next round, a preparation window
     * is opened on the client, and a timer is started to automatically
     * trigger the combat phase.
     *
     * @param msg start-of-round message
     * @return the next behavior
     */
    private Behavior<Message> onStartOfRoundMessage(StartOfRoundMessage msg) {

        for (Team team: teams){
            team.newRound();
        }

        List<Round> created = teams.stream().map(t -> t.getRounds().getLast()).toList();

        repo.persistNewRounds(created)
                .thenAccept(v -> getContext().getSelf().tell(new RoundPersistedMessage()))
                .exceptionally(err -> { getContext().getLog().error("persistNewRounds: {}", err.toString()); return null; });

        return Behaviors.same();
    }

    /**
     * Triggers end-of-round fights.
     * <p>
     * Remaining teams are paired and an asynchronous simulation is started
     * for each matchup.
     *
     * @param msg end-of-round message
     * @return the next behavior
     */
    private Behavior<Message> onEndOfRoundMessage(EndOfRoundMessage msg) {
        if (gameOver) return Behaviors.same();

        List<Team> remainingTeam = game.stillAlive();
        maxFights = remainingTeam.size()/2;



        if(remainingTeam.size() % 2 == 1) {
            maxFights++;
            remainingTeam.add(game.getLastDied());
        }

        nbFight = 0;

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

    /**
     * Handles the result of a fight.
     * <p>
     * Team health is updated, rankings are recalculated, clients are
     * informed of the results, and a new round is started when all fights
     * are finished.
     *
     * @param msg fight result
     * @return the next behavior
     */
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
            System.out.println("Failed to merge game: " + err.getMessage()+ " from " + msg);
            return null;
        });        return Behaviors.same();
    }

    /**
     * Permanently stops the actor representing the game.
     *
     * @param msg game end request
     * @return a stopped behavior
     */
    private Behavior<Message> onEndOfGame (EndOfGame msg){
        return Behaviors.stopped();
    }


    /**
     * Sends a player all the information necessary to initialize their client.
     * <p>
     * Units, items, teams, and other game data are converted to DTOs and
     * forwarded to the { ConnexionActor}.
     *
     * @param msg connection setup request
     * @return the next behavior
     */
    private Behavior<Message> onConnexionSetupMessage (ConnexionSetupMessage msg) {

        System.out.println("on est dans le setup");


        List<Team> teams1 = game.getTeams();

        Team team = game.getTeam(msg.userId);
        if (team == null) {
            return Behaviors.same();
        }



        List<UnitDTO> unitDTOS = new ArrayList<>();
        for (Pool pool : game.getPools()){
            for (PoolEntry entry: pool.getEntries()){
                unitDTOS.add(UnitDTOMapper.unitToDTO(entry.getUnit(),entry.getUnit().getSpriteKey()));
            }
        }


        List<TeamDTO> teamDTOS = new ArrayList<>();
        for (Team currentTeam: teams1){
            List<TeamsUnitDTO> units = currentTeam.getUnits().stream().map(unit -> new TeamsUnitDTO(unit.getId(), unit.getUnit().getName())).toList();
            List<String> items = currentTeam.getItems().stream().map(Item::getName).toList();
            List<String> shop = currentTeam.getShop().stream().map(Unit::getName).toList();

            teamDTOS.add(new TeamDTO(currentTeam.getId(),
                    currentTeam.getUser().getUsername(),
                    currentTeam.getStreak(),
                    currentTeam.getHealth(),
                    currentTeam.getLvl(),
                    currentTeam.getExp(),
                    currentTeam.getGold(),
                    shop,
                    items,
                    units));
        }


        System.out.println("on est avant le getrepo du setup");

        System.out.println("on est avant le frontend1");

        ObjectNode response = Json.newObject().put(ID , getUUID()).put(TYPE, SETUP).set(UNITS, Json.toJson(unitDTOS));
        response.set(ITEMS, Json.toJson(game.getItemsDTO()));
        response.set(TEAM, Json.toJson(teamDTOS));

        System.out.println("on est avant le frontend2");

        msg.respondTo.tell(new ConnexionActor.SetupMessage(response));

        System.out.println("on est a la fin du setup");


        return Behaviors.same();
    }

    private Behavior<Message> onRoundPersisted(RoundPersistedMessage msg) {
        long endTime = System.currentTimeMillis() + ROUNDTIMEMS;
        JsonNode payload = Json.newObject().put(ID, getUUID()).put(TYPE, ROUNDWINDOW).put(PAYLOAD, endTime);
        for (Pair<ActorRef<ConnexionActor.Message>, Long> pair : users) {
            pair.first().tell(new ConnexionActor.StartOfRound(payload));
        }
        timers.startSingleTimer(ROUND_TIMER_KEY, new EndOfRoundMessage(),
                Duration.ofMillis(ROUNDTIMEMS + INPUTBUFFER));
        return Behaviors.same();
    }

    /**
     * Sends a message to all game players except the one with the provided ID.
     * <p>
     * This method is used to propagate a player's actions to the other clients.
     *
     * @param payload message to forward
     * @param userId ID of the player to exclude, or {@code null}
     *               to send the message to all players
     */
    private void tellOtherUsers(JsonNode payload, Long userId){
        for (Pair<ActorRef<ConnexionActor.Message>,Long> actor : users){
            if (!Objects.equals(actor.second(), userId)){
                actor.first().tell(new ConnexionActor.ChangesFromOtherUser(payload));
            }
        }
    }

    /**
     * Selects a unit pool according to the probabilities associated with the team's level.
     *
     * @param team the team in question
     * @return the pool matching the selected rarity
     * @throws IllegalStateException if no pool matches the defined probabilities
     */
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

    /**
     * Sends an end-of-game message to a given player.
     *
     * @param payload content of the message sent to the client
     * @param userId ID of the player concerned
     */
    private void gameOver(JsonNode payload, Long userId) {
        for (Pair<ActorRef<ConnexionActor.Message>,Long> actor : users){
            if (Objects.equals(actor.second(), userId)){
                actor.first().tell(new ConnexionActor.EndGame(payload));
            }
        }
    }

    /**
     * Generates a unique identifier for messages produced by the server.
     *
     * @return a new unique identifier
     */
    private long getUUID (){
        return UUID++;
    }

    /**
     * Checks whether a team has been eliminated.
     * <p>
     * When a team has no health remaining, it is removed from the game and
     * a defeat message is sent. If only one team remains alive, the game is
     * declared over, all players are notified, and the {@code GameActor} is
     * scheduled to stop.
     *
     * @param team the team whose health is being checked
     */
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

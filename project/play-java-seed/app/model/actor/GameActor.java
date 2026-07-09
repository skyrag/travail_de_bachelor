package model.actor;

import model.entities.Fight;
import model.entities.Round;
import model.entities.Team;
import model.entities.event.unit.BuyUnitEvent;
import model.entities.game.Game;
import model.entities.game.Pool;
import model.entities.game.PoolEntry;
import model.entities.unit.InstanceUnit;
import model.entities.unit.Unit;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static model.utils.Constante.*;

public class GameActor extends AbstractBehavior<GameActor.Message> {

    public interface Message {}

    public static final class BuyingUnitMessage implements Message {
        private long userId;
        private Unit unit;
        public BuyingUnitMessage(long userId, Unit unit) {
            this.userId = userId;
            this.unit = unit;
        }
    }

    public static final class SellingUnitMessage implements Message{
        private long userId;
        private long unitId;
        public SellingUnitMessage(long userId, long unitId){
            this.userId = userId;
            this.unitId = unitId;
        }
    }

    public static final class MovingUnitMessage implements Message {
        private long userId;
        private long unitId;
        private Tuple newPosition;
        public MovingUnitMessage(long userId, long unitId, Tuple newPosition) {
            this.userId = userId;
            this.unitId = unitId;
            this.newPosition = newPosition;
        }
    }

    public static final class GivingUnitObjectMessage implements Message{
        private long userId;
        private long unitId;
        private long itemId;
        public GivingUnitObjectMessage(long userId, long unitId, long itemId) {
            this.userId = userId;
            this.unitId = unitId;
            this.itemId = itemId;
        }
    }

    public static final class RerollShopMessage implements Message {
        private long userId;
        public RerollShopMessage(long userId) {
            this.userId = userId;
        }
    }

    public static final class BuyingExpMessage implements Message {
        private long userId;
        public BuyingExpMessage(long userId) {
            this.userId = userId;
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


    private final List<ActorRef<ConnexionActor.Message>> users;
    private final Game game;
    private final List<Team> teams;
    private final List<Pool> pools;
    private final GameRepository repo;
    private final SeedMakerService seedGenerator;
    private final GameLevelService gameLevelService;
    private int nbFight;
    private int maxFights;
    private boolean gameOver;

    public static Behavior<GameActor.Message> create(List<ActorRef<ConnexionActor.Message>> users, Game game, GameRepository repo, SeedMakerService seedGenerator, GameLevelService gameLevelService) {
        return Behaviors.setup(ctx -> new GameActor(ctx, users, game, repo, seedGenerator, gameLevelService));
    }

    private GameActor(ActorContext<Message> context, List<ActorRef<ConnexionActor.Message>> users, Game game, GameRepository repo, SeedMakerService seedGenerator, GameLevelService gameLevelService) {
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

        //TODO envoyer l'état de la game a l'utilisateur.
        repo.merge(game);
        return Behaviors.same();
    }

    private Behavior<Message> onBuyingUnitMessage(BuyingUnitMessage msg){
        Team team = game.getTeam(msg.userId);
        if (team == null || !team.canBuyUnit(msg.unit) || game.canRemoveUnitToPool(msg.unit.getId())){
            //TODO encoyer un message erreur au websocket actor
            return Behaviors.same();
        }

        repo.merge(game);
        //TODO gérer l'erreur du merge on sait jamais
        //TODO envoyer les messages aux autres joueurs
        return Behaviors.same();
    }

    private Behavior<Message> onSellUnitMessage(SellingUnitMessage msg) {
        Team team = game.getTeam(msg.userId);
        if (team == null || !team.canSellUnit(msg.unitId) || game.canAddUnitToPool(msg.unitId)){
            //TODO encoyer un message erreur au websocket actor
            return Behaviors.same();
        }

        repo.merge(game);

        return Behaviors.same();
    }

    private Behavior<Message> onMovingUnitMessage(MovingUnitMessage msg){
        Team team = game.getTeam(msg.userId);
        if (team == null || !team.canMoveUnit(msg.newPosition, msg.unitId)) {
            //TODO encoyer un message erreur au websocket actor
            return Behaviors.same();
        }

        repo.merge(team);
        return Behaviors.same();
    }

    private Behavior<Message> onGivingUnitObjectMessage(GivingUnitObjectMessage msg) {
        Team team = game.getTeam(msg.userId);
        if (team == null || !team.canAddItemToUnit(msg.itemId, msg.unitId)) {
            //TODO encoyer un message erreur au websocket actor
            return Behaviors.same();
        }

        repo.merge(team);

        return Behaviors.same();
    }

    private Behavior<Message> onRerollShopMessage(RerollShopMessage msg){
        Team team = game.getTeam(msg.userId);
        if (team == null) {
            //TODO encoyer un message erreur au websocket actor
            return Behaviors.same();
        }

        List<Unit> shop = new ArrayList<>();
        for (int i = 0; i < MAXNBSHOPUNIT; i++){
            Unit unit = randomUnitFromPool(randomRarityFromPools(team),team.getSeed());
            shop.add(unit);
        }

        if (!team.canReroll(shop)){
            //TODO encoyer un message erreur au websocket actor
            return Behaviors.same();
        }

        repo.merge(team);

        return Behaviors.same();
    }

    private Behavior<Message> onBuyingExpMessage(BuyingExpMessage msg) {
        Team team = game.getTeam(msg.userId);
        if (team == null || !team.canBuyExp(gameLevelService.getExpRequired(team.getLvl()))) {
            //TODO encoyer un message erreur au websocket actor
            return Behaviors.same();
        }

        repo.merge(team);

        return Behaviors.same();
    }

    private Behavior<Message> onEndOfRoundMessage(EndOfRoundMessage msg) {

        //TODO lancer le service de combat sur pluesieurs thread, et ils sont sensé renvoyer un message au gameActor et websocketActor
        List<Team> remainingTeam = game.stillAlive();
        nbFight = remainingTeam.size()/2;

        return Behaviors.same();
    }

    private Behavior<Message> onEndOfFightMessage(EndOfFightMessage msg) {
        nbFight++;
        Team winner = game.getTeam(msg.winnerId);
        Team loser = game.getTeam(msg.loserId);
        if (winner == null || loser == null) {
            //TODO encoyer un message erreur au websocket actor
            return Behaviors.same();
        }

        loser.lostFight(msg.pvLost);
        winner.wonFight();
        game.adjustRankings();


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
        repo.merge(game);
        return Behaviors.same();
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
}

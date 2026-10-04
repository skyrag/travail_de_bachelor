package model.service.fightingService;

import model.service.fightingService.pathfinding.AStarPathfinding;
import model.service.fightingService.pathfinding.HexGrid;
import model.service.fightingService.pathfinding.Tile;
import model.utils.Tuple;
import org.apache.pekko.japi.Pair;

import java.util.*;

import static model.service.fightingService.pathfinding.HexGrid.cubeToOffset;

public class FightingContext {

    private final List<ComponentUnit> units = new ArrayList<>();
    private final Pair<List<ComponentUnit>, Long> teamA;
    private final Pair<List<ComponentUnit>, Long> teamB;
    private int tick;
    private int eventCounter;
    private Random seed;
    private HexGrid board;

    private final Set<ComponentUnit> released = new HashSet<>();


    public FightingContext(Pair<List<ComponentUnit>, Long> teamA, Pair<List<ComponentUnit>, Long> teamB, Random seed) {
        tick = 0;
        this.teamA = teamA;
        this.teamB = teamB;
        this.seed = seed;
        units.addAll(teamA.first());
        units.addAll(teamB.first());
        board = new HexGrid(8,7);

        for (ComponentUnit unit : teamB.first()){
            unit.setCurrentPosition(getInverse(unit.getCurrentPosition()));
        }

        for (ComponentUnit unit: units){
            board.getTile(unit.getCurrentPosition().x(), unit.getCurrentPosition().y()).setObstacle(true);
        }
    }

    public void incrementTick() {
        tick++;
    }

    public List<ComponentUnit> getAliveUnits () {
        System.out.println("getAlive");
        return units.stream().filter(ComponentUnit::isAlive).toList();
    }

    public ComponentUnit getCurrentTarget(ComponentUnit unit) {
       return getClosestUnit(unit, true);
    }

    public ComponentUnit getClosestAlly(ComponentUnit unit){
        return getClosestUnit(unit, false);
    }

    private ComponentUnit getClosestUnit(ComponentUnit unit, boolean isEnemy){
        if (isEnemy && unit.currentEnnemy != null && unit.currentEnnemy.isAlive()) {
            return unit.currentEnnemy;
        }
        ComponentUnit closest = null;
        for (ComponentUnit target : getGroup(unit, isEnemy)) {
            if (!target.isAlive() || target == unit) continue;
            if (closest == null || unit.getCurrentPosition().isCloserThanFrom(target.getCurrentPosition(), closest.getCurrentPosition())) {
                closest = target;
            }
        }
        if (isEnemy) unit.currentEnnemy = closest;   // ne pas écraser la cible avec un allié
        return closest;
    }

    public List<ComponentUnit> getEnemies(ComponentUnit unit){
        return getGroup(unit, true);
    }

    public List<ComponentUnit> getAllies(ComponentUnit unit){
        return getGroup(unit, false);
    }

    public List<ComponentUnit> getGroup (ComponentUnit unit, boolean isEnemy){
        if(Objects.equals(unit.getTeam().getId(), teamA.second())) {
            return isEnemy? teamB.first() : teamA.first();
        } else {
            return isEnemy? teamA.first() : teamB.first();
        }
    }

    public int randomInt(int n) {
        return seed.nextInt(n);
    }

    public int getTick() {
        return tick;
    }

    public Tuple getNextMove(ComponentUnit unit){
        Tuple current = unit.getCurrentPosition();
        ComponentUnit target = getCurrentTarget(unit);
        if (target == null) return current;

        Tile src = board.getTile(current.x(), current.y());
        Tile dest = board.getTile(target.getCurrentPosition().x(), target.getCurrentPosition().y());
        List<Tile> path = AStarPathfinding.findPath(src, dest);

        if (path.size() < 3) return current;
        return cubeToOffset(path.get(1).getPosition());
    }

    public void releaseDeadTiles() {
        for (ComponentUnit u : units) {
            if (!u.isAlive() && released.add(u)) {
                Tuple p = u.getCurrentPosition();
                board.getTile(p.x(), p.y()).setObstacle(false);
            }
        }
    }

    private Tuple getInverse(Tuple pos){
        return new Tuple(7 - pos.x(), pos.y());
    }

    public void move(Tuple oldPos, Tuple newPos) {
        board.getTile(oldPos.x(), oldPos.y()).setObstacle(false);
        board.getTile(newPos.x(), newPos.y()).setObstacle(true);
    }
}

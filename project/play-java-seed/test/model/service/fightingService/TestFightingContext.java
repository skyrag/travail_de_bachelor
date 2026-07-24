package model.service.fightingService;

import model.entities.Team;
import model.service.fightingService.pathfinding.HexGrid;
import model.service.fightingService.pathfinding.Tile;
import model.utils.Tuple;
import org.apache.pekko.japi.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * HYPOTHESES / A ADAPTER SI BESOIN :
 * - Tuple est instanciable via "new Tuple(int x, int y)" et expose des
 *   accesseurs x()/y() ainsi qu'une methode
 *   "isCloserThanFrom(Tuple candidate, Tuple currentClosest)" qui, appelee
 *   sur la position de l'unite de reference, renvoie true si "candidate"
 *   est plus proche que "currentClosest". Si la signature reelle differe,
 *   adaptez le stubbing ci-dessous.
 * - HexGrid(int width, int height) est entierement mockee via
 *   Mockito#mockConstruction : aucune logique reelle de grille/Tile n'est
 *   exercee ici, on isole donc FightingContext de la geometrie du plateau.
 * - getNextMove() (qui delegue a AStarPathfinding.findPath(...) et
 *   HexGrid.cubeToOffset(...)) n'est PAS teste ici : le type exact retourne
 *   par AStarPathfinding.findPath(...) n'etant pas fourni, il n'est pas
 *   possible d'ecrire un mock fiable sans ambiguite. Partagez les classes
 *   du package "pathfinding" (AStarPathfinding, Tile) si vous voulez que
 *   je complete la couverture sur cette methode.
 */
public class TestFightingContext {

    private MockedConstruction<HexGrid> mockedHexGrid;
    private Tile boardTile;

    @BeforeEach
    void setUp() {
        boardTile = mock(Tile.class);
        mockedHexGrid = mockConstruction(HexGrid.class,
                (mock, ctx) -> when(mock.getTile(anyInt(), anyInt())).thenReturn(boardTile));
    }

    @AfterEach
    void tearDown() {
        mockedHexGrid.close();
    }

    private ComponentUnit mockUnit(Team team, Tuple position, boolean alive) {
        ComponentUnit unit = mock(ComponentUnit.class);
        when(unit.getTeam()).thenReturn(team);
        when(unit.getCurrentPosition()).thenReturn(position);
        when(unit.isAlive()).thenReturn(alive);
        return unit;
    }

    private Tuple mockPosition(int x, int y) {
        Tuple tuple = mock(Tuple.class);
        when(tuple.x()).thenReturn(x);
        when(tuple.y()).thenReturn(y);
        return tuple;
    }

    private Team mockTeam(long id) {
        Team team = mock(Team.class);
        when(team.getId()).thenReturn(id);
        return team;
    }

    // ---------------------------------------------------------------
    // Constructeur
    // ---------------------------------------------------------------

    @Test
    void constructor_buildsHexGridWithExpectedDimensions() {
        Team teamA = mockTeam(1L);
        Team teamB = mockTeam(2L);

        new FightingContext(
                new Pair<>(List.of(), teamA.getId()),
                new Pair<>(List.of(), teamB.getId()),
                new Random(1L));

        assertEquals(1, mockedHexGrid.constructed().size());
    }

    @Test
    void constructor_marksBoardTileAsObstacleForEveryUnitOfBothTeams() {
        Team teamA = mockTeam(1L);
        Team teamB = mockTeam(2L);

        ComponentUnit a1 = mockUnit(teamA, mockPosition(0, 0), true);
        ComponentUnit a2 = mockUnit(teamA, mockPosition(1, 0), true);
        ComponentUnit b1 = mockUnit(teamB, mockPosition(2, 0), true);

        new FightingContext(
                new Pair<>(List.of(a1, a2), teamA.getId()),
                new Pair<>(List.of(b1), teamB.getId()),
                new Random(1L));

        // 3 unites au total => 3 appels a setObstacle(true)
        verify(boardTile, times(3)).setObstacle(true);
    }

    @Test
    void constructor_invertsPositionOfTeamBUnitsOnly() {
        Team teamA = mockTeam(1L);
        Team teamB = mockTeam(2L);

        Tuple posA = mockPosition(2, 3);
        Tuple posB = mockPosition(5, 4);

        ComponentUnit unitA = mockUnit(teamA, posA, true);
        ComponentUnit unitB = mockUnit(teamB, posB, true);

        new FightingContext(
                new Pair<>(List.of(unitA), teamA.getId()),
                new Pair<>(List.of(unitB), teamB.getId()),
                new Random(1L));

        // teamA : position inchangee
        verify(unitA, never()).setCurrentPosition(any());

        // teamB : position inversee -> new Tuple(7 - x, y) = new Tuple(2, 4)
        ArgumentCaptor<Tuple> captor = ArgumentCaptor.forClass(Tuple.class);
        verify(unitB, times(1)).setCurrentPosition(captor.capture());
        assertEquals(2, captor.getValue().x());
        assertEquals(4, captor.getValue().y());
    }

    // ---------------------------------------------------------------
    // getAliveUnits
    // ---------------------------------------------------------------

    @Test
    void getAliveUnits_filtersOutDeadUnits() {
        Team teamA = mockTeam(1L);
        Team teamB = mockTeam(2L);

        ComponentUnit alive = mockUnit(teamA, mockPosition(0, 0), true);
        ComponentUnit dead = mockUnit(teamA, mockPosition(1, 0), false);

        FightingContext context = new FightingContext(
                new Pair<>(List.of(alive, dead), teamA.getId()),
                new Pair<>(List.of(), teamB.getId()),
                new Random(1L));

        List<ComponentUnit> result = context.getAliveUnits();

        assertEquals(1, result.size());
        assertTrue(result.contains(alive));
        assertFalse(result.contains(dead));
    }

    // ---------------------------------------------------------------
    // getGroup / getEnemies / getAllies
    // ---------------------------------------------------------------

    @Test
    void getEnemies_forTeamAUnit_returnsTeamBUnits() {
        Team teamA = mockTeam(1L);
        Team teamB = mockTeam(2L);

        ComponentUnit teamAUnit = mockUnit(teamA, mockPosition(0, 0), true);
        ComponentUnit teamBUnit = mockUnit(teamB, mockPosition(1, 0), true);

        FightingContext context = new FightingContext(
                new Pair<>(List.of(teamAUnit), teamA.getId()),
                new Pair<>(List.of(teamBUnit), teamB.getId()),
                new Random(1L));

        List<ComponentUnit> enemies = context.getEnemies(teamAUnit);

        assertEquals(List.of(teamBUnit), enemies);
    }

    @Test
    void getAllies_forTeamAUnit_returnsTeamAUnits() {
        Team teamA = mockTeam(1L);
        Team teamB = mockTeam(2L);

        ComponentUnit teamAUnit = mockUnit(teamA, mockPosition(0, 0), true);
        ComponentUnit teamBUnit = mockUnit(teamB, mockPosition(1, 0), true);

        FightingContext context = new FightingContext(
                new Pair<>(List.of(teamAUnit), teamA.getId()),
                new Pair<>(List.of(teamBUnit), teamB.getId()),
                new Random(1L));

        List<ComponentUnit> allies = context.getAllies(teamAUnit);

        assertEquals(List.of(teamAUnit), allies);
    }

    @Test
    void getEnemies_forNonTeamAUnit_returnsTeamAUnits() {
        // couvre la branche "else" de getGroup : toute unite dont l'id
        // d'equipe ne correspond pas a teamA.second() est traitee comme
        // appartenant a "l'autre camp" (teamB), symetriquement.
        Team teamA = mockTeam(1L);
        Team teamB = mockTeam(2L);

        ComponentUnit teamAUnit = mockUnit(teamA, mockPosition(0, 0), true);
        ComponentUnit teamBUnit = mockUnit(teamB, mockPosition(1, 0), true);

        FightingContext context = new FightingContext(
                new Pair<>(List.of(teamAUnit), teamA.getId()),
                new Pair<>(List.of(teamBUnit), teamB.getId()),
                new Random(1L));

        List<ComponentUnit> enemiesOfB = context.getEnemies(teamBUnit);
        List<ComponentUnit> alliesOfB = context.getAllies(teamBUnit);

        assertEquals(List.of(teamAUnit), enemiesOfB);
        assertEquals(List.of(teamBUnit), alliesOfB);
    }

    // ---------------------------------------------------------------
    // getCurrentTarget / getClosestAlly (getClosestUnit)
    // ---------------------------------------------------------------

    @Test
    void getCurrentTarget_returnsCachedEnnemyWithoutRecomputing_whenAlreadySet() {
        Team teamA = mockTeam(1L);
        Team teamB = mockTeam(2L);

        ComponentUnit unit = mockUnit(teamA, mockPosition(0, 0), true);
        ComponentUnit cachedTarget = mockUnit(teamB, mockPosition(5, 5), true);
        unit.currentEnnemy = cachedTarget;

        ComponentUnit otherEnemy = mockUnit(teamB, mockPosition(1, 1), true);

        FightingContext context = new FightingContext(
                new Pair<>(List.of(unit), teamA.getId()),
                new Pair<>(List.of(cachedTarget, otherEnemy), teamB.getId()),
                new Random(1L));

        ComponentUnit result = context.getCurrentTarget(unit);

        assertSame(cachedTarget, result);
        // aucune comparaison de distance necessaire puisque la cible est en cache
        verify(unit.getCurrentPosition(), never()).isCloserThanFrom(any(), any());
    }

    @Test
    void getCurrentTarget_computesClosestAliveEnemy_whenNoCacheYet() {
        Team teamA = mockTeam(1L);
        Team teamB = mockTeam(2L);

        Tuple unitPos = mockPosition(0, 0);
        ComponentUnit unit = mockUnit(teamA, unitPos, true);

        ComponentUnit far = mockUnit(teamB, mockPosition(6, 6), true);
        ComponentUnit near = mockUnit(teamB, mockPosition(1, 0), true);
        ComponentUnit deadButCloser = mockUnit(teamB, mockPosition(0, 1), false);

        // "near" est considere plus proche que "far", et plus proche que le mort ignore
        when(unitPos.isCloserThanFrom(eq(far.getCurrentPosition()), any())).thenReturn(false);
        when(unitPos.isCloserThanFrom(eq(near.getCurrentPosition()), any())).thenReturn(true);

        FightingContext context = new FightingContext(
                new Pair<>(List.of(unit), teamA.getId()),
                new Pair<>(List.of(far, near, deadButCloser), teamB.getId()),
                new Random(1L));

        ComponentUnit result = context.getCurrentTarget(unit);

        assertSame(near, result);
        assertSame(near, unit.currentEnnemy);
    }

    @Test
    void getCurrentTarget_returnsNull_whenNoAliveEnemy() {
        Team teamA = mockTeam(1L);
        Team teamB = mockTeam(2L);

        ComponentUnit unit = mockUnit(teamA, mockPosition(0, 0), true);
        ComponentUnit deadEnemy = mockUnit(teamB, mockPosition(1, 0), false);

        FightingContext context = new FightingContext(
                new Pair<>(List.of(unit), teamA.getId()),
                new Pair<>(List.of(deadEnemy), teamB.getId()),
                new Random(1L));

        ComponentUnit result = context.getCurrentTarget(unit);

        assertNull(result);
        assertNull(unit.currentEnnemy);
    }

    @Test
    void getClosestAlly_ignoresCachedEnnemy_becauseCacheOnlyAppliesToEnemies() {
        // isEnemy = false pour getClosestAlly => la condition
        // "unit.currentEnnemy != null && isEnemy" est toujours fausse ici,
        // donc le calcul est refait meme si currentEnnemy est deja renseigne.
        Team teamA = mockTeam(1L);
        Team teamB = mockTeam(2L);

        Tuple unitPos = mockPosition(0, 0);
        ComponentUnit unit = mockUnit(teamA, unitPos, true);
        unit.currentEnnemy = mockUnit(teamB, mockPosition(9, 9), true); // ne doit pas etre retourne

        ComponentUnit ally = mockUnit(teamA, mockPosition(1, 0), true);
        when(unitPos.isCloserThanFrom(eq(ally.getCurrentPosition()), any())).thenReturn(true);

        FightingContext context = new FightingContext(
                new Pair<>(List.of(unit, ally), teamA.getId()),
                new Pair<>(List.of(), teamB.getId()),
                new Random(1L));

        ComponentUnit result = context.getClosestAlly(unit);

        assertSame(ally, result);
    }

    // ---------------------------------------------------------------
    // randomInt
    // ---------------------------------------------------------------

    @Test
    void randomInt_delegatesToUnderlyingRandom() {
        Team teamA = mockTeam(1L);
        Team teamB = mockTeam(2L);

        Random random = mock(Random.class);
        when(random.nextInt(10)).thenReturn(4);

        FightingContext context = new FightingContext(
                new Pair<>(List.of(), teamA.getId()),
                new Pair<>(List.of(), teamB.getId()),
                random);

        int result = context.randomInt(10);

        assertEquals(4, result);
        verify(random).nextInt(10);
    }

    // ---------------------------------------------------------------
    // tick
    // ---------------------------------------------------------------

    @Test
    void getTick_startsAtZeroAndIncrementsCorrectly() {
        Team teamA = mockTeam(1L);
        Team teamB = mockTeam(2L);

        FightingContext context = new FightingContext(
                new Pair<>(List.of(), teamA.getId()),
                new Pair<>(List.of(), teamB.getId()),
                new Random(1L));

        assertEquals(0, context.getTick());

        context.incrementTick();
        context.incrementTick();

        assertEquals(2, context.getTick());
    }

    // ---------------------------------------------------------------
    // move
    // ---------------------------------------------------------------

    @Test
    void move_freesOldTileAndOccupiesNewTile() {
        Team teamA = mockTeam(1L);
        Team teamB = mockTeam(2L);

        FightingContext context = new FightingContext(
                new Pair<>(List.of(), teamA.getId()),
                new Pair<>(List.of(), teamB.getId()),
                new Random(1L));

        Tuple oldPos = mockPosition(1, 1);
        Tuple newPos = mockPosition(2, 2);

        context.move(oldPos, newPos);

        verify(boardTile, times(1)).setObstacle(false);
        // +1 setObstacle(true) car boardTile est reutilise pour toutes les tuiles du mock HexGrid
        verify(boardTile, atLeastOnce()).setObstacle(true);
    }
}
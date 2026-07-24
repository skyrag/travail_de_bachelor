package model.service.fightingService.pathfinding;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Les tiles sont reliées à la main (sans passer par HexGrid) pour garder un
 * contrôle total sur la topologie du graphe et forcer chaque branche de
 * l'algorithme A*. Les positions utilisées ne représentent pas forcément une
 * grille hexagonale "réelle" mais respectent toujours l'invariant x+y+z=0,
 * ce qui suffit pour piloter précisément l'heuristique (distance hexagonale).
 */
public class TestAStarPathfinding {

    private static Tile tile(int x, int y, int z) {
        return new Tile(new HexPosition(x, y, z));
    }

    private static void connect(Tile a, Tile b) {
        a.addAdjacentTile(b);
        b.addAdjacentTile(a);
    }

    @Test
    void findPath_returnsSingleTileWhenStartEqualsEnd() {
        Tile start = tile(0, 0, 0);

        List<Tile> path = AStarPathfinding.findPath(start, start);

        assertEquals(1, path.size());
        assertEquals(start, path.get(0));
    }

    @Test
    void findPath_findsPathOnSimpleChain() {
        Tile s = tile(0, 0, 0);
        Tile m = tile(1, -1, 0);
        Tile e = tile(2, -2, 0);
        connect(s, m);
        connect(m, e);

        List<Tile> path = AStarPathfinding.findPath(s, e);

        assertEquals(List.of(s, m, e), path);
    }

    @Test
    void findPath_skipsObstaclesAndUsesDetour() {
        Tile s = tile(0, 0, 0);
        Tile obstacle = tile(1, -1, 0);
        Tile detour = tile(0, -1, 1);
        Tile e = tile(1, -2, 1);

        obstacle.setObstacle(true);

        connect(s, obstacle);   // route directe bloquée par l'obstacle
        connect(obstacle, e);
        connect(s, detour);     // route alternative
        connect(detour, e);

        List<Tile> path = AStarPathfinding.findPath(s, e);

        assertEquals(List.of(s, detour, e), path);
        assertFalse(path.contains(obstacle));
    }

    @Test
    void findPath_returnsEmptyListWhenNoPathExists() {
        Tile s = tile(0, 0, 0);
        Tile deadEnd = tile(1, -1, 0);
        Tile e = tile(5, -5, 0); // jamais connectée au reste du graphe

        connect(s, deadEnd);

        List<Tile> path = AStarPathfinding.findPath(s, e);

        assertTrue(path.isEmpty());
    }

    @Test
    void findPath_resetsSearchStateOfTouchedTilesAfterSearch() {
        Tile s = tile(0, 0, 0);
        Tile m = tile(1, -1, 0);
        Tile e = tile(2, -2, 0);
        connect(s, m);
        connect(m, e);

        AStarPathfinding.findPath(s, e);

        for (Tile t : List.of(s, m, e)) {
            assertEquals(Tile.SearchState.UNVISITED, t.getSearchState());
            assertEquals(0, t.getG());
            assertEquals(0, t.getH());
            assertNull(t.getParent());
        }
    }

    @Test
    void findPath_consecutiveCallsDoNotPolluteEachOther() {
        Tile s = tile(0, 0, 0);
        Tile m = tile(1, -1, 0);
        Tile e = tile(2, -2, 0);
        connect(s, m);
        connect(m, e);

        List<Tile> first = AStarPathfinding.findPath(s, e);
        List<Tile> second = AStarPathfinding.findPath(s, e);

        assertEquals(first, second);
    }

    /**
     * Force la branche "decrease-key" : X est d'abord découvert via une route
     * longue (S->P->P2->X, g=3) pendant qu'il est encore OPEN (pas encore
     * popped), puis une route plus courte (S->Q->X, g=2) est trouvée : le code
     * doit retirer X de la file, mettre à jour son g/parent, et le ré-insérer.
     */
    @Test
    void findPath_updatesOpenTileWhenCheaperPathIsFound() {
        Tile s  = tile(0, 0, 0);
        Tile p  = tile(70, -70, 0);
        Tile q  = tile(50, -50, 0);
        Tile p2 = tile(65, -65, 0);
        Tile x  = tile(40, -40, 0);
        Tile e  = tile(100, -100, 0);

        connect(s, p);
        connect(s, q);
        connect(p, p2);
        connect(p2, x);
        connect(q, x);
        connect(x, e);

        List<Tile> path = AStarPathfinding.findPath(s, e);

        assertEquals(List.of(s, q, x, e), path);
    }

    /**
     * Force la branche "adjacent déjà CLOSED -> continue" ainsi que la branche
     * "adjacent déjà OPEN mais pas amélioré -> pas de mise à jour". D et A
     * forment une impasse reliée en boucle à B ; au moment où B les examine,
     * ils sont déjà fermés.
     */
    @Test
    void findPath_skipsClosedTilesAndDoesNotWorsenOpenTiles() {
        Tile s = tile(0, 0, 0);
        Tile a = tile(90, -90, 0);
        Tile d = tile(95, -95, 0);
        Tile b = tile(10, -10, 0);
        Tile c = tile(50, -50, 0);
        Tile e = tile(100, -100, 0);

        connect(s, a);
        connect(s, b);
        connect(a, d);
        connect(d, b);   // boucle : une fois a et d fermés, b doit les ignorer
        connect(b, c);
        connect(c, e);

        List<Tile> path = AStarPathfinding.findPath(s, e);

        assertEquals(List.of(s, b, c, e), path);
        assertFalse(path.contains(a));
        assertFalse(path.contains(d));
    }

    @Test
    void getEstimatedPathCost_delegatesToHexDistance() {
        HexPosition a = new HexPosition(0, 0, 0);
        HexPosition b = new HexPosition(2, -3, 1);

        assertEquals(a.distanceTo(b), AStarPathfinding.getEstimatedPathCost(a, b));
    }
}
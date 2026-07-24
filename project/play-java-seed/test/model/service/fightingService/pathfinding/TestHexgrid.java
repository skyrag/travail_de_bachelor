package model.service.fightingService.pathfinding;

import model.utils.Tuple;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * IMPORTANT : ces tests supposent que linkAdjacentTiles() vérifie les bornes
 * avant d'indexer le tableau `tiles` (voir le correctif proposé dans ma
 * réponse). En l'état actuel du code, tiles[neighborTuple.x()][neighborTuple.y()]
 * n'est jamais protégé par un test de bornes : dès qu'une tile de bordure a un
 * voisin dont les coordonnées offset tombent hors grille, le constructeur lève
 * une ArrayIndexOutOfBoundsException. Comme la ligne 0 (ou la colonne 0/dernière)
 * existe dans n'importe quelle grille non vide, cela se produit systématiquement,
 * y compris pour `new HexGrid(1, 1)`.
 *
 * Ces tests supposent aussi que model.utils.Tuple expose des accesseurs x() et y()
 * (comme un record `Tuple(int x, int y)`) — adapte si la signature réelle diffère.
 */
public class TestHexgrid {

    @Test
    void constructor_createsAllTilesInBounds() {
        HexGrid grid = new HexGrid(3, 3);
        for (int col = 0; col < 3; col++) {
            for (int row = 0; row < 3; row++) {
                assertNotNull(grid.getTile(col, row));
            }
        }
    }

    @Test
    void cubeToOffset_isInverseOfInternalOffsetToCube() {
        HexGrid grid = new HexGrid(4, 4);
        for (int col = 0; col < 4; col++) {
            for (int row = 0; row < 4; row++) {
                Tile t = grid.getTile(col, row);
                Tuple offset = HexGrid.cubeToOffset(t.getPosition());
                assertEquals(col, offset.x());
                assertEquals(row, offset.y());
            }
        }
    }

    @Test
    void constructor_linksInteriorTileToAllSixNeighbors() {
        HexGrid grid = new HexGrid(5, 5);
        Tile center = grid.getTile(2, 2);
        assertEquals(6, center.getAdjacentTiles().size());
    }

    @Test
    void constructor_linksCornerTileToFewerThanSixNeighbors() {
        HexGrid grid = new HexGrid(5, 5);
        Tile corner = grid.getTile(0, 0);
        assertTrue(corner.getAdjacentTiles().size() < 6);
    }

    @Test
    void constructor_neverLinksNullNeighbors() {
        HexGrid grid = new HexGrid(3, 3);
        for (int col = 0; col < 3; col++) {
            for (int row = 0; row < 3; row++) {
                for (Tile neighbor : grid.getTile(col, row).getAdjacentTiles()) {
                    assertNotNull(neighbor);
                }
            }
        }
    }

    @Test
    void getTile_returnsSameInstanceOnRepeatedCalls() {
        HexGrid grid = new HexGrid(2, 2);
        assertSame(grid.getTile(1, 1), grid.getTile(1, 1));
    }
}
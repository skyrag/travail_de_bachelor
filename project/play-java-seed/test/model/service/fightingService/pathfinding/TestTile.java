package model.service.fightingService.pathfinding;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestTile {

    private Tile tile;

    @BeforeEach
    void setUp() {
        tile = new Tile(new HexPosition(1, -1, 0));
    }

    @Test
    void getPosition_returnsPositionPassedToConstructor() {
        HexPosition pos = new HexPosition(2, -2, 0);
        Tile t = new Tile(pos);
        assertEquals(pos, t.getPosition());
    }

    @Test
    void adjacentTiles_startsEmpty() {
        assertTrue(tile.getAdjacentTiles().isEmpty());
    }

    @Test
    void addAdjacentTile_addsToList() {
        Tile other = new Tile(new HexPosition(0, 0, 0));
        tile.addAdjacentTile(other);
        assertEquals(1, tile.getAdjacentTiles().size());
        assertSame(other, tile.getAdjacentTiles().get(0));
    }

    @Test
    void obstacle_defaultsToFalse() {
        assertFalse(tile.isObstacle());
    }

    @Test
    void setObstacle_updatesState() {
        tile.setObstacle(true);
        assertTrue(tile.isObstacle());
        tile.setObstacle(false);
        assertFalse(tile.isObstacle());
    }

    @Test
    void gAndH_defaultToZero() {
        assertEquals(0, tile.getG());
        assertEquals(0, tile.getH());
    }

    @Test
    void getF_isSumOfGAndH() {
        tile.setG(3);
        tile.setH(4);
        assertEquals(7, tile.getF());
    }

    @Test
    void parent_defaultsToNullAndIsSettable() {
        assertNull(tile.getParent());
        Tile parent = new Tile(new HexPosition(-1, 1, 0));
        tile.setParent(parent);
        assertSame(parent, tile.getParent());
    }

    @Test
    void searchState_defaultsToUnvisitedAndIsSettable() {
        assertEquals(Tile.SearchState.UNVISITED, tile.getSearchState());
        tile.setSearchState(Tile.SearchState.OPEN);
        assertEquals(Tile.SearchState.OPEN, tile.getSearchState());
        tile.setSearchState(Tile.SearchState.CLOSED);
        assertEquals(Tile.SearchState.CLOSED, tile.getSearchState());
    }

    @Test
    void resetSearchState_restoresDefaults() {
        tile.setG(5);
        tile.setH(6);
        tile.setParent(new Tile(new HexPosition(0, 0, 0)));
        tile.setSearchState(Tile.SearchState.CLOSED);

        tile.resetSearchState();

        assertEquals(0, tile.getG());
        assertEquals(0, tile.getH());
        assertNull(tile.getParent());
        assertEquals(Tile.SearchState.UNVISITED, tile.getSearchState());
    }

    @Test
    void equals_isTrueForSameInstance() {
        assertEquals(tile, tile);
    }

    @Test
    void equals_isFalseForNull() {
        assertNotEquals(null, tile);
    }

    @Test
    void equals_isFalseForDifferentType() {
        assertNotEquals(tile, "not a tile");
    }

    @Test
    void equals_isTrueForDifferentInstanceSamePosition() {
        Tile other = new Tile(new HexPosition(1, -1, 0));
        assertEquals(tile, other);
    }

    @Test
    void equals_isFalseForDifferentPosition() {
        Tile other = new Tile(new HexPosition(0, 0, 0));
        assertNotEquals(tile, other);
    }

    @Test
    void hashCode_isConsistentWithEquals() {
        Tile other = new Tile(new HexPosition(1, -1, 0));
        assertEquals(tile.hashCode(), other.hashCode());
    }

    @Test
    void toString_containsPosition() {
        assertTrue(tile.toString().contains(tile.getPosition().toString()));
    }
}
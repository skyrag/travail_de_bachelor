package model.service.fightingService.pathfinding;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestHexPosition {

    @Test
    void constructor_acceptsValidCubeCoordinates() {
        HexPosition pos = new HexPosition(1, -1, 0);
        assertEquals(1, pos.x());
        assertEquals(-1, pos.y());
        assertEquals(0, pos.z());
    }

    @Test
    void constructor_acceptsOrigin() {
        HexPosition pos = new HexPosition(0, 0, 0);
        assertEquals(0, pos.x());
        assertEquals(0, pos.y());
        assertEquals(0, pos.z());
    }

    @Test
    void constructor_throwsWhenSumIsNotZero() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new HexPosition(1, 1, 1));
        assertEquals("x + y + z doit être égal à 0", ex.getMessage());
    }

    @Test
    void add_combinesCoordinatesComponentWise() {
        HexPosition a = new HexPosition(1, -1, 0);
        HexPosition b = new HexPosition(0, 1, -1);
        assertEquals(new HexPosition(1, 0, -1), a.add(b));
    }

    @Test
    void neighbors_returnsSixPositions() {
        List<HexPosition> neighbors = new HexPosition(0, 0, 0).neighbors();
        assertEquals(6, neighbors.size());
    }

    @Test
    void neighbors_areAllAtDistanceOneAndRespectCubeInvariant() {
        HexPosition center = new HexPosition(2, -3, 1);
        for (HexPosition n : center.neighbors()) {
            assertEquals(1, center.distanceTo(n));
            assertEquals(0, n.x() + n.y() + n.z());
        }
    }

    @Test
    void neighbors_containsExpectedSetForOrigin() {
        List<HexPosition> neighbors = new HexPosition(0, 0, 0).neighbors();
        assertTrue(neighbors.contains(new HexPosition(1, -1, 0)));
        assertTrue(neighbors.contains(new HexPosition(1, 0, -1)));
        assertTrue(neighbors.contains(new HexPosition(0, 1, -1)));
        assertTrue(neighbors.contains(new HexPosition(-1, 1, 0)));
        assertTrue(neighbors.contains(new HexPosition(-1, 0, 1)));
        assertTrue(neighbors.contains(new HexPosition(0, -1, 1)));
    }

    @Test
    void distanceTo_isZeroForSamePosition() {
        HexPosition pos = new HexPosition(3, -2, -1);
        assertEquals(0, pos.distanceTo(pos));
    }

    @Test
    void distanceTo_isSymmetric() {
        HexPosition a = new HexPosition(0, 0, 0);
        HexPosition b = new HexPosition(3, -1, -2);
        assertEquals(a.distanceTo(b), b.distanceTo(a));
    }

    @Test
    void distanceTo_matchesExpectedHexDistance() {
        HexPosition a = new HexPosition(0, 0, 0);
        HexPosition b = new HexPosition(3, -1, -2);
        // max(|0-3|, |0-(-1)|, |0-(-2)|) = max(3,1,2) = 3
        assertEquals(3, a.distanceTo(b));
    }
}
package model.service.fightingService.pathfinding;

import java.util.ArrayList;
import java.util.List;

public record HexPosition(int x, int y, int z) {
    public HexPosition {
        if (x + y + z != 0) {
            throw new IllegalArgumentException("x + y + z doit être égal à 0");
        }
    }

    /**
     * Les 6 directions possibles sur une grille hexagonale (coordonnées cubiques).
     */
    private static final HexPosition[] DIRECTIONS = {
            new HexPosition(1, -1, 0),
            new HexPosition(1, 0, -1),
            new HexPosition(0, 1, -1),
            new HexPosition(-1, 1, 0),
            new HexPosition(-1, 0, 1),
            new HexPosition(0, -1, 1),
    };

    public HexPosition add(HexPosition other) {
        return new HexPosition(x + other.x, y + other.y, z + other.z);
    }

    /**
     * Retourne les 6 positions voisines (certaines peuvent être hors de la map,
     * c'est à celui qui construit la grille de filtrer).
     */
    public List<HexPosition> neighbors() {
        List<HexPosition> result = new ArrayList<>(6);
        for (HexPosition dir : DIRECTIONS) {
            result.add(this.add(dir));
        }
        return result;
    }

    /**
     * Distance hexagonale (nombre de pas minimum entre deux cases).
     */
    public int distanceTo(HexPosition other) {
        return Math.max(
                Math.abs(x - other.x),
                Math.max(Math.abs(y - other.y), Math.abs(z - other.z))
        );
    }
}

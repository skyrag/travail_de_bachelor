package model.service.fightingService.pathfinding;
import model.utils.Tuple;

import java.util.HashMap;
import java.util.Map;

public class HexGrid {
    private final Tile[][] tiles;

    public HexGrid(int columns, int rows) {
        tiles = new Tile[columns][rows];
        for (int col = 0; col < columns; col++) {
            for (int row = 0; row < rows; row++) {
                HexPosition pos = offsetToCube(col, row);
                tiles[col][row] =  new Tile(pos);
            }
        }
        linkAdjacentTiles();
    }

    private static HexPosition offsetToCube(int col, int row) {
        int x = col - (row - (row & 1)) / 2;
        int z = row;
        int y = -x - z;
        return new HexPosition(x, y, z);
    }

    public static Tuple cubeToOffset(HexPosition pos) {
        int row = pos.z();
        int col = pos.x() + (row - (row & 1)) / 2;

        return new Tuple(col, row);
    }

    private void linkAdjacentTiles() {
        int columns = tiles.length;
        int rows = tiles[0].length;
        for (Tile[] rowArr : tiles) {
            for (Tile tile : rowArr) {
                for (HexPosition neighbourPos : tile.getPosition().neighbors()) {
                    Tuple neighborTuple = cubeToOffset(neighbourPos);
                    int col = neighborTuple.x();
                    int row = neighborTuple.y();
                    if (col >= 0 && col < columns && row >= 0 && row < rows) {
                        Tile neighbor = tiles[col][row];
                        if (neighbor != null) {
                            tile.addAdjacentTile(neighbor);
                        }
                    }
                }
            }
        }
    }

    public Tile getTile(int col, int row) {
        return tiles[col][row];
    }

}
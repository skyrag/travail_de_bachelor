package model.service.fightingService.pathfinding;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Version sans Map : l'état de recherche (g, h, parent, searchState) est
 * stocké directement sur chaque Tile. Pour éviter qu'une recherche pollue
 * la suivante, on garde une simple liste des tiles touchées pendant CET
 * appel, et on les réinitialise à la fin (pas besoin de parcourir toute
 * la grille).
 */
public class AStarPathfinding {

    /**
     * Trouve un chemin entre startPoint et endPoint.
     * Retourne une liste vide si aucun chemin n'existe.
     */
    public static List<Tile> findPath(Tile startPoint, Tile endPoint) {
        PriorityQueue<Tile> openQueue = new PriorityQueue<>(
                Comparator.comparingInt(Tile::getF)
                        .thenComparing(Comparator.comparingInt(Tile::getG).reversed())
        );
        // toutes les tiles touchées par CETTE recherche, pour pouvoir
        // les réinitialiser à la fin sans toucher au reste de la grille
        List<Tile> touched = new ArrayList<>();

        startPoint.setG(0);
        startPoint.setH(getEstimatedPathCost(startPoint.getPosition(), endPoint.getPosition()));
        startPoint.setSearchState(Tile.SearchState.OPEN);
        touched.add(startPoint);
        openQueue.add(startPoint);

        List<Tile> path = new ArrayList<>();

        while (!openQueue.isEmpty()) {
            Tile current = openQueue.poll();
            current.setSearchState(Tile.SearchState.CLOSED);

            if (current.equals(endPoint)) {
                path = reconstructPath(current);
                break;
            }

            for (Tile adjacent : current.getAdjacentTiles()) {
                if (adjacent.isObstacle()) {
                    continue;
                }
                if (adjacent.getSearchState() == Tile.SearchState.CLOSED) {
                    continue;
                }

                int tentativeG = current.getG() + 1; // coût uniforme de 1 par déplacement

                if (adjacent.getSearchState() == Tile.SearchState.UNVISITED) {
                    adjacent.setG(tentativeG);
                    adjacent.setH(getEstimatedPathCost(adjacent.getPosition(), endPoint.getPosition()));
                    adjacent.setParent(current);
                    adjacent.setSearchState(Tile.SearchState.OPEN);
                    touched.add(adjacent);
                    openQueue.add(adjacent);
                } else if (tentativeG < adjacent.getG()) {
                    // chemin plus court trouvé : on RETIRE l'ancienne entrée avant
                    // de muter g, sinon on casse l'invariant du tas (PriorityQueue
                    // suppose que la priorité d'un élément ne change pas une fois inséré)
                    openQueue.remove(adjacent);
                    adjacent.setG(tentativeG);
                    adjacent.setParent(current);
                    openQueue.add(adjacent);
                }
            }
        }

        // on nettoie uniquement ce qu'on a touché, pour que le prochain
        // appel à findPath reparte d'un état propre
        for (Tile tile : touched) {
            tile.resetSearchState();
        }

        return path;
    }

    private static List<Tile> reconstructPath(Tile end) {
        List<Tile> path = new ArrayList<>();
        Tile current = end;
        while (current != null) {
            path.add(current);
            current = current.getParent();
        }
        Collections.reverse(path);
        return path;
    }

    /**
     * Distance hexagonale (heuristique admissible pour A* sur grille hexagonale).
     */
    public static int getEstimatedPathCost(HexPosition start, HexPosition target) {
        return start.distanceTo(target);
    }
}
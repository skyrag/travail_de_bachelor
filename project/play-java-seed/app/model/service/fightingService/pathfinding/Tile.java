package model.service.fightingService.pathfinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Une case de la grille hexagonale.
 *
 * Cette version stocke l'état de recherche A* (g, h, parent, searchState)
 * directement sur la Tile plutôt que dans une structure séparée. Pour éviter
 * que deux recherches successives se marchent dessus, AStarPathfinding
 * réinitialise ces champs (via resetSearchState()) sur toutes les tiles
 * qu'il a touchées, à la fin de chaque appel.
 * Ne modifie pas ces champs toi-même en dehors de AStarPathfinding.
 */
public class Tile {

    /**
     * État d'une tile pour la recherche A* en cours.
     * UNVISITED = jamais touchée par cette recherche.
     * OPEN      = découverte, en attente d'exploration.
     * CLOSED    = déjà explorée, chemin optimal vers elle confirmé.
     */
    public enum SearchState {
        UNVISITED, OPEN, CLOSED
    }

    private final HexPosition position;
    private final List<Tile> adjacentTiles = new ArrayList<>();
    private boolean obstacle;

    // --- état de recherche A*, remis à zéro après chaque appel à findPath ---
    private int g;
    private int h;
    private Tile parent;
    private SearchState searchState = SearchState.UNVISITED;

    public Tile(HexPosition position) {
        this.position = position;
    }

    public HexPosition getPosition() {
        return position;
    }

    public List<Tile> getAdjacentTiles() {
        return adjacentTiles;
    }

    /**
     * À appeler une fois la grille construite, pour relier chaque Tile
     * à ses voisines existantes (voir HexGrid.buildAdjacency ci-dessous
     * pour un exemple d'utilisation).
     */
    public void addAdjacentTile(Tile tile) {
        adjacentTiles.add(tile);
    }

    public boolean isObstacle() {
        return obstacle;
    }

    public void setObstacle(boolean obstacle) {
        this.obstacle = obstacle;
    }

    // --- accesseurs réservés à AStarPathfinding ---

    public int getG() {
        return g;
    }

    public void setG(int g) {
        this.g = g;
    }

    public int getH() {
        return h;
    }

    public void setH(int h) {
        this.h = h;
    }

    public int getF() {
        return g + h;
    }

    public Tile getParent() {
        return parent;
    }

    public void setParent(Tile parent) {
        this.parent = parent;
    }

    public SearchState getSearchState() {
        return searchState;
    }

    public void setSearchState(SearchState searchState) {
        this.searchState = searchState;
    }

    /**
     * Remet la tile dans son état "jamais visitée par une recherche".
     * Appelé par AStarPathfinding, uniquement sur les tiles qu'il a
     * effectivement touchées (pas besoin de parcourir toute la grille).
     */
    public void resetSearchState() {
        this.g = 0;
        this.h = 0;
        this.parent = null;
        this.searchState = SearchState.UNVISITED;
    }

    /**
     * Deux tiles sont égales si elles ont la même position.
     * Nécessaire pour que les Map<Tile, ...> et les comparaisons
     * fonctionnent correctement même si jamais tu recrées des instances.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tile tile)) return false;
        return position.equals(tile.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position);
    }

    @Override
    public String toString() {
        return "Tile" + position;
    }
}
import {Container, Sprite, Graphics} from "pixi.js";

export class Arena {

    constructor(app, layers) {
        this.app = app;
        this.units = []

        //création du sprite pour l'arène
        const COLS = 8;
        const ROWS = 7;
        const RADIUS = 70;

        const H_SPACING = Math.sqrt(3) * RADIUS;
        this.H_SPACING = H_SPACING
        const V_SPACING = 1.5 * RADIUS;

        const OFFSET_X = 80;
        const OFFSET_Y = 80;

        // Container qui va contenir tout le terrain
        const terrain = new Container();
        this.container = terrain;
        this.layers = layers;
        layers.board.addChild(terrain);

        this.hexGrid = []; // pour garder les centres


        function createHexagon(radius, isblocked) {
            const hex = new Graphics();

            const points = [];

            for (let i = 0; i < 6; i++) {
                const angle = (60 * i - 30) * Math.PI / 180;
                points.push(
                    Math.cos(angle) * radius,
                    Math.sin(angle) * radius
                );
            }

            const color = isblocked ? 0xff0000 :0x7ec8e3;

            hex.poly(points).fill(color);
            hex.poly(points).stroke({ width: 2, color: 0x000000 });

            return hex;
        }

        for (let row = 0; row < ROWS; row++) {
            this.hexGrid[row] = [];
            for (let col = 0; col < COLS; col++) {

                const x = OFFSET_X
                    + col * H_SPACING
                    + (row % 2) * H_SPACING / 2;

                const y = OFFSET_Y
                    + row * V_SPACING;

                const isBlocked = col >= 4;
                const hex = createHexagon(RADIUS, isBlocked);
                hex.x = x;
                hex.y = y - V_SPACING/2;

                terrain.addChild(hex); // on ajoute au container
                this.hexGrid[row][col] = { x, y, col, row, hex, unit: null, isBlocked: isBlocked};
            }
        }
        // Ensuite, pour déplacer TOUT le terrain :
        terrain.x = 500;
        terrain.y = 50;

    }


    getClosestCell(x, y) {
        const local = this.container.toLocal({ x, y }, this.layers.root );

        let best = null;
        let bestDist = Infinity;

        for (const cell of this.hexGrid.flat()) {
            const dist = this.distanceFrom(local, cell)

            if (dist < bestDist) {
                bestDist = dist;
                best = cell;
            }
        }
        console.log(best); // debug
        return best;
    }

    findUnitAt(globalX, globalY) {
        for (const unit of this.units) {
            if (!unit) continue; // this.units est indexé par id, donc peut avoir des trous
            const bounds = unit.container.getBounds();
            if (bounds.containsPoint(globalX, globalY)) {
                return unit;
            }
        }
        return null;
    }

    setToClosesCell(unit, x, y) {
        const cell = this.getClosestCell(x, y);
        if (!cell || cell.isBlocked || (cell.unit && cell.unit !== unit)) return false;

        this.container.addChild(unit.container);
        this.units[unit.id] = unit
        cell.unit = unit;
        unit.container.position.set(cell.x, cell.y);
        unit.hex = {x: cell.col, y: cell.row}
        unit.setOnArena()

        return true;
    }

    getCellAtPoint(x, y) {
        const local = this.container.toLocal({ x, y }, this.layers.root);

        for (const cell of this.hexGrid.flat()) {
            const point = { x: local.x - cell.x, y: local.y - cell.y };
            if (cell.hex.containsPoint(point) && !cell.isBlocked) {
                return cell;
            }
        }

        return null;
    }

    isInRange(x, y) {
        return this.getCellAtPoint(x, y) !== null;
    }

    setToNextEmptyCell(unit){
        for (const cell of this.hexGrid.flat()) {
            if (cell.unit == null && !cell.isBlocked){
                this.container.addChild(unit.fightingSprite);
                this.units[unit.id] = unit
                cell.unit = unit;
                unit.container.position.set(cell.x, cell.y);
                unit.hex = {x: cell.col, y: cell.row}
                unit.setOnArena()
                return true;
            }
        }
        return false; // bonus : utile pour vérifier l'échec si jamais toutes les cases sont pleines
    }

    removeUnit(unit){
        for (const hex of this.hexGrid.flat()) {
            if (hex.unit === unit) {
                hex.unit = null;
                break;
            }
        }
        this.units[unit.id] = undefined
        unit.hex = {x: -1, y: -1}
        this.container.removeChild(unit.container);
        unit.setOffArena()
    }

    getCellOfUnit(unit) {
        for (const cell of this.hexGrid.flat()) {
            if (cell.unit === unit) return cell;
        }
        return null;
    }

    moveUnit(unit, x, y) {
        const cell = this.getClosestCell(x, y);
        if (!cell || cell.isBlocked) return false;

        // Case déjà occupée par une AUTRE unité → on refuse le déplacement
        if (cell.unit && cell.unit !== unit) return false;

        // On libère l'ancienne case de cette unité
        const current = this.getCellOfUnit(unit);
        if (current) current.unit = null;

        cell.unit = unit;
        unit.container.position.set(cell.x, cell.y);
        unit.hex = {x: cell.col, y: cell.row}
        unit.setOnArena()

        return true;
    }

    setToCell(unit, x, y){ //debug purposes only
        const cell = this.hexGrid[y][x]

        if (!cell || (cell.unit && cell.unit !== unit)) return;

        this.container.addChild(unit.container);
        this.units[unit.id] = unit
        cell.unit = unit;
        unit.container.position.set(cell.x, cell.y);
        unit.hex = {x: cell.col, y: cell.row}
        unit.setOnArena()

    }

    getCell(x,y) {
        return this.hexGrid[y][x];
    }

    checkDeath(){
        for (const unit of this.units){
            if (unit.isDead()){
                this.container.removeChild(unit.container)
            }
        }
    }

    clean () {
        for (const unit of this.units) {
            this.container.removeChild(unit.container)
        }
        this.units.length = 0
    }

    distanceFrom(src, dst){
        const dx = src.x - dst.x;
        const dy = src.y - dst.y;
        const dist = dx * dx + dy * dy;
        return dist
    }
}
import {Container, Sprite, Graphics} from "pixi.js";

export class Arena {

    constructor(app) {
        this.app = app;
        this.units = Array.from(
            { length: 8 },
            () => Array(8).fill(null)
        );

        // valeur du placeholder mais aussi de la détection pour le drag
        this.x = 300;
        this.width = window.innerWidth -500;
        this.y = window.innerHeight/12 - 150;
        this.height = window.innerHeight/2 + 300;

        this.generateHexGrid(650 - this.x,220 - this.y,8,8);


        //création du sprite pour l'arène
        const COLS = 8;
        const ROWS = 7;
        const RADIUS = 70;

        const H_SPACING = Math.sqrt(3) * RADIUS;
        const V_SPACING = 1.5 * RADIUS;

        const OFFSET_X = 80;
        const OFFSET_Y = 80;

        // Container qui va contenir tout le terrain
        const terrain = new Container();
        this.container = terrain;
        terrain.zIndex = 0;
        app.stage.addChild(terrain);

        this.hexGrid = []; // pour garder les centres


        function createHexagon(radius) {
            const hex = new Graphics();

            const points = [];

            for (let i = 0; i < 6; i++) {
                const angle = (60 * i - 30) * Math.PI / 180;
                points.push(
                    Math.cos(angle) * radius,
                    Math.sin(angle) * radius
                );
            }

            hex.poly(points).fill(0x7ec8e3);
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

                const hex = createHexagon(RADIUS);
                hex.x = x;
                hex.y = y - V_SPACING/2;

                terrain.addChild(hex); // on ajoute au container
                this.hexGrid[row][col] = { x, y, hex, unit: null};
            }
        }
        // Ensuite, pour déplacer TOUT le terrain :
        terrain.x = 600;
        terrain.y = 100;
    }


    getClosestCell(x, y) {
        const local = this.container.toLocal({ x, y });

        let best = null;
        let bestDist = Infinity;

        for (const cell of this.hexGrid.flat()) {
            const dx = local.x - cell.x;
            const dy = local.y - cell.y;
            const dist = dx * dx + dy * dy;

            if (dist < bestDist) {
                bestDist = dist;
                best = cell;
            }
        }

        return best;
    }

    setToClosesCell(unit, x, y) {
        const cell = this.getClosestCell(x, y);
        if (!cell || (cell.unit && cell.unit !== unit)) return false;

        this.container.addChild(unit);
        cell.unit = unit;
        unit.position.set(cell.x, cell.y);
        return true;
    }

    getCellAtPoint(x, y) {
        const local = this.container.toLocal({ x, y });

        for (const cell of this.hexGrid.flat()) {
            const point = { x: local.x - cell.x, y: local.y - cell.y };
            if (cell.hex.containsPoint(point)) {
                return cell;
            }
        }

        return null;
    }

    isInRange(x, y) {
        return this.getCellAtPoint(x, y) !== null;
    }

    generateHexGrid(startX, startY, rows, cols) {
        const HEX_X = 160;
        const HEX_Y = 60;

        const cells = [];

        for (let row = 0; row < rows; row++) {
            cells[row] = [];

            for (let col = 0; col < cols; col++) {
                const x = startX + col * HEX_X + (row % 2) * (HEX_X / 2);
                const y = startY + row * HEX_Y;

                cells[row][col] = {
                    row,
                    col,
                    x,
                    y,
                    unit: null
                };
            }
        }

        this.cells = cells;
        console.log(this.cells)
    }

    setToNextEmptyCell(unit){
        for (const cell of this.hexGrid.flat()) {
            if (cell.unit == null){
                this.container.addChild(unit); // <-- ajout du fix : bon parent
                cell.unit = unit;
                unit.position.set(cell.x, cell.y);
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
        this.container.removeChild(unit);
    }

    getCellOfUnit(unit) {
        for (const cell of this.hexGrid.flat()) {
            if (cell.unit === unit) return cell;
        }
        return null;
    }

    moveUnit(unit, x, y) {
        const target = this.getClosestCell(x, y);
        if (!target) return false;

        // Case déjà occupée par une AUTRE unité → on refuse le déplacement
        if (target.unit && target.unit !== unit) return false;

        // On libère l'ancienne case de cette unité
        const current = this.getCellOfUnit(unit);
        if (current) current.unit = null;

        target.unit = unit;
        unit.position.set(target.x, target.y);
        return true;
    }
}
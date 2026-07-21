import {Container, Sprite} from "pixi.js";

export class Arena {

    constructor(app, sprite) {
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

        // Create and add a container to the stage
        const container = new Container();
        container.x = this.x;
        container.y = this.y;
        this.container = container;
        app.stage.getChildAt(0).addChild(container);

        //création du sprite pour l'arène
        const arena = new Sprite(sprite);
        arena.x = 0;
        arena.y = 0;

        arena.scale.set(0.5);
        arena.width = this.width;
        arena.height = this.height;
        container.addChild(arena);

    }

    getClosestCell(x,y) {
        let best = null;
        let bestDist = Infinity;

        for (const row of this.cells) {
            for (const cell of row) {
                const dx = x - cell.x - this.x;
                const dy = y - cell.y - this.y;

                const dist = dx * dx + dy * dy;

                if (dist < bestDist) {
                    bestDist = dist;
                    best = cell;
                }
            }
        }

        console.log(best)
        return best;
    }

    isInRange (x,y){
        return x > this.x + 100 &&
            x < this.x + this.width -100 &&
            y > this.y &&
            y < this.y + this.height -100
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
}
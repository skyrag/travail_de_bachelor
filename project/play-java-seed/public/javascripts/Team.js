import {Container} from "pixi.js";

export class Team {

    UNITWIDTH = 150;

    constructor(app) {
        this.app = app;
        this.units = [];
        this.bench = new Array(10);

        // valeur du placeholder mais aussi de la détection pour le drag
        this.x = 350;
        this.width = window.innerWidth -700;
        this.y = 4 * window.innerHeight / 6;
        this.height = 175;

        // cération du conteneur du bench
        // Create and add a container to the stage
        const container = new Container();
        container.x = this.x;
        container.y = this.y;
        this.container = container;
        app.stage.getChildAt(0).addChild(container);

    }

    addUnit(unit) {
        for (let i = 0 ; i < this.bench.length; i++){
            console.log(this.bench)
            if (!this.bench[i]){
                this.bench[i] = unit.createfighting(i * this.UNITWIDTH + this.UNITWIDTH/2, 175, this.container);
                return true;
            }
        }
        return false;
    }

    isInRange (x,y){
        return x > this.x &&
            x < this.x + this.width &&
            y > this.y &&
            y < this.y + this.height
    }

    removeIfBenched(unit){
       for (let i = 0 ; i < this.bench.length ; i++){
           if (this.bench[i] && this.bench[i].name === unit.name && this.bench[i].id === unit.id){
               this.bench[i] = null;
           }
       }
    }
}
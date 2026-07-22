import {Container, Graphics, Text} from "pixi.js";

export class Team {

    UNITWIDTH = 150;
    YAXIS = 175;

    constructor(app) {
        this.app = app;
        this.units = [];
        this.level = 1;
        this.exp = 0;
        this.gold = 30;
        this.bench = new Array(10).fill(null);

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
        container.zIndex = 10;
        app.stage.addChild(container);

    }

    setShop(shop){
        this.shop = shop
    }

    addUnitToBench(unit) {
        for (let i = 0 ; i < this.bench.length; i++){
            console.log(this.bench)
            if (!this.bench[i]){
                this.container.addChild(unit)
                this.bench[i] = unit;
                unit.position.set(i * this.UNITWIDTH + this.UNITWIDTH/2, this.YAXIS)
                return true;
            }
        }
        return false;
    }

    addUnit(unit){
        if (this.canAddOne()){
            this.units.push(unit)
        }
    }

    isInRange (x,y){
        console.log("on est la")
        return x > this.x &&
            x < this.x + this.width &&
            y > this.y &&
            y < this.y + this.height
    }

    removeIfBenched(unit){
        if(this.units.length + 1 <= this.level){
            for (let i = 0 ; i < this.bench.length ; i++){
                if (this.bench[i] === unit){
                    console.log("found")
                    this.bench[i] = null;
                    this.container.removeChild(unit)
                    this.units.push(unit)
                }
            }
        }
    }

    removeUnitFromEverywhere(unit) {
        this.removeFromBench(unit);
        this.removeUnit(unit); // celle qui gère this.units
    }

    removeFromBench(unit) {
        const index = this.bench.indexOf(unit);
        if (index !== -1) {
            this.bench[index] = null;
            return true;
        }
        return false;
    }

    removeUnit(unit){
        const index = this.units.indexOf(unit);

        if (index !== -1){
            this.units.splice(index, 1)
        }

    }

    canAddOne(){
        return this.units.length + 1 <= this.level;
    }

    canAddInBench() {
        return this.bench.some(slot => slot == null);
    }
    // XP nécessaire pour passer du niveau courant au suivant
    getExpNeeded(level = this.level) {
        return 2 + level * 2;
    }

    addGold(amount) {
        this.gold += amount;
        this.updateUI();
    }

    buyExperience() {
        if (this.gold < this.shop.BUY_XP_COST) return false;

        this.gold -= this.shop.BUY_XP_COST;
        this.exp += this.shop.BUY_XP_AMOUNT;
        this.checkLevelUp();
        this.shop.updateUI();
        return true;
    }

    checkLevelUp() {
        let needed = this.getExpNeeded();
        while (this.exp >= needed) {
            this.exp -= needed;
            this.level += 1;
            needed = this.getExpNeeded();
        }
    }
}
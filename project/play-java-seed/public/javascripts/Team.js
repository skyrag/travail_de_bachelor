import {Container, Graphics, Text} from "pixi.js";

export class Team {

    constructor(app, rect, layers, id, name) {
        this.app = app;
        this.id = id;
        this.name = name;
        this.units = [];
        this.level = 1;
        this.exp = 0;
        this.gold = 30;
        this.bench = new Array(10).fill(null);

        // valeur du placeholder mais aussi de la détection pour le drag
        this.x = rect.x;
        this.width = rect.width;
        this.y = rect.y;
        this.height = rect.height;

        this.UNITWIDTH = this.width/10;

        // cération du conteneur du bench
        // Create and add a container to the stage
        const container = new Container();
        container.x = this.x;
        container.y = this.y;
        this.container = container;
        layers.units.addChild(container);   // au lieu de app.stage

    }

    setShop(shop){
        this.shop = shop
    }

    setItemBox(box){
        this.items = box
    }

    addUnitToBench(unit) {
        for (let i = 0 ; i < this.bench.length; i++){
            console.log(this.bench)
            if (!this.bench[i]){
                this.container.addChild(unit.container)
                this.bench[i] = unit;
                unit.container.position.set(i * this.UNITWIDTH + this.UNITWIDTH/2, this.height)
                return true;
            }
        }
        return false;
    }

    addUnit(unit){
        if (this.canAddOne()){
            this.units[unit.id] = unit
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
        if(this.nbFieldedUnits()+ 1 <= this.level){
            for (let i = 0 ; i < this.bench.length ; i++){
                if (this.bench[i] === unit){
                    console.log("found")
                    this.bench[i] = null;
                    this.container.removeChild(unit.container)
                    this.units[unit.id] = unit
                }
            }
        }
    }

    removeUnitFromEverywhere(unit) {
        this.removeFromBench(unit);
        delete this.units[unit.id]
        this.container.removeChild(unit.container)
    }

    removeFromBench(unit) {
        const index = this.bench.indexOf(unit);
        if (index !== -1) {
            this.bench[index] = null;
            return true;
        }
        return false;
    }

    nbFieldedUnits(){
        return this.units.filter(Boolean).length;
    }

    canAddOne(){
        return this.nbFieldedUnits() + 1 <= this.level;
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
    }

    removeGold(amount) {
        this.gold -= amount;
    }

    buyExperience() {
        if (this.gold < this.shop.BUY_XP_COST) return false;

        //TODO faire un message au backend pour lui demander de buy de l'exp

        this.gold -= this.shop.BUY_XP_COST;
        this.exp += this.shop.BUY_XP_AMOUNT;
        this.checkLevelUp();
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

    sell(unit) {

        //TODO envoyer un message au backend pour prévenir de la vente

        this.addGold(unit.cost)
        this.removeUnitFromEverywhere(unit);

        for (let i = 0; i < unit.items.length; i++) {
            const item = unit.items.pop()
            unit.container.removeChild(item.sprite)
            this.items.addAnItem(item)
        }
    }

    clean() {
        this.units.forEach(unit => {
            this.removeUnitFromEverywhere(unit.fightingSprite)
        })
    }

    resetPositions(arena) { // en gros finir le combat attendre 10 tick puis lancer le clean et cette fonction afin que l'on ait de nouveau notre équipe
        this.units.forEach(unit => {
            console.log(unit)
            if (unit.fightingSprite != null){
                arena.setToCell(unit, unit.hex.x, unit.hex.y);
            }
        })
    }

    resetUnits() {
        this.units.forEach(unit => {
            unit.reset()
        })
    }

    addItem(item){
        this.items.addAnItem(item)
    }

    findBenchedUnitAt(globalX, globalY) {
        for (const unit of this.bench) {
            if (!unit) continue;
            const bounds = unit.container.getBounds(); // rectangle en coordonnées globales/écran
            console.log('test point', globalX, globalY);
            console.log(bounds); // regarde si x/y/width/height sont cohérents

            if (bounds.containsPoint(globalX, globalY)) {
                return unit;
            }
        }
        return null;
    }

    update(dt) {
        this.units.forEach(unit => {
            unit.update(dt)
        })
    }
}
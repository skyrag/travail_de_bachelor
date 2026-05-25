import {Sprite} from "pixi.js";
import {Item} from "./Item.js";

export class Unit {
    constructor(app, name, fightingSprite, shoppingSprite) {
        this.app = app;
        this.name = name;
        this.fightingSprite = fightingSprite;  // Store texture, not Sprite
        this.shoppingSprite = shoppingSprite;
        this.item = Item[3];
        this.trait = String[5];
    }

    buyUnit(x,y) {
        return new Unit(app, name, this.fightingSprite, this.shoppingSprite)
    }

    create(x,y){
        const shop = new Sprite(this.shoppingSprite);
        const fight = new Sprite(this.fightingSprite);
        fight.y = y;
        fight.x = x;
        fight.scale.set(0.5);
        fight.width = 100;
        fight.height = 100;
        this.app.stage.getChildAt(0).addChild(fight);
        return new Unit(this.app, this.name, this.fightingSprite, this.shoppingSprite);
    }

    addItem(item){
        this.item.add(item);
    }
}
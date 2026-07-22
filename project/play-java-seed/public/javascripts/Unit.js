import {Sprite} from "pixi.js";
import {Item} from "./Item.js";
import {Dragger} from "./Drag.js";

export class Unit {
    constructor(app, name, fightingSprite, shoppingSprite, dragger, id = 0) {
        this.app = app;
        this.id = id;
        this.dragger = dragger;
        this.name = name;
        this.fightingSprite = fightingSprite;  // Store texture, not Sprite
        this.shoppingSprite = shoppingSprite;
        this.item = Item[3];
        this.trait = String[5];
        this.getParent = null;
    }

    copy(){
        return new Unit(this.app, this.name, this.fightingSprite, this.shoppingSprite, this.dragger, ++this.id)
    }

    createShopping(x,y, onClick, container, width, height) {
        const shop = new Sprite(this.shoppingSprite);
        shop.x = x;
        shop.y = y;

        shop.scale.set(0.5);
        shop.width = width;
        shop.height = height;

        // Opt-in to interactivity
        shop.eventMode = 'static';

        // Shows hand cursor
        shop.cursor = 'pointer';

        // add the sprite to the container
        container.addChild(shop);
        this.getParent = container;

        return new Unit(this.app, this.name, this.fightingSprite, shop, this.dragger, this.id);
    }


    createfighting(x, y, container){
        const fighter = new Sprite(this.fightingSprite);
        fighter.y = y;
        fighter.x = x;
        fighter.scale.set(0.5);
        fighter.width = 100;
        fighter.height = 100;

        // Enable the bunny to be interactive... this will allow it to respond to mouse and touch events
        fighter.eventMode = 'static';

        // This button mode will mean the hand cursor appears when you roll over the bunny with your mouse
        fighter.cursor = 'pointer';

        // Center the bunny's anchor point
        fighter.anchor.set(0.5, 1);

        // Setup events for mouse + touch using the pointer events
        fighter.dragger = this.dragger;
        fighter.unit = this;
        fighter.on('pointerdown', fighter.dragger.onDragStart, fighter);

        fighter.zIndex = 10;
        container.addChild(fighter);
        this.getParent = container;

        return new Unit(this.app, this.name, fighter, this.shoppingSprite, this.dragger, this.id);
    }

    addItem(item){
        this.item.add(item);
    }

    removeParent(sprite) {
        this.getParent.removeChild(sprite)
        this.getParent.detachParent(sprite);
    }
}
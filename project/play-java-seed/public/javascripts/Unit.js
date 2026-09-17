import {Sprite} from "pixi.js";
import {Item} from "./Item.js";
import {Dragger} from "./Drag.js";

export class Unit {
    constructor(app, name, fightingSprite, shoppingSprite, dragger, id = 0, maxHealth, maxMana, startingMana, basicDamage, attackSpeed, armor, magicResist, range, abilityName, abilityDescription, rarity, cost  ) {
        this.app = app;
        this.id = id;
        this.dragger = dragger;
        this.name = name;
        this.fightingSprite = fightingSprite;  // Store texture, not Sprite
        this.shoppingSprite = shoppingSprite;

        //static stats
        this.trait = String[5];
        this.maxHealth = maxHealth;
        this.health = this.maxHealth;
        this.maxMana = maxMana;
        this.startingMana = startingMana;
        this.basicDamage = basicDamage;
        this.attackDamage = 0;
        this.abilityPower = 0;
        this.attackSpeed = attackSpeed;
        this.armor = armor;
        this.magicResist = magicResist;
        this.range = range;

        this.abilityName = abilityName
        this.abilityDescription = abilityDescription

        this.rarity = rarity;
        this.cost = cost;


        //changing parameters
        this.item = Item[3];
        this.getParent = null;
    }

    copy(id){
        return new Unit(this.app, this.name, this.fightingSprite, this.shoppingSprite, this.dragger, id, this.maxHealth, this.maxMana, this.startingMana, this.basicDamage, this.attackSpeed, this.armor, this.magicResist, this.range, this.abilityName, this.abilityDescription, this.rarity, this.cost)
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

        return new Unit(this.app, this.name, this.fightingSprite, shop, this.dragger, this.id, this.maxHealth, this.maxMana, this.startingMana, this.basicDamage, this.attackSpeed, this.armor, this.magicResist, this.range, this.abilityName, this.abilityDescription, this.rarity, this.cost)

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

        return new Unit(this.app, this.name, fighter, this.shoppingSprite, this.dragger, this.id, this.maxHealth, this.maxMana, this.startingMana, this.basicDamage, this.attackSpeed, this.armor, this.magicResist, this.range, this.abilityName, this.abilityDescription, this.rarity, this.cost)

    }

    addItem(item){
        this.item.add(item);
    }

    removeParent(sprite) {
        this.getParent.removeChild(sprite)
        this.getParent.detachParent(sprite);
    }

}
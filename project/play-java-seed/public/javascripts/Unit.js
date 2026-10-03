import {Sprite, Container, Graphics, Text} from "pixi.js";
import {Item} from "./Item.js";
import {Dragger} from "./Drag.js";

export class Unit {

    constructor(app, layers, name, fightingSprite, shoppingSprite, dragger, id = 0, maxHealth, maxMana, startingMana, basicDamage, attackSpeed, armor, magicResist, range, abilityName, abilityDescription, rarity, cost  ) {
        this.app = app;
        this.fightingSprite = fightingSprite;
        this.shoppingSprite = shoppingSprite;
        this.dragger = dragger;
        this.id = id;
        this.name = name;


        //static stats
        this.trait = String[5];
        this.maxHealth = maxHealth;
        this.health = this.maxHealth;
        this.maxMana = maxMana;
        this.startingMana = startingMana;
        this.currentMana = startingMana;
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
        this.items = [];
        this.getParent = null;

        this.hex = {x: -1, y: -1}

        const unit = new Container();
        this.container = unit;
        this.layers = layers;
        layers.units.addChild(unit);

        this.ISONARENA = false
        this.ISFIGHTING = false

        // Barre de pv
        this.healthBar = new Graphics();
        this.healthBar.eventMode = 'none';
        this.healthShown = 1;
        this.healthDrawn = -1;
        // Barre de mana
        this.manaBar = new Graphics();
        this.manaBar.eventMode = 'none';
        this.manaShown = this.startingMana / this.maxMana; // ou 0
        this.manaDrawn = -1;

        this.UNITWIDTH = 100
        this.MAXITEM = 3

        this.itemRect = {x: -this.UNITWIDTH / 2, y: -this.UNITWIDTH + 8, width: 15, height: 15}
    }

    copy(id){
        return new Unit(this.app, this.layers, this.name, this.fightingSprite, this.shoppingSprite, this.dragger, id, this.maxHealth, this.maxMana, this.startingMana, this.basicDamage, this.attackSpeed, this.armor, this.magicResist, this.range, this.abilityName, this.abilityDescription, this.rarity, this.cost)
    }

    createShopping(x, y, onClick, container, width, height) {
        const shop = new Sprite(this.shoppingSprite);

        shop.scale.set(0.5);
        shop.width = width;
        shop.height = height;
        shop.eventMode = 'static';
        shop.cursor = 'pointer';

        this.shoppingSprite = shop

        // add the sprite to the container
        container.addChild(this.container);
        this.container.addChild(shop)
        this.getParent = container;

        this.container.position.x = x
        this.container.position.y = y

    }


    createfighting(x, y, container  , isEnnemy = false){

        const fighter = new Sprite(this.fightingSprite);
        fighter.y = y;
        fighter.x = x;
        fighter.scale.set(0.5);
        fighter.width = this.UNITWIDTH;
        fighter.height = this.UNITWIDTH;

        // Enable the bunny to be interactive... this will allow it to respond to mouse and touch events
        fighter.eventMode = 'static';
        fighter.cursor = 'pointer';
        fighter.anchor.set(0.5, 1);

        //we change the direction of ennemy because they should watch left
        if(isEnnemy) {
            fighter.scale.x *= -1;
            this.isEnemy = true
        } else {
            this.isEnemy = false
        }

        // Setup events for mouse + touch using the pointer events
        fighter.dragger = this.dragger;
        fighter.unit = this;
        fighter.on('pointerdown', fighter.dragger.onDragStart, fighter);

        this.fightingSprite = fighter

        container.addChild(this.container);
        this.container.addChild(fighter)
        this.getParent = container;
    }

    removeSprite() {
        if (this.fightingSprite != null) {
            this.getParent.container.removeChild(this.fightingSprite)
        }
    }
    
    takeDamage(damage) {
        this.health -= damage;
    }

    isDead(){
        return this.health <= 0;
    }

    cast(){
        this.currentMana = this.startingMana
    }

    manaUp() {
        this.currentMana = Math.min(this.currentMana + 5, this.maxMana)
    }

    reset() {
        //TODO a voir si il n'y a pas d'autre chose a reset
        this.health = this.maxHealth
        this.currentMana = this.startingMana
        this.endFight()

    }

    setOnArena(){
        if (this.ISONARENA) return
        this.container.addChild(this.healthBar);
        this.container.addChild(this.manaBar)
        this.ISONARENA = true
    }

    startFight() {
        this.ISFIGHTING = true
    }

    endFight() {
        this.ISFIGHTING = false;
    }

    setOffArena(){
        if (this.ISONARENA) {
            this.container.removeChild(this.healthBar);
            this.container.removeChild(this.manaBar)
            this.ISONARENA = false
        }
    }

    canAddOneItem(){
        return this.items.length < this.MAXITEM
    }

    addItem(item){
        if (this.canAddOneItem()) {
            item.sprite.position.set(this.itemRect.x, this.itemRect.y + this.items.length * this.itemRect.height)
            item.sprite.width = this.itemRect.width
            item.sprite.height = this.itemRect.height
            this.container.addChild(item.sprite)
            item.apply(this)
            this.items.push(item)
        } else {
            console.log("on peut pas ajouter d'objets a cette unité")
        }
    }

    showStats() {
        // ferme un panneau déjà ouvert (le sien ou celui d'une autre unité)
        Unit.closeActiveStatsPanel();

        const PANEL_W = 300;
        const PANEL_H = 380;
        const PADDING = 20;

        // conteneur racine : pas d'offset, couvre tout l'écran (pour l'overlay)
        const root = new Container();

        // overlay plein écran, capte le clic "en dehors" pour fermer
        const overlay = new Graphics()
            .rect(0, 0, this.app.screen.width, this.app.screen.height)
            .fill({ color: 0x000000, alpha: 0.001 });
        overlay.eventMode = 'static';
        overlay.on('pointerdown', () => this.closeStats());

        // panneau centré, positionné en absolu dans root
        const bg = new Graphics()
            .roundRect(0, 0, PANEL_W, PANEL_H, 12)
            .fill(0x1e1e1e)
            .stroke({ width: 2, color: 0xffffff, alpha: 0.3 });
        bg.x = (this.app.screen.width - PANEL_W) / 2;
        bg.y = (this.app.screen.height - PANEL_H) / 2;
        bg.eventMode = 'static';
        bg.on('pointerdown', (e) => e.stopPropagation()); // empêche de fermer en cliquant sur le panneau

        const title = new Text({
            text: this.name,
            style: { fill: 0xffffff, fontSize: 24, fontWeight: 'bold' }
        });
        title.x = PADDING;
        title.y = PADDING;

        const statsLines = [
            `PV : ${Math.round(this.health)} / ${this.maxHealth}`,
            `Mana : ${Math.round(this.currentMana)} / ${this.maxMana}`,
            `Dégâts : ${this.basicDamage * this.attackDamage}`,
            `AbilityPower : ${this.abilityPower}`,
            `Vitesse d'attaque : ${this.attackSpeed}`,
            `Armure : ${this.armor}`,
            `Résistance magique : ${this.magicResist}`,
            `Portée : ${this.range}`,
            `Rareté : ${this.rarity}`,
            `Coût : ${this.cost}`,
            '',
            `Capacité : ${this.abilityName}`,
        ];

        const statsText = new Text({
            text: statsLines.join('\n'),
            style: {
                fill: 0xdddddd,
                fontSize: 16,
                lineHeight: 22,
                wordWrap: true,
                wordWrapWidth: PANEL_W - PADDING * 2,
            }
        });
        statsText.x = PADDING;
        statsText.y = title.y + title.height + 16;

        const descText = new Text({
            text: this.abilityDescription ?? '',
            style: {
                fill: 0xaaaaaa,
                fontSize: 13,
                wordWrap: true,
                wordWrapWidth: PANEL_W - PADDING * 2,
            }
        });
        descText.x = PADDING;
        descText.y = statsText.y + statsText.height + 10;

        const closeBtn = new Text({
            text: '✕',
            style: { fill: 0xffffff, fontSize: 20 }
        });
        closeBtn.x = PANEL_W - PADDING - closeBtn.width;
        closeBtn.y = PADDING - 4;
        closeBtn.eventMode = 'static';
        closeBtn.cursor = 'pointer';
        closeBtn.on('pointerdown', (e) => {
            e.stopPropagation();
            this.closeStats();
        });

        bg.addChild(title, statsText, descText, closeBtn);
        root.addChild(overlay, bg);

        root.x = 700
        root.y = 100
        this.layers.ui.addChild(root);
        this.statsPanel = root;
        Unit.activeStatsPanel = this;
    }

    closeStats() {
        if (this.statsPanel) {
            this.layers.ui.removeChild(this.statsPanel);
            this.statsPanel.destroy({ children: true });
            this.statsPanel = null;
        }
        if (Unit.activeStatsPanel === this) {
            Unit.activeStatsPanel = null;
        }
    }

    static closeActiveStatsPanel() {
        if (Unit.activeStatsPanel) {
            Unit.activeStatsPanel.closeStats();
        }
    }

    update(dt) {

        if (!this.ISFIGHTING) {
            this.healthShown = 1;
            this.healthDrawn = -1;

            this.health = this.maxHealth;
            this.currentMana = this.startingMana;

            this.manaShown = this.startingMana / this.maxMana; // ou 0
            this.manaDrawn = -1;
        } else {
            const ratioh = Math.min(this.health / this.maxHealth, 1);
            this.healthShown += (ratioh - this.healthShown) * Math.min(1, 0.15 * dt);  // lerp
            if (Math.abs(ratioh - this.healthShown) < 0.001) this.healthShown = ratioh;

            const ratiom = Math.min(this.currentMana / this.maxMana, 1);
            this.manaShown += (ratiom - this.manaShown) * Math.min(1, 0.15 * dt);  // lerp
            if (Math.abs(ratiom - this.manaShown) < 0.001) this.manaShown = ratiom;
        }


        if (this.healthShown !== this.healthDrawn) {         // redessine seulement si ça bouge
            this.healthBar.clear()
                .rect(-this.UNITWIDTH / 2, -this.UNITWIDTH, this.UNITWIDTH * this.healthShown, 4)
                .fill(0xff0000);
            this.healthDrawn = this.healthShown;
        }


        if (this.manaShown !== this.manaDrawn) {         // redessine seulement si ça bouge
            this.manaBar.clear()
                .rect(-this.UNITWIDTH / 2, -this.UNITWIDTH + 4, this.UNITWIDTH * this.manaShown, 4)
                .fill(0x0000ff);
            this.manaDrawn = this.manaShown;
        }
    }



}
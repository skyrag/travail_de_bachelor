import {Sprite, Container, Graphics, Text} from "pixi.js";
import {Item} from "./Item.js";
import {Dragger} from "./Drag.js";

export class Unit {
    static activeUnits = []; // toutes les unités actuellement "vivantes" en jeu, pour pouvoir toutes les basculer d'un coup

    constructor(app, fightingSprite, shoppingSprite, dragger, id, name, cost, rarity,
                abilityName, abilityDescription, maxHealth, maxMana, startingMana,
                baseAttack, attackDamage, abilityPower, attackSpeed, armor, magicResist, range) {
        this.app = app;
        this.fightingSprite = fightingSprite;
        this.shoppingSprite = shoppingSprite;
        this.dragger = dragger;
        this.id = id;
        this.name = name;
        this.cost = cost;
        this.rarity = rarity;
        this.abilityName = abilityName;
        this.abilityDescription = abilityDescription;
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth; // <-- PV actuels, initialisés au max
        this.maxMana = maxMana;
        this.startingMana = startingMana;
        this.baseAttack = baseAttack;
        this.attackDamage = attackDamage;
        this.abilityPower = abilityPower;
        this.attackSpeed = attackSpeed;
        this.armor = armor;
        this.magicResist = magicResist;
        this.range = range;

        this.item = [];
        this.trait = [];
        this.getParent = null;
        this.tooltip = null;
        this.healthBar = null; // référence au container de la barre de vie
    }

    clone(overrides = {}) {
        const data = {
            app: this.app, fightingSprite: this.fightingSprite, shoppingSprite: this.shoppingSprite,
            dragger: this.dragger, id: this.id, name: this.name, cost: this.cost, rarity: this.rarity,
            abilityName: this.abilityName, abilityDescription: this.abilityDescription,
            maxHealth: this.maxHealth, maxMana: this.maxMana, startingMana: this.startingMana,
            baseAttack: this.baseAttack, attackDamage: this.attackDamage, abilityPower: this.abilityPower,
            attackSpeed: this.attackSpeed, armor: this.armor, magicResist: this.magicResist, range: this.range,
            ...overrides
        };

        return new Unit(
            data.app, data.fightingSprite, data.shoppingSprite, data.dragger, data.id,
            data.name, data.cost, data.rarity, data.abilityName, data.abilityDescription,
            data.maxHealth, data.maxMana, data.startingMana, data.baseAttack,
            data.attackDamage, data.abilityPower, data.attackSpeed, data.armor,
            data.magicResist, data.range
        );
    }

    copy(newId) {
        return this.clone({ id: newId ?? this.id + 1 });
    }

    createShopping(x, y, onClick, container, width, height) {
        const shop = new Sprite(this.shoppingSprite);
        shop.x = x;
        shop.y = y;
        shop.scale.set(0.5);
        shop.width = width;
        shop.height = height;
        shop.eventMode = 'static';
        shop.cursor = 'pointer';

        container.addChild(shop);
        this.getParent = container;

        const newUnit = this.clone({ shoppingSprite: shop });
        this.attachTooltipEvents(shop, newUnit);

        return newUnit;
    }

    createfighting(x, y, container) {
        const fighter = new Sprite(this.fightingSprite);
        fighter.y = y;
        fighter.x = x;
        fighter.scale.set(0.5);
        fighter.width = 100;
        fighter.height = 100;
        fighter.eventMode = 'static';
        fighter.cursor = 'pointer';
        fighter.anchor.set(0.5, 1);

        fighter.dragger = this.dragger;
        fighter.on('pointerdown', fighter.dragger.onDragStart, fighter);
        fighter.zIndex = 10;

        container.addChild(fighter);
        this.getParent = container;

        const newUnit = this.clone({ fightingSprite: fighter });
        fighter.unit = newUnit;
        this.attachTooltipEvents(fighter, newUnit);
        newUnit.createHealthBar(fighter, container);

        return newUnit;
    }

    addItem(item) {
        this.item.push(item);
    }

    removeParent(sprite) {
        this.getParent.removeChild(sprite);
    }

    // --- Tooltip (inchangé) ---

    attachTooltipEvents(sprite, unit) {
        sprite.on('pointerover', () => unit.showTooltip(sprite));
        sprite.on('pointerout', () => unit.hideTooltip());
    }

    showTooltip(sprite) {
        this.hideTooltip();

        const tooltip = new Container();
        tooltip.eventMode = 'none';

        const lines = [
            `${this.name} (${this.rarity ?? '?'}) — ${this.cost}💰`,
            `${this.abilityName ?? ''}`,
            `PV: ${this.currentHealth}/${this.maxHealth}   Mana: ${this.startingMana}/${this.maxMana}`,
            `AD: ${this.attackDamage}   AP: ${this.abilityPower}`,
            `Vitesse d'attaque: ${this.attackSpeed}`,
            `Armure: ${this.armor}   RM: ${this.magicResist}`,
            `Portée: ${this.range}`
        ];

        const text = new Text({
            text: lines.join('\n'),
            style: { fill: 0xffffff, fontSize: 14, lineHeight: 18 }
        });
        text.x = 8;
        text.y = 8;

        const bg = new Graphics()
            .rect(0, 0, text.width + 16, text.height + 16)
            .fill({ color: 0x000000, alpha: 0.85 })
            .stroke({ width: 1, color: 0xffffff });

        tooltip.addChild(bg, text);

        const globalPos = sprite.getGlobalPosition();
        tooltip.x = globalPos.x;
        tooltip.y = globalPos.y - tooltip.height - 10;

        this.app.stage.addChild(tooltip);
        this.tooltip = tooltip;
    }

    hideTooltip() {
        if (this.tooltip) {
            this.tooltip.parent?.removeChild(this.tooltip);
            this.tooltip = null;
        }
    }

    // --- Barre de vie ---

    createHealthBar(sprite, container) {
        const barWidth = 60;
        const barHeight = 8;

        const healthBar = new Container();
        healthBar.eventMode = 'none';
        healthBar.visible = false; // cachée par défaut, affichée seulement en combat

        const bg = new Graphics()
            .rect(0, 0, barWidth, barHeight)
            .fill(0x333333)
            .stroke({ width: 1, color: 0x000000 });

        const fill = new Graphics()
            .rect(0, 0, barWidth, barHeight)
            .fill(0x2ecc71);

        healthBar.addChild(bg, fill);
        healthBar.fillBar = fill;
        healthBar.barWidth = barWidth;
        healthBar.barHeight = barHeight;

        // positionne la barre au-dessus du sprite (sprite.anchor = 0.5, 1, donc le sprite "monte" depuis y=0)
        healthBar.x = sprite.x - barWidth / 2;
        healthBar.y = sprite.y - sprite.height - 12;

        container.addChild(healthBar);
        this.healthBar = healthBar;

        Unit.activeUnits.push(this);
    }

    updateHealthBar() {
        if (!this.healthBar) return;

        const ratio = Math.max(this.currentHealth / this.maxHealth, 0);
        const fill = this.healthBar.fillBar;

        fill.clear();
        fill.rect(0, 0, this.healthBar.barWidth * ratio, this.healthBar.barHeight);

        // couleur qui vire au rouge quand les PV baissent
        const color = ratio > 0.5 ? 0x2ecc71 : ratio > 0.2 ? 0xf39c12 : 0xe74c3c;
        fill.fill(color);
    }

    takeDamage(amount) {
        this.currentHealth = Math.max(this.currentHealth - amount, 0);
        this.updateHealthBar();

        if (this.currentHealth <= 0) {
            this.onDeath();
        }
    }

    heal(amount) {
        this.currentHealth = Math.min(this.currentHealth + amount, this.maxHealth);
        this.updateHealthBar();
    }

    resetHealth() {
        this.currentHealth = this.maxHealth;
        this.updateHealthBar();
    }

    onDeath() {
        // à adapter selon ta logique (retrait de l'arène, animation, etc.)
        console.log(`${this.name} est mort`);
    }

    removeFromActiveUnits() {
        const index = Unit.activeUnits.indexOf(this);
        if (index !== -1) Unit.activeUnits.splice(index, 1);
    }

    // --- Bascule d'affichage combat / hors combat ---

    static startCombat() {
        for (const unit of Unit.activeUnits) {
            unit.resetHealth(); // remet tout le monde à fond avant le combat
            if (unit.healthBar) unit.healthBar.visible = true;
        }
    }

    static endCombat() {
        for (const unit of Unit.activeUnits) {
            if (unit.healthBar) unit.healthBar.visible = false;
        }
    }
}
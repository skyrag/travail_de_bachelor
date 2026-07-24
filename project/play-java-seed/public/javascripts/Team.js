import {Container, Graphics, Text} from "pixi.js";

export class Team {

    UNITWIDTH = 150;
    YAXIS = 175;

    constructor(app) {
        this.app = app;
        this.units = [];       // sprites sur le plateau
        this.level = 1;
        this.exp = 0;
        this.gold = 30;
        this.bench = new Array(10).fill(null);

        this.x = 350;
        this.width = window.innerWidth - 700;
        this.y = 4 * window.innerHeight / 6;
        this.height = 175;

        const container = new Container();
        container.x = this.x;
        container.y = this.y;
        this.container = container;
        container.zIndex = 10;
        app.stage.addChild(container);
    }

    setShop(shop) {
        this.shop = shop;
    }

    // --- Recherche d'une unité (bench + plateau) par son instanceId serveur ---

    findUnitById(instanceId) {
        const onBoard = this.units.find(sprite => sprite.unitData?.instanceId === instanceId);
        if (onBoard) return { sprite: onBoard, location: "board" };

        const benchIndex = this.bench.findIndex(sprite => sprite?.unitData?.instanceId === instanceId);
        if (benchIndex !== -1) return { sprite: this.bench[benchIndex], location: "bench", benchIndex };

        return null;
    }

    // --- Or ---

    spendGold(amount) {
        this.gold -= amount;
        this.shop?.updateUI();
    }

    refundGold(amount) {
        this.gold += amount;
        this.shop?.updateUI();
    }

    // --- Achat (appelé de manière optimiste par Shop) ---

    applyBuyUnit(unit) {
        this.spendGold(unit.cost);

        const newUnit = unit.createfighting(0, 0, this.container);
        // id temporaire tant que le serveur n'a pas confirmé/renvoyé un vrai id d'instance
        newUnit.instanceId = `tmp-${Date.now()}-${Math.random().toString(36).slice(2)}`;

        if (this.addUnitToBench(newUnit.fightingSprite)) {
            return newUnit;
        }
        if (this.canAddOne()) {
            this.addUnit(newUnit.fightingSprite);
            newUnit.fightingSprite.unitData = newUnit;
        }
        return newUnit;
    }

    rollbackBuyUnit(newUnit) {
        this.refundGold(newUnit.cost);
        this.removeUnitFromEverywhere(newUnit.fightingSprite);
        newUnit.fightingSprite.parent?.removeChild(newUnit.fightingSprite);
    }

    // --- Vente ---

    applySellUnit(instanceId) {
        const found = this.findUnitById(instanceId);
        if (!found) return null;

        const { sprite } = found;
        const sellValue = sprite.unitData?.cost ?? 0;

        this.refundGold(sellValue);
        this.removeUnitFromEverywhere(sprite);
        sprite.parent?.removeChild(sprite);

        return { sprite, sellValue, wasOnBoard: found.location === "board" };
    }

    rollbackSellUnit(snapshot) {
        if (!snapshot) return;
        this.spendGold(snapshot.sellValue);
        this.container.addChild(snapshot.sprite);
        if (snapshot.wasOnBoard) {
            this.addUnit(snapshot.sprite);
        } else {
            this.addUnitToBench(snapshot.sprite);
        }
    }

    // --- Déplacement ---

    applyMoveUnit(instanceId, x, y) {
        const found = this.findUnitById(instanceId);
        if (!found) return null;

        const previous = { x: found.sprite.x, y: found.sprite.y, location: found.location, benchIndex: found.benchIndex };

        if (found.location === "bench" && this.canAddOne()) {
            this.bench[found.benchIndex] = null;
            this.container.removeChild(found.sprite);
            this.container.addChild(found.sprite);
            this.addUnit(found.sprite);
        }

        found.sprite.position.set(x, y);
        return { sprite: found.sprite, previous };
    }

    rollbackMoveUnit(snapshot) {
        if (!snapshot) return;
        snapshot.sprite.position.set(snapshot.previous.x, snapshot.previous.y);
        if (snapshot.previous.location === "bench") {
            this.removeUnit(snapshot.sprite);
            this.bench[snapshot.previous.benchIndex] = snapshot.sprite;
        }
    }

    // --- Objet donné à une unité ---

    applyGiveItem(unitInstanceId, item) {
        const found = this.findUnitById(unitInstanceId);
        if (!found) return null;

        found.sprite.unitData?.addItem(item);
        return { sprite: found.sprite, item };
    }

    rollbackGiveItem(snapshot) {
        if (!snapshot) return;
        const unitData = snapshot.sprite.unitData;
        if (!unitData) return;
        const index = unitData.item.indexOf(snapshot.item);
        if (index !== -1) unitData.item.splice(index, 1);
    }

    // --- Existant, inchangé ---

    addUnitToBench(unit) {
        for (let i = 0; i < this.bench.length; i++) {
            if (!this.bench[i]) {
                this.container.addChild(unit);
                this.bench[i] = unit;
                unit.position.set(i * this.UNITWIDTH + this.UNITWIDTH / 2, this.YAXIS);
                return true;
            }
        }
        return false;
    }

    addUnit(unit) {
        if (this.canAddOne()) {
            this.units.push(unit);
        }
    }

    isInRange(x, y) {
        return x > this.x && x < this.x + this.width && y > this.y && y < this.y + this.height;
    }

    removeIfBenched(unit) {
        if (this.units.length + 1 <= this.level) {
            for (let i = 0; i < this.bench.length; i++) {
                if (this.bench[i] === unit) {
                    this.bench[i] = null;
                    this.container.removeChild(unit);
                    this.units.push(unit);
                }
            }
        }
    }

    removeUnitFromEverywhere(unit) {
        this.removeFromBench(unit);
        this.removeUnit(unit);
    }

    removeFromBench(unit) {
        const index = this.bench.indexOf(unit);
        if (index !== -1) {
            this.bench[index] = null;
            return true;
        }
        return false;
    }

    removeUnit(unit) {
        const index = this.units.indexOf(unit);
        if (index !== -1) {
            this.units.splice(index, 1);
        }
    }

    canAddOne() {
        return this.units.length + 1 <= this.level;
    }

    canAddInBench() {
        return this.bench.some(slot => slot == null);
    }

    getExpNeeded(level = this.level) {
        return 2 + level * 2;
    }

    addGold(amount) {
        this.gold += amount;
        this.shop?.updateUI();
    }

    buyExperience() {
        if (!this.shop || this.gold < this.shop.BUY_XP_COST) return false;

        // apply optimiste ; rollback si le serveur refuse
        this.spendGold(this.shop.BUY_XP_COST);
        const previousExp = this.exp;
        const previousLevel = this.level;

        this.exp += this.shop.BUY_XP_AMOUNT;
        this.checkLevelUp();
        this.shop.updateUI();

        this.shop.ws.buyExp({
            apply: () => {}, // déjà appliqué ci-dessus (send() ré-appellerait apply, donc on laisse vide et applique avant l'appel)
            rollback: () => {
                this.refundGold(this.shop.BUY_XP_COST);
                this.exp = previousExp;
                this.level = previousLevel;
                this.shop.updateUI();
            }
        }).catch(() => {}); // rollback déjà géré par le callback

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
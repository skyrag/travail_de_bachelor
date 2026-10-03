import {Unit} from "./Unit.js";
import {Container, Graphics, Sprite, Text} from "pixi.js";
import {GAME_H} from "./Layers.js";

export class Shop {

    SHOPITEMWIDTH = 180;
    SHOPITEMHIEGHT = 180;

    constructor(app,layers,  team, arena, sprite, list, rect, ws) {
        this.app = app;
        this.team = team;
        team.setShop(this);
        this.units = [];
        this.arena = arena;
        this.ws = ws;
        this.BUY_XP_COST = 4;
        this.BUY_XP_AMOUNT = 4;

        this.SHOPWIDTH = rect.width
        this.SHOPHEIGHT = rect.height
        this.x = rect.x
        this.y = rect.y

        this.SHOPITEMWIDTH = this.SHOPWIDTH / 8
        this.SHOPITEMHIEGHT = this.SHOPHEIGHT;


        // création du conteneur du shop
        // Create and add a container to the stage
        const container = new Container();
        container.x = this.x;
        container.y = this.y;
        this.container = container;
        layers.ui.addChild(container);   // au lieu de app.stage

        this.createUI(sprite, list);
    }

    resetShop(units){

        if (this.team.gold < 2) {
            console.log("Pas de gold, reroll impossible");
            return; // on ne touche à rien, pas de sprite retiré, pas d'unité créée
        }

        //TODO envoyer une message backend pour le reroll et chopper les unités

        let list = []
        for (const unit of units){
            list.push(unit.copy(units.length + unit.id))
        }


        this.team.removeGold(2);

        if (this.units.length >= 1){
            for (let unit of this.units){
                if (unit) {
                    this.container.removeChild(unit.container);
                    this.units[unit.id] = undefined
                }
            }
        }

        this.createShop(list)
    }

    createShop(units){
        let i = 2;
        for (let unit of units){
            // creating the sprite for the unit
            unit.createShopping(i * this.SHOPITEMWIDTH + i * (this.SHOPITEMWIDTH / 6), 0, (chosen) => this.onUnitClick(chosen), this.container, this.SHOPITEMWIDTH, this.SHOPITEMHIEGHT);
            unit.shoppingSprite.on('pointerdown', () => {
                console.log("it has been clicked")
                this.onUnitClick(unit)
            });
            this.units[unit.id] = unit;
            i++;
        }
    }

    onUnitClick(unit) {
        const benchHasRoom = this.team.canAddInBench();
        const teamHasRoom = this.team.canAddOne();

        if (!benchHasRoom && !teamHasRoom) {
            console.log("Pas de place (banc et équipe pleins), achat annulé");
            return; // on ne touche à rien, pas de sprite retiré, pas d'unité créée
        }

        if (this.team.gold < unit.cost) {
            console.log("Pas assez d'argent, achat annulé");
            return; // on ne touche à rien, pas de sprite retiré, pas d'unité créée
        }

        //TODO envoyer une message au backend pour lui prévenir que l'on acheté l'unité et rollback si nécessaire

        this.team.removeGold(unit.cost);

        unit.createfighting(0, 0, this.team.container);
        this.container.removeChild(unit.container);
        unit.container.removeChild(unit.shoppingSprite)
        this.units[unit.id] = undefined


        if (this.team.addUnitToBench(unit)) {
            return;
        }
        if (teamHasRoom) {
            this.arena.setToNextEmptyCell(unit);
            this.team.addUnit(unit);
            unit.fightingSprite.unitData = unit
        }
    }

    createButton(sprite, list, container) {
        const reroll = new Sprite(sprite);
        reroll.x = 60 + this.SHOPITEMWIDTH;
        reroll.y = 0;
        reroll.scale.set(0.5);
        reroll.width = this.SHOPITEMWIDTH;
        reroll.height = this.SHOPITEMHIEGHT/2;

        // Opt-in to interactivity
        reroll.eventMode = 'static';
        reroll.cursor = 'pointer';
        reroll.on('pointerdown', () => this.onReroll());
        container.addChild(reroll);
    }

    onButtonClick(list) {
        this.resetShop(list);
    }

    // --- UI ---

    createUI(sprite, list) {
        const uiContainer = new Container();// au-dessus du banc, à ajuster selon ton layout
        this.uiContainer = uiContainer;
        this.container.addChild(uiContainer);

        this.createButton(sprite, list, uiContainer)

        // Texte de l'or
        this.goldText = new Text({
            text: `Or : 0`,
            style: { fill: 0xffffff, fontSize: 50, fontWeight: 'bold' }
        });
        this.goldText.eventMode = 'none';
        this.goldText.x = 60 + this.SHOPITEMWIDTH;
        this.goldText.y = this.SHOPITEMHIEGHT/2  ;
        uiContainer.addChild(this.goldText);

        // Texte du niveau
        this.levelText = new Text({
            text: `Niveau 1`,
            style: { fill: 0xffffff, fontSize: 20, fontWeight: 'bold' }
        });
        this.levelText.eventMode = 'none';
        this.levelText.x = 60;
        this.levelText.y = 30;
        uiContainer.addChild(this.levelText);

        // Barre d'XP (fond)
        this.xpBarWidth = 200;
        this.xpBarHeight = 16;

        this.xpBarBg = new Graphics()
            .rect(60, this.levelText.y + 30, this.xpBarWidth, this.xpBarHeight)
            .fill(0x333333)
            .stroke({ width: 2, color: 0x000000 });
        this.xpBarBg.eventMode = 'none';
        uiContainer.addChild(this.xpBarBg);

        // Barre d'XP (remplissage) — largeur ajustée dynamiquement dans updateUI
        this.xpBarFill = new Graphics();
        this.xpBarFill.eventMode = 'none';
        uiContainer.addChild(this.xpBarFill);

        // Bouton "Acheter XP"
        const buyButton = new Graphics()
            .rect(60, this.levelText.y + 60, 120, 36)
            .fill(0x2ecc71)
            .stroke({ width: 2, color: 0x000000 });
        buyButton.eventMode = 'static';
        buyButton.cursor = 'pointer';
        buyButton.on('pointerdown', () => this.team.buyExperience());
        uiContainer.addChild(buyButton);

        const buyText = new Text({
            text: `Acheter XP (${this.BUY_XP_COST}💰)`,
            style: { fill: 0xffffff, fontSize: 14 }
        });
        buyText.eventMode = 'none';
        buyText.x = 64;
        buyText.y = this.levelText.y + 68;
        uiContainer.addChild(buyText);

        //TODO a enlever lorsque l'on aura les emssage
        let list2 = []
        for (const unit of list){
            list2.push(unit.copy(list.length + unit.id))
        }
        //

        this.createShop(list2)

        this._last = { gold: null, level: null };
        this.xpShown = 0;
        this.xpDrawn = -1;
        this.update(0);
    }

    update(dt) {
        const { gold, level, exp } = this.team;

        if (gold !== this._last.gold) {
            this.goldText.text = `Or : ${gold}`;
            this._last.gold = gold;
        }

        if (level !== this._last.level) {
            this.levelText.text = `Niveau ${level}`;
            this._last.level = level;
            this.xpShown = 0;                        // snap au level up
        }

        const ratio = Math.min(exp / this.team.getExpNeeded(), 1);
        this.xpShown += (ratio - this.xpShown) * Math.min(1, 0.15 * dt);  // lerp
        if (Math.abs(ratio - this.xpShown) < 0.001) this.xpShown = ratio;

        if (this.xpShown !== this.xpDrawn) {         // redessine seulement si ça bouge
            this.xpBarFill.clear()
                .rect(60, this.levelText.y + 30, this.xpBarWidth * this.xpShown, this.xpBarHeight)
                .fill(0x3498db);
            this.xpDrawn = this.xpShown;
        }
    }
}
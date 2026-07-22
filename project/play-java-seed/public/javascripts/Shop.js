import {Unit} from "./Unit.js";
import {Container, Graphics, Sprite, Text} from "pixi.js";

export class Shop {

    SHOPITEMWIDTH = 250;
    SHOPITEMHIEGHT = 190;

    constructor(app, team, arena, sprite, list) {
        this.app = app;
        this.team = team;
        team.setShop(this)
        this.units = [];
        this.arena = arena;
        this.BUY_XP_COST = 4;
        this.BUY_XP_AMOUNT = 4;

        // création du conteneur du shop
        // Create and add a container to the stage
        const container = new Container();
        container.x = 150;
        container.y = 5 * window.innerHeight / 6 - 10;
        this.container = container;
        app.stage.addChild(container);

        /*
        const rectWidth = window.innerWidth -300;
        const rectHeight = window.innerHeight /6;
        const rect = new Graphics()
            .rect(150, 5 * window.innerHeight / 6 - 10, rectWidth, rectHeight)
            .fill(0xffd700)
            .stroke({ width: 4, color: 'black' });
        container.addChild(rect);

         */

        this.createUI(sprite, list)
    }

    resetShop(units){
        let i = 2;
        if (this.units.length >= 1){
            for (let unit of this.units){
                this.container.removeChild(unit.shoppingSprite);
            }
        }
        for (let unit of units){
            // creating the sprite for the unit
            const currentUnit = unit.createShopping(i * this.SHOPITEMWIDTH, 0, (chosen) => this.onUnitClick(chosen), this.container, this.SHOPITEMWIDTH, this.SHOPITEMHIEGHT);
            currentUnit.shoppingSprite.on('pointerdown', () => this.onUnitClick(currentUnit));
            this.units.push(currentUnit);
            i++;
        }
    }


    onUnitClick(unit){
        const benchHasRoom = this.team.canAddInBench();
        const teamHasRoom = this.team.canAddOne();

        if (!benchHasRoom && !teamHasRoom) {
            console.log("Pas de place (banc et équipe pleins), achat annulé");
            return; // on ne touche à rien, pas de sprite retiré, pas d'unité créée
        }

        const newUnit = unit.createfighting(0, 0, this.team.container);
        this.container.removeChild(unit.shoppingSprite);

        if (this.team.addUnitToBench(newUnit.fightingSprite)) {
            return;
        }
        if (teamHasRoom) {
            this.arena.setToNextEmptyCell(newUnit.fightingSprite);
            this.team.addUnit(newUnit.fightingSprite);
            newUnit.fightingSprite.unitData = newUnit
        }
    }


    createButton(sprite, list, container) {
        const reroll = new Sprite(sprite);
        reroll.x = this.SHOPITEMWIDTH;
        reroll.y = 0;

        reroll.scale.set(0.5);
        reroll.width = this.SHOPITEMWIDTH;
        reroll.height = this.SHOPITEMHIEGHT/2;

        // Opt-in to interactivity
        reroll.eventMode = 'static';

        // Shows hand cursor
        reroll.cursor = 'pointer';

        reroll.on('pointerdown', () => this.onButtonClick(list));

        // add the sprite to the container
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

        this.updateUI();
    }

    updateUI() {
        this.goldText.text = `Or : ${this.team.gold}`;
        this.levelText.text = `Niveau ${this.team.level}`;

        const needed = this.team.getExpNeeded();
        const ratio = Math.min(this.team.exp / needed, 1);

        this.xpBarFill.clear();
        this.xpBarFill
            .rect(60, this.levelText.y + 30, this.xpBarWidth * ratio, this.xpBarHeight)
            .fill(0x3498db);
    }
}
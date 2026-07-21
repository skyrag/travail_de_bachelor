import {Unit} from "./Unit.js";
import {Container, Graphics, Sprite} from "pixi.js";

export class Shop {

    SHOPITEMWIDTH = 250;
    SHOPITEMHIEGHT = 190;

    constructor(app, team) {
        this.app = app;
        this.team = team;
        this.units = [];

        // création du conteneur du shop
        // Create and add a container to the stage
        const container = new Container();
        container.x = 150;
        container.y = 5 * window.innerHeight / 6 - 10;
        this.container = container;
        app.stage.getChildAt(0).addChild(container);

        /*
        const rectWidth = window.innerWidth -300;
        const rectHeight = window.innerHeight /6;
        const rect = new Graphics()
            .rect(150, 5 * window.innerHeight / 6 - 10, rectWidth, rectHeight)
            .fill(0xffd700)
            .stroke({ width: 4, color: 'black' });
        container.addChild(rect);

         */
    }

    resetShop(units){
        let i = 1;
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
        if (this.team.addUnit(unit)){
            this.container.removeChild(unit.shoppingSprite);
        }
        // team.add(unit) // a faire

    }

    createButton(sprite, list) {
        const reroll = new Sprite(sprite);
        reroll.x = 0;
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
        this.container.addChild(reroll);
    }

    onButtonClick(list) {
        this.resetShop(list);
    }
}
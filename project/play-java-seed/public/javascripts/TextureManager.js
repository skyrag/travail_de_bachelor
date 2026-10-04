import  {Assets,Graphics, Sprite } from 'pixi.js';

export class TextureManager {


    constructor() {
    }

    async init() {
        this.buttonSprite = await Assets.load('/assets/images/RerollButton.png');
    }


    async getUnit(name) {

        const fight = await Assets.load(`/assets/images/${name}_sprite.png`);
        const shop = await Assets.load(`/assets/images/${name}_shopSprite.png`);

        return {
            fightingSprite: fight,
            shoppingSprite: shop
        }
    }

    async getItem(name) {
        return await Assets.load(`/assets/images/${name}.png`)
    }


    getButton() {
        return this.buttonSprite;
    }
}
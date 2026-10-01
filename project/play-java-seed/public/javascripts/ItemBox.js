import {Container} from "pixi.js";

export class ItemBox {

    constructor(app,layers, rect) {
        this.app = app;
        this.items = new Array(10).fill(null);

        this.WIDTH = rect.width
        this.HEIGHT = rect.height
        this.x = rect.x
        this.y = rect.y

        this.ITEMWIDTH = this.WIDTH / 4
        this.ITEMHIEGHT = this.HEIGHT / 5;


        // création du conteneur
        const container = new Container();
        container.x = this.x;
        container.y = this.y;
        this.container = container;
        layers.ui.addChild(container);
    }

    addAnItem(item) {
        for (let i = 0 ; i < this.items.length; i++){
            console.log(this.items)
            if (!this.items[i]){
                this.container.addChild(item.sprite)
                this.items[i] = item;
                item.sprite.height = this.ITEMHIEGHT
                item.sprite.width = this.ITEMWIDTH
                item.sprite.position.set((i % 4) * this.ITEMWIDTH , Math.floor(i / 4) * this.ITEMHIEGHT)
                return true;
            }
        }
        return false;
    }

    removeItem(item){
        for (let i = 0 ; i < this.items.length ; i++){
            if (this.items[i] === item){
                console.log("found")
                this.items[i] = null;
                this.container.removeChild(item.sprite)
            }
        }
    }

    removeByName(name) {
        for (let i = 0; i < this.items.length; i++){
            if (this.items[i].name === name) {
                const item = this.items[i]
                this.items[i] = null
                this.container.removeChild(item)
                return item
            }
        }
    }
}
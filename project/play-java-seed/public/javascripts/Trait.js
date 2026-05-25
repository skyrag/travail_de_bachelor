import {Sprite} from "pixi.js";

export class Trait {
    constructor(name, description, icon) {
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.quantity = 0;
    }

    create() {
        return new Trait(this.name, this.description, new Sprite(this.icon))
    }

}
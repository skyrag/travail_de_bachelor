import {Sprite} from "pixi.js";

export class Item {
    constructor(name, description, sprite) {
        this.name = name;
        this.description = description;
        this.sprite = sprite;
    }

    create(){
        return new Item(this.name,this.description, new Sprite(this.sprite));
    }
}
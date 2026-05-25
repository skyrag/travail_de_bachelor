import { Application, Assets, Container,Graphics, Sprite } from 'pixi.js';
import {setup} from "./Drag.js";
import {Unit} from "./Unit.js";
import {Item} from "./Item.js";
import {Trait} from "./Trait.js";

(async () => {
    // Create a new application
    const app = new Application();

    // Initialize the application
    await app.init({ background: '#1099bb', resizeTo: window });

    // Append the application canvas to the document body
    document.body.appendChild(app.canvas);

    // Create and add a container to the stage
    const container = new Container();

    app.stage.addChild(container);

    // Load the unit texture
    const fighting = await Assets.load('assets/images/Geralt_sprite.png');
    const shop = await Assets.load("assets/images/Geralt_shopSprite.png");
    const trait = await Assets.load("assets/images/médaillon_TheWitcher.png");
    const item = await Assets.load("assets/images/item.png");

    // setup drag and drop
    setup(app);

    // setup geralt
    const geralt = new Unit(app, "geralt", fighting, shop);

    // setup item
    const bfSword = new Item("bfSword", "a big sword", item);

    // setup trait
    const witcher = new Trait("witcher", "hunters of monsters", trait);

    // setup shop place holder (gold)
    const rectWidth = window.innerWidth -300;
    const rectHeight = window.innerHeight /6;
    const rect = new Graphics()
        .rect(150, 5 * window.innerHeight / 6 - 10, rectWidth, rectHeight)
        .fill(0xffd700)
        .stroke({ width: 4, color: 'black' });
    container.addChild(rect);

    // setup traits placeholder (red)
    const rectWidth2 = 100;
    const rectHeight2 = window.innerHeight /2;
    const rect2 = new Graphics()
        .rect(25, window.innerHeight/2 - window.innerHeight/12, rectWidth2, rectHeight2)
        .fill(0xff0000)
        .stroke({ width: 4, color: 'black' });
    container.addChild(rect2);

    // setup items placeholder (blue)
    const rectWidth3 = 200;
    const rectHeight3 = window.innerHeight /4;
    const rect3 = new Graphics()
        .rect(25, window.innerHeight/12, rectWidth3, rectHeight3)
        .fill(0x3498db)
        .stroke({ width: 4, color: 'black' });
    container.addChild(rect3);


    // Create a 5x5 grid of units
    for (let i = 0; i < 25; i++) {
        var x = (i % 5) * 100 + 375;
        var y = Math.floor(i / 5) * 100 + 75;
        geralt.create(x,y);
    }


    // Move the container to the top left
    container.x = 0;
    container.y = 0;
})();

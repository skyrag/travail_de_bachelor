import { Application, Assets, Container,Graphics, Sprite } from 'pixi.js';
import {Dragger} from "./Drag.js";
import {Unit} from "./Unit.js";
import {Item} from "./Item.js";
import {Trait} from "./Trait.js";
import {Shop} from "./Shop.js";
import {Team} from "./Team.js";
import {Arena} from "./Arena.js";


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
    const geraltFighting = await Assets.load('assets/images/Geralt_sprite.png');
    const geraltShop = await Assets.load("assets/images/Geralt_shopSprite.png");
    const witcherTrait = await Assets.load("assets/images/médaillon_TheWitcher.png");
    const item = await Assets.load("assets/images/item.png");
    const buttonSprite = await Assets.load('assets/images/RerollButton.png');
    const arenaSprite = await Assets.load('assets/images/arena.png');

    // setup drag and drop
    const dragger = new Dragger(app);

    // setup geralt
    const geralt = new Unit(app, "geralt", geraltFighting, geraltShop, dragger);

    // setup item
    const bfSword = new Item("bfSword", "a big sword", item);

    // setup trait
    const witcher = new Trait("witcher", "hunters of monsters", witcherTrait);

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

    // bench placeholder (blanc)
    const rectWidth4 = window.innerWidth -700;
    const rectHeight4 = 175;
    const rect4 = new Graphics()
        .rect(350, 4 * window.innerHeight / 6 , rectWidth4, rectHeight4)
        .fill(0xffffff)
        .stroke({ width: 4, color: 'black' });
    container.addChild(rect4);

    // arena placeholder (green)
    /*
    const rectWidth5 = window.innerWidth -500;
    const rectHeight5 = window.innerHeight /2;
    const rect5 = new Graphics()
        .rect(350, window.innerHeight/12 , rectWidth5, rectHeight5)
        .fill(0x00ff00)
        .stroke({ width: 4, color: 'black' });
    container.addChild(rect5);

     */

    // creating arena
    const arena = new Arena(app, arenaSprite);
    dragger.setArena(arena);

    // creating the team
    const team = new Team(app);
    dragger.setBench(team);

    // creating the shop
    const shop = new Shop(app, team);


    // create shopUnits
    const list = [];
    for (let i = 0; i < 5 ; i++){
        list.push(geralt.copy());
    }
    console.log(list);
    shop.resetShop(list);

    // rerollbutton
    shop.createButton(buttonSprite, list);


    // Move the container to the top left
    container.x = 0;
    container.y = 0;
})();

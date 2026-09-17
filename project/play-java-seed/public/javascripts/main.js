import { Application, Assets, Container,Graphics, Sprite } from 'pixi.js';
import {Dragger} from "./Drag.js";
import {Unit} from "./Unit.js";
import {Item} from "./Item.js";
import {Trait} from "./Trait.js";
import {Shop} from "./Shop.js";
import {Team} from "./Team.js";
import {Arena} from "./Arena.js";
import {TextureManager} from "./TextureManager.js";


(async () => {
    // Create a new application
    const app = new Application();

    app.stage.sortableChildren = true; // à activer une fois sur le container parent


    // Initialize the application
    await app.init({ background: '#1099bb', resizeTo: window });

    // Append the application canvas to the document body
    document.body.appendChild(app.canvas);

    // Create and add a container to the stage
    const container = new Container();
    container.zIndex = 0;
    app.stage.addChild(container);

    // setup drag and drop
    const dragger = new Dragger(app);

    //AssetsManager
    const textureManager = new TextureManager();
    await textureManager.init();

    const basicUnits = new Map();


    //---------------------------------reception du backend

    // la liste après extraction du TDO
    let unitsDTO = [];

    //initialisation de la liste en attendant les connexions
    //DTO pour l'instant (id, nom, maxHealth, startingMana, maxMana, basicDamage, attackSpeed, armor, magicResist, range, rarity, cost
    const geraltDTO = {
        id: 0,
        name: "geralt",
        maxHealth: 100,
        startingMana: 10,
        maxMana: 40,
        basicDamage: 20,
        attackSpeed: 0.9,
        armor: 40,
        magicResist: 40,
        range: 1,
        rarity: "COMMON",
        cost: 1,
    }

    const geraltRangeDTO = {
        id: 0,
        name: "geralt",
        maxHealth: 100,
        startingMana: 10,
        maxMana: 40,
        basicDamage: 20,
        attackSpeed: 0.9,
        armor: 40,
        magicResist: 40,
        range: 2,
        rarity: "UNCOMMON",
        cost: 2,
    }
    unitsDTO.push(geraltDTO)
    unitsDTO.push(geraltRangeDTO)

    // traitement de la réception

    for (const dto of unitsDTO){
        const texture = textureManager.getUnit(dto.name)
        const unit = new Unit(app, dto.name, (await texture).fightingSprite, (await texture).shoppingSprite, dragger, 0, dto.maxHealth, dto.maxMana, dto.startingMana, dto.basicDamage, dto.attackSpeed, dto.armor, dto.magicResist, dto.range, dto.abilityName, dto.abilityDescription, dto.rarity, dto.cost)
        basicUnits.set(dto.name, unit);
    }

    // traitement des OBJETs TODO

    // traitement de la liste des unité proposé dans le shop TODO

    // create shopUnits
    const list = [];
    for (let i = 0; i < 5 ; i++){
        list.push(basicUnits.get("geralt").copy(i + 1));
    }
    //-----------------------------------fin du traitement de la récéption

    // Load the unit texture
    const witcherTrait = await Assets.load("assets/images/médaillon_TheWitcher.png");
    const item = await Assets.load("assets/images/item.png");

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

    /*
    // setup traits placeholder (red)
    const rectWidth2 = 100;
    const rectHeight2 = window.innerHeight /2;
    const rect2 = new Graphics()
        .rect(25, window.innerHeight/2 - window.innerHeight/12, rectWidth2, rectHeight2)
        .fill(0xff0000)
        .stroke({ width: 4, color: 'black' });
    container.addChild(rect2);

     */

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
    const rectHeight4 = 100;
    const rect4 = new Graphics()
        .rect(350, 4 * window.innerHeight / 6 + 75 , rectWidth4, rectHeight4)
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
    const arena = new Arena(app);
    dragger.setArena(arena);

    // creating the team
    const team = new Team(app);
    dragger.setTeam(team);

    dragger.setSellZone({ x: 150, y: 5 * window.innerHeight / 6 - 10, width: rectWidth, height: rectHeight });


    // creating the shop
    const shop = new Shop(app, team, arena, textureManager.getButton(), list);
    console.log(list);



    // Move the container to the top left
    container.x = 0;
    container.y = 0;

})();

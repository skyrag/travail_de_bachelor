import { Application, Assets, Container,Graphics, Sprite } from 'pixi.js';
import {Dragger} from "./Drag.js";
import {Unit} from "./Unit.js";
import {Item} from "./Item.js";
import {Trait} from "./Trait.js";
import {Shop} from "./Shop.js";
import {Team} from "./Team.js";
import {Arena} from "./Arena.js";
import {TextureManager} from "./TextureManager.js";
import { createLayers, fitToScreen, GAME_W, GAME_H } from './Layers.js';
import {Fight} from "./Fight.js";
import{ItemBox} from "./ItemBox.js";
import {ItemDragger} from "./ItemDragger.js";


(async () => {
    // Create a new application
    const app = new Application();

    app.stage.sortableChildren = true; // à activer une fois sur le container parent


    // Initialize the application
    await app.init({ background: '#1099bb', resizeTo: window });

    const layers = createLayers(app);
    fitToScreen(app, layers.root);
    app.renderer.on('resize', () => fitToScreen(app, layers.root));

    // Append the application canvas to the document body
    document.body.appendChild(app.canvas);

    // Create and add a container to the stage
    const container = new Container();
    app.stage.addChild(container);

    // setup drag and drop
    const dragger = new Dragger(app, layers);
    const itemDragger = new ItemDragger(app, layers);

    //AssetsManager
    const textureManager = new TextureManager();
    await textureManager.init();

    const basicUnits = new Map();
    const items = new Map();
    const teams = [];

    const layout = computeLayout(GAME_W, GAME_H); // constant, plus besoin de recalculer

    layers.background.addChild(drawZone(layout.shop, 0xffd700));
    layers.background.addChild(drawZone(layout.bench, 0xffffff));
    layers.background.addChild(drawZone(layout.items, 0x3498db));

    // creating arena
    const arena = new Arena(app, layers);

    const fightManager = new Fight(layers, arena);
    let ISFIGHTINGPHASE = false;

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
        attackSpeed: 45,
        armor: 40,
        magicResist: 40,
        range: 1,
        rarity: "COMMON",
        cost: 1,
    }

    const geraltRangeDTO = {
        id: 0,
        name: "geralt2",
        maxHealth: 100,
        startingMana: 10,
        maxMana: 40,
        basicDamage: 20,
        attackSpeed: 45,
        armor: 40,
        magicResist: 40,
        range: 4,
        rarity: "UNCOMMON",
        cost: 2,
    }
    unitsDTO.push(geraltDTO)
    unitsDTO.push(geraltRangeDTO)

    // traitement de la réception


    for (const dto of unitsDTO){
        const texture = textureManager.getUnit("geralt") // Débug, it should be dto.name
        const unit = new Unit(app, layers, dto.name, (await texture).fightingSprite, (await texture).shoppingSprite, dragger, 0, dto.maxHealth, dto.maxMana, dto.startingMana, dto.basicDamage, dto.attackSpeed, dto.armor, dto.magicResist, dto.range, dto.abilityName, dto.abilityDescription, dto.rarity, dto.cost)
        basicUnits.set(dto.name, unit);
    }

    // traitement des OBJETs TODO

    let itemsDTO = []

    const bfDTO = {
        name : "bfSword",
        description : "a big fucking sword",
        sprite : "item.png",
        effect : [
            {
                type : "ATTACKDAMAGE",
                value : 10,
            },
            {
                type : "HEALTH",
                value : 100,
            },
        ],
    }

    itemsDTO.push(bfDTO);

    for (const dto of itemsDTO) {
        const texture = textureManager.getItem(dto.sprite)
        const item = new Item(app, layers, dto.name, dto.description, (await texture), dto.effect)
        items.set(dto.name, item);
    }

    // traitement de la liste des unité proposé dans le shop TODO

    // create shopUnits
    const list = [];
    for (let i = 0; i < 5 ; i++){
        list.push(basicUnits.get("geralt").copy(i + 1));
    }

    //création des teams

    // creating our team

    const itemBox = new ItemBox(app, layers, layout.items)

    const team = new Team(app, layout.bench, layers, 0, "skyrag");

    team.setItemBox(itemBox)

    teams[team.id] = team;

    for (let i = 1; i < 8; i++) {
        teams[i] = new Team(app, layout.bench, layers, i, `player${i}`)
    }

    //-----------------------------------fin du traitement de la récéption



    // Load the unit texture
    const witcherTrait = await Assets.load("assets/images/médaillon_TheWitcher.png");

    // setup trait
    const witcher = new Trait("witcher", "hunters of monsters", witcherTrait);


/*
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
/*
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


    dragger.setArena(arena);
    dragger.setTeam(team);
    dragger.setSellZone(layout.shop);


    itemDragger.setArena(arena);
    itemDragger.setTeam(team);
    itemDragger.setItemBox(itemBox);


    // creating the shop
    const shop = new Shop(app,layers, team, arena, textureManager.getButton(), list, layout.shop);
    console.log(list);

    // le tick
    app.ticker.add((ticker) => {
        shop.update(ticker.deltaTime);
        team.update(ticker.deltaTime);

        if (!ISFIGHTINGPHASE) return;

        fightManager.advancePlaybackTime(ticker.deltaMS)
        arena.checkDeath()
        if (fightManager.checkEnd()){
            arena.clean()
            team.resetPositions(arena)
            team.resetUnits()
            ISFIGHTINGPHASE = false
        }

    });



    // Move the container to the top left
    container.x = 0;
    container.y = 0;

    function computeLayout(w, h) {
        return {
            shop:  { x: 150, y: 5 * h / 6 - 10, width: w - 300, height: h / 6 },
            bench: { x: 350, y: 4 * h / 6 + 75, width: w - 700, height: 100 },
            items: { x: 25,  y: h / 12, width: 200, height: h / 4 },
        };
    }

    function drawZone(zone, color) {
        return new Graphics()
            .rect(zone.x, zone.y, zone.width, zone.height)
            .fill(color)
            .stroke({ width: 4, color: 'black' });
    }


    // Bouton lancer un combat
    const fightButton = new Graphics()
        .rect(-75, GAME_H / 12 + 100, 100, 100)
        .fill(0x2ecc71)
        .stroke({ width: 2, color: 0x000000 });
    fightButton.eventMode = 'static';
    fightButton.cursor = 'pointer';
    fightButton.on('pointerdown', () => setupFight() );
    layers.ui.addChild(fightButton);


    // Bouton lancer un combat
    const itemButton = new Graphics()
        .rect(225, GAME_H / 12 + 100, 100, 100)
        .fill(0xff0000)
        .stroke({ width: 2, color: 0x000000 });
    itemButton.eventMode = 'static';
    itemButton.cursor = 'pointer';
    itemButton.on('pointerdown', () => team.addItem(items.get("bfSword").create(itemDragger)) );
    layers.ui.addChild(itemButton);



    function setupFight() {
        // on va simuler un combat entre nous (0) et player1 (1)

        //clean des deux teams
        teams[0].clean()
        teams[1].clean()


        //setup
        const ourteam = teams[0];
        ourteam.buyExperience()

        //distanceUnit
        const ourDist = basicUnits.get("geralt2").copy(0)
        ourDist.createfighting(0,0, ourteam.container)
        arena.setToCell(ourDist, 0,0)
        ourteam.addUnit(ourDist)

        //meleeUnit
        const ourMelee = basicUnits.get("geralt").copy(1)
        ourMelee.createfighting(0,0, ourteam.container)
        arena.setToCell(ourMelee, 3,0)
        ourteam.addUnit(ourMelee)


        const ennemyTeam = teams[1]
        const ennemyMelee = basicUnits.get("geralt").copy(2)
        ennemyMelee.createfighting(0,0, ennemyTeam.container, true)
        arena.setToCell(ennemyMelee, 4, 0)
        ennemyTeam.addUnit(ennemyMelee)

        //création des events

        const EventsDTO = [
            {
                type: "ATTACK",
                tick: 0,
                src: 0,
                target: 2,
                damage: 30,
            },
            {
                type: "ATTACK",
                tick: 0,
                src: 1,
                target: 2,
                damage: 30,
            },
            {
                type: "ATTACK",
                tick: 0,
                src: 2,
                target: 1,
                damage: 50,
            },
            {
                type: "MOVE",
                tick: 30,
                src: 0,
                x: 0,
                y: 1,
            },
            {
                type: "ABILITY",
                tick: 60,
                src: 0,
                groups: [
                    {targets : [
                        2,
                        ],
                     damage: 50,
                    }
                ],
            },
        ]

        // lancer le combat
        fightManager.loadFight(ourteam, ennemyTeam, EventsDTO)
        ISFIGHTINGPHASE = true;

    }

})();

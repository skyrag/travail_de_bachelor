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
import {Countdown} from "./Countdown.js";


let app;
let textureManager, layers, layout;
let dragger, itemDragger;
let arena, ourTeam, shop, itemBox;
let basicUnits, items, teams;
let fightManager;
let ISFIGHTINGPHASE, ENDOFROUND, countdown;
let ws;


export async function initGame(rootElementId, websocket) {

    // Create a new application
    app = new Application();

    app.stage.sortableChildren = true; // à activer une fois sur le container parent

    // Initialize the application
    await app.init({ background: '#1099bb', resizeTo: window });

    //AssetsManager
    textureManager = new TextureManager();
    await textureManager.init();

    //layers (for sprites)
    layers = createLayers(app);

    //resize of the screen
    fitToScreen(app, layers.root);
    app.renderer.on('resize', () => fitToScreen(app, layers.root));

    // Append the application canvas to the document body
    document.body.appendChild(app.canvas);

    /*
    version d'avant avais :
     const root = document.getElementById(rootElementId);
    root.appendChild(app.canvas);
     */

    // setup drag and drop for unit and items
    dragger = new Dragger(app, layers, websocket);
    itemDragger = new ItemDragger(app, layers, websocket);


    //map to contain all units and items in the game
    basicUnits = new Map();
    items = new Map();

    // all teams of this game
    teams = [];

    // precomputed zones for sprites
    layout = computeLayout(GAME_W, GAME_H);

    layers.background.addChild(drawZone(layout.shop, 0xffd700));
    layers.background.addChild(drawZone(layout.bench, 0xffffff));
    layers.background.addChild(drawZone(layout.items, 0x3498db));

    // creating arena
    arena = new Arena(app, layers);


    fightManager = new Fight(layers, arena);

    ISFIGHTINGPHASE = false;

    // what holds and diplays the items
    itemBox = new ItemBox(app, layers, layout.items)


    //essencial settings for our draggers
    dragger.setArena(arena);
    dragger.setSellZone(layout.shop);

    itemDragger.setArena(arena);
    itemDragger.setItemBox(itemBox);

    //coutdown
    countdown = new Countdown(layers, 90, GAME_W / 2 + 15, 28)
    countdown.onComplete = () => {
        ENDOFROUND = true;
    }

    //websocket
    ws = websocket;

    //débug
    countdown.start(90)

}

export async function startGame(payload, myUserId, username) {

    console.log(payload)
    console.log(myUserId)
    console.log(username)

    //---------------------------------reception du backend

    // la liste après extraction du TDO
    let unitsDTO = payload.units;

    // traitement de la réception


    for (const dto of unitsDTO) {
        const texture = textureManager.getUnit(dto.name) // Débug, it should be dto.name
        const unit = new Unit(app, layers, dto.name, (await texture).fightingSprite, (await texture).shoppingSprite, dragger, 0, dto.maxHealth, dto.maxMana, dto.startingMana, dto.baseAttack, dto.attackSpeed, dto.armor, dto.magicResist, dto.range, dto.abilityName, dto.abilityDescription, dto.rarity, dto.cost)
        basicUnits.set(dto.name, unit);
    }

    // traitement des OBJETs

    let itemsDTO = payload.items

    for (const dto of itemsDTO) {
        const texture = textureManager.getItem(dto.name)
        const item = new Item(app, layers, dto.name, dto.description, (await texture), dto.effects)
        items.set(dto.name, item);
    }

    // traitement de la liste des unité proposé dans le shop TODO

    const teamsDTO = payload.team

    for (const team of teamsDTO){

        console.log("items disponibles:", [...items.entries()]);
        console.log("items de l'équipe (brut):", team.items);

        if (team.username === username){ // creating our team
            ourTeam = new Team(app, layout.bench, layers, team.id, team.username)

            dragger.setTeam(ourTeam);
            itemDragger.setTeam(ourTeam);
            ourTeam.setItemBox(itemBox);

            console.log("objets" + items)

            for (const name of team.items) {
                ourTeam.items.addAnItem(items.get(name).create(itemDragger))
            }

            // create shopUnits
            const list = [];
            for (const name of team.shop) {
                list.push(basicUnits.get(name).copy(0)); // those are not instances
            }

            shop = new Shop(app, layers, ourTeam, arena, textureManager.getButton(), list, layout.shop, ws, basicUnits);

            for (const unitDTO of team.units) {
                const unit = basicUnits.get(unitDTO.name).copy(unitDTO.instanceId)
                unit.createfighting(0,0, ourTeam.container)
                arena.setToCell(unit, 0, 0)
                ourTeam.addUnit(unit)
            }

        } else {
            // pas encore d'utilisation des autres équipes
            teams.push( new Team(app, layout.bench, layers, team.id, team.username))
            console.log(teams)
        }
    }

    //-----------------------------------fin du traitement de la récéption

    // le tick
    app.ticker.add((ticker) => {
        shop.update(ticker.deltaTime);
        ourTeam.update(ticker.deltaTime);
        countdown.update(ticker.deltaMS);

        if (!ISFIGHTINGPHASE) return;

        fightManager.advancePlaybackTime(ticker.deltaMS)
        arena.checkDeath()
        if (fightManager.checkEnd()) {
            arena.clean()
            ourTeam.resetPositions(arena)
            ourTeam.resetUnits()
            ISFIGHTINGPHASE = false
            countdown.start(90);
        }

    });


    // Bouton lancer un combat (débug)
    const fightButton = new Graphics()
        .rect(-75, GAME_H / 12 + 100, 100, 100)
        .fill(0x2ecc71)
        .stroke({width: 2, color: 0x000000});
    fightButton.eventMode = 'static';
    fightButton.cursor = 'pointer';
    fightButton.on('pointerdown', () => setupFight());
    layers.ui.addChild(fightButton);


    // Bouton pour ajouter un objet (débug
    const itemButton = new Graphics()
        .rect(225, GAME_H / 12 + 100, 100, 100)
        .fill(0xff0000)
        .stroke({width: 2, color: 0x000000});
    itemButton.eventMode = 'static';
    itemButton.cursor = 'pointer';
    itemButton.on('pointerdown', () => ourTeam.addItem(items.get("bfSword").create(itemDragger)));
    layers.ui.addChild(itemButton);
}

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

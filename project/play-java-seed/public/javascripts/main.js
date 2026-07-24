// main.js
import { Application, Assets, Container, Graphics } from 'pixi.js';
import { Dragger } from "./Drag.js";
import { Unit } from "./Unit.js";
import { Item } from "./Item.js";
import { Shop } from "./Shop.js";
import { Team } from "./Team.js";
import { Arena } from "./Arena.js";

// --- Registre local : sprite (clé technique venant du DTO) -> chemins des assets ---
const UNIT_ASSET_PATHS = {
    geralt: {
        fighting: 'assets/images/Geralt_sprite.png',
        shopping: 'assets/images/Geralt_shopSprite.png',
    },
    // ex: yennefer: { fighting: '...', shopping: '...' },
};

const ITEM_ASSET_PATHS = {
    bfSword: 'assets/images/item.png',
    // ...
};

const BUTTON_ASSET_PATH = 'assets/images/RerollButton.png';

let app, dragger, arena, team, shop;
let buttonTexture = null;
let assetsLoaded = false;

/**
 * Initialise l'application Pixi et précharge les assets.
 * À appeler une seule fois, avant startGame().
 */
export async function initGame(rootElementId) {
    app = new Application();
    app.stage.sortableChildren = true;

    await app.init({ background: '#1099bb', resizeTo: window });

    const root = document.getElementById(rootElementId);
    root.appendChild(app.canvas);

    if (!assetsLoaded) {
        await preloadAssets();
        assetsLoaded = true;
    }
}

async function preloadAssets() {
    for (const paths of Object.values(UNIT_ASSET_PATHS)) {
        paths.fightingTexture = await Assets.load(paths.fighting);
        paths.shoppingTexture = await Assets.load(paths.shopping);
    }

    for (const [name, path] of Object.entries(ITEM_ASSET_PATHS)) {
        ITEM_ASSET_PATHS[name] = { path, texture: await Assets.load(path) };
    }

    buttonTexture = await Assets.load(BUTTON_ASSET_PATH);
}

/**
 * Démarre la partie à partir du payload reçu du serveur au setup.
 * payload = { units: UnitDTO[], items: ItemDTO[], team: TeamDTO[] }
 * myUserId = id de l'utilisateur courant, pour identifier sa propre équipe parmi toutes celles reçues
 */
export function startGame(payload, myUserId) {
    const container = new Container();
    container.zIndex = 0;
    app.stage.addChild(container);

    // 1. Registres construits à partir des DTO + assets locaux
    const unitDataByKey = buildUnitRegistry(payload.units);
    const itemsById = buildItemRegistry(payload.items);

    // 2. Trouve l'équipe du joueur actuel parmi toutes les équipes reçues
    const myTeamDTO = payload.team.find(t => t.userId === myUserId); // ⚠️ nom de champ à confirmer sur TeamDTO
    if (!myTeamDTO) {
        console.error("Impossible de trouver l'équipe du joueur", myUserId, payload.team);
        return;
    }
    const opponentTeamDTOs = payload.team.filter(t => t.userId !== myUserId);

    // Placeholders visuels
    drawPlaceholders(container);

    // 3. Setup drag & drop
    dragger = new Dragger(app);

    // 4. Setup arena + team
    arena = new Arena(app);
    dragger.setArena(arena);

    team = new Team(app);
    dragger.setTeam(team);

    // 5. Applique l'état reçu (gold, level, exp...)
    applyTeamState(myTeamDTO);

    // 6. Construit le shop à partir des ids d'unités du shop de l'équipe
    const shopUnits = myTeamDTO.shop
        .map(unitId => findUnitDataById(payload.units, unitDataByKey, unitId))
        .filter(Boolean);

    shop = new Shop(app, team, arena, buttonTexture, shopUnits);
    shop.resetShop(shopUnits);

    container.x = 0;
    container.y = 0;

    // Expose l'état pour le reste du jeu (notamment CombatPlayer, à instancier plus tard)
    window.__gameState = {
        app, arena, team, shop, dragger,
        unitDataByKey, itemsById,
        myTeamDTO, opponentTeamDTOs,
        myUserId,
    };
}

// --- Construction des registres à partir des DTO ---

function buildUnitRegistry(unitDTOs) {
    const map = new Map();

    for (const dto of unitDTOs) {
        const assets = UNIT_ASSET_PATHS[dto.sprite];
        if (!assets) {
            console.warn(`Pas d'assets locaux pour le sprite "${dto.sprite}" (unité "${dto.name}"), ignorée`);
            continue;
        }

        const unit = new Unit(
            app,
            assets.fightingTexture,
            assets.shoppingTexture,
            dragger,
            dto.id,
            dto.name,
            dto.cost,
            dto.rarity,
            dto.abilityName,
            dto.abilityDescription,
            dto.maxHealth,
            dto.maxMana,
            dto.startingMana,
            dto.baseAttack,
            dto.attackDamage,
            dto.abilityPower,
            dto.attackSpeed,
            dto.armor,
            dto.magicResist,
            dto.range
        );

        map.set(`sprite:${dto.sprite}`, unit);
        map.set(`id:${dto.id}`, unit);
        map.set(`name:${dto.name}`, unit); // utile pour CombatPlayer, qui ne reçoit que "name" via ComponentUnitDTO
    }

    return map;
}

function buildItemRegistry(itemDTOs) {
    const map = new Map();
    for (const dto of itemDTOs) {
        const assetEntry = ITEM_ASSET_PATHS[dto.name];
        const texture = assetEntry?.texture ?? null;
        const item = new Item(dto.name, dto.description, texture);
        map.set(dto.id, item);
    }
    return map;
}

function findUnitDataById(unitDTOs, unitDataByKey, unitId) {
    const dto = unitDTOs.find(u => u.id === unitId);
    if (!dto) return null;
    return unitDataByKey.get(`id:${dto.id}`)?.copy(dto.id);
}

// --- Application de l'état reçu ---

function applyTeamState(teamDTO) {
    team.gold = teamDTO.gold;
    team.level = teamDTO.lvl; // ⚠️ nom de champ à confirmer (lvl vs level)
    team.exp = teamDTO.exp;
    team.updateUI?.();

    // TODO: placer les unités déjà possédées (teamDTO.units, liste d'ids) sur l'arène/le banc.
    // Nécessite de connaître leur position (x,y) et leur type d'origine — à voir selon
    // la structure que tu choisis pour représenter une InstanceUnit côté client.
}

// --- Placeholders visuels ---

function drawPlaceholders(container) {
    const rectWidth = window.innerWidth - 300;
    const rectHeight = window.innerHeight / 6;
    container.addChild(new Graphics()
        .rect(150, 5 * window.innerHeight / 6 - 10, rectWidth, rectHeight)
        .fill(0xffd700)
        .stroke({ width: 4, color: 'black' }));

    const rectWidth3 = 200;
    const rectHeight3 = window.innerHeight / 4;
    container.addChild(new Graphics()
        .rect(25, window.innerHeight / 12, rectWidth3, rectHeight3)
        .fill(0x3498db)
        .stroke({ width: 4, color: 'black' }));

    const rectWidth4 = window.innerWidth - 700;
    const rectHeight4 = 100;
    container.addChild(new Graphics()
        .rect(350, 4 * window.innerHeight / 6 + 75, rectWidth4, rectHeight4)
        .fill(0xffffff)
        .stroke({ width: 4, color: 'black' }));
}
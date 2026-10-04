const BOARD_MAX_X = 7;

const pick = (obj, ...keys) => {
    for (const k of keys) if (obj?.[k] !== undefined) return obj[k];
    return undefined;
};

/**
 * Transforme le FightingResultDTO du serveur en données prêtes pour Fight.js.
 * Si les noms de champs de tes DTO diffèrent, ajuste uniquement ce fichier.
 */
export function adaptReplay(replay, myTeamId) {
    const iAmTeamB = replay.idTeamB === myTeamId;
    const opponentTeamId = iAmTeamB ? replay.idTeamA : replay.idTeamB;
    const fx = (x) => (iAmTeamB ? BOARD_MAX_X - x : x);   // je m'affiche toujours à gauche

    // unités présentes au début du combat
    const units = (replay.initialState ?? []).map(u => {
        const pos = pick(u, 'currentPosition', 'position', 'pos') ?? u;
        return {
            id: pick(u, 'id', 'instanceId'),
            name: pick(u, 'name', 'unitName'),
            x: fx(pos.x),
            y: pos.y,
        };
    });

    const events = [];
    const abilities = new Map();

    for (const e of replay.events ?? []) {
        const kind = String(pick(e, 'type', 'kind') ?? '').toUpperCase();

        if (kind.includes('MOVE')) {
            events.push({ type: 'MOVE', tick: e.tick, src: pick(e, 'src', 'unitId', 'id'), x: fx(e.x), y: e.y });

        } else if (kind.includes('ATTACK')) {
            events.push({
                type: 'ATTACK', tick: e.tick,
                src: pick(e, 'src', 'srcId', 'sourceId', 'attackerId'),
                target: pick(e, 'target', 'targetId'),
                damage: pick(e, 'damage', 'mitigatedDamage') ?? 0,
            });

        } else if (kind.includes('DEATH') || kind.includes('END')) {

        } else {
            const src = pick(e, 'src', 'casterId', 'sourceId');
            const key = `${src}:${e.tick}:${pick(e, 'abilityId', 'abilityUUID', 'uuid') ?? ''}`;
            let ab = abilities.get(key);
            if (!ab) {
                ab = { type: 'ABILITY', tick: e.tick, src, groups: [] };
                abilities.set(key, ab);
                events.push(ab);
            }
            const damage = pick(e, 'damage', 'value', 'amount') ?? 0;
            let g = ab.groups.find(g => g.damage === damage);
            if (!g) { g = { targets: [], damage }; ab.groups.push(g); }
            g.targets.push(pick(e, 'target', 'targetId'));
        }
    }

    const lastTick = (replay.events ?? []).reduce((m, e) => Math.max(m, e.tick ?? 0), 0);
    return { opponentTeamId, units, events, lastTick };
}
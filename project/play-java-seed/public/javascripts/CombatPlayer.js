import { Container, Text } from "pixi.js";

export class CombatPlayer {
    static TICKS_PER_SECOND = 60;

    constructor(app, arena, myTeamId, opponentTeamUnits, unitDataByName = new Map()) {
        this.app = app;
        this.arena = arena;
        this.myTeamId = myTeamId;
        this.unitDataByName = unitDataByName;

        // Uniquement pour créer les sprites ennemis (les nôtres sont déjà sur l'arène)
        this.opponentUnitDataById = new Map();
        for (const unit of opponentTeamUnits) {
            this.opponentUnitDataById.set(unit.id, unit);
        }

        this.unitsById = new Map(); // id combat -> { sprite, unit, teamId, isOwn, originalCell }
        this.timeouts = [];
        this.mirrored = false;
        this.onCombatEndCallback = null;
    }

    // --- Lancement ---

    play(result, onCombatEnd) {
        this.onCombatEndCallback = onCombatEnd;
        this.mirrored = result.idTeamB === this.myTeamId;

        this.combatContainer = new Container();
        this.combatContainer.x = this.arena.container.x;
        this.combatContainer.y = this.arena.container.y;
        this.arena.container.parent.addChild(this.combatContainer);

        this.arena.container.visible = false;

        this.setupInitialState(result.initialState);
        this.playEvents(result.events);
    }

    tickToMs(t) {
        return (t / CombatPlayer.TICKS_PER_SECOND) * 1000;
    }

    toDisplayCol(col) {
        const cols = this.arena.hexGrid[0]?.length ?? 8;
        return this.mirrored ? (cols - 1 - col) : col;
    }

    // --- Placement initial ---

    setupInitialState(initialState) {
        for (const unitDto of initialState) {
            const displayCol = this.toDisplayCol(unitDto.x);
            const cell = this.arena.hexGrid[unitDto.y]?.[displayCol];
            if (!cell) {
                console.warn(`Cellule introuvable pour l'unité ${unitDto.id} à (${unitDto.x},${unitDto.y})`);
                continue;
            }

            const isOwn = unitDto.teamId === this.myTeamId;
            const entry = isOwn
                ? this.reuseOwnSprite(unitDto, cell)
                : this.createEnemySprite(unitDto, cell);

            if (entry) this.unitsById.set(unitDto.id, entry);
        }
    }

    // Réutilise le sprite déjà présent sur l'arène (pas de clone, pas de flip)
    reuseOwnSprite(unitDto, cell) {
        const sprite = this.findOwnArenaSprite(unitDto.id);
        if (!sprite) {
            console.warn(`Sprite introuvable sur l'arène pour ma propre unité ${unitDto.id}`);
            return null;
        }

        const unit = sprite.unit;

        // Mémorise où le sprite était pour pouvoir le restaurer après le combat
        const originalParent = sprite.parent;
        const originalX = sprite.x;
        const originalY = sprite.y;

        this.combatContainer.addChild(sprite); // reparente vers le combat
        sprite.position.set(cell.x, cell.y);

        // Synchronise les stats réelles envoyées par le combat (bonus d'objets/synergies)
        unit.maxHealth = unitDto.maxHealth;
        unit.currentHealth = unitDto.maxHealth;
        unit.attackDamage = unitDto.attackDamage;
        unit.abilityPower = unitDto.abilityPower;
        unit.armor = unitDto.armor;
        unit.magicResist = unitDto.magicResist;

        return {
            sprite,
            unit,
            teamId: unitDto.teamId,
            isOwn: true,
            originalParent,
            originalX,
            originalY,
        };
    }

    findOwnArenaSprite(unitId) {
        for (const cell of this.arena.hexGrid.flat()) {
            if (cell.unit && cell.unit.unit && cell.unit.unit.id === unitId) {
                return cell.unit; // c'est le sprite (fightingSprite)
            }
        }
        return null;
    }

    // Crée un nouveau sprite pour une unité adverse, jamais présente côté client
    createEnemySprite(unitDto, cell) {
        let baseUnit = this.opponentUnitDataById.get(unitDto.id)
            ?? this.unitDataByName.get(unitDto.name);

        if (!baseUnit) {
            console.warn(`Unité ennemie introuvable (id=${unitDto.id}, name=${unitDto.name})`);
            return null;
        }

        const combatUnit = baseUnit.clone({
            id: unitDto.id,
            maxHealth: unitDto.maxHealth,
            maxMana: unitDto.maxMana,
            startingMana: unitDto.currentMana,
            baseAttack: unitDto.baseAttack,
            attackDamage: unitDto.attackDamage,
            abilityPower: unitDto.abilityPower,
            attackSpeed: unitDto.attackSpeed,
            armor: unitDto.armor,
            magicResist: unitDto.magicResist,
            range: unitDto.range,
        });

        const fighter = combatUnit.createfighting(0, 0, this.combatContainer);
        fighter.currentHealth = unitDto.maxHealth;

        const sprite = fighter.fightingSprite;
        sprite.position.set(cell.x, cell.y);

        // Retourne le sprite horizontalement : nos sprites sont dessinés vers la droite,
        // l'ennemi doit regarder vers la gauche (vers le joueur)
        sprite.scale.x = -Math.abs(sprite.scale.x);

        return {
            sprite,
            unit: fighter,
            teamId: unitDto.teamId,
            isOwn: false,
        };
    }

    // --- Lecture temporelle des events ---

    playEvents(events) {
        const sorted = [...events].sort((a, b) => a.t - b.t);
        for (const event of sorted) {
            const delayMs = this.tickToMs(event.t);
            this.timeouts.push(setTimeout(() => this.handleEvent(event), delayMs));
        }
    }

    stop() {
        this.timeouts.forEach(clearTimeout);
        this.timeouts = [];
    }

    handleEvent(event) {
        switch (event.type) {
            case 'moveTo': return this.handleMoveTo(event);
            case 'attack': return this.handleAttack(event);
            case 'abilityCast': return this.handleAbilityCast(event);
            case 'effectApplied': return this.handleEffectApplied(event);
            case 'death': return this.handleDeath(event);
            case 'combatEnd': return this.handleCombatEnd(event);
            default: console.warn("Type d'event inconnu:", event.type, event);
        }
    }

    // --- Handlers ---

    handleMoveTo(event) {
        const entry = this.unitsById.get(event.unitId);
        if (!entry) return;

        const displayCol = this.toDisplayCol(event.x);
        const cell = this.arena.hexGrid[event.y]?.[displayCol];
        if (!cell) return;

        entry.sprite.position.set(cell.x, cell.y);
    }

    handleAttack(event) {
        const attacker = this.unitsById.get(event.unit);
        const target = this.unitsById.get(event.target);
        if (!attacker || !target) return;

        this.lungeSprite(attacker.sprite, target.sprite);
        this.showFloatingDamage(target.sprite, event.damage, event.crit);
        target.unit.takeDamage(event.damage);
    }

    handleAbilityCast(event) {
        const caster = this.unitsById.get(event.casterId);
        if (!caster) return;
        this.flashSprite(caster.sprite, 0xffff00);
    }

    handleEffectApplied(event) {
        const target = this.unitsById.get(event.targetId);
        if (!target) return;

        const colors = { poison: 0x2ecc71, stun: 0xf1c40f, shield: 0x3498db, burn: 0xe74c3c };
        const color = colors[event.effectType] ?? 0xffffff;
        this.flashSprite(target.sprite, color);
        this.showFloatingText(target.sprite, `${event.effectType} ${event.value > 0 ? '+' : ''}${event.value}`, color);
    }

    handleDeath(event) {
        const entry = this.unitsById.get(event.unitId);
        if (!entry) return;

        entry.unit.onDeath();
        entry.sprite.parent?.removeChild(entry.sprite);
        entry.unit.hideTooltip?.();
        this.unitsById.delete(event.unitId);
    }

    handleCombatEnd(event) {
        const won = event.winner === this.myTeamId;
        this.cleanup();
        this.onCombatEndCallback?.({ won, winnerTeamId: event.winner });
    }

    // --- Animation d'attaque : petit aller-retour vers la cible ---

    lungeSprite(attackerSprite, targetSprite, distance = 20, duration = 180) {
        const dx = targetSprite.x - attackerSprite.x;
        const dy = targetSprite.y - attackerSprite.y;
        const dist = Math.hypot(dx, dy) || 1;

        const offsetX = (dx / dist) * distance;
        const offsetY = (dy / dist) * distance;

        const startX = attackerSprite.x;
        const startY = attackerSprite.y;
        const startTime = performance.now();

        const animate = (now) => {
            const elapsed = now - startTime;
            const t = Math.min(elapsed / duration, 1);

            // aller (0 -> 0.5) puis retour (0.5 -> 1), courbe simple triangle
            const progress = t < 0.5 ? t * 2 : (1 - t) * 2;

            attackerSprite.x = startX + offsetX * progress;
            attackerSprite.y = startY + offsetY * progress;

            if (t < 1) {
                requestAnimationFrame(animate);
            } else {
                attackerSprite.position.set(startX, startY);
            }
        };
        requestAnimationFrame(animate);
    }

    // --- Effets visuels ---

    showFloatingDamage(sprite, damage, crit) {
        const color = crit ? 0xff0000 : 0xffffff;
        const size = crit ? 22 : 16;
        this.showFloatingText(sprite, `-${damage}`, color, size);
    }

    showFloatingText(sprite, text, color = 0xffffff, fontSize = 14) {
        const label = new Text({ text, style: { fill: color, fontSize, fontWeight: 'bold' } });
        label.anchor.set(0.5);
        label.x = sprite.x;
        label.y = sprite.y - sprite.height - 10;
        label.eventMode = 'none';

        this.combatContainer.addChild(label);

        const duration = 800;
        const startTime = performance.now();
        const startY = label.y;

        const animate = (now) => {
            const elapsed = now - startTime;
            const t = Math.min(elapsed / duration, 1);
            label.y = startY - t * 30;
            label.alpha = 1 - t;

            if (t < 1) requestAnimationFrame(animate);
            else label.parent?.removeChild(label);
        };
        requestAnimationFrame(animate);
    }

    flashSprite(sprite, color, duration = 200) {
        const original = sprite.tint ?? 0xffffff;
        sprite.tint = color;
        setTimeout(() => { sprite.tint = original; }, duration);
    }

    // --- Nettoyage ---

    cleanup() {
        this.stop();

        for (const entry of this.unitsById.values()) {
            if (entry.isOwn) {
                // Remet le sprite exactement où il était avant le combat
                entry.originalParent.addChild(entry.sprite);
                entry.sprite.position.set(entry.originalX, entry.originalY);
            } else {
                // Sprite créé pour le combat uniquement : on le détruit
                entry.sprite.parent?.removeChild(entry.sprite);
                entry.unit.hideTooltip?.();
            }
        }
        this.unitsById.clear();

        this.combatContainer?.parent?.removeChild(this.combatContainer);
        this.combatContainer = null;

        this.arena.container.visible = true;
    }
}
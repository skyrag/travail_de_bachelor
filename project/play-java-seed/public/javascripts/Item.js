import {Sprite, Container, Graphics, Text} from "pixi.js";

export class Item {
    constructor(app, layers, name, description, sprite, effect,  dragger = null) {

        this.app = app
        this.layers = layers

        this.name = name;
        this.description = description;
        this.sprite = sprite;

        this.effect = effect

        if (dragger){
            sprite.eventMode = 'static';
            sprite.cursor = 'pointer';

            sprite.item = this
            sprite.dragger = dragger
            sprite.on('pointerdown', sprite.dragger.onDragStart, sprite);
        }




    }

    create(dragger){
        return new Item(this.app, this.layers, this.name,this.description, new Sprite(this.sprite), this.effect, dragger);
    }

    apply(unit) {
        this.effect.forEach(effect => {
            switch (effect.type) {
                case "HEALTH" :
                    unit.maxHealth += effect.value
                    break;

                case "MANA" :
                    unit.startingMana = Math.min(unit.maxMana, unit.startingMana + effect.value)
                    break;

                case "ATTACKDAMAGE" :
                    unit.attackDamage += effect.value
                    break;

                case "ABILITYPOWER" :
                    unit.abilityPower += effect.value
                    break;

                case "ATTACKSPEED" :
                    unit.attackSpeed += effect.value
                    break;

                case "ARMOR" :
                    unit.armor += effect.value
                    break;

                case "MAGICRESIST" :
                    unit.magicResist += effect.value
                    break;

                default:
                    console.log("Nan j'ai rien vu")
            }
        })
    }

    showStats() {
        Item.closeActiveStatsPanel();

        const PANEL_W = 300;
        const PANEL_H = 150; // moins de contenu qu'une unité, plus court
        const PADDING = 20;

        const root = new Container();

        const overlay = new Graphics()
            .rect(0, 0, this.app.screen.width, this.app.screen.height)
            .fill({ color: 0x000000, alpha: 0.001 });
        overlay.eventMode = 'static';
        overlay.on('pointerdown', () => this.closeStats());

        const bg = new Graphics()
            .roundRect(0, 0, PANEL_W, PANEL_H, 12)
            .fill(0x1e1e1e)
            .stroke({ width: 2, color: 0xffffff, alpha: 0.3 });
        bg.x = (this.app.screen.width - PANEL_W) / 2;
        bg.y = (this.app.screen.height - PANEL_H) / 2;
        bg.eventMode = 'static';
        bg.on('pointerdown', (e) => e.stopPropagation());

        const title = new Text({
            text: this.name,
            style: { fill: 0xffffff, fontSize: 24, fontWeight: 'bold' }
        });
        title.x = PADDING;
        title.y = PADDING;

        // libellé + signe lisible pour chaque type d'effet
        const LABELS = {
            HEALTH: 'PV max',
            MANA: 'Mana de départ',
            ATTACKDAMAGE: "Dégâts d'attaque",
            ABILITYPOWER: 'Puissance des sorts',
            ATTACKSPEED: "Vitesse d'attaque",
            ARMOR: 'Armure',
            MAGICRESIST: 'Résistance magique',
        };

        const effectLines = this.effect.map(effect => {
            const label = LABELS[effect.type] ?? effect.type;
            const value = effect.value ?? effect.armor ?? effect.magicResist ?? 0;
            const sign = value >= 0 ? '+' : '';
            return `${label} : ${sign}${value}`;
        });

        const statsText = new Text({
            text: effectLines.join('\n'),
            style: {
                fill: 0xdddddd,
                fontSize: 16,
                lineHeight: 24,
                wordWrap: true,
                wordWrapWidth: PANEL_W - PADDING * 2,
            }
        });
        statsText.x = PADDING;
        statsText.y = title.y + title.height + 16;

        const descText = new Text({
            text: this.description ?? '',
            style: {
                fill: 0xaaaaaa,
                fontSize: 13,
                wordWrap: true,
                wordWrapWidth: PANEL_W - PADDING * 2,
            }
        });
        descText.x = PADDING;
        descText.y = statsText.y + statsText.height + 10;

        const closeBtn = new Text({
            text: '✕',
            style: { fill: 0xffffff, fontSize: 20 }
        });
        closeBtn.x = PANEL_W - PADDING - closeBtn.width;
        closeBtn.y = PADDING - 4;
        closeBtn.eventMode = 'static';
        closeBtn.cursor = 'pointer';
        closeBtn.on('pointerdown', (e) => {
            e.stopPropagation();
            this.closeStats();
        });

        bg.addChild(title, statsText, descText, closeBtn);
        root.addChild(overlay, bg);

        root.x = -1000
        root.y = 100

        this.layers.ui.addChild(root);
        this.statsPanel = root;
        Item.activeStatsPanel = this;
    }

    closeStats() {
        if (this.statsPanel) {
            this.layers.ui.removeChild(this.statsPanel);
            this.statsPanel.destroy({ children: true });
            this.statsPanel = null;
        }
        if (Item.activeStatsPanel === this) {
            Item.activeStatsPanel = null;
        }
    }

    static closeActiveStatsPanel() {
        if (Item.activeStatsPanel) {
            Item.activeStatsPanel.closeStats();
        }
    }

}
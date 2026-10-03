import { Container } from 'pixi.js';

export const GAME_W = 1920;
export const GAME_H = 1080;

export function createLayers(app) {
    const root = new Container();
    app.stage.addChild(root);

    // l'ordre d'ajout = l'ordre d'affichage
    const layers = { root };
    for (const name of ['background', 'board', 'units', 'ui', 'dragging']) {
        layers[name] = new Container();
        root.addChild(layers[name]);
    }
    return layers;
}

export function fitToScreen(app, root) {
    const s = Math.min(app.screen.width / GAME_W, app.screen.height / GAME_H);
    root.scale.set(s);
    root.position.set(
        (app.screen.width  - GAME_W * s) / 2,
        (app.screen.height - GAME_H * s) / 2
    );
}
import { Text } from "pixi.js";

export class Countdown {
    constructor(layers, duration = 90, x = 0, y = 0) {
        this.duration = duration;       // in seconds
        this.remaining = duration;
        this.running = false;
        this.onComplete = null;

        this.text = new Text({
            text: this.format(this.remaining),
            style: { fill: 0xffffff, fontSize: 40, fontWeight: 'bold' }
        });
        this.text.x = x;
        this.text.y = y;
        layers.ui.addChild(this.text);
    }

    start(duration = this.duration) {
        this.duration = duration;
        this.remaining = duration;
        this.running = true;
        this.updateText();
    }

    stop() {
        this.running = false;
    }

    update(deltaMS) {
        if (!this.running) return;

        this.remaining -= deltaMS / 1000;

        if (this.remaining <= 0) {
            this.remaining = 0;
            this.running = false;
            this.updateText();
            if (this.onComplete) this.onComplete();
            return;
        }

        this.updateText();
    }

    updateText() {
        this.text.text = this.format(this.remaining);
    }

    format(seconds) {
        const s = Math.ceil(seconds);
        const m = Math.floor(s / 60);
        const r = s % 60;
        return `${m}:${r.toString().padStart(2, '0')}`;
    }
}
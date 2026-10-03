export class Dragger {

    constructor(app, layers) {
        app.stage.eventMode = 'static';
        app.stage.hitArea = app.screen;

        this.onDragMove = this.onDragMove.bind(this);
        this.onDragEnd = this.onDragEnd.bind(this);

        app.stage.on('pointerup', this.onDragEnd);
        app.stage.on('pointerupoutside', this.onDragEnd);
        this.app = app;
        this.sellZone = null; // { x, y, width, height }

        this.layers = layers

        this.CLICK_THRESHOLD = 8; // px, marge de tolérance avant de considérer que c'est un drag


    }
    setArena(arena){
        this.arena = arena
    }
    setTeam(team){
        this.team = team;
    }

    setSellZone(rect){
        this.sellZone = rect;
    }

    isInSellZone(x, y){
        if (!this.sellZone) return false;
        return (
            x >= this.sellZone.x &&
            x <= this.sellZone.x + this.sellZone.width &&
            y >= this.sellZone.y &&
            y <= this.sellZone.y + this.sellZone.height
        );
    }

    onDragMove(event) {
        if (!this.dragUnit) return;

        // on mesure le déplacement total depuis le pointerdown
        const dx = event.global.x - this.startGlobal.x;
        const dy = event.global.y - this.startGlobal.y;
        if (Math.hypot(dx, dy) > this.CLICK_THRESHOLD) {
            this.moved = true;
        }

        const c = this.dragUnit.container;
        c.parent.toLocal(event.global, null, c.position);
    }

    onDragStart(event) {
        const unit = this.unit;
        const c = unit.container;
        const dragger = this.dragger;

        c.alpha = 0.5;
        dragger.dragUnit = unit;
        dragger.dragSource = this;
        dragger.lastPos = { x: c.x, y: c.y };
        dragger.startGlobal = { x: event.global.x, y: event.global.y };
        dragger.moved = false;

        dragger.app.stage.on('pointermove', dragger.onDragMove);
    }

    onDragEnd() {
        if (!this.dragUnit) return;

        this.app.stage.off('pointermove', this.onDragMove);

        const unit = this.dragUnit;

        const wasClick = !this.moved;

        unit.container.alpha = 1;
        this.dragUnit = null;

        if (wasClick) {
            unit.showStats()
            return;
        }

        const { x, y } = this.layers.root.toLocal(unit.container.getGlobalPosition());

        const wasOnArena = this.arena.getCellOfUnit(unit) !== null;


        if (this.isInSellZone(x, y)) {

            if (wasOnArena) this.arena.removeUnit(unit);

            this.team.sell(unit)

            unit.container.alpha = 1;
            this.dragUnit = null;
            return;
        }

        if (this.arena.isInRange(x, y)) {

            if (wasOnArena) {
                // Déplacement arène -> arène
                const moved = this.arena.moveUnit(unit, x, y);
                if (!moved) unit.container.position.set(this.lastPos.x, this.lastPos.y);

            } else if (this.team.canAddOne()) {

                console.log(this.team.canAddOne())
                // Arrivée depuis le banc -> arène
                this.team.removeIfBenched(unit);
                const placed = this.arena.setToClosesCell(unit, x, y);
                if (placed) {
                    this.team.addUnit(unit);
                } else {
                    unit.container.position.set(this.lastPos.x, this.lastPos.y);
                }

            } else {
                unit.container.position.set(this.lastPos.x, this.lastPos.y);
            }

        } else if (this.team.isInRange(x, y) && this.team.canAddInBench()) {
            // Retour au banc (que ce soit depuis l'arène ou ailleurs)
            if (wasOnArena) this.arena.removeUnit(unit);
            this.team.removeUnitFromEverywhere(unit);
            this.team.addUnitToBench(unit);

        } else {
            unit.container.position.set(this.lastPos.x, this.lastPos.y);
        }
    }
}

export class Dragger {

    constructor(app) {
        app.stage.eventMode = 'static';
        app.stage.hitArea = app.screen;

        this.onDragMove = this.onDragMove.bind(this);
        this.onDragEnd = this.onDragEnd.bind(this);

        app.stage.on('pointerup', this.onDragEnd);
        app.stage.on('pointerupoutside', this.onDragEnd);
        this.app = app;
        this.dragTarget = null

    }
    setArena(arena){
        this.arena = arena
    }
    setTeam(team){
        this.team = team;
    }

    onDragMove(event) {
        if (this.dragTarget) {
            this.dragTarget.parent.toLocal(event.global, null, this.dragTarget.position);
        }
    }

    onDragStart() {
        // Store a reference to the data
        // * The reason for this is because of multitouch *
        // * We want to track the movement of this particular touch *
        console.log(this)
        this.alpha = 0.5;
        this.dragger.dragTarget = this;
        this.dragger.lastPos = { x: this.dragger.dragTarget.x, y: this.dragger.dragTarget.y };
        this.dragger.app.stage.on('pointermove', this.dragger.onDragMove);
    }

    onDragEnd() {
        if (!this.dragTarget) return;

        this.app.stage.off('pointermove', this.onDragMove);

        const x = this.dragTarget.getGlobalPosition().x;
        const y = this.dragTarget.getGlobalPosition().y;

        const wasOnArena = this.arena.getCellOfUnit(this.dragTarget) !== null;

        if (this.arena.isInRange(x, y)) {

            if (wasOnArena) {
                // Déplacement arène -> arène
                const moved = this.arena.moveUnit(this.dragTarget, x, y);
                if (!moved) this.dragTarget.position.set(this.lastPos.x, this.lastPos.y);

            } else if (this.team.canAddOne()) {
                // Arrivée depuis le banc -> arène
                this.team.removeIfBenched(this.dragTarget);
                const placed = this.arena.setToClosesCell(this.dragTarget, x, y);
                if (!placed) this.dragTarget.position.set(this.lastPos.x, this.lastPos.y);

            } else {
                this.dragTarget.position.set(this.lastPos.x, this.lastPos.y);
            }

        } else if (this.team.isInRange(x, y) && this.team.canAddInBench()) {
            // Retour au banc (que ce soit depuis l'arène ou ailleurs)
            if (wasOnArena) this.arena.removeUnit(this.dragTarget);
            this.team.removeUnitFromEverywhere(this.dragTarget);
            this.team.addUnitToBench(this.dragTarget);

        } else {
            this.dragTarget.position.set(this.lastPos.x, this.lastPos.y);
        }

        this.dragTarget.alpha = 1;
        this.dragTarget = null;
    }
}

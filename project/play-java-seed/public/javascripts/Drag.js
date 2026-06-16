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
    setBench(bench){
        this.bench = bench;
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
        this.dragger.app.stage.on('pointermove', this.dragger.onDragMove);
    }

    onDragEnd() {
        if (this.dragTarget) {
            this.app.stage.off('pointermove', this.onDragMove);
            console.log(this.dragTarget.position);
            let x = this.dragTarget.getGlobalPosition().x;
            let y = this.dragTarget.getGlobalPosition().y;

            if (this.arena.isInRange(x,y)) {
                console.log("gooooo")
                this.dragTarget.parent.removeChild(this.dragTarget)
                this.bench.removeIfBenched(this.dragTarget.unit)
                this.arena.container.addChild(this.dragTarget)
                let pos = this.arena.getClosestCell(x,y)
                this.dragTarget.position.set(pos.x, pos.y)

            } else {
                if (this.bench.isInRange(x,y)) {
                    this.dragTarget.parent.removeChild(this.dragTarget)
                    this.bench.container.addChild(this.dragTarget)
                }
            }
            this.dragTarget.alpha = 1;
            this.dragTarget = null;
        }
    }
}

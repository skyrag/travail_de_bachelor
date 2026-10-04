
export class ItemDragger {

    constructor(app, layers, websocket) {
        app.stage.eventMode = 'static';
        app.stage.hitArea = app.screen;

        this.onDragMove = this.onDragMove.bind(this);
        this.onDragEnd = this.onDragEnd.bind(this);

        app.stage.on('pointerup', this.onDragEnd);
        app.stage.on('pointerupoutside', this.onDragEnd);
        this.app = app;

        this.ws = websocket

        this.layers = layers

        this.CLICK_THRESHOLD = 8; // px, marge de tolérance avant de considérer que c'est un drag

    }
    setArena(arena){
        this.arena = arena
    }
    setTeam(team){
        this.team = team;
    }

    setItemBox(box) {
        this.itemBox = box
    }

    onDragMove(event) {
        if (!this.dragItem) return;

        // on mesure le déplacement total depuis le pointerdown
        const dx = event.global.x - this.startGlobal.x;
        const dy = event.global.y - this.startGlobal.y;
        if (Math.hypot(dx, dy) > this.CLICK_THRESHOLD) {
            this.moved = true;
        }

        const sprite = this.dragItem.sprite;
        sprite.parent.toLocal(event.global, null, sprite.position);
    }

    onDragStart(event) {
        const item = this.item;
        const sprite = item.sprite;
        const dragger = this.dragger;

        console.log("ca doit drag")

        sprite.alpha = 0.5;
        dragger.dragItem = item;
        dragger.dragSource = this;
        dragger.lastPos = { x: sprite.x, y: sprite.y };
        dragger.startGlobal = { x: event.global.x, y: event.global.y };
        dragger.moved = false;

        dragger.app.stage.on('pointermove', dragger.onDragMove);
    }

    onDragEnd() {
        if (!this.dragItem) return;

        this.app.stage.off('pointermove', this.onDragMove);

        const item = this.dragItem;

        const wasClick = !this.moved;

        item.sprite.alpha = 1;
        this.dragItem = null;

        if (wasClick) {
            item.showStats()
            return;
        }

        const globalPos = item.sprite.getGlobalPosition();
        const { x, y } = this.layers.root.toLocal(globalPos);

        if (this.arena.isInRange(x, y)) {

            console.log("in arena")

            const unit = this.arena.findUnitAt(globalPos.x, globalPos.y);

            console.log(unit)

            if (unit && unit.canAddOneItem()){
                this.giveItem(item, unit)

            } else {
                item.sprite.position.set(this.lastPos.x, this.lastPos.y);
            }

        } else if (this.team.isInRange(x, y)) {
            console.log("benching in progress....")
            const unit = this.team.findBenchedUnitAt(globalPos.x,globalPos.y)
            if (unit && unit.canAddOneItem()){
                this.giveItem(item, unit)
            } else {
                item.sprite.position.set(this.lastPos.x, this.lastPos.y);
            }

        } else {
            item.sprite.position.set(this.lastPos.x, this.lastPos.y);
        }
    }

    giveItem(item, unit) {
        this.ws.giveItemToUnit(unit.id, item.name, {
            apply: () => {
                this.itemBox.removeItem(item)
                unit.addItem(item)
            },
            rollback: (reason) => {
                console.warn("action annulé :", reason);
            }
        })
    }
}
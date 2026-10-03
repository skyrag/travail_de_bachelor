import {Graphics} from "pixi.js";


export class Replay {

    constructor(team, shop, arena, fightManager) {


        this.shop = shop
        this.team = team
        this.arena = arena

        this.current = undefined
        this.last = undefined

        this.events = []
        this.pastEvent = []
        this.currentRound = undefined

        this.pastRounds = []
        this.nextRounds = []

        this.fightManager = fightManager

    }

    next() {
        if (this.current === undefined) {
            this.currentRound.fight
        }

        switch (this.current.type){
            case "BUYUNIT" :
                this.buyingAUnit(this.current)
                break;

            case "SELL" :
                this.sellingAUnit(this.current)
                break;

            case "CHANGEOBJECT" :
                this.changingObject(this.current)
                break;

            case "MOVE" :
                this.moveAUnit(this.current)
                break;

            case "REROLL" :
                this.reroll(this.current)
                break;

            case "BUYEXP" :
                this.buyingExp(this.current)
                break;
            default : console.log("Wallahi on connais pas ce type")
        }

    }

    previous() {
        if (this.last === undefined) return

        switch (this.last.type){
            case "BUYUNIT" :
                this.returnAUnit(this.last)
                break;

            case "SELL" :
                this.rewindSell(this.last)
                break;

            case "CHANGEOBJECT" :
                this.rewindChangingObjects(this.last)
                break;

            case "MOVE" :
                this.rewindMove(this.last)
                break;

            case "REROLL" :
                this.rewindReroll(this.last)
                break;

            case "BUYEXP" :
                this.rewindExp(this.last)
                break;
            default : console.log("Wallahi on connais pas ce type")
        }
    }

    continue(event){
        this.pastEvent.push(this.last)
        this.last = event
        this.current = this.events.pop()
    }

    rewind(event) {
        this.events.push(this.current)
        this.current = event
        this.last = this.pastEvent.pop()
    }

    buyingAUnit(event) {
        const unit = this.shop.units[event.unit]
        const pos = unit.shoppingSprite.position

        event.i = pos.x / (this.shop.SHOPITEMWIDTH + this.shop.SHOPITEMWIDTH/6)

        this.shop.onUnitClick(this.shop.units[event.unit])

        this.continue(event)
    }

    sellingAUnit(event) {

        const unit = this.team.units[event.unit]

        event.pos = {x : unit.container.position.x, y: unit.container.position.y}
        event.instance = unit

        if (this.arena.getCellOfUnit(unit) !== null) this.arena.removeUnit(unit);

        event.items = []
        for (const item of unit.items){
            event.items.push(item)
        }

        this.team.sell(unit)

        event.items = unit.items //TODO a revoir lorsque l'on aura implémenter les objects,

        this.continue(event)
    }

    changingObject(event) {
        //TODO lorsque l'on aura implémenter les objects

        const items = event.items
        const unit = this.team.units[event.unit]

        for (let i = 0; i < unit.items.length; i++) {
            for (let j = 0; j < items.length; j++) {
                if (unit.items[i].name === items[j].name){
                    items.splice(j, 1)
                    break
                }
            }
        }



        const item = this.team.items.removeByName(items.pop())

        event.item = item

        this.continue(event)
    }

    moveAUnit(event) {
        const unit = this.team.units[event.unit]
        let pos = {x: unit.hex.x, y: unit.hex.y }
        this.arena.setToCell(unit, event.x, event.y)
        event.x = pos.x
        event.y = pos.y
        this.continue(event)
    }

    reroll(event) {
        const units = this.shop.units
        // A voir si le reroll est considéré comme une unité entière, faudra surement faire un dto to unit
        this.shop.onButtonClick(event.units)
        event.units = units
        this.continue(event)
    }

    buyingExp(event) {
        this.team.buyExperience()
        this.continue(event)
    }

    returnAUnit(event) {
        const unit = this.team.units[event.unit]

        this.sellingAUnit(event)

        this.shop.units[unit.id] = unit
        this.shop.container.addChild(unit.container)
        unit.container.position.set(event.i * (this.shop.SHOPITEMWIDTH + this.shop.SHOPITEMWIDTH/6), 0)

        this.rewind(event)
    }

    rewindSell (event) {
        const unit = event.instance

        this.team.units[unit.id] = unit

        //TODO enlever les objects de l'équipe lorsque les objets seront implémenter

        for (const item of event.items){
            this.team.items.removeItem(item)
            unit.addItem(item)
        }


        this.rewind(event)
    }

    rewindChangingObjects(event) {
        //TODO quand on aura fait les objets
    }

    rewindMove(event){
        const unit = this.team.units[event.unit]
        let pos = {x: unit.hex.x, y: unit.hex.y }
        this.arena.setToCell(unit, event.x, event.y)
        event.x = pos.x
        event.y = pos.y

        this.rewind(event)
    }

    rewindReroll(event) {
        const units = this.shop.units
        this.shop.onButtonClick(event.units)
        event.units = units

        this.rewind(event)
    }

    rewindExp(event) {
        const exp = this.team.exp - 4
        if (exp < 0){
            this.team.exp = this.team.getExpNeeded(this.team.level - 1) + exp
            this.team.level -= 1;
        } else {
            this.team.exp = exp
        }
        this.team.gold += 4;

        this.rewind(event)
    }
}

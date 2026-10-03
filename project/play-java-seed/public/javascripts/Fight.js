import gsap from "gsap";
import {Graphics} from "pixi.js";


export class Fight {

    constructor(layers, arena) {

        this.ourTeam = [];
        this.ennemyTeam = [];
        this.tick = 0;

        this.cursor = 0;          // index du prochain event à jouer
        this.playbackTime = 0;

        this.arena = arena

        this.TICKRATE = 60;
        this.MAX_DELTA_MS = 100; // sécurité anti rattrapage brutal (cf. tab en arrière-plan)

    }



    advancePlaybackTime(deltaMS) {
        const clampedDelta = Math.min(deltaMS, this.MAX_DELTA_MS);
        this.playbackTime += clampedDelta / 1000;
        const currentTick = this.playbackTime * this.TICKRATE;
        console.log(currentTick)


        while (this.cursor < this.events.length && this.events[this.cursor].tick <= currentTick) {
            const event = this.events[this.cursor]
            switch(event.type){
                case "MOVE" :
                    this.makeMove(event.src, event.x, event.y)
                    break;
                case "ATTACK" :
                    this.makeAttack(event.src, event.target, event.damage)
                    break;
                case "ABILITY" :
                    this.makeAbility(event.src, event.groups)
                    break;

                default :
                    console.log("Error : Unknown type of event : Fight.js.nextTurn()")

            }
            this.cursor++;
        }
    }

    loadFight(ourTeam, ennemyTeam, eventsDTO) {

        //sort by turn
        this.events = eventsDTO.sort((a,b) => a.tick - b.tick);

        ourTeam.units.forEach(unit => {
            this.ourTeam[unit.id] = unit
            unit.startFight()
            unit.pos = {x : unit.container.position.x, y: unit.container.position.y}
        })

        ennemyTeam.units.forEach(unit => {
            this.ennemyTeam[unit.id] = unit
            unit.startFight()
            unit.pos = {x : unit.container.position.x, y: unit.container.position.y}
        })
    }

    makeMove(src, targetX, targetY) {

        const duration = 30

        const cell = this.arena.getCell(targetX, targetY);

        const sprite = this.arena.units[src].container

        console.log(cell)

        return gsap.to(sprite, {
            x: cell.x,
            y: cell.y,
            duration : duration / this.TICKRATE,
            ease: "power1.inOut", // accélère puis ralentit, naturel pour un déplacement
        });
    }

    makeAttack(source, enemy, damage) {

        console.log(this.arena)
        const unit = this.arena.units[source]
        const enemyUnit = this.arena.units[enemy]

        console.log(unit.attackSpeed)
        const speedOfAttack = unit.attackSpeed / 2

        const distance = 30

        unit.manaUp()

        const startX = unit.fightingSprite.position.x;
        const sprite = unit.fightingSprite
        const offset = unit.isEnemy? - distance : distance

        if (unit.range > 1) {
            const ball = new Graphics();
            console.log("lancer la boule")
            ball.circle(0, 0, 5);
            ball.fill(0xFFFFFF);

            ball.x = unit.container.position.x
            ball.y = unit.container.position.y - unit.container.height / 2


            this.arena.container.addChild(ball);

            const avgTileNb = Math.sqrt(this.arena.distanceFrom(unit.container.position, enemyUnit.container.position))/this.arena.H_SPACING

            const tickDuration = avgTileNb * 10 // 80 is tile Radius and 10 is the number of tick to fly across a single tile

            gsap.to(ball, {
                x: enemyUnit.container.position.x,
                y: enemyUnit.container.position.y - enemyUnit.container.height / 2,
                duration: tickDuration/this.TICKRATE ,
                ease: "power1.inOut", // accélère puis ralentit, naturel pour un déplacement
                onComplete: () => {
                    enemyUnit.takeDamage(damage);
                    this.arena.container.removeChild(ball)
                },
            });

            console.log("ca a lancer la boule")


            return gsap.to(sprite, {
                x: startX + offset,
                duration: (speedOfAttack / this.TICKRATE) / 2,
                ease: "power2.out",
                yoyo: true,
                repeat: 1,
            });

        }

        return gsap.to(sprite, {
            x: startX + offset,
            duration: (speedOfAttack / this.TICKRATE) / 2,
            ease: "power2.out",
            yoyo: true,
            repeat: 1,
            onComplete: () => {
                enemyUnit.takeDamage(damage);
            },
        });
    }

    makeAbility(src, groups) { //ici groups est un JSON qui contient les enemies touchés par l'effect ainsi que les dégats pris par chaque enemy, a voir si autre chose
        const distance = 30
        const unit = this.arena.units[src]
        unit.cast()

        const speedOfAttack = unit.attackSpeed / 2

        const sprite = unit.fightingSprite
        const startY = sprite.y;
        return gsap.to(sprite, {
            y: startY - distance, // "-" car en PixiJS l'axe Y va vers le bas
            duration: (speedOfAttack / this.TICKRATE) / 2,
            ease: "power2.out",
            yoyo: true,
            repeat: 1,
            onComplete: () => { //a voir a quel point c'est intérressant de faire des boucle dans cce onComplete et si ca ne bloque pas tout le thread la ou un appel asynch serait mieux ?
                for (const group of groups){
                    for (const unit of group.targets){
                        this.arena.units[unit].takeDamage(group.damage)
                    }
                }
            },
        });
    }

    checkEnd(){

        const ourAlive = this.ourTeam.filter((unit) => unit && !unit.isDead())
        const enemyAlive = this.ennemyTeam.filter((unit) => !unit.isDead())

        return ourAlive.length === 0 || enemyAlive.length === 0;

    }


}
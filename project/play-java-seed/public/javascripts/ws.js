const TYPE = {
    BUY: 'BuyUnit',
    SELL: 'SellUnit',
    MOVE: 'MoveUnit',
    GIVE: 'GiveToUnit',
    REROLL: 'RerollShop',
    EXP: 'BuyExp',
    ACK: 'Ack',
    RECO: 'Reconnect',
    PONG: 'Pong',
    PING: 'Ping',
    OK: 'ok',
    ERROR: 'error',
    FIGHTRESULT: 'FightResult',
    GAMEOVER: 'GameOver',
    GAMELOST: 'GameLost',
    SETUP: 'setup',
    ROUNDWINDOW: 'RoundWindow',
    HISTORIQUE: 'Historique',
    JOUER: 'Jouer',
};

const OPTIMISTIC_TYPES = new Set([TYPE.BUY, TYPE.SELL, TYPE.MOVE, TYPE.GIVE, TYPE.EXP]);

const DEFAULT_TIMEOUT_MS = 8000;
const RECONNECT_BASE_DELAY_MS = 1000;
const RECONNECT_MAX_DELAY_MS = 15000;

export class GameConnection {
    constructor(url) {
        this.url = url;
        this.socket = null;
        this.nextMessageId = 1;
        this.pendingMessages = new Map(); // id -> { type, payload, resolve, reject, rollback, timeoutHandle }

        this.hasConnectedOnce = false;
        this.manuallyClosed = false;
        this.reconnectAttempts = 0;
        this.reconnectTimer = null;

        this.listeners = {
            fightResult: [],
            fightReplay: [],
            roundWindow: [],
            setup: [],
            changesFromOtherUser: [],
            gameLost: [],
            gameOver: [],
            error: [],
            open: [],
            close: [],
            historique: [],
        };
    }

    connect() {
        this.manuallyClosed = false;
        this.socket = new WebSocket(this.url);

        this.socket.onopen = () => {
            this.reconnectAttempts = 0;

            // Si c'est une reconnexion (pas la toute première ouverture),
            // on demande au serveur de renvoyer le buffer des messages manqués.
            if (this.hasConnectedOnce) {
                this.requestReconnectionBuffer();
            }
            this.hasConnectedOnce = true;

            this.emit('open');
        };

        this.socket.onmessage = (event) => {
            const data = JSON.parse(event.data);
            this.handleIncoming(data);
        };

        this.socket.onclose = () => {
            this.emit('close');
            // On ne relance pas si la fermeture est volontaire (ex: fin de partie, logout)
            if (!this.manuallyClosed) {
                this.scheduleReconnect();
            }
        };

        this.socket.onerror = (err) => console.error('Erreur WebSocket:', err);
    }

    disconnect() {
        this.manuallyClosed = true;
        clearTimeout(this.reconnectTimer);
        this.socket?.close();
    }

    scheduleReconnect() {
        const delay = Math.min(
            RECONNECT_BASE_DELAY_MS * 2 ** this.reconnectAttempts,
            RECONNECT_MAX_DELAY_MS
        );
        this.reconnectAttempts++;
        this.reconnectTimer = setTimeout(() => this.connect(), delay);
    }

    getNextId() {
        return this.nextMessageId++;
    }

    getHistorique() {
        return this.send(TYPE.HISTORIQUE);
    }

    jouer() {
        return this.send(TYPE.JOUER);
    }

    // rollback : appelé si le serveur répond "error" OU si la requête timeout
    send(type, payload = {}, { optimisticApply, rollback, timeoutMs = DEFAULT_TIMEOUT_MS } = {}) {
        const id = this.getNextId();
        const message = { id, type, time: Date.now(), payload };

        if (optimisticApply) optimisticApply();

        return new Promise((resolve, reject) => {
            const timeoutHandle = setTimeout(() => {
                this.pendingMessages.delete(id);
                if (rollback) rollback('timeout');
                reject(new Error(`Timeout: pas de réponse du serveur pour le message ${id} (${type})`));
            }, timeoutMs);

            this.pendingMessages.set(id, { type, payload, resolve, reject, rollback, timeoutHandle });

            if (this.socket?.readyState === WebSocket.OPEN) {
                this.socket.send(JSON.stringify(message));
            } else {
                clearTimeout(timeoutHandle);
                this.pendingMessages.delete(id);
                if (rollback) rollback('not_connected');
                reject(new Error('WebSocket non connectée'));
            }
        });
    }

    // --- Actions du joueur ---

    buyUnit(unitId, { apply, rollback }) {
        return this.send(TYPE.BUY, { unitId }, { optimisticApply: apply, rollback });
    }

    sellUnit(unitId, { apply, rollback }) {
        return this.send(TYPE.SELL, { unitId }, { optimisticApply: apply, rollback });
    }

    moveUnit(unitId, x, y, { apply, rollback }) {
        return this.send(TYPE.MOVE, { unitId, position: { x, y } }, { optimisticApply: apply, rollback });
    }

    giveItemToUnit(unitId, itemId, { apply, rollback }) {
        return this.send(TYPE.GIVE, { unitId, itemId }, { optimisticApply: apply, rollback });
    }

    buyExp({ apply, rollback }) {
        return this.send(TYPE.EXP, {}, { optimisticApply: apply, rollback });
    }

    rerollShop() {
        return this.send(TYPE.REROLL, {});
    }

    // --- Accusés / heartbeat ---

    ackMessage(id) {
        if (id == null) return;
        this.socket.send(JSON.stringify({ id, type: TYPE.ACK, time: Date.now(), payload: {} }));
    }

    sendPong() {
        this.socket.send(JSON.stringify({ id: this.getNextId(), type: TYPE.PONG, time: Date.now(), payload: {} }));
    }

    requestReconnectionBuffer() {
        this.socket.send(JSON.stringify({ id: this.getNextId(), type: TYPE.RECO, time: Date.now(), payload: {} }));
    }

    // --- Traitement des messages entrants ---

    handleIncoming(data) {
        if (data.type === undefined && data.events !== undefined) {
            this.emit('fightReplay', data);
            return;
        }

        const { type, id } = data;

        switch (type) {
            case TYPE.OK:
                this.resolvePending(id, data.payload);
                this.ackMessage(id);
                break;

            case TYPE.ERROR: {
                const pending = this.pendingMessages.get(id);
                if (pending?.rollback) pending.rollback('server_error');
                this.rejectPending(id, data.log);
                this.ackMessage(id);
                this.emit('error', { id, log: data.log });
                break;
            }

            case TYPE.FIGHTRESULT:
                this.emit('fightResult', data.payload);
                this.ackMessage(id);
                break;

            case TYPE.ROUNDWINDOW:
                this.emit('roundWindow', data.payload);
                this.ackMessage(id);
                break;

            case TYPE.SETUP:
                this.emit('setup', { units: data.units, items: data.items, team: data.team });
                this.ackMessage(id);
                break;

            case TYPE.BUY:
            case TYPE.SELL:
            case TYPE.MOVE:
            case TYPE.GIVE:
                this.emit('changesFromOtherUser', { type, payload: data.payload });
                this.ackMessage(id);
                break;

            case TYPE.GAMELOST:
                this.emit('gameLost');
                this.ackMessage(id);
                break;

            case TYPE.GAMEOVER:
                this.emit('gameOver', data.payload);
                this.ackMessage(id);
                break;

            case TYPE.PING:
                this.sendPong();
                break;

            default:
                console.warn('Type de message inconnu reçu:', type, data);
        }
    }

    resolvePending(id, payload) {
        const pending = this.pendingMessages.get(id);
        if (pending) {
            clearTimeout(pending.timeoutHandle);
            pending.resolve(payload);
            this.pendingMessages.delete(id);
        }
    }

    rejectPending(id, log) {
        const pending = this.pendingMessages.get(id);
        if (pending) {
            clearTimeout(pending.timeoutHandle);
            pending.reject(new Error(log));
            this.pendingMessages.delete(id);
        }
    }

    on(event, callback) {
        if (this.listeners[event]) this.listeners[event].push(callback);
    }

    emit(event, data) {
        (this.listeners[event] || []).forEach(cb => cb(data));
    }
}
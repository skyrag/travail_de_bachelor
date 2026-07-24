import { GameConnection } from "./ws.js";
import { initGame, startGame } from "./main.js";

export function setupLobby(userId) {
    const url = `${window.location.protocol === "https:" ? "wss:" : "ws:"}//${window.location.host}/ws?userId=${userId}`;
    const ws = new GameConnection(url);
    ws.connect();

    ws.on("open", () => {
        document.getElementById("message").innerText = "Hello " + userId;
    });

    document.getElementById("btn-historique").addEventListener("click", () => {
        ws.getHistorique();
    });

    document.getElementById("btn-jouer").addEventListener("click", () => {
        ws.jouer();
        showView("view-waiting");
    });

    ws.on("historique", (matches) => {
        const list = document.getElementById("historique-list");
        list.innerHTML = "";
        (matches || []).forEach(item => {
            const li = document.createElement("li");
            li.textContent = JSON.stringify(item);
            list.appendChild(li);
        });
        showView("view-historique");
    });

    ws.on("setup", async (payload) => {
        showView("view-game");
        await initGame("view-game");
        startGame(payload); // payload = { units, items, team }
    });

    function showView(id) {
        document.querySelectorAll("body > div[id^='view-']")
            .forEach(d => d.style.display = "none");
        document.getElementById(id).style.display = "block";
    }
}
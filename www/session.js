import CONFIG from "./config.js";

let usuarioCache = null;
let heartbeatInterval = null;

export async function getSessionUser() {
    if (usuarioCache) return usuarioCache;
    try {
        const res = await fetch(`${CONFIG.API_BASE_URL}/auth/me`, {
            credentials: "include"
        });
        if (!res.ok) return null;
        usuarioCache = await res.json();

        iniciarHeartbeat();

        return usuarioCache;
    } catch (e) {
        console.error("Error sesión:", e);
        return null;
    }
}

export async function refreshSessionUser() {
    try {
        const res = await fetch(`${CONFIG.API_BASE_URL}/auth/me`, {
            credentials: "include"
        });
        if (!res.ok) {
            usuarioCache = null;
            return null;
        }
        usuarioCache = await res.json();
        return usuarioCache;
    } catch (e) {
        console.error("Error refrescando sesión:", e);
        return null;
    }
}

export function clearSessionCache() {
    usuarioCache = null;
}

export async function logout() {
    detenerHeartbeat();
    try {
        await fetch(`${CONFIG.API_BASE_URL}/auth/logout`, {
            method: "POST",
            credentials: "include"
        });
    } catch (e) {
        console.error("Error cerrando sesión:", e);
    }
    usuarioCache = null;
    window.location.href = "login.html";
}


function iniciarHeartbeat() {
    if (heartbeatInterval) return; 

    enviarHeartbeat();

    heartbeatInterval = setInterval(enviarHeartbeat, 60_000);

    window.addEventListener("beforeunload", detenerHeartbeat);
}

function detenerHeartbeat() {
    if (heartbeatInterval) {
        clearInterval(heartbeatInterval);
        heartbeatInterval = null;
    }
}

async function enviarHeartbeat() {
    try {
        await fetch(`${CONFIG.API_BASE_URL}/presencia/heartbeat`, {
            method: "POST",
            credentials: "include"
        });
        await refreshSessionUser();
    } catch (e) {
    }
}
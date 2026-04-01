import CONFIG from "./config.js";

let usuarioCache = null;

export async function getSessionUser() {

    if (usuarioCache) return usuarioCache;

    try {

        const res = await fetch(`${CONFIG.API_BASE_URL}/auth/me`, {
            credentials: "include"
        });

        if (!res.ok) return null;

        usuarioCache = await res.json();
        return usuarioCache;

    } catch (e) {
        console.error("Error sesión:", e);
        return null;
    }
}

export async function logout() {

    await fetch(`${CONFIG.API_BASE_URL}/auth/logout`, {
        method: "POST",
        credentials: "include"
    });

    window.location.href = "login.html";
}
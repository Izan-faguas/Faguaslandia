import CONFIG from "./config.js";

export async function logout() {

    await fetch(`${CONFIG.API_BASE_URL}/auth/logout`, {
        method: "POST",
        credentials: "include"
    });

    window.location.href = "/login.html";
}
import CONFIG from "./config.js";

async function login() {

    const email = document.getElementById("nombre").value;
    const password = document.getElementById("password").value;

    try {

        const response = await fetch(`${CONFIG.API_BASE_URL}/auth/login`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            credentials: "include",
            body: JSON.stringify({
                email: email,
                password: password
            })
        });

        if (!response.ok) {
            alert("Login incorrecto");
            return;
        }

        const usuario = await response.json();

        console.log("Usuario logueado:", usuario);

        window.location.href = "index.html";

    } catch (error) {

        console.error("Error en login:", error);
    }
}

document.getElementById("loginForm").addEventListener("submit", function(e) {
    e.preventDefault();
    login();
});
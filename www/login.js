import CONFIG from "./config.js";

const form = document.getElementById('loginForm');
const mensajeDiv = document.getElementById('mensaje');

form.addEventListener('submit', async (e) => {
    e.preventDefault();

    const data = Object.fromEntries(new FormData(form).entries());

    try {
        const response = await fetch(`${CONFIG.API_BASE_URL}/usuarios/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });

        if (!response.ok) throw new Error('Usuario o contraseña incorrectos');

        const result = await response.json();

        localStorage.setItem('usuario', JSON.stringify(result));

        mensajeDiv.textContent = `Bienvenido ${result.nombre}`;
        mensajeDiv.style.color = 'lime';

        setTimeout(() => window.location.href = 'perfil.html', 1000);

    } catch (error) {
        mensajeDiv.textContent = error.message;
        mensajeDiv.style.color = 'red';
    }
});

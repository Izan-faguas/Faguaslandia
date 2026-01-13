const form = document.getElementById('loginForm');
const mensajeDiv = document.getElementById('mensaje');

form.addEventListener('submit', async (e) => {
    e.preventDefault();

    const formData = new FormData(form);
    const data = Object.fromEntries(formData.entries());

    try {
        const response = await fetch('http://192.168.1.159:8081/usuarios/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });

        if (!response.ok) {
            throw new Error('Usuario o contraseña incorrectos');
        }

        const result = await response.json();

        // Si tu backend devuelve un token o algo similar
        if (result.token) {
            // Guarda el token en el navegador
            localStorage.setItem('token', result.token);
            localStorage.setItem('usuario', result.nombre || data.nombre);

            mensajeDiv.textContent = `Bienvenido ${result.nombre || data.nombre}`;
            mensajeDiv.style.color = 'lime';

            // Redirige al perfil o página principal
            setTimeout(() => {
                window.location.href = 'perfil.html';
            }, 1000);
        } else {
            mensajeDiv.textContent = 'Inicio de sesión exitoso (sin token)';
            mensajeDiv.style.color = 'lime';
        }
    } catch (error) {
        mensajeDiv.textContent = error.message;
        mensajeDiv.style.color = 'red';
    }
});

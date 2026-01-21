const listaJuegosDiv = document.getElementById('listaJuegos');

async function cargarJuegos() {
    try {
        // Fetch al backend
        const response = await fetch('http://10.116.192.57:8081/juegos'); 
        if (!response.ok) {
            throw new Error('No se pudieron cargar los juegos');
        }

        const juegos = await response.json();

        if (!juegos || juegos.length === 0) {
            listaJuegosDiv.textContent = 'No hay juegos disponibles';
            return;
        }

        // Limpiar contenido previo
        listaJuegosDiv.innerHTML = '';

        // Crear contenedor de tarjetas
        const contenedor = document.createElement('div');
        contenedor.className = 'juegos-contenedor';

        juegos.forEach(juego => {
            // Crear tarjeta
            const tarjeta = document.createElement('div');
            tarjeta.className = 'juego-tarjeta';

            tarjeta.innerHTML = `
                <img src="${juego.imagen_url || 'default.png'}" alt="${juego.nombre}" class="juego-img">
                <h3>${juego.nombre}</h3>
                <p>${juego.descripcion || 'Sin descripción'}</p>
                <p class="precio">${juego.precio ? juego.precio + '€' : 'Gratis'}</p>
            `;

            contenedor.appendChild(tarjeta);
        });

        listaJuegosDiv.appendChild(contenedor);

    } catch (error) {
        listaJuegosDiv.textContent = error.message;
        listaJuegosDiv.style.color = 'red';
    }
}

// Ejecutar al cargar la página
document.addEventListener('DOMContentLoaded', cargarJuegos);

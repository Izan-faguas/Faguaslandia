const API_URL = "http://192.168.1.159:8081/juegos";
const IMG_URL = "http://192.168.1.159:8081/uploads/";

document.addEventListener("DOMContentLoaded", () => {
  cargarJuegoDestacado();
  cargarJuegos();
});

/* =========================
   ⭐ JUEGO DESTACADO
========================= */
async function cargarJuegoDestacado() {
  try {
    const res = await fetch(API_URL + "/destacado");
    const juego = await res.json();

    document.getElementById("featuredGame").innerHTML = `
      <div class="featured-image">
        <img src="${IMG_URL + juego.imagen}" alt="${juego.nombre}">
      </div>

      <div class="featured-info">
        <h2>${juego.nombre}</h2>
        <p>${juego.descripcion}</p>

        <div class="featured-price">
          ${juego.descuento > 0
            ? `<span class="discount">-${juego.descuento}%</span>`
            : ""
          }
          <span class="price">${juego.precio}€</span>
        </div>
      </div>
    `;
  } catch (e) {
    console.error("Error cargando juego destacado", e);
  }
}

/* =========================
   🎮 LISTA DE JUEGOS
========================= */
async function cargarJuegos() {
  try {
    const res = await fetch(API_URL);
    const juegos = await res.json();

    const grid = document.getElementById("gameGrid");
    grid.innerHTML = "";

    juegos.forEach(juego => {
      grid.innerHTML += `
        <article class="game-card">
          <img src="${IMG_URL + juego.imagen}" alt="${juego.nombre}">
          <div class="game-info">
            <h3>${juego.nombre}</h3>
            <span>${juego.precio}€</span>
          </div>
        </article>
      `;
    });
  } catch (e) {
    console.error("Error cargando juegos", e);
  }
}

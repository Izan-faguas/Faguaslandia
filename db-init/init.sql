-- ==============================
-- BASE DE DATOS
-- ==============================
SET NAMES utf8mb4;
DROP DATABASE IF EXISTS faguaslandia;
CREATE DATABASE faguaslandia CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE faguaslandia;

-- ==============================
-- TABLA USUARIOS
-- ==============================
CREATE TABLE usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255),
    email VARCHAR(255) UNIQUE,
    password VARCHAR(255),
    avatar_url VARCHAR(255) DEFAULT 'default_avatar.png',
    fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP,
    estado ENUM('online', 'offline', 'ocupado', 'ausente') DEFAULT 'offline',
    ultima_actividad DATETIME NULL
) ENGINE=InnoDB;

-- ==============================
-- TABLA JUEGOS
-- ==============================
CREATE TABLE juegos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(100) NOT NULL,
    descripcion TEXT,
    precio DECIMAL(10,2) NOT NULL,
    imagen_url VARCHAR(255),
    categoria VARCHAR(50),
    desarrollador VARCHAR(100),
    fecha_lanzamiento DATE,
    valoracion_promedio DECIMAL(3,2) DEFAULT 0
) ENGINE=InnoDB;

-- ==============================
-- TABLA COMPRAS
-- ==============================
CREATE TABLE compras (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_usuario BIGINT NOT NULL,
    id_juego BIGINT NOT NULL,
    fecha_compra DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (id_juego) REFERENCES juegos(id) ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE (id_usuario, id_juego)
) ENGINE=InnoDB;

-- ==============================
-- TABLA AMIGOS
-- ==============================
CREATE TABLE amigos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_usuario1 BIGINT NOT NULL,
    id_usuario2 BIGINT NOT NULL,
    estado ENUM('pendiente', 'aceptado', 'bloqueado') DEFAULT 'pendiente',
    fecha_solicitud DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_usuario1) REFERENCES usuarios(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (id_usuario2) REFERENCES usuarios(id) ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE (id_usuario1, id_usuario2)
) ENGINE=InnoDB;

-- ==============================
-- TABLA MENSAJES
-- ==============================
CREATE TABLE mensajes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_emisor BIGINT NOT NULL,
    id_receptor BIGINT NOT NULL,
    contenido TEXT NOT NULL,
    fecha_envio DATETIME DEFAULT CURRENT_TIMESTAMP,
    leido BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (id_emisor) REFERENCES usuarios(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (id_receptor) REFERENCES usuarios(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ==============================
-- TABLA LOGROS
-- ==============================
CREATE TABLE logros (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    icono_url VARCHAR(255),
    tipo ENUM('compra', 'amistad', 'chat', 'reseña', 'puntuacion', 'especial') DEFAULT 'especial',
    id_juego BIGINT NULL,
    FOREIGN KEY (id_juego) REFERENCES juegos(id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ==============================
-- TABLA LOGROS_USUARIOS
-- ==============================
CREATE TABLE logros_usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_usuario BIGINT NOT NULL,
    id_logro BIGINT NOT NULL,
    fecha_desbloqueo DATETIME DEFAULT CURRENT_TIMESTAMP,
    notificado BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (id_logro) REFERENCES logros(id) ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE (id_usuario, id_logro)
) ENGINE=InnoDB;

-- ==============================
-- TABLA RESEÑAS
-- ==============================
CREATE TABLE resenas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_usuario BIGINT NOT NULL,
    id_juego BIGINT NOT NULL,
    puntuacion INT NOT NULL,
    comentario TEXT,
    fecha DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (id_juego) REFERENCES juegos(id) ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE (id_usuario, id_juego)
) ENGINE=InnoDB;

-- ==============================
-- TABLA SESIONES DE JUEGO
-- ==============================
CREATE TABLE sesiones_juego (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_usuario BIGINT NOT NULL,
    id_juego BIGINT NOT NULL,
    inicio DATETIME NOT NULL,
    fin DATETIME NULL,
    horas_jugadas DECIMAL(10,2) DEFAULT 0,
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (id_juego) REFERENCES juegos(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ==============================
-- TABLA ACTUALIZACIONES
-- ==============================
CREATE TABLE actualizaciones (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_juego BIGINT NOT NULL,
    titulo VARCHAR(200) NOT NULL,
    descripcion TEXT,
    fecha DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_juego) REFERENCES juegos(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ==============================
-- DATOS: USUARIOS
-- ==============================
INSERT INTO usuarios (id, nombre, email, password, avatar_url, fecha_registro, estado, ultima_actividad) VALUES
(1,  'Izanfg06',   'izanfaguasgarcia@gmail.com', 'Alcachofa21',     'user_1.jpg',         '2026-03-08 11:07:29', 'offline', NULL),
(2,  'esparrago',  'esparragoalvapor@yahoo.es',  'esparrago',       'default_avatar.png', '2026-03-08 11:07:29', 'offline', NULL),
(3,  'Victor',     'v.plasoler@edu.gva.es',       'Abcd1234',        'default_avatar.png', '2026-03-08 11:07:29', 'offline', NULL),
(4,  'RedNex',     'toni@gmail.com',              'Texmex',          'default_avatar.png', '2026-03-08 11:07:29', 'offline', NULL),
(8,  'Starinja02', 'ratonpoeta@gmail.com',        'Contrasena',      'default_avatar.png', '2026-05-10 15:30:32', 'offline', NULL),
(9,  'Paunav3D',   'gillgrunt@gmail.com',         'WildFireLaCabra', 'default_avatar.png', '2026-05-12 13:41:58', 'offline', NULL),
(10, 'Hiban',      'panadero@gmail.com',          'Panpan',          'user_10.jpg',        '2026-05-17 11:14:20', 'offline', NULL),
(11, 'Jorge',      'jfaguas@hotmail.com',          '1234abcd',          'user_11.jpg',        '2026-05-25 12:05:30', 'offline', NULL);

-- ==============================
-- DATOS: JUEGOS
-- ==============================
INSERT INTO juegos (id, titulo, descripcion, precio, imagen_url, categoria, desarrollador, fecha_lanzamiento, valoracion_promedio) VALUES
(1, 'Eternal Orbit', 'Eternal Orbit es un juego de supervivencia en el que debes resistir oleadas de monstruos cada vez más intensas. Pon a prueba tus reflejos y aguanta todo lo que puedas.', 30.00, 'img/Eternal_Orbit.png', 'Rogue-like', 'Antonio Novejarque Escamilla', '2026-02-17', 0.00),
(2, 'Mini Golf', 'Un juego de minigolf 2D con 15 niveles llenos de desafíos, físicas precisas y escenarios coloridos.', 7.99, 'img/MiniGolf.png', 'Estrategia', 'FaguasStudio', '2026-01-22', 0.00),
(3, 'Smashy Road', 'Un juego low poly de conducción en el que debes escapar de la policía mientras sobrevives el mayor tiempo posible.', 29.99, 'img/SmashyRoad.png', 'Acción', 'FaguasStudio', '2026-05-22', 0.00),
(4, 'Dodge Rush', 'Eres un cuadrado blanco atrapado en una arena caótica. Bloques de colores se lanzan hacia ti desde todos los ángulos: rápidos, gigantes, en zigzag, en onda... Tu único objetivo es sobrevivir el mayor tiempo posible. Sin armas, sin atajos. Solo tú, tus reflejos y el caos.', 15.00, 'img/DodgeRush.png', 'Arcade', 'FaguasStudio', '2026-05-23', 0.00);

-- ==============================
-- DATOS: COMPRAS
-- ==============================
INSERT INTO compras (id, id_usuario, id_juego, fecha_compra) VALUES
(1, 1, 2, '2026-03-18 09:12:31'),
(2, 1, 1, '2026-04-01 10:25:33'),
(3, 4, 1, '2026-04-14 11:14:39'),
(4, 1, 3, '2026-05-22 18:25:08'),
(5, 1, 4, '2026-05-23 09:12:52');


-- ==============================
-- DATOS: AMIGOS
-- ==============================
INSERT INTO amigos (id, id_usuario1, id_usuario2, estado, fecha_solicitud) VALUES
(1, 1,  4,  'aceptado', '2026-04-21 12:01:49'),
(3, 3,  4,  'aceptado', '2026-04-21 12:37:30'),
(4, 8,  1,  'aceptado', '2026-05-10 17:36:02'),
(5, 1,  3,  'aceptado', '2026-05-10 23:05:18'),
(6, 9,  1,  'aceptado', '2026-05-12 13:42:47'),
(7, 1,  10, 'aceptado', '2026-05-17 11:14:20'),
(8, 1,  11, 'aceptado', '2026-05-17 14:50:31');

-- ==============================
-- DATOS: RESEÑAS
-- ==============================
INSERT INTO resenas (id, id_usuario, id_juego, puntuacion, comentario, fecha) VALUES
(2, 1, 2, 3, 'Buen juego',      '2026-05-07 10:59:38'),
(3, 1, 1, 2, 'No esta tan mal', '2026-05-07 11:03:10');

-- ==============================
-- DATOS: ACTUALIZACIONES
-- ==============================
INSERT INTO actualizaciones (id_juego, titulo, descripcion, fecha) VALUES
(1, 'Versión 1.0 — Lanzamiento', 'Primera versión pública del juego.', '2026-02-17 10:00:00'),
(1, 'Versión 1.1 — Corrección de bugs', 'Se han corregido varios errores de colisión y el juego ya no se congela en la ronda 5.', '2026-03-10 12:00:00'),
(1, 'Versión 1.2 — Nuevo modo', 'Se añade el modo Supervivencia con oleadas infinitas.', '2026-04-05 09:00:00'),
(2, 'Versión 1.0 — Lanzamiento', 'Primera versión pública de Mini Golf.', '2026-01-22 10:00:00'),
(2, 'Versión 1.1 — Nuevos hoyos', 'Se añaden 5 hoyos nuevos al circuito principal.', '2026-02-28 11:00:00'),

(4, 'Versión 1.0 — Lanzamiento', 'Primera versión pública de Dodge Rush. Cinco tipos de enemigos disponibles: normal, rápido, grande, zigzag, onda sinusoidal y contracción.', '2026-05-23 10:00:00'),
(4, 'Versión 1.1 — Nuevos efectos', 'Se añaden efectos de partículas al morir y trails de luz en los enemigos para mejorar la legibilidad visual.', '2026-05-23 14:00:00'),
(4, 'Versión 1.2 — Equilibrio de dificultad', 'Se ajusta la tasa de aparición de enemigos y se añade un límite mínimo de intervalo para evitar que la pantalla se sature demasiado pronto.', '2026-05-23 18:00:00');


-- ==============================
-- DATOS: LOGROS — Eternal Orbit
-- ==============================
INSERT INTO logros (nombre, descripcion, icono_url, tipo, id_juego) VALUES
('Primera Orbita',     'Completa tu primera partida.',                    'logros/eternal_orbit/primera_orbita.png',     'especial',   1),
('Sin Escudos',        'Sobrevive 5 minutos sin recibir daño.',           'logros/eternal_orbit/sin_escudos.png',        'especial',   1),
('Coleccionista EO',   'Recoge 50 objetos en una sola partida.',          'logros/eternal_orbit/coleccionista_eo.png',   'especial',   1),
('Orbita Infinita',    'Alcanza la ronda 10.',                            'logros/eternal_orbit/orbita_infinita.png',    'especial',   1),
('Comprador Espacial', 'Compra Eternal Orbit.',                           'logros/eternal_orbit/comprador_espacial.png', 'compra',     1),
('Critico Espacial',   'Deja una reseña de Eternal Orbit.',               'logros/eternal_orbit/critico_espacial.png',   'reseña',     1);

-- ==============================
-- DATOS: LOGROS — Mini Golf
-- ==============================
INSERT INTO logros (id, nombre, descripcion, icono_url, tipo, id_juego) VALUES
(15, 'Hoyo 1 Completado',  'Supera el primer hoyo del circuito.',            'logros/minigolf/hoyo_1.png',  'especial', 2),
(16, 'Hoyo 2 Completado',  'Supera el segundo hoyo del circuito.',           'logros/minigolf/hoyo_2.png',  'especial', 2),
(17, 'Hoyo 3 Completado',  'Supera el tercer hoyo del circuito.',            'logros/minigolf/hoyo_3.png',  'especial', 2),
(18, 'Hoyo 4 Completado',  'Supera el cuarto hoyo del circuito.',            'logros/minigolf/hoyo_4.png',  'especial', 2),
(19, 'Hoyo 5 Completado',  'Supera el quinto hoyo del circuito.',            'logros/minigolf/hoyo_5.png',  'especial', 2),
(20, 'Hoyo 6 Completado',  'Supera el sexto hoyo del circuito.',             'logros/minigolf/hoyo_6.png',  'especial', 2),
(21, 'Hoyo 7 Completado',  'Supera el séptimo hoyo del circuito.',           'logros/minigolf/hoyo_7.png',  'especial', 2),
(22, 'Hoyo 8 Completado',  'Supera el octavo hoyo del circuito.',            'logros/minigolf/hoyo_8.png',  'especial', 2),
(23, 'Hoyo 9 Completado',  'Supera el noveno hoyo del circuito.',            'logros/minigolf/hoyo_9.png',  'especial', 2),
(24, 'Hoyo 10 Completado', 'Supera el décimo hoyo del circuito.',            'logros/minigolf/hoyo_10.png', 'especial', 2),
(25, 'Hoyo 11 Completado', 'Supera el undécimo hoyo del circuito.',          'logros/minigolf/hoyo_11.png', 'especial', 2),
(26, 'Hoyo 12 Completado', 'Supera el duodécimo hoyo del circuito.',         'logros/minigolf/hoyo_12.png', 'especial', 2),
(27, 'Hoyo 13 Completado', 'Supera el decimotercer hoyo del circuito.',      'logros/minigolf/hoyo_13.png', 'especial', 2),
(28, 'Hoyo 14 Completado', 'Supera el decimocuarto hoyo del circuito.',      'logros/minigolf/hoyo_14.png', 'especial', 2),
(29, 'Hoyo 15 Completado', 'Supera el decimoquinto hoyo del circuito.',      'logros/minigolf/hoyo_15.png', 'especial', 2);

-- ==============================
-- DATOS: LOGROS — Smashy Road
-- ==============================
INSERT INTO logros (id, nombre, descripcion, icono_url, tipo, id_juego) VALUES
(30, 'Primera Fuga',        'Inicia tu primera partida en SmashyRoad.',     'logros/smashyroad/primera_fuga.png',         'especial',   3),
(31, '5 Policías Caídos',   'Destruye 5 coches de policía en total.',       'logros/smashyroad/5_policias_caidos.png',    'especial',   3),
(32, '10 Policías Caídos',  'Destruye 10 coches de policía en total.',      'logros/smashyroad/10_policias_caidos.png',   'especial',   3),
(33, '20 Policías Caídos',  'Destruye 20 coches de policía en total.',      'logros/smashyroad/20_policias_caidos.png',   'especial',   3),
(34, 'Fugitivo Novel',      'Alcanza 1.000 puntos en una sesión.',          'logros/smashyroad/fugitivo_novel.png',       'puntuacion', 3),
(35, 'Fugitivo Experto',    'Alcanza 5.000 puntos en una sesión.',          'logros/smashyroad/fugitivo_experto.png',     'puntuacion', 3),
(36, 'Fugitivo Legendario', 'Alcanza 10.000 puntos en una sesión.',         'logros/smashyroad/fugitivo_legendario.png',  'puntuacion', 3),
(37, 'Piel de Acero I',     'Sobrevive 1 minuto sin recibir daño.',         'logros/smashyroad/piel_de_acero_1.png',      'especial',   3),
(38, 'Piel de Acero II',    'Sobrevive 2 minutos sin recibir daño.',        'logros/smashyroad/piel_de_acero_2.png',      'especial',   3),
(39, 'Intocable',           'Sobrevive 5 minutos sin recibir daño.',        'logros/smashyroad/intocable.png',            'especial',   3);

-- ==============================
-- LOGROS: DODGE RUSH
-- ==============================
INSERT INTO logros (id, nombre, descripcion, icono_url, tipo, id_juego) VALUES
(40, 'Primeros pasos',       'Sobrevive 15 segundos.',                    'logros/dodge_rush/primeros_pasos.png',        'especial', 4),
(41, 'Medio minuto',         'Sobrevive 30 segundos.',                    'logros/dodge_rush/medio_minuto.png',          'especial', 4),
(42, 'Sin prisa',            'Sobrevive 45 segundos.',                    'logros/dodge_rush/sin_prisa.png',             'especial', 4),
(43, 'Un minuto exacto',     'Sobrevive 60 segundos.',                    'logros/dodge_rush/un_minuto_exacto.png',      'especial', 4),
(44, 'Calentando motores',   'Sobrevive 75 segundos.',                    'logros/dodge_rush/calentando_motores.png',    'especial', 4),
(45, 'Minuto y medio',       'Sobrevive 90 segundos.',                    'logros/dodge_rush/minuto_y_medio.png',        'especial', 4),
(46, 'Casi dos minutos',     'Sobrevive 105 segundos.',                   'logros/dodge_rush/casi_dos_minutos.png',      'especial', 4),
(47, 'Dos minutos',          'Sobrevive 120 segundos.',                   'logros/dodge_rush/dos_minutos.png',           'especial', 4),
(48, 'Estatua de piedra I',  'Sobrevive 10 segundos sin moverte.',        'logros/dodge_rush/estatua_piedra_1.png',      'especial', 4),
(49, 'Estatua de piedra II', 'Sobrevive 20 segundos sin moverte.',        'logros/dodge_rush/estatua_piedra_2.png',      'especial', 4),
(50, 'Estatua de piedra III','Sobrevive 30 segundos sin moverte.',        'logros/dodge_rush/estatua_piedra_3.png',      'especial', 4);

-- ==============================
-- DATOS: LOGROS GLOBALES
-- ==============================
INSERT INTO logros (nombre, descripcion, icono_url, tipo, id_juego) VALUES
('Primer juego',   'Compra tu primer juego en Faguaslandia', 'logros/globales/primer_juego.png',   'compra', NULL),
('Coleccionista',  'Compra 3 juegos en total',               'logros/globales/coleccionista.png',  'compra', NULL),
('Fanatico',       'Compra 5 juegos en total',               'logros/globales/fanatico.png',       'compra', NULL);

INSERT INTO logros (nombre, descripcion, icono_url, tipo, id_juego) VALUES
('Primer amigo',         'Añade a tu primer amigo',   'logros/globales/primer_amigo.png',         'amistad', NULL),
('Bien acompañado',      'Ten 3 amigos a la vez',     'logros/globales/bien_acompanado.png',      'amistad', NULL),
('El alma de la fiesta', 'Ten 5 amigos a la vez',     'logros/globales/alma_de_la_fiesta.png',    'amistad', NULL);

INSERT INTO logros (nombre, descripcion, icono_url, tipo, id_juego) VALUES
('Critico novel',       'Escribe tu primera reseña',  'logros/globales/critico_novel.png',        'reseña', NULL),
('Critico experto',     'Escribe 3 reseñas en total', 'logros/globales/critico_experto.png',      'reseña', NULL),
('Critico profesional', 'Escribe 5 reseñas en total', 'logros/globales/critico_profesional.png',  'reseña', NULL);

INSERT INTO logros (nombre, descripcion, icono_url, tipo, id_juego) VALUES
('Primera hora',    'Juega tu primera hora en total', 'logros/globales/primera_hora.png',    'puntuacion', NULL),
('Maratonista',     'Acumula 10 horas de juego',      'logros/globales/maratonista.png',     'puntuacion', NULL),
('Sin vida social', 'Acumula 50 horas de juego',      'logros/globales/sin_vida_social.png', 'puntuacion', NULL);
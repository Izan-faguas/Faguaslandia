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
-- DATOS: USUARIOS
-- ==============================
INSERT INTO usuarios (id, nombre, email, password, avatar_url, fecha_registro, estado, ultima_actividad) VALUES
(1,  'Izanfg06',   'izanfaguasgarcia@gmail.com', 'Alcachofa21',     'user_1.jpg',         '2026-03-08 11:07:29', 'offline', NULL),
(2,  'esparrago',  'esparragoalvapor@yahoo.es',  'esparrago',       'default_avatar.png', '2026-03-08 11:07:29', 'offline', NULL),
(3,  'Victor',     'v.plasoler@edu.gva.es',       'Abcd1234',        'default_avatar.png', '2026-03-08 11:07:29', 'offline', NULL),
(4,  'RedNex',     'toni@gmail.com',              'Texmex',          'default_avatar.png', '2026-03-08 11:07:29', 'offline', NULL),
(8,  'Starinja02', 'ratonpoeta@gmail.com',        'Contrasena',      'default_avatar.png', '2026-05-10 15:30:32', 'offline', NULL),
(9,  'Paunav3D',   'gillgrunt@gmail.com',         'WildFireLaCabra', 'default_avatar.png', '2026-05-12 13:41:58', 'offline', NULL),
(10, 'Hiban',      'panadero@gmail.com',          'Panpan',          'user_10.jpg',        '2026-05-17 11:14:20', 'offline', NULL);

-- ==============================
-- DATOS: JUEGOS
-- ==============================
INSERT INTO juegos (id, titulo, descripcion, precio, imagen_url, categoria, desarrollador, fecha_lanzamiento, valoracion_promedio) VALUES
(1, 'Eternal Orbit', 'Un juego simple sin mucho esfuerzo realizado.', 30.00, 'img/Eternal_Orbit.png', 'Rogue-like', 'Antonio Novejarque Escamilla', '2026-02-17', 2.00),
(2, 'Mini Golf',     'Un juego de minigolf 2D.',                       7.99,  'img/algo.png',          'Estrategia', 'FaguasStudio',                '2026-01-22', 3.00);

-- ==============================
-- DATOS: COMPRAS
-- ==============================
INSERT INTO compras (id, id_usuario, id_juego, fecha_compra) VALUES
(1, 1, 2, '2026-03-18 09:12:31'),
(2, 1, 1, '2026-04-01 10:25:33'),
(3, 4, 1, '2026-04-14 11:14:39');

-- ==============================
-- DATOS: AMIGOS
-- ==============================
INSERT INTO amigos (id, id_usuario1, id_usuario2, estado, fecha_solicitud) VALUES
(1, 1,  4,  'aceptado', '2026-04-21 12:01:49'),
(3, 3,  4,  'aceptado', '2026-04-21 12:37:30'),
(4, 8,  1,  'aceptado', '2026-05-10 17:36:02'),
(5, 1,  3,  'aceptado', '2026-05-10 23:05:18'),
(6, 9,  1,  'aceptado', '2026-05-12 13:42:47'),
(7, 1,  10, 'aceptado', '2026-05-17 11:14:20');

-- ==============================
-- DATOS: RESEÑAS
-- ==============================
INSERT INTO resenas (id, id_usuario, id_juego, puntuacion, comentario, fecha) VALUES
(2, 1, 2, 3, 'Buen juego',      '2026-05-07 10:59:38'),
(3, 1, 1, 2, 'No esta tan mal', '2026-05-07 11:03:10');

-- ==============================
-- DATOS: LOGROS — Eternal Orbit
-- ==============================
INSERT INTO logros (nombre, descripcion, icono_url, tipo, id_juego) VALUES
('Primera Orbita',     'Completa tu primera partida.',                    '🚀', 'especial',   1),
('Sin Escudos',        'Sobrevive 5 minutos sin recibir daño.',           '🛡️', 'especial',   1),
('Coleccionista EO',   'Recoge 50 objetos en una sola partida.',          '📦', 'especial',   1),
('Orbita Infinita',    'Alcanza la ronda 10.',                            '🌌', 'especial',   1),
('Comprador Espacial', 'Compra Eternal Orbit.',                           '🛒', 'compra',     1),
('Critico Espacial',   'Deja una reseña de Eternal Orbit.',               '✍️', 'reseña',     1);

-- ==============================
-- DATOS: LOGROS — Mini Golf
-- ==============================
INSERT INTO logros (nombre, descripcion, icono_url, tipo, id_juego) VALUES
('Hole in One',        'Consigue un hoyo en uno.',                        '⛳', 'especial',   2),
('Eagle',              'Termina un hoyo con 2 golpes menos del par.',     '🦅', 'especial',   2),
('Ronda Perfecta',     'Completa una ronda sin sobrepasar el par.',       '🏆', 'especial',   2),
('Golfista Novato',    'Completa tu primera partida de Mini Golf.',       '🏌️', 'especial',   2),
('Comprador Golfista', 'Compra Mini Golf.',                               '🛒', 'compra',     2),
('Critico Golfista',   'Deja una reseña de Mini Golf.',                   '✍️', 'reseña',     2);

-- ==============================
-- DATOS: LOGROS GLOBALES
-- ==============================
INSERT INTO logros (nombre, descripcion, icono_url, tipo, id_juego) VALUES
('Primer juego',   'Compra tu primer juego en Faguaslandia', '🛒', 'compra', NULL),
('Coleccionista',  'Compra 3 juegos en total',               '📦', 'compra', NULL),
('Fanatico',       'Compra 5 juegos en total',               '🎮', 'compra', NULL);

INSERT INTO logros (nombre, descripcion, icono_url, tipo, id_juego) VALUES
('Primer amigo',         'Añade a tu primer amigo',   '🤝', 'amistad', NULL),
('Bien acompañado',      'Ten 3 amigos a la vez',     '👫', 'amistad', NULL),
('El alma de la fiesta', 'Ten 5 amigos a la vez',     '🎉', 'amistad', NULL);

INSERT INTO logros (nombre, descripcion, icono_url, tipo, id_juego) VALUES
('Critico novel',       'Escribe tu primera reseña', '✍️', 'reseña', NULL),
('Critico experto',     'Escribe 3 reseñas en total','📝', 'reseña', NULL),
('Critico profesional', 'Escribe 5 reseñas en total','🏅', 'reseña', NULL);

INSERT INTO logros (nombre, descripcion, icono_url, tipo, id_juego) VALUES
('Primera hora',    'Juega tu primera hora en total', '⏱️', 'puntuacion', NULL),
('Maratonista',     'Acumula 10 horas de juego',      '🏃', 'puntuacion', NULL),
('Sin vida social', 'Acumula 50 horas de juego',      '🛋️', 'puntuacion', NULL);
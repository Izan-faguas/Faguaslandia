-- ==============================
-- BASE DE DATOS
-- ==============================
DROP DATABASE IF EXISTS faguaslandia;
CREATE DATABASE faguaslandia CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE faguaslandia;

-- ==============================
-- TABLA USUARIOS
-- ==============================
CREATE TABLE usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255),
    email VARCHAR(255),
    password VARCHAR(255),
    avatar_url VARCHAR(255) DEFAULT 'default_avatar.png',
    fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP,
    estado ENUM('online', 'offline', 'ocupado', 'ausente') DEFAULT 'offline'
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
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (id_juego) REFERENCES juegos(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
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
    FOREIGN KEY (id_usuario1) REFERENCES usuarios(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (id_usuario2) REFERENCES usuarios(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
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
    FOREIGN KEY (id_emisor) REFERENCES usuarios(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (id_receptor) REFERENCES usuarios(id)
        ON DELETE CASCADE ON UPDATE CASCADE
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
    FOREIGN KEY (id_juego) REFERENCES juegos(id)
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB;

-- ==============================
-- TABLA LOGROS_USUARIOS
-- ==============================
CREATE TABLE logros_usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_usuario BIGINT NOT NULL,
    id_logro BIGINT NOT NULL,
    fecha_desbloqueo DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (id_logro) REFERENCES logros(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
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
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (id_juego) REFERENCES juegos(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE (id_usuario, id_juego)
) ENGINE=InnoDB;

-- ==============================
-- DATOS DE PRUEBA
-- ==============================
INSERT INTO usuarios (nombre, email, password)
VALUES
('Izanfg06', 'izanfaguasgarcia@gmail.com', 'Alcachofa21'),
('esparrago', 'esparragoalvapor@yahoo.es', 'esparrago'),
('Victor', 'v.plasoler@edu.gva.es', 'Abcd1234'),
('RedNex', 'toni@gmail.com', 'Texmex'),
('a','a@a.com','a');


INSERT INTO juegos (
    titulo, descripcion, precio, imagen_url, categoria, desarrollador, fecha_lanzamiento, valoracion_promedio
) VALUES (
    'Eternal Orbit', 
    'Un juego simple sin mucho esferzo realizado.', 
    30.00, 
    'img/Eternal_Orbit.png', 
    'Rogue-like', 
    'Antonio Novejarque Escamilla', 
    '2026-02-17', 
    4.2
);


INSERT INTO juegos (
    titulo, descripcion, precio, imagen_url, categoria, desarrollador, fecha_lanzamiento, valoracion_promedio
) VALUES (
    'Mini Golf', 
    'Un juego de minigolf 2D.', 
    7.99, 
    'img/algo.png', 
    'Estrategia', 
    'FaguasStudio', 
    '2026-01-22', 
    3.7
);



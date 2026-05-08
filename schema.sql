-- Script SQL para la base de datos de MovilidadMDQ
-- Materia: Programación 3 (UTN)

CREATE DATABASE IF NOT EXISTS movilidadmdq;
USE movilidadmdq;

-- 1. Tabla de Usuarios
CREATE TABLE IF NOT EXISTS usuario (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Tabla de Tarifas (Para parametrizar precios sin tocar código)
CREATE TABLE IF NOT EXISTS tarifa (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tipo_transporte VARCHAR(50) NOT NULL UNIQUE, -- TAXI, UBER, DIDI
    precio_base DECIMAL(10, 2) NOT NULL,
    precio_por_km DECIMAL(10, 2) NOT NULL,
    ultima_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Tabla de Historial de Viajes
CREATE TABLE IF NOT EXISTS viaje (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    origen VARCHAR(255) NOT NULL,
    destino VARCHAR(255) NOT NULL,
    distancia_en_metros BIGINT NOT NULL,
    tiempo_estimado_min INT NOT NULL,
    precio_taxi DECIMAL(10, 2) NOT NULL,
    precio_min_app DECIMAL(10, 2) NOT NULL,
    precio_max_app DECIMAL(10, 2) NOT NULL,
    fecha_hora DATETIME DEFAULT CURRENT_TIMESTAMP,
    usuario_id BIGINT NOT NULL,
    CONSTRAINT fk_viaje_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==========================================
-- DATOS INICIALES DE EJEMPLO
-- ==========================================

-- Usuarios de prueba
INSERT INTO usuario (username, password, email) VALUES 
('admin', 'admin123', 'admin@movilidadmdq.com.ar'),
('anibal', 'password123', 'anibal@example.com');

-- Tarifas base iniciales (Valores de Mar del Plata Mayo 2026)
INSERT INTO tarifa (tipo_transporte, precio_base, precio_por_km) VALUES 
('TAXI_DIURNO', 2250.00, 937.50),  -- 150 por ficha (160m) -> ~937.50 por km
('TAXI_NOCTURNO', 2700.00, 1125.00), -- 180 por ficha (160m) -> ~1125 por km
('UBER_BASE', 0.00, 0.00),          -- Calculado dinámicamente sobre taxi
('DIDI_BASE', 0.00, 0.00);          -- Calculado dinámicamente sobre taxi

-- Ejemplo de un viaje guardado
INSERT INTO viaje (origen, destino, distancia_en_metros, tiempo_estimado_min, precio_taxi, precio_min_app, precio_max_app, usuario_id) 
VALUES ('Luro y Mitre, Mar del Plata', 'Juan B. Justo 1500, Mar del Plata', 5200, 15, 7125.50, 6050.00, 8500.00, 2);

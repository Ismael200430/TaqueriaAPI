CREATE DATABASE IF NOT EXISTS taqueria_db;
USE taqueria_db;

-- 1. Usuarios (Meseros / Cajeros)
CREATE TABLE usuario (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    rol VARCHAR(30) NOT NULL, -- 'Mesero', 'Cajero', 'Admin'
    pin_acceso VARCHAR(10) NOT NULL,
    activo BOOLEAN DEFAULT TRUE
);

-- 2. Categorías del Menú
CREATE TABLE categoria (
    id_categoria INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL
);

-- 3. Productos (Tacos, Bebidas, Gringas)
CREATE TABLE producto (
    id_producto INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    precio DECIMAL(10, 2) NOT NULL,
    disponible BOOLEAN DEFAULT TRUE,
    id_categoria INT NOT NULL,
    imagen VARCHAR(100) NOT NULL DEFAULT 'taco_pastor',
    FOREIGN KEY (id_categoria) REFERENCES categoria(id_categoria)
);

-- 4. Órdenes (Comandas)
CREATE TABLE orden (
    id_orden INT AUTO_INCREMENT PRIMARY KEY,
    numero_mesa INT NOT NULL,
    fecha_hora DATETIME DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(20) DEFAULT 'Abierta', -- 'Abierta', 'Pagada', 'Cancelada'
    total DECIMAL(10, 2) DEFAULT 0.00,
    id_usuario INT NOT NULL,
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
);

-- 5. Detalle de la Orden
CREATE TABLE detalle_orden (
    id_detalle INT AUTO_INCREMENT PRIMARY KEY,
    cantidad INT NOT NULL,
    subtotal DECIMAL(10, 2) NOT NULL,
    notas VARCHAR(200),
    id_orden INT NOT NULL,
    id_producto INT NOT NULL,
    FOREIGN KEY (id_orden) REFERENCES orden(id_orden) ON DELETE CASCADE,
    FOREIGN KEY (id_producto) REFERENCES producto(id_producto)
);

-- Datos semilla iniciales para probar
INSERT INTO usuario (nombre, rol, pin_acceso, activo) VALUES ('Carlos Pérez', 'Mesero', '1234', TRUE);
INSERT INTO categoria (nombre) VALUES ('Tacos'), ('Gringas'), ('Bebidas');
INSERT INTO producto (nombre, precio, disponible, id_categoria, imagen) VALUES 
('Taco al Pastor', 18.00, TRUE, 1, 'taco_pastor'),
('Taco de Bistec', 24.00, TRUE, 1, 'taco_pastor'),
('Gringa de Pastor', 45.00, TRUE, 2, 'taco_pastor'),
('Refresco 600ml', 25.00, TRUE, 3, 'taco_pastor');

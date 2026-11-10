DROP DATABASE IF EXISTS hotel_db;
CREATE DATABASE IF NOT EXISTS hotel_db;
USE hotel_db;

-- -----------------------------------------------------------------------------
-- 1. TABLA BASE: Usuario
-- -----------------------------------------------------------------------------
CREATE TABLE Usuario (
    usuarioId BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(50) NOT NULL,
    apellido VARCHAR(50) NOT NULL,
    email VARCHAR(50) NOT NULL UNIQUE KEY,
    dni VARCHAR(50) NOT NULL UNIQUE KEY,
    telefono VARCHAR(50) NOT NULL,
    nombreUsuario VARCHAR(50) NOT NULL,
    contrasenia VARCHAR(50) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE, -- Baja lógica (TRUE: activo, FALSE: inactivo)
    tipoUsuario ENUM('administrador', 'conserje', 'pasajero') not null
);

-- -----------------------------------------------------------------------------
-- 2. TABLAS HIJAS
-- -----------------------------------------------------------------------------

-- Rol: Administrador
CREATE TABLE Administrador (
    usuarioId BIGINT UNSIGNED PRIMARY KEY,
    CONSTRAINT fk_administrador_usuario FOREIGN KEY (usuarioId)
        REFERENCES Usuario(usuarioId) ON DELETE CASCADE
);

-- Rol: Conserje
CREATE TABLE Conserje (
    usuarioId BIGINT UNSIGNED PRIMARY KEY,
    turno ENUM('mañana', 'tarde', 'noche') NOT NULL,
    salario DECIMAL(9, 2) NOT NULL,
    CONSTRAINT fk_conserje_usuario FOREIGN KEY (usuarioId)
        REFERENCES Usuario(usuarioId) ON DELETE CASCADE
);

-- Rol: Pasajero (Cliente que realiza reservas)
CREATE TABLE Pasajero (
    usuarioId BIGINT UNSIGNED PRIMARY KEY,
    origen VARCHAR(50) NOT NULL,
    domicilioOrigen VARCHAR(50) NOT NULL,
    CONSTRAINT fk_pasajero_usuario FOREIGN KEY (usuarioId)
        REFERENCES Usuario(usuarioId) ON DELETE CASCADE
);

-- -----------------------------------------------------------------------------
-- 3. TABLA: Habitacion
-- -----------------------------------------------------------------------------
CREATE TABLE Habitacion (
    habitacionId BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    numeroHabitacion INT UNSIGNED UNIQUE KEY NOT NULL,
    tipoHabitacion ENUM('habitacion_simple', 'habitacion_doble', 'suite') NOT NULL,
    estado ENUM('disponible', 'ocupada', 'reservada', 'no_disponible') NOT NULL,
    motivoBloqueo ENUM('limpieza', 'reparacion', 'desinfeccion', 'otro') NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE -- Baja lógica para inhabilitar habitaciones
);

-- -----------------------------------------------------------------------------
-- 4. TABLA: Reserva
-- -----------------------------------------------------------------------------
CREATE TABLE Reserva (
    reservaId BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    precio BIGINT UNSIGNED NOT NULL,
    cantidadPasajeros TINYINT UNSIGNED NOT NULL,
    estadoReserva ENUM('activa', 'cumplida', 'cancelada') NOT NULL,
    duracionEstadia INT UNSIGNED NOT NULL,
    diaReservado DATETIME NOT NULL,
    checkIn DATETIME NULL,
    checkOut DATETIME NULL,
    habitacion BIGINT UNSIGNED NOT NULL,
    pasajero BIGINT UNSIGNED NOT NULL,

    CONSTRAINT fk_reserva_habitacion FOREIGN KEY (habitacion)
        REFERENCES Habitacion(habitacionId),
    CONSTRAINT fk_reserva_usuario FOREIGN KEY (pasajero)
        REFERENCES Pasajero(usuarioId)
);

USE hotel_db;

-- =============================================================================
-- 1. CARGA DE USUARIOS
-- =============================================================================
INSERT INTO Usuario (nombre, apellido, email, dni, telefono, nombreUsuario, contrasenia, activo, tipoUsuario) VALUES
('Carlos', 'Gómez', 'carlos.gomez@email.com', '30111222', '2234111111', 'admin1', 'admin123', true, 'administrador'),
('Mariana', 'López', 'mariana.lopez@email.com', '31222333', '2234222222', 'mlopez', 'conserje123', true, 'conserje'),
('Roberto', 'Fernández', 'roberto.f@email.com', '32333444', '2234333333', 'rfernandez', 'conserje456', true, 'conserje'),
('Esteban', 'Morales', 'esteban.morales@email.com', '33444555', '2234444444', 'emorales', 'conserje789', true, 'conserje'),
('Lucía', 'Pérez', 'lucia.perez@email.com', '34555666', '2234555555', 'lperez', 'conserje012', true, 'conserje'),
('Gonzalo', 'Martínez', 'gonzalo.m@email.com', '35666777', '2234666666', 'gmartinez', 'conserje345', true, 'conserje'),
('Juan', 'Rodríguez', 'juan.rodriguez@email.com', '36777888', '2234777777', 'jrodriguez', 'pasajero1', true, 'pasajero'),
('Ana', 'Sánchez', 'ana.sanchez@email.com', '37888999', '2234888888', 'asanchez', 'pasajero2', true, 'pasajero'),
('Diego', 'Romero', 'diego.romero@email.com', '38999000', '2234999999', 'dromero', 'pasajero3', true, 'pasajero'),
('Sofia', 'Sánchez', 'sofia.222@email.com', '39000111', '2234000111', 'sdiaz', 'pasajero4', true, 'pasajero'),
('Lucas', 'Álvarez', 'lucas.alvarez@email.com', '40111222', '2234111222', 'lalvarez', 'pasajero5', true, 'pasajero'),
('Valentina', 'Torres', 'valentina.t@email.com', '41222333', '2234222333', 'vtorres', 'pasajero6', true, 'pasajero'),
('Mateo', 'Ruiz', 'mateo.ruiz@email.com', '42333444', '2234333444', 'mruiz', 'pasajero7', true, 'pasajero'),
('Camila', 'Benítez', 'camila.b@email.com', '43444555', '2234444555', 'cbenitez', 'pasajero8', true, 'pasajero'),
('Joaquín', 'Acosta', 'joaquin.a@email.com', '44555666', '2234555666', 'jacosta', 'pasajero9', true, 'pasajero');

-- =============================================================================
-- 2. CARGA DE ROLES
-- =============================================================================
INSERT INTO Administrador (usuarioId) VALUES (1);

INSERT INTO Conserje (usuarioId, turno, salario) VALUES
(2, 'mañana', 450000.00),
(3, 'tarde', 460000.00),
(4, 'mañana', 450000.00),
(5, 'tarde', 460000.00),
(6, 'noche', 490000.00);

INSERT INTO Pasajero (usuarioId, origen, domicilioOrigen) VALUES
(7, 'Buenos Aires', 'Av. Corrientes 1234'),
(8, 'Córdoba', 'Av. Colón 567'),
(9, 'Rosario', 'Calle Italia 890'),
(10, 'Mendoza', 'Av. San Martín 432'),
(11, 'La Plata', 'Calle 7 Nro 1200'),
(12, 'Tucumán', 'San Martín 654'),
(13, 'Salta', 'Belgrano 321'),
(14, 'Neuquén', 'Rivadavia 987'),
(15, 'Santa Fe', 'Urquiza 456');

-- =============================================================================
-- 3. CARGA DE HABITACIONES
-- =============================================================================
INSERT INTO Habitacion (numeroHabitacion, tipoHabitacion, estado, motivoBloqueo, activo) VALUES
(101, 'habitacion_simple', 'disponible', NULL, true),
(102, 'habitacion_simple', 'ocupada', NULL, true),
(103, 'habitacion_simple', 'reservada', NULL, true),
(104, 'habitacion_simple', 'no_disponible', 'limpieza', true),
(105, 'habitacion_simple', 'disponible', NULL, true),
(201, 'habitacion_doble', 'disponible', NULL, true),
(202, 'habitacion_doble', 'ocupada', NULL, true),
(203, 'habitacion_doble', 'reservada', NULL, true),
(204, 'habitacion_doble', 'no_disponible', 'reparacion', true),
(205, 'habitacion_doble', 'disponible', NULL, true),
(301, 'suite', 'disponible', NULL, true),
(302, 'suite', 'ocupada', NULL, true),
(303, 'suite', 'reservada', NULL, true),
(304, 'suite', 'no_disponible', 'desinfeccion', true),
(305, 'suite', 'disponible', NULL, true);

-- =============================================================================
-- 4. CARGA DE RESERVAS
-- =============================================================================
INSERT INTO Reserva (precio, cantidadPasajeros, estadoReserva, duracionEstadia, diaReservado, checkIn, checkOut, habitacion, pasajero) VALUES
(40000, 1, 'cumplida', 2, '2026-10-01 10:00:00', '2026-10-01 14:00:00', '2026-10-03 10:00:00', 1, 7),
(80000, 2, 'activa', 3, '2026-11-01 11:30:00', '2026-11-05 15:00:00', NULL, 2, 8),
(120000, 2, 'activa', 2, '2026-11-02 09:15:00', NULL, NULL, 3, 9),
(45000, 1, 'cancelada', 1, '2026-10-10 16:00:00', NULL, NULL, 4, 10),
(50000, 1, 'cumplida', 2, '2026-10-12 12:00:00', '2026-10-14 14:00:00', '2026-10-16 10:00:00', 5, 11),
(90000, 2, 'cumplida', 3, '2026-10-15 08:00:00', '2026-10-20 14:00:00', '2026-10-23 10:00:00', 6, 12),
(150000, 3, 'activa', 4, '2026-11-03 14:20:00', '2026-11-06 16:00:00', NULL, 7, 13),
(100000, 2, 'activa', 2, '2026-11-04 18:00:00', NULL, NULL, 8, 14),
(95000, 2, 'cancelada', 3, '2026-10-18 11:00:00', NULL, NULL, 9, 15),
(60000, 1, 'cumplida', 2, '2026-10-22 10:00:00', '2026-10-25 14:00:00', '2026-10-27 10:00:00', 10, 7),
(200000, 4, 'activa', 4, '2026-11-05 13:00:00', NULL, NULL, 11, 8),
(250000, 2, 'activa', 5, '2026-11-05 15:30:00', '2026-11-07 12:00:00', NULL, 12, 9),
(180000, 3, 'activa', 3, '2026-11-06 09:00:00', NULL, NULL, 13, 10),
(120000, 2, 'cancelada', 2, '2026-10-28 17:00:00', NULL, NULL, 14, 11),
(220000, 4, 'cumplida', 4, '2026-10-29 11:45:00', '2026-10-30 14:00:00', '2026-11-03 10:00:00', 15, 12);

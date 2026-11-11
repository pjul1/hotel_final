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
    tipoUsuario ENUM('ADMINISTRADOR', 'CONSERJE', 'PASAJERO') NOT NULL
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
    turno ENUM('MANIANA', 'TARDE', 'NOCHE') NOT NULL,
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
    tipoHabitacion ENUM('HABITACION_SIMPLE', 'HABITACION_DOBLE', 'SUITE') NOT NULL,
    estadoHabitacion ENUM('DISPONIBLE', 'OCUPADA', 'RESERVADA', 'NO_DISPONIBLE') NOT NULL,
    motivoBloqueo ENUM('LIMPIEZA', 'REPARACION', 'DESINFECCION', 'OTRO') NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE -- Baja lógica para inhabilitar habitaciones
);

-- -----------------------------------------------------------------------------
-- 4. TABLA: Reserva
-- -----------------------------------------------------------------------------
CREATE TABLE Reserva (
    reservaId BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    precio DECIMAL(10, 2) UNSIGNED NOT NULL,
    cantidadPasajeros TINYINT UNSIGNED NOT NULL,
    estadoReserva ENUM('ACTIVA', 'CUMPLIDA', 'CANCELADA') NOT NULL,
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

-- =============================================================================
-- 1. CARGA DE USUARIOS
-- =============================================================================
INSERT INTO Usuario (nombre, apellido, email, dni, telefono, nombreUsuario, contrasenia, activo, tipoUsuario) VALUES
('Carlos', 'Gómez', 'carlos.gomez@email.com', '30111222', '2234111111', 'admin1', 'admin123', true, 'ADMINISTRADOR'),
('Mariana', 'López', 'mariana.lopez@email.com', '31222333', '2234222222', 'mlopez', 'conserje123', true, 'CONSERJE'),
('Roberto', 'Fernández', 'roberto.f@email.com', '32333444', '2234333333', 'rfernandez', 'conserje456', true, 'CONSERJE'),
('Esteban', 'Morales', 'esteban.morales@email.com', '33444555', '2234444444', 'emorales', 'conserje789', true, 'CONSERJE'),
('Lucía', 'Pérez', 'lucia.perez@email.com', '34555666', '2234555555', 'lperez', 'conserje012', true, 'CONSERJE'),
('Gonzalo', 'Martínez', 'gonzalo.m@email.com', '35666777', '2234666666', 'gmartinez', 'conserje345', true, 'CONSERJE'),
('Juan', 'Rodríguez', 'juan.rodriguez@email.com', '36777888', '2234777777', 'jrodriguez', 'pasajero1', true, 'PASAJERO'),
('Ana', 'Sánchez', 'ana.sanchez@email.com', '37888999', '2234888888', 'asanchez', 'pasajero2', true, 'PASAJERO'),
('Diego', 'Romero', 'diego.romero@email.com', '38999000', '2234999999', 'dromero', 'pasajero3', true, 'PASAJERO'),
('Sofia', 'Sánchez', 'sofia.222@email.com', '39000111', '2234000111', 'sdiaz', 'pasajero4', true, 'PASAJERO'),
('Lucas', 'Álvarez', 'lucas.alvarez@email.com', '40111222', '2234111222', 'lalvarez', 'pasajero5', true, 'PASAJERO'),
('Valentina', 'Torres', 'valentina.t@email.com', '41222333', '2234222333', 'vtorres', 'pasajero6', true, 'PASAJERO'),
('Mateo', 'Ruiz', 'mateo.ruiz@email.com', '42333444', '2234333444', 'mruiz', 'pasajero7', true, 'PASAJERO'),
('Camila', 'Benítez', 'camila.b@email.com', '43444555', '2234444555', 'cbenitez', 'pasajero8', true, 'PASAJERO'),
('Joaquín', 'Acosta', 'joaquin.a@email.com', '44555666', '2234555666', 'jacosta', 'pasajero9', true, 'PASAJERO');

-- =============================================================================
-- 2. CARGA DE ROLES
-- =============================================================================
INSERT INTO Administrador (usuarioId) VALUES (1);

INSERT INTO Conserje (usuarioId, turno, salario) VALUES
(2, 'MANIANA', 450000.00),
(3, 'TARDE', 460000.00),
(4, 'MANIANA', 450000.00),
(5, 'TARDE', 460000.00),
(6, 'NOCHE', 490000.00);

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
INSERT INTO Habitacion (numeroHabitacion, tipoHabitacion, estadoHabitacion, motivoBloqueo, activo) VALUES
(101, 'HABITACION_SIMPLE', 'DISPONIBLE', NULL, true),
(102, 'HABITACION_SIMPLE', 'OCUPADA', NULL, true),
(103, 'HABITACION_SIMPLE', 'RESERVADA', NULL, true),
(104, 'HABITACION_SIMPLE', 'NO_DISPONIBLE', 'LIMPIEZA', true),
(105, 'HABITACION_SIMPLE', 'DISPONIBLE', NULL, true),
(201, 'HABITACION_DOBLE', 'DISPONIBLE', NULL, true),
(202, 'HABITACION_DOBLE', 'OCUPADA', NULL, true),
(203, 'HABITACION_DOBLE', 'RESERVADA', NULL, true),
(204, 'HABITACION_DOBLE', 'NO_DISPONIBLE', 'REPARACION', true),
(205, 'HABITACION_DOBLE', 'DISPONIBLE', NULL, true),
(301, 'SUITE', 'DISPONIBLE', NULL, true),
(302, 'SUITE', 'OCUPADA', NULL, true),
(303, 'SUITE', 'RESERVADA', NULL, true),
(304, 'SUITE', 'NO_DISPONIBLE', 'DESINFECCION', true),
(305, 'SUITE', 'DISPONIBLE', NULL, true);

-- =============================================================================
-- 4. CARGA DE RESERVAS
-- =============================================================================
INSERT INTO Reserva (precio, cantidadPasajeros, estadoReserva, duracionEstadia, diaReservado, checkIn, checkOut, habitacion, pasajero) VALUES
(40000, 1, 'CUMPLIDA', 2, '2026-10-01 10:00:00', '2026-10-01 14:00:00', '2026-10-03 10:00:00', 1, 7),
(80000, 2, 'ACTIVA', 3, '2026-11-01 11:30:00', '2026-11-05 15:00:00', NULL, 2, 8),
(120000, 2, 'ACTIVA', 2, '2026-11-02 09:15:00', NULL, NULL, 3, 9),
(45000, 1, 'CANCELADA', 1, '2026-10-10 16:00:00', NULL, NULL, 4, 10),
(50000, 1, 'CUMPLIDA', 2, '2026-10-12 12:00:00', '2026-10-14 14:00:00', '2026-10-16 10:00:00', 5, 11),
(90000, 2, 'CUMPLIDA', 3, '2026-10-15 08:00:00', '2026-10-20 14:00:00', '2026-10-23 10:00:00', 6, 12),
(150000, 3, 'ACTIVA', 4, '2026-11-03 14:20:00', '2026-11-06 16:00:00', NULL, 7, 13),
(100000, 2, 'activa', 2, '2026-11-04 18:00:00', NULL, NULL, 8, 14),
(95000, 2, 'cancelada', 3, '2026-10-18 11:00:00', NULL, NULL, 9, 15),
(60000, 1, 'cumplida', 2, '2026-10-22 10:00:00', '2026-10-25 14:00:00', '2026-10-27 10:00:00', 10, 7),
(200000, 4, 'activa', 4, '2026-11-05 13:00:00', NULL, NULL, 11, 8),
(250000, 2, 'activa', 5, '2026-11-05 15:30:00', '2026-11-07 12:00:00', NULL, 12, 9),
(180000, 3, 'activa', 3, '2026-11-06 09:00:00', NULL, NULL, 13, 10),
(120000, 2, 'cancelada', 2, '2026-10-28 17:00:00', NULL, NULL, 14, 11),
(220000, 4, 'cumplida', 4, '2026-10-29 11:45:00', '2026-10-30 14:00:00', '2026-11-03 10:00:00', 15, 12);
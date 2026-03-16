INSERT INTO usuarios
(nombre, apellidos, email, password_hash, telefono, rol, fecha_registro, activo)
VALUES
    ('Admin', 'Sistema', 'admin@padel.com', '$2y$10$y01iDBo/ru3SKNcmtjwFReYYiDxU4kbwnVjt0aOPbdQf.Rmc7e0XK', '600000001', 'ADMIN', CURRENT_TIMESTAMP, TRUE);

INSERT INTO usuarios
(nombre, apellidos, email, password_hash, telefono, rol, fecha_registro, activo)
VALUES
    ('Usuario', 'Prueba', 'user@padel.com', '$2y$10$fT.31OKPIoapxTMU2b8YXuBvhEBN96.pV0300y8uKEDQJSNcCIGQq', '600000002', 'USER', CURRENT_TIMESTAMP, TRUE);
INSERT INTO usuarios
(nombre, apellidos, email, password_hash, telefono, rol, fecha_registro, activo)
VALUES
    ('Admin', 'Sistema', 'admin@padel.com', 'admin', '600000001', 'ADMIN', CURRENT_TIMESTAMP, TRUE);

INSERT INTO usuarios
(nombre, apellidos, email, password_hash, telefono, rol, fecha_registro, activo)
VALUES
    ('Usuario', 'Prueba', 'user@padel.com', 'user', '600000002', 'USER', CURRENT_TIMESTAMP, TRUE);
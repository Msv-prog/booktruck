-- =====================================================================
-- Administrador inicial de demostración (ejecutar después de booktruck_db.sql)
-- Correo: admin@booktruck.test  ·  Contraseña: Test1234!
-- Cámbiala o crea otro administrador desde Usuarios → "Crear administrador".
-- Empresas y transportadores se registran desde la página de inicio.
-- =====================================================================
USE booktruck_db;

INSERT INTO usuarios (email, password_hash, rol, activo)
VALUES ('admin@booktruck.test', '$2a$10$CnrOv6vXyaJ8cBauS6bPoe29OcodvZ3B4svB97ebwLm1e96q6nGRO', 'ADMINISTRADOR', TRUE);

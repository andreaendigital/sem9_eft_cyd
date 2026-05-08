-- SOLO INFRAESTRUCTURA: Creación de base de datos, usuario y permisos
-- Ejecutar como root/admin UNA SOLA VEZ antes de cualquier aplicación
-- En Docker: ejecutar en el host antes de hacer docker-compose up
-- En local: ejecutar contra MySQL directamente
-- EJEMPLO: mysql -u root -p < infrastructure-setup.sql

-- Crear la base de datos
CREATE DATABASE IF NOT EXISTS mydatabase;

-- Crear el usuario con permisos para % (acceso desde cualquier host)
CREATE USER IF NOT EXISTS 'myuser'@'%' IDENTIFIED BY 'password';

-- Otorgar todos los privilegios en la BD al usuario
GRANT ALL PRIVILEGES ON mydatabase.* TO 'myuser'@'%';

-- Aplicar cambios de permisos
FLUSH PRIVILEGES;

-- Mensaje de confirmación (opcional, algunos shells lo soportan)
-- SELECT 'Infrastructure setup completed' AS status;

-- Semilla de datos de aplicación: creación de tablas e inserción de datos de prueba
-- Ejecutado automáticamente por Spring Boot al iniciar (spring.sql.init.mode=always)
-- También puede ser copiado al contenedor MySQL en docker-entrypoint-initdb.d/

CREATE TABLE IF NOT EXISTS user (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255),
    password VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS patient (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    species VARCHAR(255),
    breed VARCHAR(255),
    age INT,
    owner VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS pet (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    species VARCHAR(255),
    breed VARCHAR(255),
    age INT,
    gender VARCHAR(255),
    location VARCHAR(255),
    status VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS pet_photos (
    pet_id INT NOT NULL,
    photo_url VARCHAR(255),
    CONSTRAINT fk_pet_photos_pet FOREIGN KEY (pet_id) REFERENCES pet(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS appointment (
    id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT,
    date DATE,
    time TIME,
    reason VARCHAR(255),
    veterinarian VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS invoice (
    id INT AUTO_INCREMENT PRIMARY KEY,
    appointment_id INT,
    issue_date DATE,
    vat_rate DECIMAL(38,2),
    subtotal DECIMAL(38,2),
    vat_amount DECIMAL(38,2),
    total DECIMAL(38,2),
    notes VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS invoice_line_item (
    id INT AUTO_INCREMENT PRIMARY KEY,
    invoice_id INT NOT NULL,
    type VARCHAR(255),
    description VARCHAR(255),
    quantity INT,
    unit_price DECIMAL(38,2),
    line_total DECIMAL(38,2),
    CONSTRAINT fk_invoice_line_item_invoice FOREIGN KEY (invoice_id) REFERENCES invoice(id) ON DELETE CASCADE
);

-- Datos de prueba: usuarios con contraseña hash BCrypt (contraseña plana: 1234)
-- Hash BCrypt correcto de "1234" con 12 rondas
INSERT INTO user (username, email, password)
SELECT 'admin', 'admin@veterinaria.cl', '1234'
WHERE NOT EXISTS (SELECT 1 FROM user WHERE username = 'admin');

INSERT INTO user (username, email, password)
SELECT 'veterinario1', 'vet1@veterinaria.cl', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5YmMxSUqqhhkm'
WHERE NOT EXISTS (SELECT 1 FROM user WHERE username = 'veterinario1');

INSERT INTO user (username, email, password)
SELECT 'asistente', 'asist@veterinaria.cl', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5YmMxSUqqhhkm'
WHERE NOT EXISTS (SELECT 1 FROM user WHERE username = 'asistente');

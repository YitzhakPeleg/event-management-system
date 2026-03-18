-- ============================================================
-- Event Management System - Database Schema
-- ============================================================
-- Run this file once to create the database and all tables.
-- Usage: mysql -u root -p < schema.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS event_management
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE event_management;

-- ------------------------------------------------------------
-- Table 1: users
-- Stores login credentials and role for each system user.
-- role = 'ADMIN' can manage events/halls/participants.
-- role = 'USER'  can register to events and view their own registrations.
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    username     VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(64) NOT NULL,          -- SHA-256 hex string
    role         ENUM('ADMIN', 'USER') NOT NULL DEFAULT 'USER',
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------------------
-- Table 2: halls
-- Stores event venues. capacity is used to limit registrations.
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS halls (
    id        INT AUTO_INCREMENT PRIMARY KEY,
    name      VARCHAR(100) NOT NULL,
    capacity  INT          NOT NULL CHECK (capacity > 0),
    location  VARCHAR(200) NOT NULL
);

-- ------------------------------------------------------------
-- Table 3: events
-- Each event takes place in a hall (hall_id) and is created
-- by an admin user (created_by).
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS events (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    title        VARCHAR(150) NOT NULL,
    description  TEXT,
    event_date   DATE         NOT NULL,
    event_time   TIME         NOT NULL,
    hall_id      INT          NOT NULL,
    created_by   INT          NOT NULL,
    CONSTRAINT fk_event_hall    FOREIGN KEY (hall_id)    REFERENCES halls(id)  ON DELETE RESTRICT,
    CONSTRAINT fk_event_creator FOREIGN KEY (created_by) REFERENCES users(id)  ON DELETE RESTRICT
);

-- ------------------------------------------------------------
-- Table 4: participants
-- Personal details. email and phone are stored encrypted (AES)
-- to protect sensitive data.
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS participants (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    first_name      VARCHAR(50)  NOT NULL,
    last_name       VARCHAR(50)  NOT NULL,
    email_encrypted VARCHAR(300) NOT NULL,   -- AES-encrypted, Base64-encoded
    phone_encrypted VARCHAR(300) NOT NULL,   -- AES-encrypted, Base64-encoded
    user_id         INT          NOT NULL UNIQUE,  -- one participant record per user
    CONSTRAINT fk_participant_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ------------------------------------------------------------
-- Table 5: event_registrations  (linking / junction table)
-- Links participants to events. Enforces:
--   - One registration per participant per event (UNIQUE constraint)
--   - Capacity check is done in Java (RegistrationService)
-- qr_code_token is a unique random string used for QR scanning.
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS event_registrations (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    event_id        INT          NOT NULL,
    participant_id  INT          NOT NULL,
    registered_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    qr_code_token   VARCHAR(64)  NOT NULL UNIQUE,
    CONSTRAINT fk_reg_event       FOREIGN KEY (event_id)       REFERENCES events(id)       ON DELETE CASCADE,
    CONSTRAINT fk_reg_participant FOREIGN KEY (participant_id)  REFERENCES participants(id) ON DELETE CASCADE,
    CONSTRAINT uq_reg_once        UNIQUE (event_id, participant_id)  -- no double registration
);

-- ============================================================
-- Seed Data (sample records for development and testing)
-- ============================================================

-- Admin user: username=admin, password=Admin1234
-- (SHA-256 of "Admin1234")
INSERT INTO users (username, password_hash, role) VALUES
    ('admin', '60fe74406e7f353ed979f350f2fbb6a2e8690a5fa7d1b0c32983d1d8b3f95f67', 'ADMIN');

-- Regular users
-- yossi: password=Yossi1234  |  dana: password=Dana1234
-- moshe: password=Moshe1234  |  sarah: password=Sarah1234  |  david: password=David1234
-- rachel: password=Rachel1234 |  avi: password=Avi1234    |  tamar: password=Tamar1234
-- noam: password=Noam1234    |  maya: password=Maya1234   |  itay: password=Itay1234
-- noa: password=Noa1234
INSERT INTO users (username, password_hash, role) VALUES
    ('yossi',  '5da2302d885e1a34bf4a25f3cfa6d3cee16e835df33028c33100236b7409c564', 'USER'),
    ('dana',   '00aca380c9986d6423b5629bb5b76f76272b16eaf8d65c69baae26951884ce5d', 'USER'),
    ('moshe',  'aae5c2716eac9dfd92afdff77ab446d626d7bba475c08981f3b87151df4b69af', 'USER'),
    ('sarah',  'af2b3655d05e3e420ef0b39efc636a9f76e481e9debe14b39ce51e281693c8d5', 'USER'),
    ('david',  '6f186e817dbe400af280ef52acfdeb843e98df2c4f470c170f01585097c8558e', 'USER'),
    ('rachel', '8693245e726b77310e6e064521c7c0661064d2d7166ea8811415cd1ab9e4a940', 'USER'),
    ('avi',    '316fb4b88f990cf4bc8cbef17bcb03c79c85eff5b006c363ddb2d7fe8e53f31d', 'USER'),
    ('tamar',  '2c0a1d3694983abe850337b83f1d80138d90f92c79f6b8a05affe286449ba0cc', 'USER'),
    ('noam',   'd3d10e4130802ccfb0a89908ab0b6428f28b4bdfb2839098ac7ff8e99704da52', 'USER'),
    ('maya',   '048fcdd45257e7c3673ae77bee7dc483926774635d252c42b4a46f1c7bd8bcae', 'USER'),
    ('itay',   '4ba7417e2c7da3b0c3ec15a654f0720811899505810e277d19c147bb32d18be0', 'USER'),
    ('noa',    'd09703e2193877fd2d306f7f505b8399f9cb1480b347417971f9eb975b3a4182', 'USER');

-- Halls
INSERT INTO halls (name, capacity, location) VALUES
    ('Hall A - Auditorium', 300, 'Building 1, Floor 1'),
    ('Hall B - Conference Room', 50,  'Building 2, Floor 3'),
    ('Hall C - Workshop Room',   20,  'Building 3, Floor 2');

-- Events (created by admin, id=1)
INSERT INTO events (title, description, event_date, event_time, hall_id, created_by) VALUES
    ('Annual Tech Conference 2026', 'A full-day tech conference with guest speakers.', '2026-05-10', '09:00:00', 1, 1),
    ('Java Workshop for Beginners', 'Hands-on Java programming workshop.',             '2026-05-20', '14:00:00', 3, 1),
    ('AI and the Future Panel',     'Panel discussion on AI trends.',                  '2026-06-01', '18:00:00', 2, 1);

-- Participants (email and phone values here are PLAINTEXT placeholders)
-- In real usage, EncryptionUtil.encrypt() is called before INSERT.
-- These are inserted via the Java app, not raw SQL, so we use a marker.
-- For seed purposes only, we store a placeholder string.
-- email and phone are AES-encrypted with key 'EventMgmt2026Key' (see EncryptionUtil.java)
INSERT INTO participants (first_name, last_name, email_encrypted, phone_encrypted, user_id) VALUES
    ('Yossi',  'Cohen',    'oDEqgtyQUorfghRIOzH8UVTaHL4tJWqsby2BeTikMqs=', 'nZk5b43G8SC2dpEsNlIe0Q==', 2),
    ('Dana',   'Levi',     'MKKqoUEkdtFcubL5fA6HdB4/UY4jpSURM6k1nD36wxI=', 'zACpUBqn9rdkWF8wrW0+Hg==', 3),
    ('Moshe',  'Ben-David','aod+buQoiuBavIbGAjPAYFTaHL4tJWqsby2BeTikMqs=', 'feRmsQtNFrjdtfMDPsGMJQ==', 4),
    ('Sarah',  'Mizrahi',  'gQcX8SNo7YwZfpkVsNCMNlTaHL4tJWqsby2BeTikMqs=','76X/tmbM2MUeZkrOsW3k0w==', 5),
    ('David',  'Shapiro',  'oyorWRIlogvm1q+SlvYA0FTaHL4tJWqsby2BeTikMqs=', 'oZoVTYKVCUfynzkVH/iZAQ==', 6),
    ('Rachel', 'Katz',     '+fQf4fVx0izTxy+QdalToxyG9QmWRcIxsa0NSKfA9F0=', '7QBvkek/fcR9lOiOWaFc6A==', 7),
    ('Avi',    'Peretz',   'qP37IX5WBnK0NbEKpaVmdA==',                       'Hk3dL5P10PucP+b3nAZhFw==', 8),
    ('Tamar',  'Goldberg', '+6XkdJ4RQLbqS5qAjW6kDFTaHL4tJWqsby2BeTikMqs=', '+nAFXEPXabOXtPRtg8OEuQ==', 9),
    ('Noam',   'Israeli',  'FD9vBfQ/lw62L/R29d9FXB4/UY4jpSURM6k1nD36wxI=', 'w6SuqIppuF4i29gytl6p7A==', 10),
    ('Maya',   'Frank',    'zxA4ZzLsXtUOle36MtXOaR4/UY4jpSURM6k1nD36wxI=', '4iOu7yWM7Mq1bXP7pUyeKw==', 11),
    ('Itay',   'Bar',      'eJ2Iv7dBtJq5rFTEn4SHrx4/UY4jpSURM6k1nD36wxI=', 'x2XBIo+Mu3Fwry1WetU/rw==', 12),
    ('Noa',    'Dagan',    'UTFKmsywl/3suAij6GscfQ==',                       'K7372sPGsx19a/P9qAoZ2w==', 13);

-- Sample registrations
INSERT INTO event_registrations (event_id, participant_id, qr_code_token) VALUES
    (1, 1, 'QR-TOKEN-YOSSI-EVENT1-ABCDE12345'),
    (2, 2, 'QR-TOKEN-DANA-EVENT2-FGHIJ67890');

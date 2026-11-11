-- =====================================================================
-- Online Reservation System - Sample / Seed Data
-- Database: online_reservation_system
-- =====================================================================

USE online_reservation_system;

-- 1. Insert sample users (passwords are hashed using PBKDF2WithHmacSHA256)
-- Default credentials:
-- username: admin / password: admin123
-- username: user1 / password: user123
INSERT INTO users (username, password_hash) VALUES
('admin', '0102030405060708090a0b0c0d0e0f10:489df821aa0e35d088df49d584fee1e3575b6b7d50a7e086058872e9b63bd93a')
ON DUPLICATE KEY UPDATE username=username;

INSERT INTO users (username, password_hash) VALUES
('user1', '0102030405060708090a0b0c0d0e0f10:a8434bf29bd546ae4c7290ba3d75fd78772608721f0128248e9c3d4c6bd6b619')
ON DUPLICATE KEY UPDATE username=username;

-- 2. Insert sample trains
INSERT INTO trains (train_number, train_name, source_station, destination_station) VALUES
('12001', 'Shatabdi Express', 'New Delhi', 'Bhopal'),
('12301', 'Rajdhani Express', 'Howrah', 'New Delhi'),
('12951', 'Mumbai Rajdhani', 'Mumbai Central', 'New Delhi'),
('12626', 'Kerala Express', 'New Delhi', 'Trivandrum Central'),
('12259', 'Sealdah Duronto', 'Sealdah', 'Bikaner'),
('10101', 'Euro Express', 'London Kings Cross', 'Edinburgh Waverley'),
('10202', 'Trans-Alpine Line', 'Geneva Central', 'Zurich HB'),
('10303', 'Coastliner Superfast', 'San Francisco', 'Los Angeles')
ON DUPLICATE KEY UPDATE
    train_name=VALUES(train_name),
    source_station=VALUES(source_station),
    destination_station=VALUES(destination_station);

-- ===========================================
-- MASTER DATA: Countries & Cities
-- ===========================================
USE cms_db;

INSERT IGNORE INTO country (name, code) VALUES
('Sri Lanka',     'LK'),
('India',         'IN'),
('United Kingdom','GB'),
('United States', 'US'),
('Australia',     'AU'),
('Canada',        'CA'),
('Singapore',     'SG'),
('Maldives',      'MV');

INSERT IGNORE INTO city (name, country_id) VALUES
-- Sri Lanka
('Colombo',       (SELECT id FROM country WHERE code='LK')),
('Kandy',         (SELECT id FROM country WHERE code='LK')),
('Galle',         (SELECT id FROM country WHERE code='LK')),
('Negombo',       (SELECT id FROM country WHERE code='LK')),
('Jaffna',        (SELECT id FROM country WHERE code='LK')),
('Matara',        (SELECT id FROM country WHERE code='LK')),
-- India
('Mumbai',        (SELECT id FROM country WHERE code='IN')),
('Delhi',         (SELECT id FROM country WHERE code='IN')),
('Bangalore',     (SELECT id FROM country WHERE code='IN')),
('Chennai',       (SELECT id FROM country WHERE code='IN')),
-- UK
('London',        (SELECT id FROM country WHERE code='GB')),
('Manchester',    (SELECT id FROM country WHERE code='GB')),
-- USA
('New York',      (SELECT id FROM country WHERE code='US')),
('Los Angeles',   (SELECT id FROM country WHERE code='US')),
-- Australia
('Sydney',        (SELECT id FROM country WHERE code='AU')),
('Melbourne',     (SELECT id FROM country WHERE code='AU')),
-- Canada
('Toronto',       (SELECT id FROM country WHERE code='CA')),
('Vancouver',     (SELECT id FROM country WHERE code='CA')),
-- Singapore
('Singapore',     (SELECT id FROM country WHERE code='SG')),
-- Maldives
('Male',          (SELECT id FROM country WHERE code='MV'));
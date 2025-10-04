-- Flyway Migration: Insert initial genres, authors, and books
-- Encoding: UTF-8 / Line Endings: CRLF (Windows)

-- Insert Genres
INSERT INTO genres (genre_id, name) VALUES
(1, 'Fantasy'),
(2, 'Mystery'),
(3, 'Thriller'),
(4, 'Romance'),
(5, 'Horror');

-- Insert Authors
INSERT INTO authors (author_id, full_name, date_of_birth) VALUES
(1, 'Stephen King', '1947-09-21'),
(2, 'Neil Gaiman', '1960-11-10'),
(3, 'Agatha Christie', '1890-09-15'),
(4, 'Joanne Rowling', '1965-07-31'),
(5, 'Dan Brown', '1964-06-22');

-- Insert Books (5 per author, each in a different genre)

-- Stephen King
INSERT INTO books (title, one_day_rent_price_usd, available_quantity, author_id, genre_id) VALUES
('Shadows of Derry', 12.00, 5, 1, 1),
('Whispers in the Fog', 9.00, 7, 1, 2),
('Nightfall Protocol', 15.00, 2, 1, 3),
('Echoes of Yesterday', 8.00, 1, 1, 4),
('The Hollow Manor', 14.00, 6, 1, 5);

-- Neil Gaiman
INSERT INTO books (title, one_day_rent_price_usd, available_quantity, author_id, genre_id) VALUES
('Dreamwalker', 11.00, 4, 2, 1),
('Silent Enigma', 6.00, 8, 2, 2),
('The Last Cipher', 13.00, 3, 2, 3),
('Velvet Skies', 7.00, 2, 2, 4),
('Midnight Carnival', 10.00, 9, 2, 5);

-- Agatha Christie
INSERT INTO books (title, one_day_rent_price_usd, available_quantity, author_id, genre_id) VALUES
('Mystery at Rosewood Hall', 8.50, 6, 3, 1),
('Case of the Lost Heirloom', 5.00, 3, 3, 2),
('The Silent Witness', 14.00, 2, 3, 3),
('Letters from Nowhere', 9.00, 5, 3, 4),
('Footsteps in the Dark', 7.50, 1, 3, 5);

-- Joanne Rowling
INSERT INTO books (title, one_day_rent_price_usd, available_quantity, author_id, genre_id) VALUES
('Wizards of Evermore', 10.00, 4, 4, 1),
('Cursed Portrait', 6.50, 0, 4, 2),
('The Raven Pact', 12.50, 3, 4, 3),
('Moonlit Serenade', 7.00, 8, 4, 4),
('Hallows of the Forgotten', 9.00, 2, 4, 5);

-- Dan Brown
INSERT INTO books (title, one_day_rent_price_usd, available_quantity, author_id, genre_id) VALUES
('The Arcane Legacy', 11.50, 7, 5, 1),
('Cryptic Justice', 13.00, 2, 5, 2),
('The Last Countdown', 15.00, 1, 5, 3),
('Crimson Letters', 8.50, 6, 5, 4),
('Beneath the Cathedral', 10.00, 4, 5, 5);

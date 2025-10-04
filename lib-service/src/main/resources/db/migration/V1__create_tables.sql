CREATE TABLE genres (
    genre_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE authors (
    author_id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    date_of_birth DATE NOT NULL
);

CREATE TABLE books (
    book_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    one_day_rent_price_usd DECIMAL(5,2) NOT NULL,
    available_quantity INT NOT NULL,
    author_id INT,
    genre_id INT,
    CONSTRAINT fk_book_author FOREIGN KEY (author_id) REFERENCES authors(author_id),
    CONSTRAINT fk_book_genre FOREIGN KEY (genre_id) REFERENCES genres(genre_id)
);

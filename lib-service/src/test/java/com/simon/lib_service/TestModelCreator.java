package com.simon.lib_service;

import com.simon.lib_service.model.Author;
import com.simon.lib_service.model.Book;
import com.simon.lib_service.model.Genre;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TestModelCreator {

    public static Book bookWithoutId() {
        return new Book("title", BigDecimal.valueOf(.5), 4);
    }

    public static Book bookWithId(int id) {
        return new Book(id, "title", BigDecimal.valueOf(.5), 4);
    }

    public static Author authorWithoutId() {
        return new Author("author", LocalDate.of(1990, 5, 14));
    }

    public static Author authorWithId(int id) {
        return new Author(id, "author", LocalDate.of(1990, 5, 14));
    }

    public static Genre genreWithId(int id) {
        return new Genre(id, "genre");
    }

    public static Genre genreWithoutId() {
        return new Genre("genre");
    }

    public static Author authorWithBook() {
        Author author = authorWithoutId();
        author.addBook(bookWithoutId());
        return author;
    }

    public static Book bookWithAuthorAndGenre() {
        Book book = bookWithoutId();
        book.setAuthor(authorWithoutId());
        book.setGenre(genreWithoutId());
        return book;
    }

    public static Genre genreWithBook() {
        Genre genre = genreWithoutId();
        genre.addBook(bookWithoutId());
        return genre;
    }
}

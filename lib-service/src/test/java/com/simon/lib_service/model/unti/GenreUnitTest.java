package com.simon.lib_service.model.unti;

import com.simon.lib_service.TestModelCreator;
import com.simon.lib_service.model.Book;
import com.simon.lib_service.model.Genre;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GenreUnitTest {

    private Genre genre;
    private Book book;

    @BeforeEach
    void init() {
        genre = TestModelCreator.genreWithId(1);
        book = TestModelCreator.bookWithId(1);
    }

    @Test
    public void addOrder() {
        genre.addBook(book);

        assertTrue(genre.getBooks().contains(book));
        assertEquals(genre, book.getGenre());
    }

    @Test
    public void removeOrder() {
        genre.addBook(book);
        genre.removeBook(book);

        assertFalse(genre.getBooks().contains(book));
        assertNull(book.getGenre());
    }
}

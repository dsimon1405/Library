package com.simon.lib_service.model.unti;

import com.simon.lib_service.TestModelCreator;
import com.simon.lib_service.model.Author;
import com.simon.lib_service.model.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AuthorUnitTest {

    private Author author;
    private Book book;

    @BeforeEach
    void init() {
        author = TestModelCreator.authorWithId(1);
        book = TestModelCreator.bookWithId(1);
    }

    @Test
    public void addBook() {
        author.addBook(book);

        assertTrue(author.getBooks().contains(book));
        assertEquals(author, book.getAuthor());
    }

    @Test
    public void removeBook() {
        author.addBook(book);
        author.removeBook(book);

        assertFalse(author.getBooks().contains(book));
        assertNull(book.getAuthor());
    }
}

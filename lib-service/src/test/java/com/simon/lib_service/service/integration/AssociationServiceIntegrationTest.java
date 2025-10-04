package com.simon.lib_service.service.integration;

import static com.simon.lib_service.TestModelCreator.*;
import com.simon.lib_service.model.Author;
import com.simon.lib_service.model.Book;
import com.simon.lib_service.model.Genre;
import com.simon.lib_service.repository.AuthorRepository;
import com.simon.lib_service.repository.BookRepository;
import com.simon.lib_service.repository.GenreRepository;
import com.simon.lib_service.service.AssociationService;
import com.simon.exception.NotFoundException;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static com.simon.lib_service.TestModelCreator.bookWithoutId;
import static com.simon.lib_service.TestModelCreator.genreWithoutId;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "/clear-database.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class AssociationServiceIntegrationTest {

    @Autowired
    private AssociationService associationService;
    @Autowired
    private AuthorRepository authorRepository;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private GenreRepository genreRepository;

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void associateAuthorWithBook_WhenAuthorAndBookExists_MakeAssociation() {
        Book book =  bookRepository.save(bookWithoutId());
        Author author = authorRepository.save(authorWithoutId());

        Book result = associationService.associateAuthorWithBook(author.getId(), book.getId());

        assertNotNull(result);
        assertDoesNotThrow(() -> result.getAuthor().getFullName());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void associateBookWithAuthorAndGenre_WhenBookAuthorAndGenreExists_MakeAssociation() {
        Book book =  bookRepository.save(bookWithoutId());
        Author author = authorRepository.save(authorWithoutId());
        Genre genre = genreRepository.save(genreWithoutId());

        Book result = associationService.associateBookWithAuthorAndGenre(book.getId(), author.getId(), genre.getId());

        assertNotNull(result);
        assertDoesNotThrow(() -> result.getAuthor().getFullName());
        assertDoesNotThrow(() -> result.getAuthor().getFullName());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void associateGenreWithBook_WhenGenreAndBookExists_MakeAssociation() {
        Book book =  bookRepository.save(bookWithoutId());
        Genre genre = genreRepository.save(genreWithoutId());

        Book result = associationService.associateGenreWithBook(genre.getId(), book.getId());

        assertNotNull(result);
        assertDoesNotThrow(() -> result.getGenre().getName());
    }

    @Test
    void makeAssociation_BookNull_ThrowNotFoundException() {
        assertThatThrownBy(() -> associationService.makeAssociation(null, 1, 1))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Must present book_id");
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void makeAssociation_WithAuthorId_Associate() {
        Book book = bookRepository.save(bookWithoutId());
        Author author = authorRepository.save(authorWithoutId());

        Book result = associationService.makeAssociation(book.getId(), author.getId(), null);

        assertNotNull(result);
        assertEquals(book.getId(), result.getId());
        assertDoesNotThrow(() -> result.getAuthor().getFullName());
        assertEquals(author.getId(), result.getAuthor().getId());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void makeAssociation_WithGenreId_Associate() {
        Book book = bookRepository.save(bookWithoutId());
        Genre genre = genreRepository.save(genreWithoutId());

        Book result = associationService.makeAssociation(book.getId(), null, genre.getId());

        assertNotNull(result);
        assertEquals(book.getId(), result.getId());
        assertDoesNotThrow(() -> result.getGenre().getName());
        assertEquals(genre.getId(), result.getGenre().getId());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void makeAssociation_WithAuthorIdAndGenreId_Associate() {
        Book book = bookRepository.save(bookWithoutId());
        Author author = authorRepository.save(authorWithoutId());
        Genre genre = genreRepository.save(genreWithoutId());

        Book result = associationService.makeAssociation(book.getId(), author.getId(), genre.getId());

        assertNotNull(result);
        assertEquals(book.getId(), result.getId());
        assertDoesNotThrow(() -> result.getAuthor().getFullName());
        assertEquals(author.getId(), result.getAuthor().getId());
        assertDoesNotThrow(() -> result.getGenre().getName());
        assertEquals(genre.getId(), result.getGenre().getId());
    }

    @Test
    void makeAssociation_() {
        assertThatThrownBy(() -> associationService.makeAssociation(1, null, null))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Must present genre_id or author_id or both");
    }
}
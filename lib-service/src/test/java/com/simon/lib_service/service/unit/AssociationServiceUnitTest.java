package com.simon.lib_service.service.unit;

import com.simon.lib_service.model.Author;
import com.simon.lib_service.model.Book;
import com.simon.lib_service.model.Genre;
import com.simon.lib_service.repository.AuthorRepository;
import com.simon.lib_service.repository.BookRepository;
import com.simon.lib_service.repository.GenreRepository;
import com.simon.lib_service.service.AssociationService;
import com.simon.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.simon.lib_service.TestModelCreator.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AssociationServiceUnitTest {

    @Mock
    private BookRepository bookRepository;
    @Mock
    private AuthorRepository authorRepository;
    @Mock
    private GenreRepository genreRepository;
    @InjectMocks
    private AssociationService associationService;

    @Test
    void associateAuthorWithBook_WhenExistsAuthorAndBook_MakeAssociation() {
        int id = 1;
        Author author = authorWithId(id);
        Book book = bookWithId(id);
        when(authorRepository.findById(id)).thenReturn(Optional.of(author));
        when(bookRepository.findById(id)).thenReturn(Optional.of(book));
        when(bookRepository.save(book)).thenReturn(book);

        Book result = associationService.associateAuthorWithBook(id, id);

        assertEquals(id, result.getId());
        assertEquals(id, result.getAuthor().getId());
        verify(authorRepository, times(1)).findById(id);
        verify(bookRepository, times(1)).findById(id);
        verify(bookRepository, times(1)).save(book);
    }

    @Test
    void associateAuthorWithBook_WhenAuthorNotExists_ThrowNotFoundException() {
        int id = 1;
        when(authorRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> associationService.associateAuthorWithBook(id, id))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Can't find author with id");
    }

    @Test
    void associateAuthorWithBook_WhenBookNotExists_ThrowNotFoundException() {
        int id = 1;
        when(authorRepository.findById(id)).thenReturn(Optional.of(new Author()));
        when(bookRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> associationService.associateAuthorWithBook(id, id))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Can't find book with id");
        verify(authorRepository, times(1)).findById(id);
    }

    @Test
    void associateBookWithAuthorAndGenre_WhenBookAuthorAndGenreExists_MakeAssociation() {
        int id = 1;
        Author author = new Author();
        author.setId(id);
        Book book = new Book();
        book.setId(id);
        Genre genre = new Genre();
        genre.setId(id);
        when(authorRepository.findById(id)).thenReturn(Optional.of(author));
        when(bookRepository.findById(id)).thenReturn(Optional.of(book));
        when(genreRepository.findById(id)).thenReturn(Optional.of(genre));
        when(bookRepository.save(book)).thenReturn(book);

        Book result = associationService.associateBookWithAuthorAndGenre(id, id, id);

        assertEquals(id, result.getId());
        assertEquals(id, result.getAuthor().getId());
        assertEquals(id, result.getGenre().getId());
        verify(authorRepository, times(1)).findById(id);
        verify(bookRepository, times(1)).findById(id);
        verify(genreRepository, times(1)).findById(id);
        verify(bookRepository, times(1)).save(book);
    }

    @Test
    void associateBookWithAuthorAndGenre_WhenAuthorNotExists_ThrowNotFoundException() {
        int id = 1;
        when(authorRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> associationService.associateBookWithAuthorAndGenre(id, id, id))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Can't find author with id");
    }

    @Test
    void associateBookWithAuthorAndGenre_WhenBookNotExists_ThrowNotFoundException() {
        int id = 1;
        when(authorRepository.findById(id)).thenReturn(Optional.of(new Author()));
        when(bookRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> associationService.associateBookWithAuthorAndGenre(id, id, id))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Can't find book with id");
        verify(authorRepository, times(1)).findById(id);
    }

    @Test
    void associateBookWithAuthorAndGenre_WhenGenreNotExists_ThrowNotFoundException() {
        int id = 1;
        when(authorRepository.findById(id)).thenReturn(Optional.of(new Author()));
        when(bookRepository.findById(id)).thenReturn(Optional.of(new Book()));
        when(genreRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> associationService.associateBookWithAuthorAndGenre(id, id, id))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Can't find genre with id");
        verify(authorRepository, times(1)).findById(id);
        verify(bookRepository, times(1)).findById(id);
    }

    @Test
    void associateGenreWithBook_WhenExistsGenreAndBook_MakeAssociation() {
        int id = 1;
        Genre genre = genreWithId(1);
        Book book = bookWithId(id);
        when(genreRepository.findById(id)).thenReturn(Optional.of(genre));
        when(bookRepository.findById(id)).thenReturn(Optional.of(book));
        when(bookRepository.save(book)).thenReturn(book);

        Book result = associationService.associateGenreWithBook(id, id);

        assertEquals(id, result.getId());
        assertEquals(id, result.getGenre().getId());
        verify(genreRepository, times(1)).findById(id);
        verify(bookRepository, times(1)).findById(id);
        verify(bookRepository, times(1)).save(book);
    }

    @Test
    void associateGenreWithBook_WhenGenreNotExists_ThrowNotFoundException() {
        int id = 1;
        when(genreRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> associationService.associateGenreWithBook(id, id))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Can't find genre with id");
    }

    @Test
    void associateGenreWithBook_WhenBookNotExists_ThrowNotFoundException() {
        int id = 1;
        when(genreRepository.findById(id)).thenReturn(Optional.of(new Genre()));
        when(bookRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> associationService.associateGenreWithBook(id, id))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Can't find book with id");
        verify(genreRepository, times(1)).findById(id);
    }
}

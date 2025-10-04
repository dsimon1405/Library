package com.simon.lib_service.service.unit;

import com.simon.lib_service.exception.RequestParameterException;
import com.simon.lib_service.model.Author;
import com.simon.lib_service.model.Book;
import com.simon.lib_service.model.page.AuthorPageResponse;
import com.simon.lib_service.repository.AuthorRepository;
import com.simon.lib_service.repository.SqlHelper;
import com.simon.lib_service.service.AuthorService;
import com.simon.lib_service.service.IntegrationService;
import com.simon.dto.lib.AuthorDTO;
import com.simon.exception.ExistsException;
import com.simon.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthorServiceUnitTest {

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private IntegrationService integrationService;

    @InjectMocks
    private AuthorService authorService;

    @Test
    public void findAll_ReturnAllAuthors() {
        List<Author> auths = List.of(new Author(), new Author());
        when(authorRepository.findAll()).thenReturn(auths);
        List<AuthorDTO> result = authorService.findAll();

        assertEquals(2, result.size());
        verify(authorRepository, times(1)).findAll();
    }

    @Test
    void findAll_ReturnsPageOfAuthors() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Author> expectedPage = new PageImpl<>(List.of(new Author(), new Author()));

        when(authorRepository.findAll(pageable)).thenReturn(expectedPage);

        AuthorPageResponse actualPage = authorService.findAll(pageable);

        assertThat(actualPage.content()).hasSize(2);
        verify(authorRepository, times(1)).findAll(pageable);
    }

    @Test
    void findById_WhenExists_ReturnsAuthor() {
        Author author = new Author();
        author.setId(1);
        author.setFullName("Test Author");
        when(authorRepository.findById(1)).thenReturn(Optional.of(author));

        AuthorDTO found = authorService.findById(1);

        assertThat(found.id()).isEqualTo(author.getId());
    }

    @Test
    void findById_WhenNotExists_ThrowsNotFoundException() {
        when(authorRepository.findById(404)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authorService.findById(404))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Can't find author with id: 404");
    }

    @Test
    void findByFullNameLike_WhenExists_ReturnAuthor() {
        String name = "Dima";
        Author auth = new Author();
        auth.setFullName(name);
        when(authorRepository.findByFullNameLike(SqlHelper.toLike(name))).thenReturn(List.of(auth));

        List<AuthorDTO> result = authorService.findByFullNameLike(name);

        assertThat(result).hasSize(1);
        assertEquals(name, result.getFirst().fullName());
        verify(authorRepository, times(1)).findByFullNameLike(SqlHelper.toLike(name));
    }

    @Test
    void findByFullNameLike_WhenNull_ThrowRequestParamException() {
        assertThrows(RequestParameterException.class, () -> authorService.findByFullNameLike(null));
    }

    @Test
    void findByFullNameLike_WhenEmpty_ThrowRequestParamException() {
        assertThrows(RequestParameterException.class, () -> authorService.findByFullNameLike("  "));
    }

    @Test
    void addAuthor_WhenAuthorIdIsNotNull_ThrowExistsException() {
        Author auth = new Author();
        auth.setId(4);

        Exception result = assertThrows(ExistsException.class, () -> authorService.addAuthor(auth));
        assertEquals("Can't add author with custom id", result.getMessage());
    }

    @Test
    void addAuthor_WhenAuthorExists_ThrowExistsException() {
        Author auth = new Author();
        auth.setFullName("Bob");
        when(authorRepository.exists(any())).thenReturn(true);

        Exception result = assertThrows(ExistsException.class, () -> authorService.addAuthor(auth));
        assertTrue(result.getMessage().contains("Author already exists"));
        verify(authorRepository, never()).save(any());
    }

    @Test
    void addAuthor_WhenNotExists_SaveAndReturnAuthor() {
        String name = "Bob";
        Author auth = new Author();
        auth.setFullName(name);

        when(authorRepository.exists(any())).thenReturn(false);
        when(authorRepository.save(auth)).thenReturn(auth);

        AuthorDTO result = authorService.addAuthor(auth);

        assertEquals(name, result.fullName());
        verify(authorRepository, times(1)).exists(any());
        verify(authorRepository, times(1)).save(auth);
    }

    @Test
    void updateAuthor_WhenAuthorIdNull_ThrowNotFoundException() {
        Exception result = assertThrows(NotFoundException.class, () -> authorService.updateAuthor(new Author()));
        assertTrue(result.getMessage().contains("Can't find author with id:"));
        verify(authorRepository, never()).save(any());
    }

    @Test
    void updateAuthor_WhenAuthorNotExists_ThrowNotFoundException() {
        Author auth = new Author();
        auth.setId(4);
        when(authorRepository.existsById(auth.getId())).thenReturn(false);

        Exception result = assertThrows(NotFoundException.class, () -> authorService.updateAuthor(auth));
        assertTrue(result.getMessage().contains("Can't find author with id:"));
        verify(authorRepository, never()).save(any());
    }

    @Test
    void updateAuthor_WhenAuthorExists_SaveAndReturnAuthor() {
        int id = 4;
        Author auth = new Author();
        auth.setId(id);

        when(authorRepository.existsById(auth.getId())).thenReturn(true);
        when(authorRepository.save(auth)).thenReturn(auth);

        AuthorDTO result = authorService.updateAuthor(auth);

        assertEquals(id, result.id());
        verify(authorRepository, times(1)).existsById(id);
        verify(authorRepository, times(1)).save(auth);
    }

    @Test
    void deleteAuthorById_WhenAuthorNotFound_ThrowNotFoundException() {
        when(authorRepository.findWithBooksById(1)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> authorService.deleteAuthorById(1, false));
        verify(authorRepository, never()).deleteById(anyInt());
    }

    @Test
    void deleteAuthorById_WithBooksAndDeleteBooksFalse_ThrowExistsException() {
        Author author = new Author();
        Book book = new Book();
        book.setId(10);
        author.setBooks(Set.of(book));
        when(authorRepository.findWithBooksById(1)).thenReturn(Optional.of(author));

        assertThrows(ExistsException.class, () -> authorService.deleteAuthorById(1, false));
        verify(authorRepository, never()).deleteById(anyInt());
    }

    @Test
    void deleteAuthorById_WithBooksAndDeleteBooksTrue_ThrowsFromIntegrationService() {
        Author author = new Author();
        Book book = new Book();
        book.setId(99);
        author.setBooks(Set.of(book));

        when(authorRepository.findWithBooksById(1)).thenReturn(Optional.of(author));
        doThrow(new ExistsException("In active orders")).when(integrationService)
                .checkOpenOrdersByBookIdsAndThrowExistingException(List.of(99), "Can't delete author with books that are in active orders: ");

        assertThrows(ExistsException.class, () -> authorService.deleteAuthorById(1, true));
        verify(authorRepository, never()).deleteById(anyInt());
    }

    @Test
    void deleteAuthorById_WhenNoBooks_DeletesSuccessfully() {
        Author author = new Author();
        author.setId(1);
        author.setBooks(Set.of());
        when(authorRepository.findWithBooksById(1)).thenReturn(Optional.of(author));

        authorService.deleteAuthorById(1, false);

        verify(authorRepository).deleteById(1);
    }
}

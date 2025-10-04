package com.simon.lib_service.service.integration;

import com.simon.lib_service.exception.RequestParameterException;
import com.simon.lib_service.model.Author;
import com.simon.lib_service.model.page.AuthorPageResponse;
import com.simon.lib_service.repository.AuthorRepository;
import com.simon.lib_service.service.AuthorService;
import com.simon.dto.lib.AuthorDTO;
import com.simon.dto.user.OrderDTO;
import com.simon.exception.ExistsException;
import com.simon.exception.NotFoundException;
import com.simon.utils.HttpRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.util.List;

import static com.simon.lib_service.TestModelCreator.*;
import static com.simon.lib_service.TestModelCreator.authorWithBook;
import static com.simon.lib_service.TestModelCreator.authorWithoutId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "/clear-database.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class AuthorServiceIntegrationTest {

    @Autowired
    private AuthorService authorService;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private HttpRequest httpRequest;

    @TestConfiguration
    static class TestConfig {
        @Primary
        @Bean
        public HttpRequest httpRequestTest() {
            return Mockito.mock(HttpRequest.class);
        }
    }

    @Test
    void findAll_WhenAuthorsExist_ReturnsAllAuthors() {
        authorRepository.saveAll(List.of(authorWithoutId(), authorWithoutId()));

        List<AuthorDTO> authors = authorService.findAll();

        assertThat(authors).hasSize(2);
    }

    @Test
    void findAll_WhenCalled_ReturnsPageOfAuthors() {
        preparePagination();
        Pageable pageable = PageRequest.of(0, 10);

        AuthorPageResponse authorsPage = authorService.findAll(pageable);

        assertThat(authorsPage).isNotNull();
        assertThat(authorsPage.totalPages()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void findByFullName_WhenExists_ReturnsAuthor() {
        Author author = authorWithoutId();
        author.setFullName("Dostoevsky");
        authorService.addAuthor(author);

        List<AuthorDTO> found = authorService.findByFullNameLike("Dostoevsky");

        assertThat(found).hasSize(1);
        assertThat(found.get(0).fullName()).isEqualTo("Dostoevsky");
    }

    @Test
    void findByFullName_WhenNullOrEmpty_ThrowsRequestParameterException() {
        assertThatThrownBy(() -> authorService.findByFullNameLike(null))
                .isInstanceOf(RequestParameterException.class);

        assertThatThrownBy(() -> authorService.findByFullNameLike("  "))
                .isInstanceOf(RequestParameterException.class);
    }

    @Test
    void findById_WhenExists_ReturnsAuthor() {
        AuthorDTO saved = authorService.addAuthor(authorWithoutId());

        AuthorDTO found = authorService.findById(saved.id());

        assertThat(found).isNotNull();
        assertThat(found.fullName()).isEqualTo(saved.fullName());
    }

    @Test
    void findById_WhenNotExists_ThrowsNotFoundException() {
        assertThatThrownBy(() -> authorService.findById(9999))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void findWithBooksById_WhenExists_ReturnsAuthor() {
        Author author = authorWithBook();
        authorRepository.save(author);

        Author found = authorService.findWithBooksById(author.getId());

        assertThat(found).isNotNull();
        assertThat(found.getBooks()).isNotEmpty();
    }

    @Test
    void findWithBooksById_WhenNotExists_ThrowsNotFoundException() {
        assertThatThrownBy(() -> authorService.findWithBooksById(9999))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void addAuthor_WhenValidAuthor_ReturnsSavedAuthor() {
        Author author = authorWithoutId();

        AuthorDTO saved = authorService.addAuthor(author);

        assertThat(saved.id()).isNotNull();
        assertThat(saved.fullName()).isEqualTo(author.getFullName());
    }

    @Test
    void addAuthor_WhenAuthorHasId_ThrowsExistsException() {
        Author author = authorWithId(1);

        assertThatThrownBy(() -> authorService.addAuthor(author))
                .isInstanceOf(ExistsException.class)
                .hasMessageContaining("Can't add author with custom id");
    }

    @Test
    void addAuthor_WhenDuplicateAuthor_ThrowsExistsException() {
        Author author = authorWithoutId();
        authorService.addAuthor(author);

        Author duplicate = authorWithoutId();

        assertThatThrownBy(() -> authorService.addAuthor(duplicate))
                .isInstanceOf(ExistsException.class)
                .hasMessageContaining("Author already exists");
    }

    @Test
    void updateAuthor_WhenAuthorExists_UpdatesSuccessfully() {
        Author author = authorWithoutId();
        AuthorDTO saved = authorService.addAuthor(author);

        Author toUpdate = Author.toAuthor(saved);
        toUpdate.setFullName("Updated name");

        AuthorDTO updated = authorService.updateAuthor(toUpdate);

        assertThat(updated.fullName()).isEqualTo("Updated name");
    }

    @Test
    void updateAuthor_WhenAuthorNotExists_ThrowsNotFoundException() {
        Author ghost = authorWithId(9999);

        assertThatThrownBy(() -> authorService.updateAuthor(ghost))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Can't find author with id");
    }

    @Test
    void deleteAuthorById_WhenNotExists_ThrowsNotFoundException() {
        assertThatThrownBy(() -> authorService.deleteAuthorById(404, false))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Can't find author with id");
    }

    @Test
    void deleteAuthorById_WhenNoBooksDeleteBooksFalse_DeletesSuccessfully() {
        AuthorDTO saved = authorService.addAuthor(authorWithoutId());

        authorService.deleteAuthorById(saved.id(), false);

        assertThat(authorRepository.existsById(saved.id())).isFalse();
    }

    @Test
    void deleteAuthorById_WhenNoBooksDeleteBooksTrue_DeletesSuccessfully() {
        AuthorDTO saved = authorService.addAuthor(authorWithoutId());

        authorService.deleteAuthorById(saved.id(), true);

        assertThat(authorRepository.existsById(saved.id())).isFalse();
    }

    @Test
    void deleteAuthorById_WhenAuthorHasBooksAndDeleteBooksFalse_ThrowsExistsException() {
        Author author = authorRepository.save(authorWithBook());

        assertThatThrownBy(() -> authorService.deleteAuthorById(author.getId(), false))
                .isInstanceOf(ExistsException.class)
                .hasMessageContaining("Can't delete author associated with book Ids");
    }

    @Test
    void deleteAuthorById_WhenAuthorHasBooksAndDeleteBooksTrue_NoActiveOrders_DeletesSuccessfully() {
        Author author = authorRepository.save(authorWithBook());

        Mockito.when(httpRequest.request(
                Mockito.anyString(),
                Mockito.eq(HttpMethod.POST),
                Mockito.any(HttpEntity.class),
                Mockito.any(ParameterizedTypeReference.class),
                Mockito.isNull()
        )).thenReturn(ResponseEntity.ok(List.of()));

        authorService.deleteAuthorById(author.getId(), true);

        assertThat(authorRepository.existsById(author.getId())).isFalse();
    }

    @Test
    void deleteAuthorById_WhenAuthorHasBooksAndDeleteBooksTrue_HasActiveOrders_ThrowsExistsException() {
        Author author = authorRepository.save(authorWithBook());

        Mockito.when(httpRequest.request(
                Mockito.anyString(),
                Mockito.eq(HttpMethod.POST),
                Mockito.any(HttpEntity.class),
                Mockito.any(ParameterizedTypeReference.class),
                Mockito.isNull()
        )).thenReturn(ResponseEntity.ok(List.of(Mockito.mock(OrderDTO.class))));

        assertThatThrownBy(() -> authorService.deleteAuthorById(author.getId(), true))
                .isInstanceOf(ExistsException.class)
                .hasMessageContaining("Can't delete author with books that are in active orders");
    }


    private void preparePagination() {
        Author author1 = authorWithBook();
        author1.setFullName("Author One");
        author1.setDateOfBirth(LocalDate.of(1960, 1, 1));

        Author author2 = authorWithoutId();
        author2.setFullName("Author Two");
        author2.setDateOfBirth(LocalDate.of(1970, 2, 2));

        authorRepository.saveAll(List.of(author1, author2));
    }
}

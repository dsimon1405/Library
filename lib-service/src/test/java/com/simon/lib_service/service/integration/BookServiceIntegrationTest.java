package com.simon.lib_service.service.integration;

import static com.simon.lib_service.TestModelCreator.*;

import com.simon.lib_service.model.Book;
import com.simon.lib_service.model.page.BookPageResponse;
import com.simon.dto.lib.BookDTO;
import com.simon.lib_service.repository.AuthorRepository;
import com.simon.lib_service.repository.BookRepository;
import com.simon.lib_service.repository.GenreRepository;
import com.simon.lib_service.service.BookService;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.util.List;

import static com.simon.lib_service.TestModelCreator.bookWithoutId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "/clear-database.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class BookServiceIntegrationTest {

    @Autowired
    private BookService bookService;
    @Autowired
    BookRepository bookRepository;
    @Autowired
    AuthorRepository authorRepository;
    @Autowired
    GenreRepository genreRepository;
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
    void findAll_WithFilters_ShouldReturnMatchingBooks() {
        Book b1 = bookWithoutId();
        b1.setTitle("Java Basics");
        b1.setOneDayRentPriceUSD(new BigDecimal("10.00"));
        b1.setAvailableQuantity(5);
        b1.setAuthor(authorWithoutId());
        b1.setGenre(genreWithoutId());
        b1 = bookRepository.save(b1);

        Book b2 = bookWithoutId();
        b2.setTitle("Spring Boot");
        b2.setOneDayRentPriceUSD(new BigDecimal("20.00"));
        b2.setAvailableQuantity(0);
        b2.setAuthor(authorWithoutId());
        b2.setGenre(genreWithoutId());
        b2 = bookRepository.save(b2);

        Book b3 = bookWithoutId();
        b3.setTitle("Cooking Book");
        b3.setOneDayRentPriceUSD(new BigDecimal("5.00"));
        b3.setAvailableQuantity(3);
        b3.setAuthor(authorWithoutId());
        b3.setGenre(genreWithoutId());
        b3 = bookRepository.save(b3);

        List<BookDTO> result = bookService.findAll(
                "Java", new BigDecimal("5"), new BigDecimal("15"), true, null, null
        );

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().title()).isEqualTo("Java Basics");
    }

    @Test
    void findAll_Pageable_ShouldReturnPagedResult() {
        for (int i = 1; i <= 10; i++) {
            Book b = bookWithoutId();
            b.setTitle("Book " + i);
            b.setAuthor(authorWithoutId());
            b.setGenre(genreWithoutId());
            bookRepository.save(b);
        }

        BookPageResponse response = bookService.findAll(PageRequest.of(0, 5), null, null, null, null, null, null);

        assertThat(response.content()).hasSize(5);
        assertThat(response.totalElements()).isEqualTo(10);
    }



    @Test
    void findById_WhenBookExists_ShouldReturnBookDTO() {
        Book saved = bookRepository.save(bookWithAuthorAndGenre());

        Book found = bookService.findByIdWithAuthorAndGenre(saved.getId());

        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(found.getAuthor()).isNotNull();
        assertThat(found.getGenre()).isNotNull();
    }

    @Test
    void findById_WhenBookNotExists_ShouldThrowNotFoundException() {
        int nonExistentId = 9999;
        NotFoundException thrown = assertThrows(NotFoundException.class, () -> bookService.findByIdWithAuthorAndGenre(nonExistentId));
        assertThat(thrown).hasMessageContaining("Can't find book with id: " + nonExistentId);
    }

    @Test
    void addBook_WhenBookHasId_ShouldThrowExistsException() {
        ExistsException thrown = assertThrows(ExistsException.class, () -> bookService.addBook(bookWithId(1)));
        assertThat(thrown).hasMessageContaining("Can't add book with custom id");
    }

    @Test
    void updateBook_WhenBookExists_ShouldUpdateAndReturnBook() {
        Book saved = bookRepository.save(bookWithoutId());
        saved.setTitle("After Update");

        BookDTO updated = bookService.updateBook(saved);

        assertThat(updated.title()).isEqualTo("After Update");
    }

    @Test
    void updateBook_WhenBookNotExists_ShouldThrowNotFoundException() {
        NotFoundException thrown = assertThrows(NotFoundException.class, () -> bookService.updateBook(bookWithId(666)));
        assertThat(thrown).hasMessageContaining("Can't find book with id");
    }

    @Test
    void deleteBookById_WhenBookExistsNotOrdered_ShouldDeleteSuccessfully() {
        BookDTO saved = bookService.addBook(bookWithoutId());

        Mockito.when(httpRequest.request(
                Mockito.anyString(),
                Mockito.eq(HttpMethod.POST),
                Mockito.any(HttpEntity.class),
                Mockito.any(ParameterizedTypeReference.class),
                Mockito.isNull()
        )).thenReturn(ResponseEntity.ok(List.of()));

        bookService.deleteBookById(saved.id());

        NotFoundException thrown = assertThrows(NotFoundException.class, () -> bookService.findByIdWithAuthorAndGenre(saved.id()));
        assertThat(thrown).hasMessageContaining("Can't find book with id");
    }

    @Test
    void deleteBookById_WhenBookExistsOrdered_ShouldThrowExistsException() {
        BookDTO saved = bookService.addBook(bookWithoutId());

        Mockito.when(httpRequest.request(
                Mockito.anyString(),
                Mockito.eq(HttpMethod.POST),
                Mockito.any(HttpEntity.class),
                Mockito.any(ParameterizedTypeReference.class),
                Mockito.isNull()
        )).thenReturn(ResponseEntity.ok(List.of(Mockito.mock(OrderDTO.class))));

        assertThatThrownBy(() -> bookService.deleteBookById(saved.id()))
                .isInstanceOf(ExistsException.class)
                .hasMessageContaining("Can't delete books that are in active orders:");
    }

    @Test
    void deleteBookById_WhenBookNotExists_ShouldThrowNotFoundException() {
        NotFoundException thrown = assertThrows(NotFoundException.class, () -> bookService.deleteBookById(99999));
        assertThat(thrown).hasMessageContaining("Can't find book with id");
    }
}
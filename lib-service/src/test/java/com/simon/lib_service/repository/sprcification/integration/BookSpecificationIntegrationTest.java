package com.simon.lib_service.repository.sprcification.integration;

import com.simon.lib_service.exception.RequestParameterException;
import com.simon.lib_service.model.Author;
import com.simon.lib_service.model.Book;
import com.simon.lib_service.model.Genre;
import com.simon.lib_service.model.specification.BookSpecification;
import com.simon.lib_service.repository.AuthorRepository;
import com.simon.lib_service.repository.BookRepository;
import com.simon.lib_service.repository.GenreRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class BookSpecificationIntegrationTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private GenreRepository genreRepository;

    @BeforeEach
    void setup() {
        // Clear DB
        bookRepository.deleteAll();
        authorRepository.deleteAll();
        genreRepository.deleteAll();

        Author author1 = new Author("John Smith", LocalDate.of(1970, 1, 1));
        Genre genre1 = new Genre("Fiction");

        authorRepository.save(author1);
        genreRepository.save(genre1);

        Book book1 = new Book("Spring in Action", BigDecimal.valueOf(15.0), 10);
        book1.setAuthor(author1);
        book1.setGenre(genre1);

        Book book2 = new Book("Java Basics", BigDecimal.valueOf(8.0), 0);
        book2.setAuthor(author1);
        book2.setGenre(genre1);

        bookRepository.saveAll(List.of(book1, book2));
    }

    // title_Like

    @Test
    void title_Like_WhenTitlePartIsNull_ReturnsAllBooks() {
        var spec = BookSpecification.title_Like(null);
        var books = bookRepository.findAll(spec);

        assertThat(books).hasSize(2);
    }

    @Test
    void title_Like_WhenTitlePartIsEmpty_ThrowsException() {
        var spec = BookSpecification.title_Like("  ");

        assertThatThrownBy(() -> bookRepository.findAll(spec))
                .isInstanceOf(RequestParameterException.class)
                .hasMessage("title_part can't be empty");
    }

    @Test
    void title_Like_WhenTitlePartIsValid_ReturnsMatchingBooks() {
        var spec = BookSpecification.title_Like("spring");
        var books = bookRepository.findAll(spec);

        assertThat(books).hasSize(1);
        assertThat(books.get(0).getTitle()).isEqualTo("Spring in Action");
    }

    // oneDayRentPriceUSD_greaterThenOeEqualTo

    @Test
    void oneDayRentPriceUSD_greaterThenOeEqualTo_WhenMinIsNull_ReturnsAllBooks() {
        var spec = BookSpecification.oneDayRentPriceUSD_greaterThenOeEqualTo(null);
        var books = bookRepository.findAll(spec);

        assertThat(books).hasSize(2);
    }

    @Test
    void oneDayRentPriceUSD_greaterThenOeEqualTo_WhenMinIsNegative_ThrowsException() {
        var spec = BookSpecification.oneDayRentPriceUSD_greaterThenOeEqualTo(BigDecimal.valueOf(-1.0));

        assertThatThrownBy(() -> bookRepository.findAll(spec))
                .isInstanceOf(RequestParameterException.class)
                .hasMessage("oneDayRentPriceUSD_min can't be less then 0.0");
    }

    @Test
    void oneDayRentPriceUSD_greaterThenOeEqualTo_WhenMinIsValid_ReturnsBooksWithPriceGE() {
        var spec = BookSpecification.oneDayRentPriceUSD_greaterThenOeEqualTo(BigDecimal.valueOf(10.0));
        var books = bookRepository.findAll(spec);

        assertThat(books).hasSize(1);
        assertThat(books.getFirst().getOneDayRentPriceUSD()).isGreaterThanOrEqualTo(BigDecimal.valueOf(10.0));
    }

    // oneDayRentPriceUSD_lessThanOrEqualTo

    @Test
    void oneDayRentPriceUSD_lessThanOrEqualTo_WhenMaxIsNull_ReturnsAllBooks() {
        var spec = BookSpecification.oneDayRentPriceUSD_lessThanOrEqualTo(null);
        var books = bookRepository.findAll(spec);

        assertThat(books).hasSize(2);
    }

    @Test
    void oneDayRentPriceUSD_lessThanOrEqualTo_WhenMaxIsNegative_ThrowsException() {
        var spec = BookSpecification.oneDayRentPriceUSD_lessThanOrEqualTo(BigDecimal.valueOf(-0.1));

        assertThatThrownBy(() -> bookRepository.findAll(spec))
                .isInstanceOf(RequestParameterException.class)
                .hasMessage("oneDayRentPriceUSD_min can't be less then 0.0");
    }

    @Test
    void oneDayRentPriceUSD_lessThanOrEqualTo_WhenMaxIsValid_ReturnsBooksWithPriceLE() {
        var spec = BookSpecification.oneDayRentPriceUSD_lessThanOrEqualTo(BigDecimal.valueOf(10.0));
        var books = bookRepository.findAll(spec);

        assertThat(books).hasSize(1);
        assertThat(books.get(0).getOneDayRentPriceUSD()).isLessThanOrEqualTo(BigDecimal.valueOf(10.0));
    }

    // availableQuantity_greaterThenZero

    @Test
    void availableQuantity_greaterThenZero_WhenIsAvailableNull_ReturnsAllBooks() {
        var spec = BookSpecification.availableQuantity_greaterThenZero(null);
        var books = bookRepository.findAll(spec);

        assertThat(books).hasSize(2);
    }

    @Test
    void availableQuantity_greaterThenZero_WhenIsAvailableFalse_ReturnsAllBooks() {
        var spec = BookSpecification.availableQuantity_greaterThenZero(false);
        var books = bookRepository.findAll(spec);

        assertThat(books).hasSize(2);
    }

    @Test
    void availableQuantity_greaterThenZero_WhenIsAvailableTrue_ReturnsBooksWithQuantityGTZero() {
        var spec = BookSpecification.availableQuantity_greaterThenZero(true);
        var books = bookRepository.findAll(spec);

        assertThat(books).hasSize(1);
        assertThat(books.get(0).getAvailableQuantity()).isGreaterThan(0);
    }

    // authorFullName_like

    @Test
    void authorFullName_like_WhenAuthorNamePartIsNull_ReturnsAllBooks() {
        var spec = BookSpecification.authorFullName_like(null);
        var books = bookRepository.findAll(spec);

        assertThat(books).hasSize(2);
    }

    @Test
    void authorFullName_like_WhenAuthorNamePartIsEmpty_ThrowsException() {
        var spec = BookSpecification.authorFullName_like("  ");

        assertThatThrownBy(() -> bookRepository.findAll(spec))
                .isInstanceOf(RequestParameterException.class)
                .hasMessage("authorName_part can't be empty");
    }

    @Test
    void authorFullName_like_WhenAuthorNamePartIsValid_ReturnsMatchingBooks() {
        var spec = BookSpecification.authorFullName_like("john");
        var books = bookRepository.findAll(spec);

        assertThat(books).hasSize(2);
        assertThat(books).allMatch(b -> b.getAuthor().getFullName().toLowerCase().contains("john"));
    }

    // genreName_like

    @Test
    void genreName_like_WhenGenreNamePartIsNull_ReturnsAllBooks() {
        var spec = BookSpecification.genreName_like(null);
        var books = bookRepository.findAll(spec);

        assertThat(books).hasSize(2);
    }

    @Test
    void genreName_like_WhenGenreNamePartIsEmpty_ThrowsException() {
        var spec = BookSpecification.genreName_like("  ");

        assertThatThrownBy(() -> bookRepository.findAll(spec))
                .isInstanceOf(RequestParameterException.class)
                .hasMessage("genreName_part can't be empty");
    }

    @Test
    void genreName_like_WhenGenreNamePartIsValid_ReturnsMatchingBooks() {
        var spec = BookSpecification.genreName_like("fic");
        var books = bookRepository.findAll(spec);

        assertThat(books).hasSize(2);
        assertThat(books).allMatch(b -> b.getGenre().getName().toLowerCase().contains("fic"));
    }
}


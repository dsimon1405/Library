package com.simon.lib_service.repository.integration;

import com.simon.lib_service.model.Book;
import com.simon.lib_service.repository.BookRepository;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.IntStream;

import static com.simon.lib_service.TestModelCreator.bookWithAuthorAndGenre;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Sql(scripts = "/clear-database.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class BookRepositoryIntegrationTest {

    @Autowired
    BookRepository bookRepository;

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void findByIdWithAuthorAndGenre() {
        Book saved = bookRepository.save(bookWithAuthorAndGenre());

        Book found = bookRepository.findByIdWithAuthorAndGenre(saved.getId()).orElse(null);

        assertNotNull(found);
        assertEquals(saved.getGenre().getName(), found.getGenre().getName());
        assertEquals(saved.getAuthor().getFullName(), found.getAuthor().getFullName());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void findAll_withSpecification_shouldReturnMatchingBooks() {
        Book book = bookRepository.save(bookWithAuthorAndGenre());

        Specification<Book> spec = (root, query, cb) -> {
            Predicate titlePredicate = cb.like(root.get("title"), "%itle%");
            Predicate quantityPredicate = cb.greaterThan(root.get("availableQuantity"), 0);
            return cb.and(titlePredicate, quantityPredicate);
        };

        List<Book> result = bookRepository.findAll(spec);
        assertThat(result).isNotEmpty();
        assertThat(result.getFirst().getTitle()).contains("itle");
        assertThat(result.getFirst().getGenre().getName()).isEqualTo(book.getGenre().getName());
        assertThat(result.getFirst().getAuthor().getFullName()).isEqualTo(book.getAuthor().getFullName());
    }

    @Test
    void findAll_withSpecificationAndPageable_shouldReturnPagedBooks() {
        List<Book> saved = bookRepository.saveAll(IntStream.range(0, 15).mapToObj(i -> {
            Book book = bookWithAuthorAndGenre();
            book.setTitle("Book " + i);
            return book;
        }).toList());

        Specification<Book> spec = (root, query, cb) -> cb.like(root.get("title"), "Book%");
        Pageable pageable = PageRequest.of(0, 10);

        Page<Book> page = bookRepository.findAll(spec, pageable);

        assertEquals(10, page.getContent().size());
        assertEquals(15, page.getTotalElements());
        assertThat(page.getContent().getFirst().getGenre().getName()).isEqualTo(saved.getFirst().getGenre().getName());
        assertThat(page.getContent().getFirst().getAuthor().getFullName()).isEqualTo(saved.getFirst().getAuthor().getFullName());
    }
}
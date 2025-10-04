package com.simon.lib_service.repository.integration;

import static com.simon.lib_service.TestModelCreator.*;
import com.simon.lib_service.model.Author;
import com.simon.lib_service.repository.AuthorRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Sql(scripts = "/clear-database.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class AuthorRepositoryIntegrationTest {

    @Autowired
    AuthorRepository authorRepository;

    @Test
    void findByFullName() {
        Author saved = authorRepository.save(authorWithoutId());

        List<Author> found = authorRepository.findByFullNameLike(saved.getFullName());

        assertThat(found).hasSize(1);
        assertEquals(saved.getId(), found.getFirst().getId());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void findWithBooksById() {
        Author saved = authorRepository.save(authorWithBook());

        Author found = authorRepository.findWithBooksById(saved.getId()).orElse(null);

        assertNotNull(found);
        assertDoesNotThrow(() -> found.getBooks().iterator().next().getTitle());
    }
}


//@Test
//@Transactional(propagation = Propagation.NOT_SUPPORTED)
//void findAllWithBooks() {
//    Book b1 = bookWithoutId();
//    Book b2 = bookWithoutId();
//    Author a1 = authorWithoutId();
//    Author a2 = authorWithoutId();
//    a1.addBook(b1);
//    a2.addBook(b2);
//    List<Author> saved = authorRepository.saveAll(List.of(a1, a2));
//
//    List<Author> found = authorRepository.findAllWithBooks();
//
//    assertThat(found).hasSize(2);
//    assertThat(found.getFirst().getBooks()).hasSize(1);
//}
//
//@Test
//@Transactional(propagation = Propagation.NOT_SUPPORTED)
//void findAllWithBooks_WhenCalled_ReturnsAuthorsWithBooksLoaded() {
//    Author author1 = authorWithoutId();
//    author1.addBook(bookWithoutId());
//    List<Author> saved = authorRepository.saveAll(List.of(author1, authorWithoutId()));
//
//    Pageable pageable = PageRequest.of(0, 10);
//
//    Page<Author> page = authorRepository.findAllWithBooks(pageable);
//
//    assertThat(page).isNotNull();
//    assertThat(page.getContent()).hasSize(2);
//
//    Author authorWithBooks = page.getContent().stream()
//            .filter(a -> saved.getFirst().getId().equals(a.getId()))
//            .findFirst()
//            .orElseThrow();
//
//    assertThat(authorWithBooks.getBooks()).isNotEmpty();
//    assertThat(authorWithBooks.getBooks().iterator().next().getTitle())
//            .isEqualTo(author1.getBooks().iterator().next().getTitle());
//
//    Author authorWithoutBooks = page.getContent().stream()
//            .filter(a -> saved.get(1).getId().equals(a.getId()))
//            .findFirst()
//            .orElseThrow();
//
//    assertThat(authorWithoutBooks.getBooks()).isEmpty();
//}
//
//@Test
//@Transactional(propagation = Propagation.NOT_SUPPORTED)
//void findByFullNameWithBooks() {
//    Author author1 = authorWithoutId();
//    author1.setFullName("some name");
//    author1.addBook(bookWithoutId());
//    List<Author> saved = authorRepository.saveAll(List.of(author1, authorWithoutId()));
//
//    List<Author> found = authorRepository.findByFullNameWithBooks(saved.getFirst().getFullName());
//
//    assertThat(found).hasSize(1);
//    assertEquals(saved.getFirst().getId(), found.getFirst().getId());
//    assertEquals(found.getFirst().getBooks().iterator().next().getTitle(),
//            saved.getFirst().getBooks().iterator().next().getTitle());
//}

package com.simon.lib_service.repository.integration;

import static com.simon.lib_service.TestModelCreator.*;

import com.simon.lib_service.model.Genre;
import com.simon.lib_service.repository.GenreRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Sql(scripts = "/clear-database.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class GenreRepositoryIntegrationTest {

    @Autowired
    private GenreRepository genreRepository;

    @Test
    void findByNameLike() {
        Genre g = genreWithoutId();
        g.setName("some name");
        List<Genre> saved = genreRepository.saveAll(List.of(genreWithoutId(), g, genreWithoutId()));

        List<Genre> found = genreRepository.findByNameLike(saved.getFirst().getName());

        assertThat(found).hasSize(2);
        assertEquals(saved.getFirst().getId(), found.getFirst().getId());
        assertEquals(saved.get(2).getId(), found.get(1).getId());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void findWithBooksById() {
        List<Genre> saved = genreRepository.saveAll(List.of(genreWithBook(), genreWithBook()));

        Genre found = genreRepository.findWithBooksById(saved.getFirst().getId()).orElse(null);

        assertNotNull(found);
        assertEquals(saved.getFirst().getId(), found.getId());
        assertEquals(saved.getFirst().getBooks().iterator().next().getTitle(), found.getBooks().iterator().next().getTitle());
    }
}


//@Test
//@Transactional(propagation = Propagation.NOT_SUPPORTED)
//void findAllWithBooks() {
//    List<Genre> saved = genreRepository.saveAll(List.of(createGenreWithBook(), createGenreWithBook()));
//
//    List<Genre> found = genreRepository.findAllWithBooks();
//
//    assertThat(found).hasSize(2);
//    assertThat(found.getFirst().getBooks()).hasSize(1);
//}
//
//@Test
//@Transactional(propagation = Propagation.NOT_SUPPORTED)
//void findAllWithBooks_WhenCalled_ReturnsGenresWithBooks() {
//    List<Genre> saved = genreRepository.saveAll(List.of(createGenreWithBook(), createGenreWithBook()));
//
//    Pageable pageable = PageRequest.of(0, 10);
//    Page<Genre> result = genreRepository.findAllWithBooks(pageable);
//
//    assertThat(result).isNotNull();
//    assertThat(result.getContent()).hasSize(2);
//
//    Genre genre_result = result.getContent().getFirst();
//    assertThat(genre_result.getBooks()).isNotEmpty();
//    assertThat(genre_result.getBooks().iterator().next().getId())
//            .isEqualTo(saved.getFirst().getBooks().iterator().next().getId());
//}
//
//@Test
//@Transactional(propagation = Propagation.NOT_SUPPORTED)
//void findByNameWithBooks() {
//    Genre g = genreWithoutId();
//    g.setName("some name");
//    List<Genre> saved = genreRepository.saveAll(List.of(createGenreWithBook(), g, createGenreWithBook()));
//
//    List<Genre> found = genreRepository.findByNameWithBooks(saved.getFirst().getName());
//
//    assertThat(found).hasSize(2);
//    assertEquals(saved.getFirst().getId(), found.getFirst().getId());
//    assertEquals(found.getFirst().getBooks().iterator().next().getId(),
//            saved.getFirst().getBooks().iterator().next().getId());
//}

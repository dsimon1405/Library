package com.simon.lib_service.service.integration;

import com.simon.lib_service.model.Genre;
import com.simon.lib_service.repository.GenreRepository;
import com.simon.lib_service.service.GenreService;
import com.simon.dto.lib.GenreDTO;
import com.simon.exception.ExistsException;
import com.simon.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static com.simon.lib_service.TestModelCreator.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "/clear-database.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class GenreServiceIntegrationTest {

    @Autowired
    private GenreService genreService;

    @Autowired
    private GenreRepository genreRepository;

    @Test
    void findAll_WhenGenresExist_ReturnsAllGenres() {
        genreRepository.saveAll(List.of(genreWithoutId(), genreWithoutId()));

        List<GenreDTO> genres = genreService.findAll();

        assertThat(genres).hasSize(2);
    }

    @Test
    void findAll_WhenCalled_ReturnsPageOfGenres() {
        preparePagination();
        Pageable pageable = PageRequest.of(0, 10);

        var genresPage = genreService.findAll(pageable);

        assertThat(genresPage).isNotNull();
        assertThat(genresPage.totalPages()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void findById_WhenExists_ReturnsGenre() {
        Genre savedGenre = genreRepository.save(genreWithoutId());

        GenreDTO found = genreService.findById(savedGenre.getId());

        assertThat(found).isNotNull();
        assertThat(found.name()).isEqualTo(savedGenre.getName());
    }

    @Test
    void findById_WhenNotExists_ThrowsNotFoundException() {
        assertThatThrownBy(() -> genreService.findById(9999))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void addGenre_WhenValidGenre_ReturnsSavedGenre() {
        Genre genre = genreWithoutId();

        GenreDTO saved = genreService.addGenre(genre);

        assertThat(saved.id()).isNotNull();
        assertThat(saved.name()).isEqualTo(genre.getName());
    }

    @Test
    void addGenre_WhenGenreHasId_ThrowsExistsException() {
        Genre genre = genreWithId(1);

        assertThatThrownBy(() -> genreService.addGenre(genre))
                .isInstanceOf(ExistsException.class)
                .hasMessageContaining("Can't add genre with custom id");
    }

    @Test
    void addGenre_WhenDuplicateGenre_ThrowsExistsException() {
        Genre genre = genreWithoutId();
        genreService.addGenre(genre);

        Genre duplicate = genreWithoutId();

        assertThatThrownBy(() -> genreService.addGenre(duplicate))
                .isInstanceOf(ExistsException.class)
                .hasMessageContaining("Genre already exists");
    }

    @Test
    void updateGenre_WhenGenreExists_UpdatesSuccessfully() {
        Genre genre = genreWithoutId();
        GenreDTO saved = genreService.addGenre(genre);

        Genre toUpdate = new Genre(saved.id(), "Updated Fiction");

        GenreDTO updated = genreService.updateGenre(toUpdate);

        assertThat(updated.name()).isEqualTo("Updated Fiction");
    }

    @Test
    void updateGenre_WhenGenreNotExists_ThrowsNotFoundException() {
        Genre ghost = genreWithId(9999);

        assertThatThrownBy(() -> genreService.updateGenre(ghost))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Can't find genre with id");
    }

    @Test
    void deleteGenreById_WhenNotExists_ThrowsNotFoundException() {
        assertThatThrownBy(() -> genreService.deleteGenreById(404))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Can't find genre with id");
    }

    @Test
    void deleteGenreById_WhenNoBooks_DeletesSuccessfully() {
        Genre genre = genreWithoutId();
        GenreDTO saved = genreService.addGenre(genre);

        genreService.deleteGenreById(saved.id());

        assertThat(genreRepository.existsById(saved.id())).isFalse();
    }

    @Test
    void deleteGenreById_WhenGenreHasBooks_ThrowsExistsException() {
        Genre genre = genreWithBook();
        genreRepository.save(genre);

        assertThatThrownBy(() -> genreService.deleteGenreById(genre.getId()))
                .isInstanceOf(ExistsException.class)
                .hasMessageContaining("Can't delete genre associated with book Ids");
    }

    private void preparePagination() {
        Genre genre1 = genreWithBook();
        genre1.setName("Genre One");

        Genre genre2 = genreWithoutId();
        genre2.setName("Genre Two");

        genreRepository.saveAll(List.of(genre1, genre2));
    }
}

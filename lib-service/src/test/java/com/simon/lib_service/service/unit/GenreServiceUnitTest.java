package com.simon.lib_service.service.unit;

import com.simon.lib_service.exception.RequestParameterException;
import com.simon.lib_service.model.Genre;
import com.simon.lib_service.model.Book;
import com.simon.lib_service.model.page.GenrePageResponse;
import com.simon.lib_service.repository.GenreRepository;
import com.simon.lib_service.repository.SqlHelper;
import com.simon.lib_service.service.GenreService;
import com.simon.dto.lib.GenreDTO;
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
public class GenreServiceUnitTest {

    @Mock
    private GenreRepository genreRepository;

    @InjectMocks
    private GenreService genreService;

    @Test
    public void findAll_ReturnAllGenres() {
        List<Genre> genres = List.of(new Genre("Fantasy"), new Genre("Sci-Fi"));
        when(genreRepository.findAll()).thenReturn(genres);
        List<GenreDTO> result = genreService.findAll();

        assertEquals(2, result.size());
        verify(genreRepository, times(1)).findAll();
    }

    @Test
    void findAll_ReturnsPageOfGenres() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Genre> expectedPage = new PageImpl<>(List.of(new Genre("Fantasy"), new Genre("Sci-Fi")));

        when(genreRepository.findAll(pageable)).thenReturn(expectedPage);

        GenrePageResponse actualPage = genreService.findAll(pageable);

        assertThat(actualPage.content()).hasSize(2);
        verify(genreRepository, times(1)).findAll(pageable);
    }

    @Test
    void findById_WhenExists_ReturnsGenre() {
        Genre genre = new Genre(1, "Fantasy");
        when(genreRepository.findById(1)).thenReturn(Optional.of(genre));

        GenreDTO found = genreService.findById(1);

        assertThat(found.id()).isEqualTo(genre.getId());
    }

    @Test
    void findById_WhenNotExists_ThrowsNotFoundException() {
        when(genreRepository.findById(404)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> genreService.findById(404))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Can't find genre with id: 404");
    }

    @Test
    void findByNameLike_WhenExists_ReturnGenre() {
        String name = "Fan";
        Genre genre = new Genre("Fantasy");
        when(genreRepository.findByNameLike(SqlHelper.toLike(name))).thenReturn(List.of(genre));

        List<GenreDTO> result = genreService.findByNameLike(name);

        assertThat(result).hasSize(1);
        assertEquals(genre.getName(), result.getFirst().name());
        verify(genreRepository, times(1)).findByNameLike(SqlHelper.toLike(name));
    }

    @Test
    void findByNameLike_WhenNull_ThrowRequestParamException() {
        assertThrows(RequestParameterException.class, () -> genreService.findByNameLike(null));
    }

    @Test
    void findByNameLike_WhenEmpty_ThrowRequestParamException() {
        assertThrows(RequestParameterException.class, () -> genreService.findByNameLike("  "));
    }

    @Test
    void addGenre_WhenGenreIdIsNotNull_ThrowExistsException() {
        Genre genre = new Genre("Fantasy");
        genre.setId(4);

        Exception result = assertThrows(ExistsException.class, () -> genreService.addGenre(genre));
        assertEquals("Can't add genre with custom id", result.getMessage());
    }

    @Test
    void addGenre_WhenGenreExists_ThrowExistsException() {
        Genre genre = new Genre("Fantasy");
        when(genreRepository.exists(any())).thenReturn(true);

        Exception result = assertThrows(ExistsException.class, () -> genreService.addGenre(genre));
        assertTrue(result.getMessage().contains("Genre already exists"));
        verify(genreRepository, never()).save(any());
    }

    @Test
    void addGenre_WhenNotExists_SaveAndReturnGenre() {
        Genre genre = new Genre("Fantasy");

        when(genreRepository.exists(any())).thenReturn(false);
        when(genreRepository.save(genre)).thenReturn(genre);

        GenreDTO result = genreService.addGenre(genre);

        assertEquals(genre.getName(), result.name());
        verify(genreRepository, times(1)).exists(any());
        verify(genreRepository, times(1)).save(genre);
    }

    @Test
    void updateGenre_WhenGenreIdNull_ThrowNotFoundException() {
        Exception result = assertThrows(NotFoundException.class, () -> genreService.updateGenre(new Genre("Fantasy")));
        assertTrue(result.getMessage().contains("Can't find genre with id:"));
        verify(genreRepository, never()).save(any());
    }

    @Test
    void updateGenre_WhenGenreNotExists_ThrowNotFoundException() {
        Genre genre = new Genre("Fantasy");
        genre.setId(4);
        when(genreRepository.existsById(genre.getId())).thenReturn(false);

        Exception result = assertThrows(NotFoundException.class, () -> genreService.updateGenre(genre));
        assertTrue(result.getMessage().contains("Can't find genre with id:"));
        verify(genreRepository, never()).save(any());
    }

    @Test
    void updateGenre_WhenGenreExists_SaveAndReturnGenre() {
        int id = 4;
        Genre genre = new Genre(id, "Fantasy");

        when(genreRepository.existsById(genre.getId())).thenReturn(true);
        when(genreRepository.save(genre)).thenReturn(genre);

        GenreDTO result = genreService.updateGenre(genre);

        assertEquals(id, result.id());
        verify(genreRepository, times(1)).existsById(id);
        verify(genreRepository, times(1)).save(genre);
    }

    @Test
    void deleteGenreById_WhenGenreNotFound_ThrowNotFoundException() {
        when(genreRepository.findWithBooksById(1)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> genreService.deleteGenreById(1));
        verify(genreRepository, never()).deleteById(anyInt());
    }

    @Test
    void deleteGenreById_WithBooks_ThrowExistsException() {
        Genre genre = new Genre(1, "Fantasy");
        Book book = new Book();
        book.setId(10);
        genre.addBook(book);
        when(genreRepository.findWithBooksById(1)).thenReturn(Optional.of(genre));

        assertThrows(ExistsException.class, () -> genreService.deleteGenreById(1));
        verify(genreRepository, never()).deleteById(anyInt());
    }

    @Test
    void deleteGenreById_WhenNoBooks_DeletesSuccessfully() {
        Genre genre = new Genre(1, "Fantasy");
        genre.setBooks(Set.of());
        when(genreRepository.findWithBooksById(1)).thenReturn(Optional.of(genre));

        genreService.deleteGenreById(1);

        verify(genreRepository).deleteById(1);
    }
}

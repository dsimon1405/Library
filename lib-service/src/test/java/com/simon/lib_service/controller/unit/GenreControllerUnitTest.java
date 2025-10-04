package com.simon.lib_service.controller.unit;

import com.simon.lib_service.controller.GenreController;
import com.simon.lib_service.model.Genre;
import com.simon.dto.lib.GenreDTO;
import com.simon.lib_service.model.page.GenrePageResponse;
import com.simon.lib_service.service.GenreService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenreControllerUnitTest {

    @Mock
    private GenreService genreService;

    @InjectMocks
    private GenreController genreController;

    @Test
    void findAll_ReturnsListOfGenreDTO() {
        List<GenreDTO> genres = List.of(
                new GenreDTO(1, "Horror"),
                new GenreDTO(2, "Fantasy")
        );
        when(genreService.findAll()).thenReturn(genres);

        List<GenreDTO> result = genreController.findAll();

        assertEquals(2, result.size());
        verify(genreService, times(1)).findAll();
    }

    @Test
    void findPage_ReturnsGenrePageResponse() {
        Pageable pageable = PageRequest.of(0, 5);
        GenrePageResponse response = mock(GenrePageResponse.class);
        when(genreService.findAll(pageable)).thenReturn(response);

        GenrePageResponse result = genreController.findPage(pageable);

        assertEquals(response, result);
        verify(genreService, times(1)).findAll(pageable);
    }

    @Test
    void findById_WhenGenreExists_ReturnsResponseEntityWithGenreAndStatusFound() {
        GenreDTO genreDTO = new GenreDTO(1, "Drama");
        when(genreService.findById(1)).thenReturn(genreDTO);

        ResponseEntity<GenreDTO> response = genreController.findById(1);

        assertEquals(HttpStatus.FOUND, response.getStatusCode());
        assertEquals(genreDTO, response.getBody());
        verify(genreService, times(1)).findById(1);
    }

    @Test
    void findByNameLike_WhenCalled_ReturnsListOfGenreDTO() {
        String name = "Fan";
        List<GenreDTO> genres = List.of(new GenreDTO(1, "Fantasy"));
        when(genreService.findByNameLike(name)).thenReturn(genres);

        List<GenreDTO> result = genreController.findByNameLike(name);

        assertEquals(genres, result);
        verify(genreService, times(1)).findByNameLike(name);
    }

    @Test
    void add_WhenValidGenre_ReturnsResponseEntityWithCreatedStatus() {
        Genre genre = new Genre("Mystery");
        GenreDTO genreDTO = new GenreDTO(1, "Mystery");
        when(genreService.addGenre(genre)).thenReturn(genreDTO);

        ResponseEntity<GenreDTO> response = genreController.add(genre);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(genreDTO, response.getBody());
        verify(genreService, times(1)).addGenre(genre);
    }

    @Test
    void update_WhenValidGenre_ReturnsResponseEntityWithOkStatus() {
        Genre genre = new Genre("Thriller");
        genre.setId(1);
        GenreDTO genreDTO = new GenreDTO(1, "Thriller");
        when(genreService.updateGenre(genre)).thenReturn(genreDTO);

        ResponseEntity<GenreDTO> response = genreController.update(genre);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(genreDTO, response.getBody());
        verify(genreService, times(1)).updateGenre(genre);
    }

    @Test
    void deleteById_WhenCalled_InvokesServiceDelete() {
        int id = 1;
        doNothing().when(genreService).deleteGenreById(id);

        genreController.deleteById(id);

        verify(genreService, times(1)).deleteGenreById(id);
    }
}

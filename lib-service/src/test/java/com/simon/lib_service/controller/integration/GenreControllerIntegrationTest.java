package com.simon.lib_service.controller.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simon.lib_service.model.Genre;
import com.simon.lib_service.repository.GenreRepository;
import com.simon.name.PathRoles;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GenreControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private final String BASE_URL = "/api/v1/genre";
    private final String ADMIN_URL = BASE_URL + PathRoles.ADMIN;

    @BeforeEach
    void setUp() {
        genreRepository.deleteAll();
    }

    @Test
    void findAll_ReturnsAllGenres() throws Exception {
        genreRepository.saveAll(List.of(
                new Genre("Sci-Fi"),
                new Genre("Mystery")
        ));

        mockMvc.perform(get(BASE_URL + "/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Sci-Fi")));
    }

    @Test
    void findPage_ReturnsPagedGenres() throws Exception {
        genreRepository.saveAll(List.of(
                new Genre("History"),
                new Genre("Comedy"),
                new Genre("Adventure")
        ));

        mockMvc.perform(get(BASE_URL + "/page?page=0&size=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements", is(3)));
    }

    @Test
    void findById_WhenExists_ReturnsGenre() throws Exception {
        Genre genre = genreRepository.save(new Genre("Drama"));

        mockMvc.perform(get(BASE_URL + "/" + genre.getId()))
                .andExpect(status().isFound())
                .andExpect(jsonPath("$.name", is("Drama")));
    }

    @Test
    void findByNameLike_WhenMatchExists_ReturnsMatchingGenres() throws Exception {
        genreRepository.save(new Genre("Fantasy"));

        mockMvc.perform(get(BASE_URL + "?name_part=Fan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", containsString("Fan")));
    }

    @Test
    void add_WhenValidGenre_ReturnsCreatedGenre() throws Exception {
        Genre genre = new Genre("Horror");

        mockMvc.perform(post(ADMIN_URL + "/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(genre)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("Horror")));
    }

    @Test
    void update_WhenValidGenre_ReturnsUpdatedGenre() throws Exception {
        Genre genre = genreRepository.save(new Genre("Romance"));
        genre.setName("Modern Romance");

        mockMvc.perform(post(ADMIN_URL + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(genre)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Modern Romance")));
    }

    @Test
    void deleteById_WhenExists_DeletesGenre() throws Exception {
        Genre genre = genreRepository.save(new Genre("Delete Me"));

        mockMvc.perform(delete(ADMIN_URL + "/delete/{id}", genre.getId()))
                .andExpect(status().isOk());

        Assertions.assertTrue(genreRepository.findById(genre.getId()).isEmpty());
    }
}

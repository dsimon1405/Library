package com.simon.lib_service.controller.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simon.lib_service.model.Author;
import com.simon.lib_service.model.Book;
import com.simon.lib_service.model.Genre;
import com.simon.lib_service.repository.AuthorRepository;
import com.simon.lib_service.repository.BookRepository;
import com.simon.lib_service.repository.GenreRepository;
import com.simon.name.PathRoles;
import com.simon.utils.HttpRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.simon.lib_service.TestModelCreator.bookWithoutId;
import static com.simon.lib_service.TestModelCreator.authorWithoutId;
import static com.simon.lib_service.TestModelCreator.genreWithoutId;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "/clear-database.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class BookControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private BookRepository bookRepository;
    @Autowired private AuthorRepository authorRepository;
    @Autowired private GenreRepository genreRepository;
    @Autowired private ObjectMapper objectMapper;

    private final String path_book = "/api/v1/book";
    private final String path_admin = path_book + PathRoles.ADMIN;
    private final String path_service = path_book + PathRoles.SERVICE;

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
    void findAll_ReturnAllBooks() throws Exception {
        List<Book> saved = bookRepository.saveAll(List.of(bookWithoutId(), bookWithoutId()));

        mockMvc.perform(get(path_book + "/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(saved.getFirst().getId()))
                .andExpect(jsonPath("$[1].id").value(saved.getLast().getId()));
    }

    @Test
    void findById_WhenIdExists_ReturnBook() throws Exception {
        Book saved = bookRepository.save(bookWithoutId());

        mockMvc.perform(get(path_book + "/" + saved.getId()))
                .andExpect(status().isFound())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.title").value(saved.getTitle()));
    }

    @Test
    void add_ValidBook_AddsBook() throws Exception {
        Book book = bookWithoutId();

        mockMvc.perform(post(path_admin + "/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(book.getTitle()));
    }

    @Test
    void update_ValidBook_UpdatesBook() throws Exception {
        Book saved = bookRepository.save(bookWithoutId());
        saved.setTitle("Updated Title");

        mockMvc.perform(post(path_admin + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saved)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    void updateQuantity_ChangesQuantity() throws Exception {
        Book saved = bookRepository.save(bookWithoutId());

        mockMvc.perform(put(path_admin + "/update/" + saved.getId())
                        .param("quantity_change_on", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableQuantity").value(saved.getAvailableQuantity() + 2));
    }

    @Test
    void deleteById_DeletesBook() throws Exception {
        Book saved = bookRepository.save(bookWithoutId());

        Mockito.when(httpRequest.request(
                Mockito.anyString(),
                Mockito.eq(HttpMethod.POST),
                Mockito.any(HttpEntity.class),
                Mockito.any(ParameterizedTypeReference.class),
                Mockito.isNull()
        )).thenReturn(ResponseEntity.ok(List.of()));

        mockMvc.perform(delete(path_admin + "/delete/{id}", saved.getId()))
                .andExpect(status().isOk());
    }

    @Test
    void associate_AssignsAuthorAndGenre() throws Exception {
        Book book = bookRepository.save(bookWithoutId());
        Author author = authorRepository.save(authorWithoutId());
        Genre genre = genreRepository.save(genreWithoutId());

        mockMvc.perform(put(path_admin + "/associate")
                        .param("book_id", book.getId().toString())
                        .param("author_id", author.getId().toString())
                        .param("genre_id", genre.getId().toString()))
                .andExpect(status().isOk());
    }
}


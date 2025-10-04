package com.simon.lib_service.controller.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import static com.simon.lib_service.TestModelCreator.*;
import com.simon.lib_service.model.Author;
import com.simon.lib_service.repository.AuthorRepository;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static com.simon.lib_service.TestModelCreator.authorWithoutId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.List;

import static javax.swing.UIManager.get;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "/clear-database.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class AuthorControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    ObjectMapper objectMapper;

    private final String path_author = "/api/v1/author";
    private final String path_admin = path_author + PathRoles.ADMIN;

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
    void findAll_ReturnAllAuthors() throws Exception {
        List<Author> saved = authorRepository.saveAll(List.of(authorWithoutId(), authorWithoutId()));

        mockMvc.perform(MockMvcRequestBuilders.get(path_author + "/all")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$[0].id").value(saved.getFirst().getId()),
                        jsonPath("$[1].id").value(saved.getLast().getId())
                        );
    }

    @Test
    void findPage_ReturnPage() throws Exception {
        List<Author> saved = authorRepository
                .saveAll(List.of(authorWithoutId(), authorWithoutId(), authorWithoutId(), authorWithoutId()));

        mockMvc.perform(MockMvcRequestBuilders.get(path_author + "/page")
                        .param("page", "1")
                        .param("size", "2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("content", hasSize(2)),
                        jsonPath("$.content[0].id").value(saved.get(2).getId()),
                        jsonPath("$.content[1].id").value(saved.get(3).getId())
                );
    }

    @Test
    void findById_WhenIdExists_ReturnAuthor() throws Exception {
        Author saved = authorRepository.save(authorWithoutId());

        mockMvc.perform(MockMvcRequestBuilders.get(path_author + "/" + saved.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isFound(),
                        jsonPath("$.id").value(saved.getId()),
                        jsonPath("$.fullName").value(saved.getFullName()),
                        jsonPath("$.dateOfBirth").value(saved.getDateOfBirth().toString())
                );
    }

    @Test
    void findById_WhenIdNotFound_ReturnNotFoundException() throws Exception {
        int id = 666;
        mockMvc.perform(MockMvcRequestBuilders.get(path_author + "/" + id)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isNotFound(),
                        jsonPath("$.error").value("Can't find author with id: " + id)
                );
    }

    @Test
    void findByFullNameLike_WhenFullNameExists_ReturnAuthor() throws Exception {
        Author saved = authorRepository.save(authorWithoutId());
        mockMvc.perform(MockMvcRequestBuilders.get(path_author)
                        .param("fullName_part", saved.getFullName())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$[0].id").value(saved.getId()),
                        jsonPath("$[0].fullName").value(saved.getFullName()),
                        jsonPath("$[0].dateOfBirth").value(saved.getDateOfBirth().toString())
                );
    }

    @Test
    void add_AuthorValid_AddAuthor() throws Exception {
        Author author = authorWithoutId();
        mockMvc.perform(MockMvcRequestBuilders.post(path_admin + "/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(author))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isCreated(),
                        jsonPath("$.fullName").value(author.getFullName()),
                        jsonPath("$.dateOfBirth").value(author.getDateOfBirth().toString())
                );
    }

    @Test
    void add_WhenFullNameEmpty_ReturnsValidationError() throws Exception {
        Author invalidAuthor = authorWithoutId();
        invalidAuthor.setFullName("");

        mockMvc.perform(MockMvcRequestBuilders.post(path_admin + "/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidAuthor)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.fullName")
                        .value("Author.fullName - can't be empty"));
    }

    @Test
    void add_WhenDateOfBirthInFuture_ReturnsValidationError() throws Exception {
        Author invalidAuthor = authorWithoutId();
        invalidAuthor.setDateOfBirth(LocalDate.now().plusDays(1));  // дата в будущем

        mockMvc.perform(MockMvcRequestBuilders.post(path_admin + "/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidAuthor)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.dateOfBirth")
                        .value("Author.dateOfBirth - must be in the past"));
    }

    @Test
    void add_WhenIdNull_ReturnsExistsException() throws Exception {
        Author invalidAuthor = authorWithId(666);

        mockMvc.perform(MockMvcRequestBuilders.post(path_admin + "/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidAuthor)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Can't add author with custom id"));
    }

    @Test
    void update_AuthorExists_Update() throws Exception {
        Author saved = authorRepository.save(authorWithoutId());
        saved.setFullName("new name");
        mockMvc.perform(MockMvcRequestBuilders.post(path_admin + "/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saved))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.fullName").value(saved.getFullName()),
                        jsonPath("$.dateOfBirth").value(saved.getDateOfBirth().toString())
                );
    }

    @Test
    void deleteById_HaveBooksAndDeleteBooksTrue_Delete() throws Exception {
        Author saved = authorRepository.save(authorWithBook());

        Mockito.when(httpRequest.request(
                Mockito.anyString(),
                Mockito.eq(HttpMethod.POST),
                Mockito.any(HttpEntity.class),
                Mockito.any(ParameterizedTypeReference.class),
                Mockito.isNull()
        )).thenReturn(ResponseEntity.ok(List.of()));

        mockMvc.perform(MockMvcRequestBuilders.delete(path_admin + "/delete/{id}", saved.getId())
                        .param("delete_books", "true"))
                .andExpect(status().isOk());

        assertThat(authorRepository.existsById(saved.getId())).isFalse();
    }

    @Test
    void deleteById_HaveBooksAndDeleteBooksFalse_Delete() throws Exception {
        Author saved = authorRepository.save(authorWithBook());
        mockMvc.perform(MockMvcRequestBuilders.delete(path_admin + "/delete/{id}", saved.getId())
                        .param("delete_books", "false"))
                .andExpectAll(
                        status().isConflict(),
                        jsonPath("$.error")
                                .value("Can't delete author associated with book Ids: ["
                                        + saved.getBooks().iterator().next().getId() + "]")
                );
    }
}
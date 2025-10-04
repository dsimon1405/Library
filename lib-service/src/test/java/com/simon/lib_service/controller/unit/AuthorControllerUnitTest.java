package com.simon.lib_service.controller.unit;

import static com.simon.lib_service.TestModelCreator.*;
import com.simon.lib_service.controller.AuthorController;
import com.simon.dto.lib.AuthorDTO;
import com.simon.lib_service.model.Author;
import com.simon.lib_service.model.page.AuthorPageResponse;
import com.simon.lib_service.service.AuthorService;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

@ExtendWith(MockitoExtension.class)
public class AuthorControllerUnitTest {

    @Mock
    private AuthorService authorService;
    @InjectMocks
    private AuthorController authorController;


    @Test
    void findAll_ReturnsListOfAuthorDTO() {
        List<AuthorDTO> authors = List.of(Author.toDTO(authorWithId(1)),
                Author.toDTO(authorWithId(2)));
        when(authorService.findAll()).thenReturn(authors);

        List<AuthorDTO> result = authorController.findAll();

        assertEquals(2, result.size());
        verify(authorService, times(1)).findAll();
    }

    @Test
    void findPage_ReturnsAuthorPageResponse() {
        Pageable pageable = PageRequest.of(0, 3);
        AuthorPageResponse response = mock(AuthorPageResponse.class);
        when(authorService.findAll(pageable)).thenReturn(response);

        AuthorPageResponse result = authorController.findPage(pageable);

        assertEquals(response, result);
        verify(authorService, times(1)).findAll(pageable);
    }

    @Test
    void findById_WhenAuthorExists_ReturnsResponseEntityWithAuthorAndStatusFound() {
        AuthorDTO author = Author.toDTO(authorWithId(1));
        when(authorService.findById(author.id())).thenReturn(author);

        ResponseEntity<AuthorDTO> result = authorController.findById(author.id());

        assertEquals(HttpStatus.FOUND, result.getStatusCode());
        assertEquals(author, result.getBody());
        verify(authorService, times(1)).findById(author.id());
    }

    @Test
    void findByFullName_WhenCalled_ReturnsListOfAuthorDTO() {
        List<AuthorDTO> expected = List.of(Author.toDTO(authorWithoutId()));
        String fullName = "John Doe";

        when(authorService.findByFullNameLike(fullName)).thenReturn(expected);

        List<AuthorDTO> result = authorController.findByFullNameLike(fullName);

        assertEquals(expected, result);
        verify(authorService, times(1)).findByFullNameLike(fullName);
    }

    @Test
    void add_WhenValidAuthor_ReturnsResponseEntityWithCreatedStatus() {
        Author author = authorWithoutId();
        AuthorDTO dto = Author.toDTO(author);
        when(authorService.addAuthor(author)).thenReturn(dto);

        ResponseEntity<AuthorDTO> response = authorController.add(author);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(dto, response.getBody());
        verify(authorService, times(1)).addAuthor(author);
    }

    @Test
    void update_WhenValidAuthor_ReturnsResponseEntityWithOkStatus() {
        Author author = authorWithoutId();
        AuthorDTO dto = Author.toDTO(author);
        when(authorService.updateAuthor(author)).thenReturn(dto);

        ResponseEntity<AuthorDTO> response = authorController.update(author);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());
        verify(authorService, times(1)).updateAuthor(author);
    }

    @Test
    void deleteById_WhenDeleteBooks_InvokesServiceDelete() {
        int id = 1;

        doNothing().when(authorService).deleteAuthorById(id, true);

        authorController.deleteById(id, true);

        verify(authorService, times(1)).deleteAuthorById(id, true);
    }

    @Test
    void deleteById_NotDeleteBooks_ContainBooks_() {
        int id = 1;

        doNothing().when(authorService).deleteAuthorById(id, false);

        authorController.deleteById(id, false);

        verify(authorService, times(1)).deleteAuthorById(id, false);
    }
}

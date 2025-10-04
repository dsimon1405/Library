package com.simon.lib_service.controller.unit;

import com.simon.lib_service.controller.BookController;
import com.simon.lib_service.model.Book;
import com.simon.dto.lib.BookDTO;
import com.simon.lib_service.model.page.BookPageResponse;
import com.simon.lib_service.service.AssociationService;
import com.simon.lib_service.service.BookService;
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

import static com.simon.lib_service.TestModelCreator.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookControllerUnitTest {

    @Mock private BookService bookService;
    @Mock private AssociationService associationService;
    @InjectMocks private BookController bookController;

    @Test
    void findAll_ReturnsBookList() {
        List<BookDTO> books = List.of(Book.toDTO(bookWithId(1)), Book.toDTO(bookWithId(2)));
        when(bookService.findAll(null, null, null,
                null, null, null)).thenReturn(books);

        List<BookDTO> result = bookController.findAll(null, null,
                null, null, null, null);

        assertEquals(2, result.size());
        verify(bookService, times(1)).findAll(null, null,
                null, null, null, null);
    }

    @Test
    void findPage_ReturnsBookPageResponse() {
        Pageable pageable = PageRequest.of(1, 2);
        BookPageResponse response = mock(BookPageResponse.class);
        when(bookService.findAll(pageable, null, null, null,
                null, null, null))
                .thenReturn(response);

        BookPageResponse result = bookController.findPage(pageable, null, null,
                null, null, null, null);

        assertEquals(response, result);
        verify(bookService, times(1)).findAll(pageable, null, null,
                null, null, null, null);
    }

    @Test
    void findById_ReturnsBookDTOWithStatusFound() {
        Book book = bookWithId(1);
        when(bookService.findByIdWithAuthorAndGenre(1)).thenReturn(book);

        ResponseEntity<BookDTO> response = bookController.findById(1);

        assertEquals(HttpStatus.FOUND, response.getStatusCode());
        assertEquals(book.getId(), response.getBody().id());
        verify(bookService).findByIdWithAuthorAndGenre(1);
    }

    @Test
    void addBook_ReturnsCreated() {
        Book book = bookWithoutId();
        BookDTO dto = Book.toDTO(book);
        when(bookService.addBook(book)).thenReturn(dto);

        ResponseEntity<BookDTO> response = bookController.add(book);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(dto, response.getBody());
        verify(bookService).addBook(book);
    }

    @Test
    void updateBook_ReturnsOk() {
        Book book = bookWithId(1);
        BookDTO dto = Book.toDTO(book);
        when(bookService.updateBook(book)).thenReturn(dto);

        ResponseEntity<BookDTO> response = bookController.update(book);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());
        verify(bookService).updateBook(book);
    }

    @Test
    void deleteBook_CallsService() {
        doNothing().when(bookService).deleteBookById(1);

        bookController.deleteById(1);

        verify(bookService).deleteBookById(1);
    }

    @Test
    void associate_CallsAssociateGenreWhenOnlyGenreIdProvided() {
        int bookId = 1;
        int genreId = 3;
        int authorId = 3;

        Book book = bookWithId(bookId);
        when(associationService.makeAssociation(bookId, authorId, genreId)).thenReturn(book);

        bookController.associate(bookId, authorId, genreId);

        verify(associationService, times(1)).makeAssociation(bookId, authorId, genreId);
    }
}

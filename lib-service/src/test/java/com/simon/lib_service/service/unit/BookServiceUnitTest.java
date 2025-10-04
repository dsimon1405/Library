package com.simon.lib_service.service.unit;

import static com.simon.lib_service.TestModelCreator.*;
import com.simon.lib_service.exception.RequestParameterException;
import com.simon.lib_service.model.Book;
import com.simon.lib_service.service.BookService;
import com.simon.lib_service.service.IntegrationService;
import com.simon.dto.lib.BookDTO;
import com.simon.lib_service.repository.BookRepository;
import com.simon.lib_service.repository.AuthorRepository;
import com.simon.lib_service.repository.GenreRepository;
import com.simon.exception.ExistsException;
import com.simon.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookServiceUnitTest {

    @InjectMocks
    private BookService bookService;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private IntegrationService integrationService;

    private Book testBook;
    private BookDTO testBookDTO;

    @BeforeEach
    public void setup() {
        testBook = new Book(1, "Test Title", BigDecimal.valueOf(10), 5);
        testBookDTO = new BookDTO(1, "Test Title", null, null, BigDecimal.valueOf(10), 5);
    }

    @Test
    public void findByIdWithAuthorAndGenre_whenBookExists_returnBook() {
        when(bookRepository.findByIdWithAuthorAndGenre(1)).thenReturn(Optional.of(testBook));

        Book result = bookService.findByIdWithAuthorAndGenre(1);

        assertNotNull(result);
        assertEquals(testBook, result);
    }

    @Test
    public void findByIdWithAuthorAndGenre_whenBookNotFound_throwNotFoundException() {
        when(bookRepository.findByIdWithAuthorAndGenre(1)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> bookService.findByIdWithAuthorAndGenre(1));

        assertEquals("Can't find book with id: 1", exception.getMessage());
    }

    @Test
    public void addBook_whenValidBook_returnBookDTO() {
        Book b1 = bookWithoutId();
        Book b2 = bookWithId(1);
        when(bookRepository.save(b1)).thenReturn(b2);

        BookDTO result = bookService.addBook(b1);

        assertNotNull(result);
        assertEquals(b2.getId(), result.id());
    }


    @Test
    public void addBook_whenBookWithCustomId_throwExistsException() {
        testBook = new Book(1, "Test Title", BigDecimal.valueOf(10), 5);

        ExistsException exception = assertThrows(ExistsException.class, () -> bookService.addBook(testBook));

        assertEquals("Can't add book with custom id", exception.getMessage());
    }

    @Test
    public void updateBook_whenBookExists_returnBookDTO() {
        when(bookRepository.existsById(1)).thenReturn(true);
        when(bookRepository.save(testBook)).thenReturn(testBook);

        BookDTO result = bookService.updateBook(testBook);

        assertNotNull(result);
        assertEquals(testBookDTO, result);
    }

    @Test
    public void updateBook_whenBookNotFound_throwNotFoundException() {
        when(bookRepository.existsById(1)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class, () -> bookService.updateBook(testBook));

        assertEquals("Can't find book with id: 1", exception.getMessage());
    }

    @Test
    public void deleteBookById_whenBookExists_noExceptionThrown() {
        when(bookRepository.existsById(1)).thenReturn(true);
        doNothing().when(integrationService).checkOpenOrdersByBookIdsAndThrowExistingException(any(), any());

        bookService.deleteBookById(1);

        verify(bookRepository, times(1)).deleteById(1);
    }

    @Test
    public void deleteBookById_whenBookNotFound_throwNotFoundException() {
        when(bookRepository.existsById(1)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class, () -> bookService.deleteBookById(1));

        assertEquals("Can't find book with id: 1", exception.getMessage());
    }

    @Test
    public void changeAvailableQuantity_whenValidChange_returnUpdatedBook() {
        when(bookRepository.findById(1)).thenReturn(Optional.of(testBook));
        when(bookRepository.save(testBook)).thenReturn(testBook);

        Book result = bookService.changeAvailableQuantity(1, 3);

        assertNotNull(result);
        assertEquals(8, result.getAvailableQuantity());
    }

    @Test
    public void changeAvailableQuantity_whenInsufficientStock_throwRequestParameterException() {
        when(bookRepository.findById(1)).thenReturn(Optional.of(testBook));

        RequestParameterException exception = assertThrows(RequestParameterException.class, () -> bookService.changeAvailableQuantity(1, -6));

        assertEquals("Available books: 5; Required changes: -6", exception.getMessage());
    }
}

package com.simon.lib_service.service;

import com.simon.lib_service.exception.RequestParameterException;
import com.simon.lib_service.model.Book;
import com.simon.dto.lib.BookDTO;
import com.simon.lib_service.model.page.BookPageResponse;
import static com.simon.lib_service.model.specification.BookSpecification.*;

import com.simon.lib_service.repository.AuthorRepository;
import com.simon.lib_service.repository.BookRepository;
import com.simon.lib_service.repository.GenreRepository;
import com.simon.exception.ExistsException;
import com.simon.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final GenreRepository genreRepository;
    private final IntegrationService integrationService;

    public List<BookDTO> findAll(String title_part, BigDecimal oneDayRentPriceUSD_min,
            BigDecimal oneDayRentPriceUSD_max, Boolean isAvailable, String authorName_part, String genreName_part) {
        return bookRepository.findAll(prepareSpecification(title_part, oneDayRentPriceUSD_min, oneDayRentPriceUSD_max,
                isAvailable, authorName_part, genreName_part)).stream().map(Book::toDTO).toList();
    }

    public BookPageResponse findAll(Pageable pageable, String title_part, BigDecimal oneDayRentPriceUSD_min,
            BigDecimal oneDayRentPriceUSD_max, Boolean isAvailable, String authorName_part, String genreName_part) {
        return BookPageResponse.fromPage(bookRepository.findAll(
                prepareSpecification(title_part, oneDayRentPriceUSD_min, oneDayRentPriceUSD_max,
                        isAvailable, authorName_part, genreName_part), pageable).map(Book::toDTO));
    }

    @Transactional
    public Book findByIdWithAuthorAndGenre(int id) {
        return bookRepository.findByIdWithAuthorAndGenre(id).orElseThrow(() ->
                new NotFoundException("Can't find book with id: " + id));
    }

    @Transactional
    public Book findById(int id) {
        return bookRepository.findById(id).orElseThrow(() ->
                new NotFoundException("Can't find book with id: " + id));
    }

    @Transactional
    public BookDTO addBook(Book book) {
        if (book.getId() != null) throw new ExistsException("Can't add book with custom id");
        book.setAuthor(book.getAuthor() == null ? null
                : authorRepository.findById(book.getAuthor().getId()).orElseThrow(() ->
                    new RequestParameterException("Can't find associated author with id: " + book.getAuthor().getId())));
        book.setGenre(book.getGenre() == null ? null
                : genreRepository.findById(book.getGenre().getId()).orElseThrow(() ->
                new RequestParameterException("Can't find associated genre with id: " + book.getGenre().getId())));
        return Book.toDTO(bookRepository.save(book));
    }

    public BookDTO updateBook(Book book) {
        if (book.getId() == null || !bookRepository.existsById(book.getId()))
            throw new NotFoundException("Can't find book with id: " + book.getId());
        book.setAuthor(null);
        book.setGenre(null);
        return Book.toDTO(bookRepository.save(book));
    }

    public void deleteBookById(int id) {
        if (!bookRepository.existsById(id)) throw new NotFoundException("Can't find book with id: " + id);
        integrationService.checkOpenOrdersByBookIdsAndThrowExistingException(List.of(id),
                "Can't delete books that are in active orders: ");
        bookRepository.deleteById(id);
    }

    @Transactional
    public Book changeAvailableQuantity(int id, int changeOn) {
        Book book = findById(id);
        int new_quantity = book.getAvailableQuantity() + changeOn;
        if (new_quantity < 0) throw new RequestParameterException(
                "Available books: " + book.getAvailableQuantity() + "; Required changes: " + changeOn);
        book.setAvailableQuantity(new_quantity);
        return bookRepository.save(book);
    }


    private Specification<Book> prepareSpecification(String title_part, BigDecimal oneDayRentPriceUSD_min,
            BigDecimal oneDayRentPriceUSD_max, Boolean isAvailable, String authorName_part, String genreName_part) {

        if (oneDayRentPriceUSD_min != null && oneDayRentPriceUSD_max != null
                && oneDayRentPriceUSD_max.compareTo(oneDayRentPriceUSD_min) < 0)
            throw new RequestParameterException("oneDayRentPriceUSD_max can't be less then oneDayRentPriceUSD_min");

        return title_Like(title_part)   //  model.specification.BookSpecification methods
                .and(oneDayRentPriceUSD_greaterThenOeEqualTo(oneDayRentPriceUSD_min))
                .and(oneDayRentPriceUSD_lessThanOrEqualTo(oneDayRentPriceUSD_max))
                .and(availableQuantity_greaterThenZero(isAvailable))
                .and(authorFullName_like(authorName_part))
                .and(genreName_like(genreName_part));
    }
}
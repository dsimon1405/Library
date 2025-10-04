package com.simon.lib_service.controller;

import com.simon.lib_service.model.Book;
import com.simon.dto.lib.BookDTO;
import com.simon.lib_service.model.page.BookPageResponse;
import com.simon.lib_service.service.AssociationService;
import com.simon.lib_service.service.BookService;
import com.simon.name.PathRoles;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/book")
public class BookController {

    private final BookService bookService;
    private final AssociationService associationService;

    @GetMapping("/all")
    public List<BookDTO> findAll(
            @RequestParam(value = "title_part", required = false) String title_part,
            @RequestParam(value = "oneDayRentPriceUSD_min", required = false) BigDecimal oneDayRentPriceUSD_min,
            @RequestParam(value = "oneDayRentPriceUSD_max", required = false) BigDecimal oneDayRentPriceUSD_max,
            @RequestParam(value = "isAvailable", required = false) Boolean isAvailable,
            @RequestParam(value = "authorName_part", required = false) String authorName_part,
            @RequestParam(value = "genreName_part", required = false) String genreName_part
    ) {
        return bookService.findAll(title_part, oneDayRentPriceUSD_min, oneDayRentPriceUSD_max,
                isAvailable, authorName_part, genreName_part);
    }

    @GetMapping("/page")
    public BookPageResponse findPage(
            Pageable pageable,
            @RequestParam(value = "title_part", required = false) String title_part,
            @RequestParam(value = "oneDayRentPriceUSD_min", required = false) BigDecimal oneDayRentPriceUSD_min,
            @RequestParam(value = "oneDayRentPriceUSD_max", required = false) BigDecimal oneDayRentPriceUSD_max,
            @RequestParam(value = "isAvailable", required = false) Boolean isAvailable,
            @RequestParam(value = "authorName_part", required = false) String authorName_part,
            @RequestParam(value = "genreName_part", required = false) String genreName_part
    ) {
        return bookService.findAll(pageable, title_part, oneDayRentPriceUSD_min, oneDayRentPriceUSD_max,
                isAvailable, authorName_part, genreName_part);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookDTO> findById(@PathVariable int id) {
        return new ResponseEntity<>(Book.toDTO(bookService.findByIdWithAuthorAndGenre(id)), HttpStatus.FOUND);
    }

    @PostMapping(PathRoles.ADMIN + "/add")
    public ResponseEntity<BookDTO> add(@Valid @RequestBody Book book) {
        return new ResponseEntity<>(bookService.addBook(book), HttpStatus.CREATED);
    }

    @PostMapping(PathRoles.ADMIN + "/update")
    public ResponseEntity<BookDTO> update(@Valid @RequestBody Book book) {
        return new ResponseEntity<>(bookService.updateBook(book), HttpStatus.OK);
    }

    @PutMapping(PathRoles.ADMIN + "/update/{id}")
    public ResponseEntity<BookDTO> updateQuantity(
            @PathVariable int id,
            @RequestParam(value = "quantity_change_on") Integer quantity_change_on
    ) {
        return ResponseEntity.ok()
                .body(Book.toDTO(bookService.changeAvailableQuantity(id, quantity_change_on)));
    }

    @DeleteMapping(PathRoles.ADMIN + "/delete/{id}")
    public void deleteById(@PathVariable int id) {
        bookService.deleteBookById(id);
    }

    @PutMapping(PathRoles.ADMIN + "/associate")
    public ResponseEntity<BookDTO> associate(
            @RequestParam("book_id") Integer book_id,
            @RequestParam(value = "genre_id", required = false) Integer genre_id,
            @RequestParam(value = "author_id", required = false) Integer author_id
    ) {
        return ResponseEntity.ok().body(Book.toDTO(associationService.makeAssociation(book_id, author_id, genre_id)));
    }
}

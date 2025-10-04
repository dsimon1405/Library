package com.simon.lib_service.model;

import com.simon.dto.lib.BookDTO;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id")
    private Integer id;

    @NotEmpty(message = "Book.title - can't be empty")
    @Column(nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinColumn(name = "genre_id")
    private Genre genre;

    @ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinColumn(name = "author_id")
    private Author author;

    @Column(name = "one_day_rent_price_usd", precision = 5, scale = 2)
    @Positive(message = "Book.oneDayRentPriceUSD - must be positive")
    @NotNull(message = "Book.oneDayRentPriceUSD - can't be null")
    private BigDecimal oneDayRentPriceUSD;

    @Column(name = "available_quantity", nullable = false)
    @PositiveOrZero(message = "Book.availableQuantity - must be positive or zero")
    private int availableQuantity;


    public Book(int id, String title, BigDecimal oneDayRentPriceUSD, int availableQuantity) {
        this.id = id;
        this.title = title;
        this.oneDayRentPriceUSD = oneDayRentPriceUSD;
        this.availableQuantity = availableQuantity;
    }

    public Book(String title, BigDecimal oneDayRentPriceUSD, int availableQuantity) {
        this.title = title;
        this.oneDayRentPriceUSD = oneDayRentPriceUSD;
        this.availableQuantity = availableQuantity;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Book book)) return false;
        return id != null && Objects.equals(id, book.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public static BookDTO toDTO(Book book) {
        return book == null ? null
                : new BookDTO(book.getId(),
                book.getTitle(),
                book.getGenre() == null ? null : Genre.toDTO(book.getGenre()),
                book.getAuthor() == null ? null : Author.toDTO(book.getAuthor()),
                book.getOneDayRentPriceUSD(),
                book.getAvailableQuantity());
    }

    public static Book toDTO(BookDTO dto) {
        return dto == null ? null : new Book(dto.id(), dto.title(), Genre.toGenre(dto.genre()),
                Author.toAuthor(dto.author()), dto.oneDayRentPriceUSD(), dto.availableQuantity());
    }
}
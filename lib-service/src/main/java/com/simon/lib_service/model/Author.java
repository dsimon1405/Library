package com.simon.lib_service.model;

import com.simon.dto.lib.AuthorDTO;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Past;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "authors")
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "author_id")
    private Integer id;

    @Column(name = "full_name", nullable = false)
    @NotEmpty(message = "Author.fullName - can't be empty")
    private String fullName;

    @Column(name = "date_of_birth", nullable = false)
    @Past(message = "Author.dateOfBirth - must be in the past")
    private LocalDate dateOfBirth;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Book> books = new HashSet<>();

    public Author(Integer id, String fullName, LocalDate dateOfBirth) {
        this.id = id;
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
    }

    public Author(String fullName, LocalDate dateOfBirth) {
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
    }

    public void addBook(Book book) {
        book.setAuthor(this);
        books.add(book);
    }

    public void removeBook(Book book) {
        book.setAuthor(null);
        books.remove(book);
    }

    public static AuthorDTO toDTO(Author author) {
        return author == null ? null : new AuthorDTO(author.getId(), author.getFullName(), author.getDateOfBirth());
    }

    public static Author toAuthor(AuthorDTO dto) {
        return dto == null ? null : new Author(dto.id(), dto.fullName(), dto.dateOfBirth());
    }
}

package com.simon.lib_service.model;

import com.simon.dto.lib.GenreDTO;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "genres")
public class Genre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "genre_id")
    private Integer id;

    @NotEmpty(message = "Genre.name - can't be empty")
    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "genre", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Book> books = new HashSet<>();


    public Genre(Integer id, String name) {
        this.name = name;
        this.id = id;
    }

    public Genre(String name) {
        this.name = name;
    }

    public void addBook(Book b) {
        b.setGenre(this);
        books.add(b);
    }

    public void removeBook(Book b) {
        b.setGenre(null);
        books.remove(b);
    }

    public static GenreDTO toDTO(Genre genre) {
        return new GenreDTO(genre.getId(), genre.getName());
    }

    public static Genre toGenre(GenreDTO dto) {
        return new Genre(dto.id(), dto.name());
    }
}

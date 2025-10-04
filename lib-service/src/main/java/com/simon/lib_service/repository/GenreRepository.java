package com.simon.lib_service.repository;

import com.simon.lib_service.model.Genre;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GenreRepository extends JpaRepository<Genre, Integer> {

    List<Genre> findByNameLike(String name);

    @EntityGraph(attributePaths = "books")
    Optional<Genre> findWithBooksById(int id);
}


//    @Query("SELECT g FROM Genre g LEFT JOIN FETCH g.books")
//    List<Genre> findAllWithBooks();
//
//    @Query("SELECT g FROM Genre g LEFT JOIN FETCH g.books")
//    Page<Genre> findAllWithBooks(Pageable pageable);
//
//    @Query("SELECT g FROM Genre g LEFT JOIN FETCH g.books WHERE g.id = :id")
//    Optional<Genre> findByIdWithBooks(@Param("id") int id);
//
//    @Query("SELECT g FROM Genre g LEFT JOIN FETCH g.books WHERE g.name = :name")
//    List<Genre> findByNameWithBooks(@Param("name") String name);
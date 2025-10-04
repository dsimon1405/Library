package com.simon.lib_service.repository;

import com.simon.lib_service.model.Author;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AuthorRepository extends JpaRepository<Author, Integer> {

    List<Author> findByFullNameLike(String name);

    @EntityGraph(attributePaths = "books")
    Optional<Author> findWithBooksById(int id);
}

//    @Query("SELECT a FROM Author a LEFT JOIN FETCH a.books")
//    List<Author> findAllWithBooks();
//
//    @Query("SELECT a FROM Author a LEFT JOIN FETCH a.books")
//    Page<Author> findAllWithBooks(Pageable pageable);
//
//    @Query("SELECT a FROM Author a LEFT JOIN FETCH a.books WHERE a.id = :id")
//    Optional<Author> findByIdWithBooks(@Param("id") int id);
//
//    @Query("SELECT a FROM Author a LEFT JOIN FETCH a.books WHERE a.fullName = :name")
//    List<Author> findByFullNameWithBooks(@Param("name") String name);

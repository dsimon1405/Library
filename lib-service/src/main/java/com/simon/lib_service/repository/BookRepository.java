package com.simon.lib_service.repository;

import com.simon.lib_service.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Integer>, JpaSpecificationExecutor<Book> {

    @EntityGraph(attributePaths = { "author", "genre" })
    List<Book> findAll(@Nullable Specification<Book> spec);

    @EntityGraph(attributePaths = { "author", "genre" })
    Page<Book> findAll(@Nullable Specification<Book> spec, Pageable pageable);

    @Query("SELECT b FROM Book b LEFT JOIN FETCH b.author LEFT JOIN FETCH b.genre WHERE b.id = :id")
    Optional<Book> findByIdWithAuthorAndGenre(@Param("id") int id);
}


//    @Query("SELECT b FROM Book b LEFT JOIN FETCH b.author LEFT JOIN FETCH b.genre")
//    List<Book> findAllWithAuthorAndGenre();
//
//    @Query("SELECT b FROM Book b LEFT JOIN FETCH b.author LEFT JOIN FETCH b.genre")
//    Page<Book> findAllWithAuthorAndGenre(Pageable pageable);

//    List<Book> findByTitleLike(String title);
//
//    @Query("SELECT b FROM Book b LEFT JOIN FETCH b.author LEFT JOIN FETCH b.genre WHERE b.title = :title")
//    List<Book> findByTitleWithAuthorAndGenre(@Param("title") String title);
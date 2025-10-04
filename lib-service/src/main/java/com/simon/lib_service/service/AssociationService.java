package com.simon.lib_service.service;

import com.simon.lib_service.model.Author;
import com.simon.lib_service.model.Book;
import com.simon.lib_service.model.Genre;
import com.simon.lib_service.repository.AuthorRepository;
import com.simon.lib_service.repository.BookRepository;
import com.simon.lib_service.repository.GenreRepository;
import com.simon.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional
public class AssociationService {

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;
    private final GenreRepository genreRepository;

    public Book associateAuthorWithBook(int authorId, int bookId) {
        Author author = authorRepository.findById(authorId)
                .orElseThrow(() -> new NotFoundException("Can't find author with id: " + authorId));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new NotFoundException("Can't find book with id: " + bookId));
        author.addBook(book);
        return bookRepository.save(book);
    }

    public Book associateBookWithAuthorAndGenre(int bookId, int authorId, int genreId) {
        Author author = authorRepository.findById(authorId)
                .orElseThrow(() -> new NotFoundException("Can't find author with id: " + authorId));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new NotFoundException("Can't find book with id: " + bookId));
        Genre genre = genreRepository.findById(genreId)
                .orElseThrow(() -> new NotFoundException("Can't find genre with id: " + genreId));
        author.addBook(book);
        genre.addBook(book);
        return bookRepository.save(book);
    }

    public Book associateGenreWithBook(int genreId, int bookId) {
        Genre genre = genreRepository.findById(genreId)
                .orElseThrow(() -> new NotFoundException("Can't find genre with id: " + genreId));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new NotFoundException("Can't find book with id: " + bookId));
        genre.addBook(book);
        return bookRepository.save(book);
    }

    public Book makeAssociation(Integer book_id, Integer author_id, Integer genre_id) {
        if (book_id == null) throw new NotFoundException("Must present book_id");

        if (genre_id != null && author_id != null)
            return associateBookWithAuthorAndGenre(book_id, author_id, genre_id);
        else if (genre_id != null) return associateGenreWithBook(genre_id, book_id);
        else if (author_id != null) return associateAuthorWithBook(author_id, book_id);

        throw new NotFoundException("Must present genre_id or author_id or both");
    }
}

package com.simon.lib_service.service;

import com.simon.lib_service.exception.RequestParameterException;
import com.simon.lib_service.model.Book;
import com.simon.lib_service.model.Genre;
import com.simon.lib_service.model.page.GenrePageResponse;
import com.simon.lib_service.repository.GenreRepository;
import com.simon.lib_service.repository.SqlHelper;
import com.simon.dto.lib.GenreDTO;
import com.simon.exception.ExistsException;
import com.simon.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GenreService {

    private final GenreRepository genreRepository;

    public List<GenreDTO> findAll() {
        return genreRepository.findAll().stream().map(Genre::toDTO).toList();
    }

    public GenrePageResponse findAll(Pageable pageable) {
        return GenrePageResponse.fromPage(genreRepository.findAll(pageable).map(Genre::toDTO));
    }

    public GenreDTO findById(int id) {
        return Genre.toDTO(genreRepository.findById(id).orElseThrow(() ->
                new NotFoundException("Can't find genre with id: " + id)));
    }

    public Genre findByIdWithBooks(int id) {
        return genreRepository.findWithBooksById(id).orElseThrow(() ->
                new NotFoundException("Can't find genre with id: " + id));
    }

    public List<GenreDTO> findByNameLike(String name_part) {
        if (name_part == null || name_part.trim().isEmpty())
            throw new RequestParameterException("name_part can't be empty");
        return genreRepository.findByNameLike(SqlHelper.toLike(name_part)).stream().map(Genre::toDTO).toList();
    }

    public GenreDTO addGenre(Genre genre) {
        if (genre.getId() != null) throw new ExistsException("Can't add genre with custom id");
        if (genreRepository.exists(Example.of(genre)))
            throw new ExistsException("Genre already exists: " + genre.toString());
        genre.getBooks().clear();
        return Genre.toDTO(genreRepository.save(genre));
    }

    public GenreDTO updateGenre(Genre genre) {
        if (genre.getId() == null || !genreRepository.existsById(genre.getId()))
            throw new NotFoundException("Can't find genre with id: " + genre.getId());
        genre.getBooks().clear();
        return Genre.toDTO(genreRepository.save(genre));
    }

    public void deleteGenreById(int id) {
        Genre genre = findByIdWithBooks(id);
        if (!genre.getBooks().isEmpty()) throw new ExistsException("Can't delete genre associated with book Ids: "
            + genre.getBooks().stream().map(Book::getId).toList());
        genreRepository.deleteById(id);
    }
}
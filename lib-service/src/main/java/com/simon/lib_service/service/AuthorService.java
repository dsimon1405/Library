package com.simon.lib_service.service;

import com.simon.lib_service.exception.RequestParameterException;
import com.simon.lib_service.model.Book;
import com.simon.dto.lib.AuthorDTO;
import com.simon.lib_service.model.Author;
import com.simon.lib_service.model.page.AuthorPageResponse;
import com.simon.lib_service.repository.AuthorRepository;
import com.simon.lib_service.repository.SqlHelper;
import com.simon.exception.ExistsException;
import com.simon.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthorService {

    private final AuthorRepository authorRepository;
    private final IntegrationService integrationService;

    public List<AuthorDTO> findAll() {
        return authorRepository.findAll().stream().map(Author::toDTO).collect(Collectors.toList());
    }

    public AuthorPageResponse findAll(Pageable pageable) {
        return AuthorPageResponse.fromPage(authorRepository.findAll(pageable).map(Author::toDTO));
    }

    public AuthorDTO findById(int id) {
        return Author.toDTO(authorRepository.findById(id).orElseThrow(() ->
                new NotFoundException("Can't find author with id: " + id)));
    }

    public Author findWithBooksById(int id) {
        return authorRepository.findWithBooksById(id)
                .orElseThrow(() -> new NotFoundException("Can't find author with id: " + id));
    }

    public List<AuthorDTO> findByFullNameLike(String fullName_part) {
        if (fullName_part == null || fullName_part.trim().isEmpty())
            throw new RequestParameterException("fullName_part can't be empty");
        return authorRepository.findByFullNameLike(SqlHelper.toLike(fullName_part)).stream().map(Author::toDTO)
                .collect(Collectors.toList());
    }

    public AuthorDTO addAuthor(Author auth) {
        if (auth.getId() != null) throw new ExistsException("Can't add author with custom id");
        if (authorRepository.exists(Example.of(auth)))
            throw new ExistsException("Author already exists: " + Author.toDTO(auth));
        auth.getBooks().clear();
        return Author.toDTO(authorRepository.save(auth));
    }

    public AuthorDTO updateAuthor(Author auth) {
        if (auth.getId() == null || !authorRepository.existsById(auth.getId()))
                throw new NotFoundException("Can't find author with id: " + auth.getId());
        auth.getBooks().clear();
        return Author.toDTO(authorRepository.save(auth));
    }

    public void deleteAuthorById(int id, boolean delete_books) {
        Author author = findWithBooksById(id);
        if (!author.getBooks().isEmpty()) {
            List<Integer> bookIds = author.getBooks().stream().map(Book::getId).toList();
            if (delete_books) integrationService.checkOpenOrdersByBookIdsAndThrowExistingException(bookIds,
                    "Can't delete author with books that are in active orders: ");
            else throw new ExistsException("Can't delete author associated with book Ids: " + bookIds);
        }
        authorRepository.deleteById(id);
    }
}
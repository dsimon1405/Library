package com.simon.lib_service.controller;

import com.simon.dto.lib.AuthorDTO;
import com.simon.lib_service.model.Author;
import com.simon.lib_service.model.page.AuthorPageResponse;
import com.simon.lib_service.service.AuthorService;
import com.simon.name.PathRoles;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/author")
public class AuthorController {

    private final AuthorService authorService;

    @GetMapping("/all")
    public List<AuthorDTO> findAll() {
        return authorService.findAll();
    }

    @GetMapping("/page")
    public AuthorPageResponse findPage(Pageable pageable) {
        return authorService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuthorDTO> findById(@PathVariable int id) {
        return new ResponseEntity<>(authorService.findById(id), HttpStatus.FOUND);
    }

    @GetMapping
    public List<AuthorDTO> findByFullNameLike(@RequestParam(value = "fullName_part") String fullName_part) {
        return authorService.findByFullNameLike(fullName_part);
    }

    @PostMapping(PathRoles.ADMIN + "/add")
    public ResponseEntity<AuthorDTO> add(@Valid @RequestBody Author author) {
        return new ResponseEntity<>(authorService.addAuthor(author), HttpStatus.CREATED);
    }

    @PostMapping(PathRoles.ADMIN + "/update")
    public ResponseEntity<AuthorDTO> update(@Valid @RequestBody Author author) {
        return new ResponseEntity<>(authorService.updateAuthor(author), HttpStatus.OK);
    }

    @DeleteMapping(PathRoles.ADMIN + "/delete/{id}")
    public void deleteById(@PathVariable int id, @RequestParam(value = "delete_books") boolean delete_books) {
        authorService.deleteAuthorById(id, delete_books);
    }
}

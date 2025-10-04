package com.simon.lib_service.controller;

import com.simon.dto.lib.GenreDTO;
import com.simon.lib_service.model.Genre;
import com.simon.lib_service.model.page.GenrePageResponse;
import com.simon.lib_service.service.GenreService;
import com.simon.name.PathRoles;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/genre")
@RequiredArgsConstructor
public class GenreController {

    private final GenreService genreService;

    @GetMapping("/all")
    public List<GenreDTO> findAll() {
        return genreService.findAll();
    }

    @GetMapping("/page")
    public GenrePageResponse findPage(Pageable pageable) {
        return genreService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GenreDTO> findById(@PathVariable int id) {
        return new ResponseEntity<>(genreService.findById(id), HttpStatus.FOUND);
    }

    @GetMapping
    public List<GenreDTO> findByNameLike(@RequestParam(value = "name_part", required = true) String name_part) {
        return genreService.findByNameLike(name_part);
    }

    @PostMapping(PathRoles.ADMIN + "/add")
    public ResponseEntity<GenreDTO> add(@Valid @RequestBody Genre genre) {
        return new ResponseEntity<>(genreService.addGenre(genre), HttpStatus.CREATED);
    }

    @PostMapping(PathRoles.ADMIN + "/update")
    public ResponseEntity<GenreDTO> update(@Valid @RequestBody Genre genre) {
        return new ResponseEntity<>(genreService.updateGenre(genre), HttpStatus.OK);
    }

    @DeleteMapping(PathRoles.ADMIN + "/delete/{id}")
    public void deleteById(@PathVariable("id") int id) {
        genreService.deleteGenreById(id);
    }
}

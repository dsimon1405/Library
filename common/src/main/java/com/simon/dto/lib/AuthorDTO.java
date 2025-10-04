package com.simon.dto.lib;

import java.time.LocalDate;

public record AuthorDTO(Integer id, String fullName, LocalDate dateOfBirth) {
}

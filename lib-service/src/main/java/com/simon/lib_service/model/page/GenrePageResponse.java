package com.simon.lib_service.model.page;

import com.simon.dto.lib.GenreDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public record GenrePageResponse(
        List<GenreDTO> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean last,
        boolean first
) {
    public static GenrePageResponse fromPage(Page<GenreDTO> page) {
        return new GenrePageResponse(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast(),
                page.isFirst()
        );
    }
}

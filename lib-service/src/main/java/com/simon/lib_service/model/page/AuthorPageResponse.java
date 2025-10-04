package com.simon.lib_service.model.page;

import com.simon.dto.lib.AuthorDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public record AuthorPageResponse(
        List<AuthorDTO> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean last,
        boolean first
) {
    public static AuthorPageResponse fromPage(Page<AuthorDTO> page) {
        return new AuthorPageResponse(
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

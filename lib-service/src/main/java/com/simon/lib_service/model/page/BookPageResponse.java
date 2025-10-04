package com.simon.lib_service.model.page;

import com.simon.dto.lib.BookDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public record BookPageResponse(
        List<BookDTO> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean last,
        boolean first
) {
    public static BookPageResponse fromPage(Page<BookDTO> page) {
        return new BookPageResponse(
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

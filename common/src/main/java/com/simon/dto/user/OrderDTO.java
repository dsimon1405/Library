package com.simon.dto.user;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OrderDTO(int id, int book_id, BigDecimal one_day_rent_price_usd, BigDecimal paid_price_usd,
                       LocalDate rent_start, LocalDate rent_end) {
}

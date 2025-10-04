package com.simon.dto.lib;

import java.math.BigDecimal;

public record BookDTO(Integer id, String title, GenreDTO genre, AuthorDTO author,
                      BigDecimal oneDayRentPriceUSD, int availableQuantity) {
}

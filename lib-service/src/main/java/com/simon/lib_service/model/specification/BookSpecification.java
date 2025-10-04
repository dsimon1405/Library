package com.simon.lib_service.model.specification;

import com.simon.lib_service.exception.RequestParameterException;
import com.simon.lib_service.model.Book;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class BookSpecification {

    public static Specification<Book> title_Like(String title_part) {
        return (root, query, builder) -> {
            if (title_part == null) return builder.conjunction();
            if (title_part.trim().isEmpty()) throw new RequestParameterException("title_part can't be empty");
            return builder.like(builder.lower(root.get("title")), "%" + title_part.toLowerCase() + "%");
        };
    }

    public static Specification<Book> oneDayRentPriceUSD_greaterThenOeEqualTo(BigDecimal oneDayRentPriceUSD_min) {
        return (root, query, builder) -> {
            if (oneDayRentPriceUSD_min == null) return builder.conjunction();
            if (oneDayRentPriceUSD_min.compareTo(BigDecimal.ZERO) < 0)
                throw new RequestParameterException("oneDayRentPriceUSD_min can't be less then 0.0");
            return builder.greaterThanOrEqualTo(root.get("oneDayRentPriceUSD"), oneDayRentPriceUSD_min);
        };
    }

    public static Specification<Book> oneDayRentPriceUSD_lessThanOrEqualTo(BigDecimal oneDayRentPriceUSD_max) {
        return (root, query, builder) -> {
            if (oneDayRentPriceUSD_max == null) return builder.conjunction();
            if (oneDayRentPriceUSD_max.compareTo(BigDecimal.ZERO) < 0)
                throw new RequestParameterException("oneDayRentPriceUSD_min can't be less then 0.0");
            return builder.lessThanOrEqualTo(root.get("oneDayRentPriceUSD"), oneDayRentPriceUSD_max);
        };
    }

    public static Specification<Book> availableQuantity_greaterThenZero(Boolean isAvailable) {
        return (root, query, builder) -> {
            return isAvailable == null || !isAvailable ? builder.conjunction()
                    : builder.greaterThan(root.get("availableQuantity"), 0.0);
        };
    }

    public static Specification<Book> authorFullName_like(String authorName_part) {
        return (root, query, builder) -> {
            if (authorName_part == null) return builder.conjunction();
            if (authorName_part.trim().isEmpty()) throw new RequestParameterException("authorName_part can't be empty");
            return builder.like(builder.lower(root.join("author", JoinType.LEFT).get("fullName")),
                        "%" + authorName_part.toLowerCase() + "%");
        };
    }

    public static Specification<Book> genreName_like(String genreName_part) {
        return (root, query, builder) -> {
            if (genreName_part == null) return builder.conjunction();
            if (genreName_part.trim().isEmpty()) throw new RequestParameterException("genreName_part can't be empty");
            return builder.like(builder.lower(root.join("genre", JoinType.LEFT).get("name")),
                        "%" + genreName_part.toLowerCase() + "%");
        };
    }
}

package com.simon.user_service.model;

import com.simon.dto.user.OrderDTO;
import com.simon.user_service.exception.RentException;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@NoArgsConstructor
@Getter
@Setter
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.MERGE, CascadeType.PERSIST })
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "book_id", nullable = false)
    private int bookId;

    @Column(name = "one_day_rent_price_usd", nullable = false, precision = 8, scale = 2)
    private BigDecimal oneDayRentPriceUSD;

    @Column(name = "paid_price_usd", nullable = false, precision = 8, scale = 2)
    private BigDecimal paidPriceUSD = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "rent_start", updatable = false, nullable = false)
    private LocalDate rentStart;

    @Column(name = "rent_end")
    private LocalDate rentEnd;


    public Order(Account account, int bookId, BigDecimal oneDayRentPrice) {
        this.account = account;
        this.bookId = bookId;
        this.oneDayRentPriceUSD = oneDayRentPrice;
    }

    protected Order(int bookId, Account account, LocalDate rentStart) {
        this.bookId = bookId;
        this.account = account;
        this.rentStart = rentStart;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Order order)) return false;
        return id != null && Objects.equals(id, order.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public BigDecimal calcRentPrice(LocalDate rent_end) {
        if (oneDayRentPriceUSD.compareTo(BigDecimal.ZERO) < 0)
            throw new RentException("oneDayRentPriceUDS is negative: " + oneDayRentPriceUSD);
        long days = ChronoUnit.DAYS.between(rentStart, rent_end);
        if (days < 0) throw new RentException("rentStart: " + rentStart + " more then rentEnd: " + rentEnd);
        long daysInclusive = days + 1;
        return oneDayRentPriceUSD.multiply(BigDecimal.valueOf(daysInclusive));
    }

    public static OrderDTO toDTO(Order order) {
        return new OrderDTO(order.getId(), order.getBookId(), order.getOneDayRentPriceUSD(),
                order.getPaidPriceUSD(), order.getRentStart(), order.getRentEnd());
    }
}

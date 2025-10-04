package com.simon.user_service.model;

import com.simon.dto.user.AccountDTO;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "accounts")
public class Account {

    @Id
    @Column(name = "account_id")
    private Integer id;

    @Column(name = "balance_usd", nullable = false, precision = 18, scale = 2)
    private BigDecimal balanceUSD = BigDecimal.ZERO;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Order> orders = new HashSet<>();


    public Account(Integer id) {
        this.id = id;
    }

    public void addOrder(Order o) {
        o.setAccount(this);
        orders.add(o);
    }

    public void removeOrder(Order o) {
        o.setAccount(null);
        orders.remove(o);
    }

    public static AccountDTO toDTO(Account user) {
        return new AccountDTO(user.getId(), user.getBalanceUSD());
    }
}

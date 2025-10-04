package com.simon.user_service.repository.integration;

import com.simon.user_service.model.Account;
import com.simon.user_service.model.Order;
import com.simon.user_service.repository.AccountRepository;
import com.simon.user_service.repository.OrderRepository;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class OrderRepositoryIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private AccountRepository accountRepository;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        accountRepository.deleteAll();
    }

    private Order createOrder(Account account, Integer bookId, LocalDate rentEnd) {
        Order order = new Order(account, bookId, BigDecimal.TEN);
        order.setRentEnd(rentEnd);
        return order;
    }

    private static int id_counter = 1;
    private static Account createAccount() {
        return new Account(id_counter++);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void findById_ShouldLoadAccountWithEntityGraph() {
        Order order = orderRepository.save(createOrder(new Account(1), 100, null));

        Optional<Order> found = orderRepository.findById(order.getId());

        assertTrue(found.isPresent());
        assertDoesNotThrow(found.get().getAccount()::getBalanceUSD);    //  check eager load
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void findByAccount_IdAndRentEndIsNull_ShouldReturnOnlyActiveOrders() {
        Account account = createAccount();
        Account account2 = createAccount();

        Iterable<Order> saved = orderRepository.saveAll(List.of(
                createOrder(account, 1, null),
                createOrder(account, 2, LocalDate.now()),
                createOrder(account2, 3, null)));

        List<Order> results = orderRepository.findByAccount_IdAndRentEndIsNull(account.getId());

        assertThat(results).hasSize(1);
        assertEquals(saved.iterator().next().getId(), results.getFirst().getId());
        assertDoesNotThrow(results.getFirst().getAccount()::getBalanceUSD);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void findByAccount_IdAndRentEndIsNotNull_ShouldReturnClosedOrders() {
        Account account = createAccount();
        Account account2 = createAccount();

        Iterable<Order> saved = orderRepository.saveAll(List.of(
                createOrder(account, 2, LocalDate.now()),
                createOrder(account, 1, null),
                createOrder(account2, 3, null)));

        List<Order> results = orderRepository.findByAccount_IdAndRentEndIsNotNull(account.getId());

        assertThat(results).hasSize(1);
        assertEquals(saved.iterator().next().getId(), results.getFirst().getId());
        assertThrows(LazyInitializationException.class, results.getFirst().getAccount()::getBalanceUSD);
    }

    @Test
//    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void findByAccount_Id_ShouldReturnAllOrdersForAccount() {
        Account account = createAccount();
        Account account2 = createAccount();

        Iterable<Order> saved = orderRepository.saveAll(List.of(
                createOrder(account, 2, LocalDate.now()),
                createOrder(account, 1, null),
                createOrder(account2, 3, null)));

        List<Order> results = orderRepository.findByAccount_Id(account.getId());

        assertThat(results).hasSize(2);
    }
}

package com.simon.user_service.serivce.integration;

import com.simon.dto.lib.BookDTO;
import com.simon.exception.NotFoundException;
import com.simon.user_service.exception.RentException;
import com.simon.user_service.model.Account;
import com.simon.user_service.model.Order;
import com.simon.user_service.repository.AccountRepository;
import com.simon.user_service.repository.OrderRepository;
import com.simon.user_service.service.OrderService;
import com.simon.utils.HttpRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
class OrderServiceIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private HttpRequest httpRequest;

    @TestConfiguration
    static class MockConfig {
        @Bean
        @Primary
        HttpRequest mockHttpRequest() {
            return Mockito.mock(HttpRequest.class);
        }
    }

    @BeforeEach
    void cleanDb() {
        orderRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    void makeOrder_WhenValidRequest_ReturnsOrder() {
        Account account = accountRepository.save(new Account(1));

        Mockito.when(httpRequest.request(
                anyString(),
                eq(HttpMethod.PUT),
                eq(HttpEntity.EMPTY),
                eq(BookDTO.class),
                ArgumentMatchers.<Map<String, ?>>any()
        )).thenReturn(ResponseEntity.ok(new BookDTO(1, "Test", null, null, BigDecimal.TEN, 5)));

        Order order = orderService.makeOrder(1, 100);

        assertThat(order.getId()).isNotNull();
        assertThat(order.getBookId()).isEqualTo(100);
        assertThat(order.getOneDayRentPriceUSD()).isEqualByComparingTo(BigDecimal.TEN);
        assertThat(order.getAccount().getId()).isEqualTo(1);
    }

    @Test
    void makeOrder_WhenMaxOrdersExceeded_ThrowsRentException() {
        Account account = accountRepository.save(new Account(1));

        Mockito.when(httpRequest.request(anyString(), any(), any(), eq(BookDTO.class), anyMap()))
                .thenReturn(ResponseEntity.ok(new BookDTO(1, "Test", null, null, BigDecimal.TEN, 5)));

        orderService.makeOrder(1, 101);
        orderService.makeOrder(1, 102);
        orderService.makeOrder(1, 103);

        assertThatThrownBy(() -> orderService.makeOrder(1, 104))
                .isInstanceOf(RentException.class)
                .hasMessageContaining("Reached max number of open orders");
    }

    @Test
    void makeOrder_WhenSameBookAlreadyRented_ThrowsRentException() {
        Account account = accountRepository.save(new Account(1));

        Mockito.when(httpRequest.request(anyString(), any(), any(), eq(BookDTO.class), anyMap()))
                .thenReturn(ResponseEntity.ok(new BookDTO(1, "Test", null, null, BigDecimal.TEN, 5)));

        orderService.makeOrder(1, 200);

        assertThatThrownBy(() -> orderService.makeOrder(1, 200))
                .isInstanceOf(RentException.class)
                .hasMessageContaining("User already rent book with id");
    }

    @Test
    void findOrderByIdWithAccount_WhenNotExists_ThrowsExistsException() {
        assertThatThrownBy(() -> orderService.findOrderByIdWithAccount(999))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Doesn't exists order with id");
    }
    @Test
    void closeOrder_WhenValidRequest_ClosesOrderAndRestoresBook() {
        Account account = accountRepository.save(new Account(1));
        account.setBalanceUSD(new BigDecimal("100.00"));
        accountRepository.save(account);

        Mockito.when(httpRequest.request(anyString(), any(), any(), eq(BookDTO.class), anyMap()))
                .thenReturn(ResponseEntity.ok(new BookDTO(1, "Test", null, null, BigDecimal.TEN, 5)));

        Order order = orderService.makeOrder(1, 300);

        Mockito.when(httpRequest.request(anyString(), any(), any(), eq(Void.class), anyMap()))
                .thenReturn(ResponseEntity.ok().build());

        Order closedOrder = orderService.closeOrder(1, order.getId(), true, true);

        assertThat(closedOrder.getRentEnd()).isNotNull();
        assertThat(closedOrder.getAccount().getBalanceUSD()).isLessThan(new BigDecimal("100.00"));
    }

    @Test
    void closeOrder_WhenInsufficientFunds_ThrowsRentException() {
        Account account = new Account(1);
        account.setBalanceUSD(new BigDecimal("0.50"));
        accountRepository.save(account);

        Mockito.when(httpRequest.request(anyString(), any(), any(), eq(BookDTO.class), anyMap()))
                .thenReturn(ResponseEntity.ok(new BookDTO(1, "Test", null, null, BigDecimal.TEN, 5)));

        Order order = orderService.makeOrder(1, 400);

        assertThatThrownBy(() -> orderService.closeOrder(1, order.getId(), true, true))
                .isInstanceOf(RentException.class)
                .hasMessageContaining("missing for payment");
    }

    @Test
    void closeOrder_WhenOrderAlreadyClosed_ThrowsRentException() {
        Account account = new Account(1);
        account.setBalanceUSD(new BigDecimal("100.00"));
        accountRepository.save(account);

        Mockito.when(httpRequest.request(anyString(), any(), any(), eq(BookDTO.class), anyMap()))
                .thenReturn(ResponseEntity.ok(new BookDTO(1, "Test", null, null, BigDecimal.TEN, 5)));

        Order order = orderService.makeOrder(1, 500);

        Mockito.when(httpRequest.request(anyString(), any(), any(), eq(Void.class), anyMap()))
                .thenReturn(ResponseEntity.ok().build());

        orderService.closeOrder(1, order.getId(), true, true);

        assertThatThrownBy(() -> orderService.closeOrder(1, order.getId(), true, true))
                .isInstanceOf(RentException.class)
                .hasMessageContaining("Order already closed");
    }

    @Test
    void findOrdersByAccountId_WhenOpenNull_ReturnsAll() {
        Account account = accountRepository.save(new Account(1));

        Mockito.when(httpRequest.request(anyString(), any(), any(), eq(BookDTO.class), anyMap()))
                .thenReturn(ResponseEntity.ok(new BookDTO(1, "Test", null, null, BigDecimal.TEN, 5)));

        orderService.makeOrder(1, 600);
        orderService.makeOrder(1, 601);

        assertThat(orderService.findOrdersByAccountId(1, null)).hasSize(2);
    }

    @Test
    void findOrdersByAccountId_WhenOpenTrue_ReturnsOnlyActive() {
        Account account = accountRepository.save(new Account(1));
        account.setBalanceUSD(new BigDecimal("100.00"));
        accountRepository.save(account);

        Mockito.when(httpRequest.request(anyString(), any(), any(), eq(BookDTO.class), anyMap()))
                .thenReturn(ResponseEntity.ok(new BookDTO(1, "Test", null, null, BigDecimal.TEN, 5)));

        Order order1 = orderService.makeOrder(1, 700);
        Order order2 = orderService.makeOrder(1, 701);

        Mockito.when(httpRequest.request(anyString(), any(), any(), eq(Void.class), anyMap()))
                .thenReturn(ResponseEntity.ok().build());

        orderService.closeOrder(1, order1.getId(), true, true);

        assertThat(orderService.findOrdersByAccountId(1, true)).hasSize(1);
    }

    @Test
    void findOrdersByAccountId_WhenOpenFalse_ReturnsOnlyClosed() {
        Account account = accountRepository.save(new Account(1));
        account.setBalanceUSD(new BigDecimal("100.00"));
        accountRepository.save(account);

        Mockito.when(httpRequest.request(anyString(), any(), any(), eq(BookDTO.class), anyMap()))
                .thenReturn(ResponseEntity.ok(new BookDTO(1, "Test", null, null, BigDecimal.TEN, 5)));

        Order order1 = orderService.makeOrder(1, 800);
        Mockito.when(httpRequest.request(anyString(), any(), any(), eq(Void.class), anyMap()))
                .thenReturn(ResponseEntity.ok().build());
        orderService.closeOrder(1, order1.getId(), true, true);

        assertThat(orderService.findOrdersByAccountId(1, false)).hasSize(1);
    }
}
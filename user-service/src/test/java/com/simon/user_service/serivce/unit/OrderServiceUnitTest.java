package com.simon.user_service.serivce.unit;

import com.simon.dto.lib.AuthorDTO;
import com.simon.dto.lib.BookDTO;
import com.simon.dto.lib.GenreDTO;
import com.simon.exception.ExistsException;
import com.simon.exception.NotFoundException;
import com.simon.user_service.exception.RentException;
import com.simon.user_service.model.Account;
import com.simon.user_service.model.Order;
import com.simon.user_service.repository.OrderRepository;
import com.simon.user_service.service.AccountService;
import com.simon.user_service.service.OrderService;
import com.simon.utils.HttpRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceUnitTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private AccountService accountService;
    @Mock
    private HttpRequest httpRequest;
    @InjectMocks
    private OrderService orderService;

    @Test
    void makeOrder_WhenNewOrder_ShouldSaveAndCallHttpRequest() {
        Account account = new Account(1);
        when(orderRepository.findByAccount_IdAndRentEndIsNull(1)).thenReturn(List.of());
        when(accountService.findById(1)).thenReturn(account);

        BookDTO bookDTO = new BookDTO(
                5, "Book",
                new GenreDTO(1, "Fantasy"),
                new AuthorDTO(1, "John Author", LocalDate.of(1980, 1, 1)),
                BigDecimal.valueOf(3),
                10
        );

        when(httpRequest.request(anyString(), eq(HttpMethod.PUT), eq(HttpEntity.EMPTY), eq(BookDTO.class), any(Map.class)))
                .thenReturn(ResponseEntity.ok(bookDTO));

        Order savedOrder = new Order(account, 5, BigDecimal.valueOf(3));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        Order result = orderService.makeOrder(1, 5);

        assertEquals(5, result.getBookId());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void makeOrder_WhenTooManyOpenOrders_ShouldThrow() {
        Account account = new Account(1);
        List<Order> existingOrders = List.of(
                new Order(account, 2, BigDecimal.TEN),
                new Order(account, 3, BigDecimal.TEN),
                new Order(account, 4, BigDecimal.TEN)
        );
        when(orderRepository.findByAccount_IdAndRentEndIsNull(1)).thenReturn(existingOrders);

        assertThrows(RentException.class, () -> orderService.makeOrder(1, 5));
    }

    @Test
    void findOrderByIdWithAccount_WhenExists_ShouldReturnOrder() {
        Order order = new Order(new Account(1), 5, BigDecimal.TEN);
        when(orderRepository.findById(10)).thenReturn(Optional.of(order));

        Order result = orderService.findOrderByIdWithAccount(10);

        assertEquals(5, result.getBookId());
    }

    @Test
    void findOrderByIdWithAccount_WhenNotExists_ShouldThrow() {
        when(orderRepository.findById(10)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> orderService.findOrderByIdWithAccount(10));
    }

    @Test
    void closeOrder_WhenValid_ShouldCloseOrderAndRequestUpdate() {
        Account account = new Account(1);
        account.setBalanceUSD(BigDecimal.valueOf(20));
        Order order = new Order(account, 5, BigDecimal.valueOf(5));
        order.setRentStart(LocalDate.now().minusDays(2));

        when(orderRepository.findById(10)).thenReturn(Optional.of(order));
        when(httpRequest.request(anyString(), eq(HttpMethod.PUT), eq(HttpEntity.EMPTY), eq(BookDTO.class), any(Map.class)))
                .thenReturn(ResponseEntity.ok().build());

        Order savedOrder = new Order(account, 5, BigDecimal.valueOf(5));
        savedOrder.setRentEnd(LocalDate.now());
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        Order result = orderService.closeOrder(1, 10, true, true);

        assertNotNull(result.getRentEnd());
        verify(orderRepository).save(any());
    }

    @Test
    void closeOrder_WhenNotUserOrder_ShouldThrow() {
        Account account = new Account(2);
        Order order = new Order(account, 5, BigDecimal.TEN);
        when(orderRepository.findById(10)).thenReturn(Optional.of(order));

        assertThrows(ExistsException.class, () -> orderService.closeOrder(1, 10, true, true));
    }

    @Test
    void closeOrder_WhenAlreadyClosed_ShouldThrow() {
        Account account = new Account(1);
        Order order = new Order(account, 5, BigDecimal.TEN);
        order.setRentEnd(LocalDate.now());

        when(orderRepository.findById(10)).thenReturn(Optional.of(order));

        assertThrows(RentException.class, () -> orderService.closeOrder(1, 10, true, true));
    }
}

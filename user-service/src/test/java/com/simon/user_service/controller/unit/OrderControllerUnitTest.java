package com.simon.user_service.controller.unit;

import com.simon.name.Headers;
import com.simon.user_service.controller.OrderController;
import com.simon.user_service.model.Account;
import com.simon.user_service.model.Order;
import com.simon.dto.user.OrderDTO;
import com.simon.user_service.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerUnitTest {

    @Mock
    private OrderService orderService;

    @Mock
    private HttpServletRequest httpRequest;

    @InjectMocks
    private OrderController orderController;

    private Order createOrder(int orderId, int userId, int bookId, BigDecimal price, LocalDate rentEnd) {
        Order order = new Order(new Account(userId), bookId, price);
        order.setId(orderId);
        order.setRentEnd(rentEnd);
        return order;
    }

    @Test
    void makeOrder_ShouldReturnOrderDTO() {
        when(httpRequest.getHeader(Headers.USER_ID)).thenReturn("10");
        Order order = createOrder(1, 10, 5, new BigDecimal("2.50"), null);
        when(orderService.makeOrder(10, 5)).thenReturn(order);

        ResponseEntity<OrderDTO> response = orderController.makeOrder(5, httpRequest);

        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().id());
        assertEquals(5, response.getBody().book_id());
        verify(orderService).makeOrder(10, 5);
    }

    @Test
    void closeOrder_ShouldReturnOrderDTO() {
        when(httpRequest.getHeader(Headers.USER_ID)).thenReturn("3");
        LocalDate endDate = LocalDate.now();
        Order order = createOrder(99, 3, 7, new BigDecimal("1.00"), endDate);
        when(orderService.closeOrder(3, 99, true, true)).thenReturn(order);

        OrderDTO response = orderController.closeOrder(99, httpRequest);

        assertEquals(99, response.id());
        assertEquals(7, response.book_id());
        assertEquals(endDate, response.rent_end());
        verify(orderService).closeOrder(3, 99, true, true);
    }

    @Test
    void getAccountOrders_ShouldReturnOrderList() {
        when(httpRequest.getHeader(Headers.USER_ID)).thenReturn("15");
        Order o1 = createOrder(1, 15, 1, new BigDecimal("3.00"), null);
        Order o2 = createOrder(2, 15, 2, new BigDecimal("4.00"), null);
        when(orderService.findOrdersByAccountId(15, true)).thenReturn(List.of(o1, o2));

        List<OrderDTO> result = orderController.getAccountOrders(true, httpRequest);

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).id());
        assertEquals(2, result.get(1).id());
        verify(orderService).findOrdersByAccountId(15, true);
    }
}

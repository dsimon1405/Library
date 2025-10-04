package com.simon.user_service.controller;

import com.simon.dto.user.OrderDTO;
import com.simon.name.PathRoles;
import com.simon.user_service.model.Order;
import com.simon.user_service.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping(PathRoles.USER + "/make/{book_id}")
    public ResponseEntity<OrderDTO> makeOrder(@PathVariable int book_id, HttpServletRequest request) {
        return ResponseEntity.created(URI.create("/api/v1/order/r_u/get"))
                .body(Order.toDTO(orderService.makeOrder(ControllerUtil.getUserIdFromHeader(request), book_id)));
    }

    @PutMapping(PathRoles.USER + "/close/{order_id}")
    public OrderDTO closeOrder(@PathVariable int order_id, HttpServletRequest request) {
        return Order.toDTO(orderService.closeOrder(
                ControllerUtil.getUserIdFromHeader(request), order_id, true, true));
    }

    @GetMapping(PathRoles.USER + "/get")
    public List<OrderDTO> getAccountOrders(@RequestParam(value = "open", required = false) Boolean open,
                                           HttpServletRequest request) {
        return orderService.findOrdersByAccountId(ControllerUtil.getUserIdFromHeader(request), open)
                .stream().map(Order::toDTO).toList();
    }

    @PutMapping(PathRoles.ADMIN + "/close/{order_id}")
    public OrderDTO adminCloseOrder(@PathVariable int order_id,
                                    @RequestParam(value = "account_id") int account_id,
                                    @RequestParam(value = "return_book") boolean return_book) {
        return Order.toDTO(orderService.closeOrder(account_id, order_id, false, return_book));
    }

    @PostMapping(PathRoles.SERVICE + "/get/open")
    public List<OrderDTO> getOpenOrdersByBooksId(@RequestBody List<Integer> books_ids) {
        return orderService.findByBookIds(books_ids).stream().map(Order::toDTO).toList();
    }
}

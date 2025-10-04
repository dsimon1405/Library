package com.simon.user_service.service;

import com.simon.dto.lib.BookDTO;
import com.simon.exception.ExistsException;
import com.simon.exception.NotFoundException;
import com.simon.name.PathRoles;
import com.simon.user_service.exception.RentException;
import com.simon.user_service.model.Account;
import com.simon.user_service.model.Order;
import com.simon.user_service.repository.OrderRepository;
import com.simon.utils.HttpRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final AccountService accountService;
    private final HttpRequest httpRequest;
    public static final String service_url = "http://lib-service";
//    public static final String service_url = "http://localhost:8081";

    private static final int MAX_NUMBER_OF_ORDERS = 3;

    @Transactional
    public Order makeOrder(int account_id, int book_id) {
        List<Order> orders = orderRepository.findByAccount_IdAndRentEndIsNull(account_id);

        Account account = null;
        if (!orders.isEmpty()) {
            if (orders.size() >= MAX_NUMBER_OF_ORDERS)
                throw new RentException("Reached max number of open orders = " + MAX_NUMBER_OF_ORDERS);

            if (orders.stream().anyMatch(order -> order.getBookId() == book_id))
                throw new RentException("User already rent book with id: " + book_id);

            account = orders.getFirst().getAccount();
        } else account = accountService.findById(account_id);

        if (account == null) throw new NotFoundException("Account doesn't exists with id: " + account_id);

        return orderRepository.save(new Order(account, book_id,
                takeOrReturnBookToLib(book_id, -1).getBody().oneDayRentPriceUSD()));
    }

    @Transactional
    public Order findOrderByIdWithAccount(int id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Doesn't exists order with id: " + id));
    }

    @Transactional
    public Order closeOrder(int account_id, int order_id, boolean write_off_balance, boolean return_book) {
        Order order = findOrderByIdWithAccount(order_id);
        Account account = order.getAccount();
        if (account.getId() != account_id) throw new ExistsException("Account with id: " + account_id
                + ", haven't order with id: " + order_id);
        if (order.getRentEnd() != null) throw new RentException("Order already closed with id: " + order_id);

        LocalDate rentEnd = LocalDate.now();
        if (write_off_balance) {
            BigDecimal rentPrice = order.calcRentPrice(rentEnd);
            BigDecimal cur_balance = account.getBalanceUSD();
            BigDecimal new_balance = cur_balance.subtract(rentPrice);
            if (new_balance.compareTo(BigDecimal.ZERO) < 0)
                throw new RentException(new_balance.negate() + " USD is missing for payment, please top up your balance: " +
                        "http:://localhost:8080/user/account" + PathRoles.USER + "/balance?change_on=" + new_balance.negate());
            account.setBalanceUSD(new_balance);
            order.setPaidPriceUSD(rentPrice);
        }
        order.setRentEnd(rentEnd);

        if (return_book) takeOrReturnBookToLib(order.getBookId(), 1);

        return orderRepository.save(order);
    }

    public List<Order> findOrdersByAccountId(int accountId, Boolean open) {
        return open == null ? orderRepository.findByAccount_Id(accountId)
                            : open ?  orderRepository.findByAccount_IdAndRentEndIsNull(accountId)
                                    : orderRepository.findByAccount_IdAndRentEndIsNotNull(accountId);
    }

    public List<Order> findByBookIds(List<Integer> book_ids) {
        return orderRepository.findByBookIdInAndRentEndIsNull(book_ids);
    }

    private ResponseEntity<BookDTO> takeOrReturnBookToLib(int book_id, int count) {
        return httpRequest.request(
                service_url + "/lib-service/api/v1/book" + PathRoles.ADMIN
                        + "/update/{id}?quantity_change_on={changeOn}",
                HttpMethod.PUT,
                HttpEntity.EMPTY,
                BookDTO.class,
                Map.of("id", book_id, "changeOn", count));
    }
}


//    @Transactional
//    public Order adminCloseOrder(int account_id, int order_id) {
//        Order order = findOrderByIdWithAccount(order_id);
//        if (order.getAccount().getId() != account_id)
//            throw new ExistsException("Order with id: " + order_id + ", belongs to account with id: " + order.getAccount().getId());
//        if (order.getRentEnd() != null) throw new RentException("Order already closed with id: " + order_id);
//
//        order.setRentEnd(LocalDate.now());
//        return orderRepository.save(order);
//    }
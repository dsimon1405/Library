package com.simon.user_service.repository;

import com.simon.user_service.model.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends CrudRepository<Order, Integer> {

    @EntityGraph(attributePaths = "account")
    List<Order> findByAccount_IdAndRentEndIsNull(Integer accountId);

    @Override
    @EntityGraph(attributePaths = "account")
    Optional<Order> findById(Integer bookId);

    List<Order> findByAccount_Id(Integer accountId);
    List<Order> findByAccount_IdAndRentEndIsNotNull(Integer accountId);

    List<Order> findByBookIdInAndRentEndIsNull(List<Integer> bookIds);
}

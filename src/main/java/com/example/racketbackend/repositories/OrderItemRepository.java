package com.example.racketbackend.repositories;

import com.example.racketbackend.models.OrderItem;
import com.example.racketbackend.models.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository
        extends JpaRepository<OrderItem, Integer> {

    List<OrderItem> findByOrder_Id(
            Integer orderId
    );

    boolean existsByOrder_User_IdAndRacket_IdAndOrder_StatusIn(
            Integer userId,
            Integer racketId,
            List<OrderStatus> statuses
    );
}
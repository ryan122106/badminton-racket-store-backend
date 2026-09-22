package com.example.racketbackend.repositories;

import com.example.racketbackend.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository
        extends JpaRepository<Order, Integer> {

    List<Order> findByUser_Id(
            Integer userId
    );

    List<Order> findByUser_IdOrderByOrderDateDesc(
            Integer userId
    );
}
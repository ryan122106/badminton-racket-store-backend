package com.example.racketbackend.controllers;

import com.example.racketbackend.dto.PickupCodeRequest;
import com.example.racketbackend.models.Order;
import com.example.racketbackend.models.User;
import com.example.racketbackend.services.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
public class OrderController {

    private final OrderService orderService;

    public OrderController(
            OrderService orderService) {

        this.orderService = orderService;
    }

    @GetMapping("/orders")
    public ResponseEntity<Object> getOrders() {

        return orderService.getOrders();
    }

    @GetMapping("/orders/user/{userId}")
    public ResponseEntity<Object> getOrderByUserId(
            @PathVariable Integer userId) {

        return orderService
                .getOrderByUserId(userId);
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<Object> getOrderById(
            @PathVariable Integer id) {

        return orderService.getOrderById(id);
    }

    @PostMapping("/orders")
    public ResponseEntity<Object> addOrder(
            @RequestBody Order order) {

        return orderService.addOrder(order);
    }

    @PutMapping(
            "/admin/orders/{id}/prepare"
    )
    public ResponseEntity<Object> prepareOrder(
            @PathVariable Integer id) {

        return orderService.prepareOrder(id);
    }

    @PutMapping(
            "/admin/orders/{id}/ready-for-pickup"
    )
    public ResponseEntity<Object> readyForPickup(
            @PathVariable Integer id) {

        return orderService.readyForPickup(id);
    }

    @PutMapping(
            "/admin/orders/{id}/complete-pickup"
    )
    public ResponseEntity<Object> completePickup(
            @PathVariable Integer id,
            @RequestBody PickupCodeRequest request) {

        return orderService.completePickup(
                id,
                request
        );
    }

    @DeleteMapping("/orders/{id}")
    public ResponseEntity<Object> deleteOrder(
            @PathVariable Integer id) {

        return orderService.deleteOrder(id);
    }

    @GetMapping("/orders/me")
    public ResponseEntity<Object> getMyOrders(
            @AuthenticationPrincipal User loggedInUser
    ) {
        return orderService.getPurchaseHistory(
                loggedInUser
        );
    }
}
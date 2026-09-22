package com.example.racketbackend.controllers;

import com.example.racketbackend.models.OrderItem;
import com.example.racketbackend.services.OrderItemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class OrderItemController {

    private final OrderItemService orderItemService;

    public OrderItemController(
            OrderItemService orderItemService) {

        this.orderItemService = orderItemService;
    }

    @GetMapping("/order-items")
    public ResponseEntity<Object> getOrderItems() {
        return orderItemService.getOrderItems();
    }

    @GetMapping("/order-items/{id}")
    public ResponseEntity<Object> getOrderItemById(
            @PathVariable Integer id) {

        return orderItemService.getOrderItemById(id);
    }

    @GetMapping("/order-items/order/{orderId}")
    public ResponseEntity<Object> getOrderItemsByOrderId(
            @PathVariable Integer orderId) {

        return orderItemService
                .getOrderItemsByOrderId(orderId);
    }

    @PostMapping("/order-items")
    public ResponseEntity<Object> addOrderItem(
            @RequestBody OrderItem orderItem) {

        return orderItemService.addOrderItem(orderItem);
    }

    @DeleteMapping("/order-items/{id}")
    public ResponseEntity<Object> deleteOrderItem(
            @PathVariable Integer id) {

        return orderItemService.deleteOrderItem(id);
    }
}
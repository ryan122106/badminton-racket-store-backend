package com.example.racketbackend.services;

import com.example.racketbackend.models.Order;
import com.example.racketbackend.models.OrderItem;
import com.example.racketbackend.models.OrderStatus;
import com.example.racketbackend.models.Racket;
import com.example.racketbackend.repositories.OrderItemRepository;
import com.example.racketbackend.repositories.OrderRepository;
import com.example.racketbackend.repositories.RacketRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final RacketRepository racketRepository;

    public OrderItemService(
            OrderItemRepository orderItemRepository,
            OrderRepository orderRepository,
            RacketRepository racketRepository
    ) {
        this.orderItemRepository =
                orderItemRepository;

        this.orderRepository =
                orderRepository;

        this.racketRepository =
                racketRepository;
    }

    public ResponseEntity<Object> getOrderItems() {
        return ResponseEntity.ok(
                orderItemRepository.findAll()
        );
    }

    public ResponseEntity<Object> getOrderItemsByOrderId(
            Integer orderId
    ) {
        return ResponseEntity.ok(
                orderItemRepository.findByOrder_Id(
                        orderId
                )
        );
    }

    public ResponseEntity<Object> getOrderItemById(
            Integer id
    ) {
        OrderItem orderItem =
                orderItemRepository
                        .findById(id)
                        .orElse(null);

        if (orderItem == null) {
            return ResponseEntity.status(404)
                    .body("Order item not found");
        }

        return ResponseEntity.ok(orderItem);
    }

    @Transactional
    public ResponseEntity<Object> addOrderItem(
            OrderItem orderItem
    ) {
        if (orderItem == null) {
            return ResponseEntity.badRequest()
                    .body("Order item is required");
        }

        if (orderItem.getOrder() == null ||
                orderItem.getOrder().getId() == null) {

            return ResponseEntity.badRequest()
                    .body("Order ID cannot be empty");
        }

        if (orderItem.getRacket() == null ||
                orderItem.getRacket().getId() == null) {

            return ResponseEntity.badRequest()
                    .body("Racket ID cannot be empty");
        }

        Order existingOrder =
                orderRepository
                        .findById(
                                orderItem
                                        .getOrder()
                                        .getId()
                        )
                        .orElse(null);

        if (existingOrder == null) {
            return ResponseEntity.badRequest()
                    .body("Order does not exist");
        }

        if (existingOrder.getStatus() !=
                OrderStatus.PENDING) {

            return ResponseEntity.badRequest()
                    .body(
                            "Items can only be added to a pending order"
                    );
        }

        Racket existingRacket =
                racketRepository
                        .findById(
                                orderItem
                                        .getRacket()
                                        .getId()
                        )
                        .orElse(null);

        if (existingRacket == null) {
            return ResponseEntity.badRequest()
                    .body("Racket does not exist");
        }

        Integer quantity =
                orderItem.getQuantity();

        if (quantity == null ||
                quantity <= 0) {

            return ResponseEntity.badRequest()
                    .body(
                            "Quantity must be greater than 0"
                    );
        }

        if (existingRacket.getStock() == null ||
                existingRacket.getStock() < quantity) {

            return ResponseEntity.badRequest()
                    .body(
                            "Not enough racket stock"
                    );
        }

        if (existingRacket.getPrice() == null ||
                existingRacket.getPrice() <= 0) {

            return ResponseEntity.badRequest()
                    .body(
                            "Racket has an invalid price"
                    );
        }

        orderItem.setOrder(existingOrder);
        orderItem.setRacket(existingRacket);

        /*
         * Always use the price from SQL.
         * Never trust the price sent by Android.
         */
        orderItem.setPrice(
                existingRacket.getPrice()
        );

        OrderItem savedItem =
                orderItemRepository
                        .saveAndFlush(orderItem);

        recalculateOrderTotal(existingOrder);

        return ResponseEntity.ok(savedItem);
    }

    @Transactional
    public ResponseEntity<Object> deleteOrderItem(
            Integer id
    ) {
        OrderItem orderItem =
                orderItemRepository
                        .findById(id)
                        .orElse(null);

        if (orderItem == null) {
            return ResponseEntity.status(404)
                    .body("Order item not found");
        }

        Order existingOrder =
                orderItem.getOrder();

        orderItemRepository.delete(orderItem);
        orderItemRepository.flush();

        recalculateOrderTotal(existingOrder);

        return ResponseEntity.ok(
                "Order item deleted successfully"
        );
    }

    private void recalculateOrderTotal(
            Order order
    ) {
        List<OrderItem> items =
                orderItemRepository
                        .findByOrder_Id(
                                order.getId()
                        );

        BigDecimal total =
                items.stream()
                        .map(item ->
                                BigDecimal.valueOf(
                                                item.getPrice()
                                        )
                                        .multiply(
                                                BigDecimal.valueOf(
                                                        item.getQuantity()
                                                )
                                        )
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        )
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        order.setTotalPrice(
                total.doubleValue()
        );

        orderRepository.save(order);
    }
}
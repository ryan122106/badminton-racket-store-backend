package com.example.racketbackend.services;

import com.example.racketbackend.dto.PickupCodeRequest;
import com.example.racketbackend.dto.PurchaseHistoryItemResponse;
import com.example.racketbackend.dto.PurchaseHistoryResponse;
import com.example.racketbackend.models.Order;
import com.example.racketbackend.models.OrderStatus;
import com.example.racketbackend.models.Racket;
import com.example.racketbackend.models.User;
import com.example.racketbackend.repositories.OrderItemRepository;
import com.example.racketbackend.repositories.OrderRepository;
import com.example.racketbackend.repositories.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    private final SecureRandom secureRandom =
            new SecureRandom();

    private final OrderItemRepository orderItemRepository;

    public OrderService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            UserRepository userRepository
    ) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
    }

    public ResponseEntity<Object> getPurchaseHistory(
            User loggedInUser
    ) {
        if (loggedInUser == null) {
            return ResponseEntity.status(401)
                    .body("Please log in");
        }

        List<PurchaseHistoryResponse> history =
                orderRepository
                        .findByUser_IdOrderByOrderDateDesc(
                                loggedInUser.getId()
                        )
                        .stream()
                        .map(order -> {

                            List<PurchaseHistoryItemResponse> items =
                                    orderItemRepository
                                            .findByOrder_Id(order.getId())
                                            .stream()
                                            .map(item -> {
                                                Racket racket =
                                                        item.getRacket();

                                                return new PurchaseHistoryItemResponse(
                                                        racket.getId(),
                                                        racket.getTitle(),
                                                        racket.getBrand(),
                                                        racket.getImage(),
                                                        item.getQuantity(),
                                                        item.getPrice()
                                                );
                                            })
                                            .toList();

                            return new PurchaseHistoryResponse(
                                    order.getId(),
                                    order.getTotalPrice(),
                                    order.getStatus().name(),
                                    order.getOrderDate(),
                                    order.getPickupLocation(),
                                    order.getPickupDate(),
                                    order.getPickupCode(),
                                    items
                            );
                        })
                        .toList();

        return ResponseEntity.ok(history);
    }

    public ResponseEntity<Object> getOrders() {

        return ResponseEntity.ok(
                orderRepository.findAll()
        );
    }

    public ResponseEntity<Object> getOrderByUserId(
            Integer userId) {

        return ResponseEntity.ok(
                orderRepository.findByUser_Id(userId)
        );
    }

    public ResponseEntity<Object> getOrderById(
            Integer id) {

        Order order = orderRepository
                .findById(id)
                .orElse(null);

        if (order == null) {
            return ResponseEntity.status(404)
                    .body("Order not found");
        }

        return ResponseEntity.ok(order);
    }

    public ResponseEntity<Object> addOrder(
            Order order) {

        if (order.getUser() == null ||
                order.getUser().getId() == null) {

            return ResponseEntity.badRequest()
                    .body("User ID cannot be empty");
        }

        User existingUser = userRepository
                .findById(
                        order.getUser().getId()
                )
                .orElse(null);

        if (existingUser == null) {
            return ResponseEntity.badRequest()
                    .body("User does not exist");
        }

        if (!Boolean.TRUE.equals(
                existingUser.getActive()
        )) {
            return ResponseEntity.badRequest()
                    .body("User account is disabled");
        }

        if (order.getTotalPrice() == null ||
                order.getTotalPrice() <= 0) {

            return ResponseEntity.badRequest()
                    .body(
                            "Total price must be greater than 0"
                    );
        }

        if (order.getPickupLocation() == null ||
                order.getPickupLocation().isBlank()) {

            return ResponseEntity.badRequest()
                    .body(
                            "Pickup location is required"
                    );
        }

        if (order.getPickupDate() == null) {
            return ResponseEntity.badRequest()
                    .body(
                            "Pickup date is required"
                    );
        }

        if (order.getPickupDate().isBefore(
                LocalDateTime.now()
        )) {
            return ResponseEntity.badRequest()
                    .body(
                            "Pickup date must be in the future"
                    );
        }

        order.setUser(existingUser);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderDate(LocalDateTime.now());

        // Code is generated only when order is ready
        order.setPickupCode(null);

        Order savedOrder =
                orderRepository.save(order);

        return ResponseEntity.ok(savedOrder);
    }

    public ResponseEntity<Object> prepareOrder(
            Integer orderId) {

        Order order = orderRepository
                .findById(orderId)
                .orElse(null);

        if (order == null) {
            return ResponseEntity.status(404)
                    .body("Order not found");
        }

        if (order.getStatus() !=
                OrderStatus.SUCCESSFUL) {

            return ResponseEntity.badRequest()
                    .body(
                            "Only successfully paid orders can be prepared"
                    );
        }

        order.setStatus(
                OrderStatus.PREPARING
        );

        Order savedOrder =
                orderRepository.save(order);

        return ResponseEntity.ok(savedOrder);
    }

    public ResponseEntity<Object> readyForPickup(
            Integer orderId) {

        Order order = orderRepository
                .findById(orderId)
                .orElse(null);

        if (order == null) {
            return ResponseEntity.status(404)
                    .body("Order not found");
        }

        if (order.getStatus() !=
                OrderStatus.PREPARING) {

            return ResponseEntity.badRequest()
                    .body(
                            "Only preparing orders can become ready for pickup"
                    );
        }

        String pickupCode =
                generatePickupCode();

        order.setPickupCode(pickupCode);

        order.setStatus(
                OrderStatus.READY_FOR_PICKUP
        );

        Order savedOrder =
                orderRepository.save(order);

        return ResponseEntity.ok(savedOrder);
    }

    public ResponseEntity<Object> completePickup(
            Integer orderId,
            PickupCodeRequest request) {

        Order order = orderRepository
                .findById(orderId)
                .orElse(null);

        if (order == null) {
            return ResponseEntity.status(404)
                    .body("Order not found");
        }

        if (order.getStatus() !=
                OrderStatus.READY_FOR_PICKUP) {

            return ResponseEntity.badRequest()
                    .body(
                            "Order is not ready for pickup"
                    );
        }

        if (request == null ||
                request.pickupCode() == null ||
                request.pickupCode().isBlank()) {

            return ResponseEntity.badRequest()
                    .body("Pickup code is required");
        }

        String enteredCode =
                request.pickupCode().trim();

        if (order.getPickupCode() == null ||
                !order.getPickupCode()
                        .equals(enteredCode)) {

            return ResponseEntity.badRequest()
                    .body("Invalid pickup code");
        }

        order.setStatus(
                OrderStatus.COMPLETED
        );

        Order savedOrder =
                orderRepository.save(order);

        return ResponseEntity.ok(savedOrder);
    }

    private String generatePickupCode() {

        int code =
                secureRandom.nextInt(1_000_000);

        return String.format(
                "%06d",
                code
        );
    }

    public ResponseEntity<Object> deleteOrder(
            Integer id) {

        if (!orderRepository.existsById(id)) {
            return ResponseEntity.status(404)
                    .body("Order not found");
        }

        orderRepository.deleteById(id);

        return ResponseEntity.ok(
                "Order deleted successfully"
        );
    }


}
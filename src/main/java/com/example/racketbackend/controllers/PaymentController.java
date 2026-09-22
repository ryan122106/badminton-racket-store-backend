package com.example.racketbackend.controllers;

import com.example.racketbackend.dto.DemoCardPaymentRequest;
import com.example.racketbackend.models.User;
import com.example.racketbackend.services.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(
            PaymentService paymentService) {

        this.paymentService = paymentService;
    }

    // Admin endpoint
    @GetMapping("/payments")
    public ResponseEntity<Object> getPayments() {
        return paymentService.getPayments();
    }

    // Admin endpoint
    @GetMapping("/payments/order/{orderId}")
    public ResponseEntity<Object> getPaymentsByOrder(
            @PathVariable Integer orderId) {

        return paymentService
                .getPaymentByOrderId(orderId);
    }

    // Admin endpoint
    @GetMapping("/payments/{id}")
    public ResponseEntity<Object> getPaymentById(
            @PathVariable Integer id) {

        return paymentService.getPaymentById(id);
    }

    // Normal logged-in user can use this
    @PostMapping("/payments/demo/{orderId}")
    public ResponseEntity<Object> payWithDemoCard(
            @PathVariable Integer orderId,
            @RequestBody DemoCardPaymentRequest request,
            @AuthenticationPrincipal User user) {

        return paymentService.payWithDemoCard(
                orderId,
                request,
                user
        );
    }

    // Admin endpoint
    @DeleteMapping("/payments/{id}")
    public ResponseEntity<Object> deletePayment(
            @PathVariable Integer id) {

        return paymentService.deletePayment(id);
    }
}
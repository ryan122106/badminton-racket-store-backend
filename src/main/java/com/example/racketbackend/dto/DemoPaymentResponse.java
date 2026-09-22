package com.example.racketbackend.dto;

public record DemoPaymentResponse(
        Integer paymentId,
        Integer orderId,
        Double amount,
        String cardLastFour,
        String paymentStatus,
        String orderStatus,
        String message
) {
}
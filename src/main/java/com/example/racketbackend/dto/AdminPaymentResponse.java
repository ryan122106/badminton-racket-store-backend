package com.example.racketbackend.dto;

import java.time.LocalDateTime;

public record AdminPaymentResponse(
        Integer id,
        Integer orderId,
        Double amount,
        String paymentMethod,
        String paymentStatus,
        LocalDateTime paymentDate,
        String cardLastFour,
        String pickupLocation,
        LocalDateTime pickupDate,
        String pickupCode
) {
}
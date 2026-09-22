package com.example.racketbackend.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PurchaseHistoryResponse(
        Integer id,
        Double totalPrice,
        String status,
        LocalDateTime orderDate,
        String pickupLocation,
        LocalDateTime pickupDate,
        String pickupCode,
        List<PurchaseHistoryItemResponse> items
) {
}
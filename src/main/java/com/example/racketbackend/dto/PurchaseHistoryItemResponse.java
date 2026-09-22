package com.example.racketbackend.dto;

public record PurchaseHistoryItemResponse(
        Integer racketId,
        String title,
        String brand,
        String image,
        Integer quantity,
        Double price
) {
}
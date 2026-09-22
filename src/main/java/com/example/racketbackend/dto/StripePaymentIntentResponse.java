package com.example.racketbackend.dto;

public record StripePaymentIntentResponse(
        String clientSecret,
        String publishableKey,
        String paymentIntentId
) {
}
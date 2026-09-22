package com.example.racketbackend.dto;

public record DemoCardPaymentRequest(
        String cardholderName,
        String cardNumber,
        String expiry,
        String cvv
) {
}
package com.example.racketbackend.models;

public enum OrderStatus {
    PENDING,
    SUCCESSFUL,
    PAYMENT_FAILED,
    PREPARING,
    READY_FOR_PICKUP,
    COMPLETED,
    CANCELLED,

    // Keep this temporarily if old database orders use it
    SHIPPED
}
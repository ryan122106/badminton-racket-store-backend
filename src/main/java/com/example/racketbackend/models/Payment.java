package com.example.racketbackend.models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "Payment")
public class Payment {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Integer id;

    private Integer orderId;

    // Always calculated from the backend order
    private Double amount;

    // Example: STRIPE_CARD or DEMO_CARD
    private String paymentMethod;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    // Store only the final four digits
    @Column(length = 4)
    private String cardLastFour;

    private LocalDateTime paymentDate;

    // Connects this database payment to Stripe
    @Column(
            name = "stripe_payment_intent_id",
            unique = true,
            length = 100
    )
    private String stripePaymentIntentId;
}
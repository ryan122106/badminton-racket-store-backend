package com.example.racketbackend.repositories;

import com.example.racketbackend.models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository
        extends JpaRepository<Payment, Integer> {

    List<Payment> findByOrderId(
            Integer orderId
    );

    Optional<Payment>
    findByStripePaymentIntentId(
            String stripePaymentIntentId
    );

    Optional<Payment> findTopByOrderIdOrderByIdDesc(
            Integer orderId
    );
}
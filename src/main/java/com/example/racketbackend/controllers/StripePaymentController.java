package com.example.racketbackend.controllers;

import com.example.racketbackend.models.User;
import com.example.racketbackend.services.StripePaymentService;
import com.stripe.exception.SignatureVerificationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
public class StripePaymentController {

    private final StripePaymentService
            stripePaymentService;

    public StripePaymentController(
            StripePaymentService stripePaymentService
    ) {
        this.stripePaymentService =
                stripePaymentService;
    }

    @PostMapping(
            "/payments/stripe/{orderId}"
    )
    public ResponseEntity<Object>
    createStripePayment(
            @PathVariable Integer orderId,
            @AuthenticationPrincipal
            User loggedInUser
    ) {
        return stripePaymentService
                .createPaymentIntent(
                        orderId,
                        loggedInUser
                );
    }

    @PostMapping(
            "/payments/stripe/{orderId}/confirm"
    )
    public ResponseEntity<Object>
    confirmStripePayment(
            @PathVariable Integer orderId,
            @AuthenticationPrincipal
            User loggedInUser
    ) {
        return stripePaymentService
                .confirmPayment(
                        orderId,
                        loggedInUser
                );
    }

    @PostMapping("/stripe/webhook")
    public ResponseEntity<String> stripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature")
            String signature
    ) {
        try {
            stripePaymentService.handleWebhook(
                    payload,
                    signature
            );

            return ResponseEntity.ok("Received");

        } catch (
                SignatureVerificationException exception
        ) {
            return ResponseEntity
                    .badRequest()
                    .body(
                            "Invalid Stripe webhook"
                    );
        }
    }
}
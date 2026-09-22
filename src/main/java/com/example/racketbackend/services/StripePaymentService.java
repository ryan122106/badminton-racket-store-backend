package com.example.racketbackend.services;

import com.example.racketbackend.dto.StripePaymentIntentResponse;
import com.example.racketbackend.models.*;
import com.example.racketbackend.repositories.OrderRepository;
import com.example.racketbackend.repositories.PaymentRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.RequestOptions;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
public class StripePaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Value("${stripe.publishable-key}")
    private String publishableKey;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    public StripePaymentService(
            PaymentRepository paymentRepository,
            OrderRepository orderRepository
    ) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public ResponseEntity<Object> createPaymentIntent(
            Integer orderId,
            User loggedInUser
    ) {
        if (loggedInUser == null) {
            return ResponseEntity.status(401)
                    .body("Please log in");
        }

        Order order = orderRepository
                .findById(orderId)
                .orElse(null);

        if (order == null) {
            return ResponseEntity.status(404)
                    .body("Order not found");
        }

        if (!order.getUser().getId()
                .equals(loggedInUser.getId())) {

            return ResponseEntity.status(403)
                    .body(
                            "This order does not belong to you"
                    );
        }

        if (order.getStatus() ==
                OrderStatus.SUCCESSFUL ||
                order.getStatus() ==
                        OrderStatus.PREPARING ||
                order.getStatus() ==
                        OrderStatus.READY_FOR_PICKUP ||
                order.getStatus() ==
                        OrderStatus.COMPLETED) {

            return ResponseEntity.badRequest()
                    .body(
                            "This order has already been paid"
                    );
        }

        if (order.getTotalPrice() == null ||
                order.getTotalPrice() <= 0) {

            return ResponseEntity.badRequest()
                    .body("Invalid order total");
        }

        try {
            long amountInSen =
                    BigDecimal
                            .valueOf(
                                    order.getTotalPrice()
                            )
                            .setScale(
                                    2,
                                    RoundingMode.HALF_UP
                            )
                            .movePointRight(2)
                            .longValueExact();

            PaymentIntentCreateParams params =
                    PaymentIntentCreateParams
                            .builder()
                            .setAmount(amountInSen)
                            .setCurrency("myr")
                            .addPaymentMethodType("card")
                            .putMetadata(
                                    "orderId",
                                    order.getId().toString()
                            )
                            .putMetadata(
                                    "userId",
                                    loggedInUser
                                            .getId()
                                            .toString()
                            )
                            .build();

            RequestOptions requestOptions =
                    RequestOptions
                            .builder()
                            .setIdempotencyKey(
                                    "racket-order-" +
                                            order.getId()
                            )
                            .build();

            PaymentIntent paymentIntent =
                    PaymentIntent.create(
                            params,
                            requestOptions
                    );

            Payment payment =
                    paymentRepository
                            .findByStripePaymentIntentId(
                                    paymentIntent.getId()
                            )
                            .orElseGet(Payment::new);

            payment.setOrderId(order.getId());
            payment.setAmount(order.getTotalPrice());
            payment.setPaymentMethod("STRIPE_CARD");
            payment.setPaymentStatus(
                    PaymentStatus.PENDING
            );
            payment.setPaymentDate(
                    LocalDateTime.now()
            );
            payment.setStripePaymentIntentId(
                    paymentIntent.getId()
            );

            paymentRepository.save(payment);

            StripePaymentIntentResponse response =
                    new StripePaymentIntentResponse(
                            paymentIntent
                                    .getClientSecret(),
                            publishableKey,
                            paymentIntent.getId()
                    );

            return ResponseEntity.ok(response);

        } catch (StripeException exception) {
            return ResponseEntity
                    .internalServerError()
                    .body(
                            "Stripe error: " +
                                    exception.getMessage()
                    );

        } catch (ArithmeticException exception) {
            return ResponseEntity.badRequest()
                    .body("Invalid payment amount");
        }
    }

    @Transactional
    public ResponseEntity<Object> confirmPayment(
            Integer orderId,
            User loggedInUser
    ) {
        if (loggedInUser == null) {
            return ResponseEntity.status(401)
                    .body("Please log in");
        }

        Order order = orderRepository
                .findById(orderId)
                .orElse(null);

        if (order == null) {
            return ResponseEntity.status(404)
                    .body("Order not found");
        }

        if (!order.getUser().getId()
                .equals(loggedInUser.getId())) {

            return ResponseEntity.status(403)
                    .body(
                            "This order does not belong to you"
                    );
        }

        Payment payment = paymentRepository
                .findTopByOrderIdOrderByIdDesc(
                        orderId
                )
                .orElse(null);

        if (payment == null ||
                payment.getStripePaymentIntentId() == null) {

            return ResponseEntity.status(404)
                    .body("Stripe payment not found");
        }

        if (order.getStatus() ==
                OrderStatus.SUCCESSFUL &&
                payment.getPaymentStatus() ==
                        PaymentStatus.SUCCESSFUL) {

            return ResponseEntity.ok(order);
        }

        try {
            PaymentIntent paymentIntent =
                    PaymentIntent.retrieve(
                            payment.getStripePaymentIntentId()
                    );

            if (!"succeeded".equals(
                    paymentIntent.getStatus()
            )) {
                return ResponseEntity.status(409)
                        .body(
                                "Payment is not completed yet. Stripe status: " +
                                        paymentIntent.getStatus()
                        );
            }

            payment.setPaymentStatus(
                    PaymentStatus.SUCCESSFUL
            );

            payment.setPaymentDate(
                    LocalDateTime.now()
            );

            order.setStatus(
                    OrderStatus.SUCCESSFUL
            );

            paymentRepository.save(payment);

            Order savedOrder =
                    orderRepository.save(order);

            return ResponseEntity.ok(savedOrder);

        } catch (StripeException exception) {
            return ResponseEntity
                    .internalServerError()
                    .body(
                            "Unable to verify Stripe payment: " +
                                    exception.getMessage()
                    );
        }
    }

    @Transactional
    public void handleWebhook(
            String payload,
            String signature
    ) throws SignatureVerificationException {

        Event event =
                Webhook.constructEvent(
                        payload,
                        signature,
                        webhookSecret
                );

        StripeObject stripeObject =
                event
                        .getDataObjectDeserializer()
                        .getObject()
                        .orElse(null);

        if (!(stripeObject
                instanceof PaymentIntent paymentIntent)) {
            return;
        }

        Payment payment =
                paymentRepository
                        .findByStripePaymentIntentId(
                                paymentIntent.getId()
                        )
                        .orElse(null);

        if (payment == null) {
            return;
        }

        Order order =
                orderRepository
                        .findById(
                                payment.getOrderId()
                        )
                        .orElse(null);

        switch (event.getType()) {

            case "payment_intent.processing" -> {
                payment.setPaymentStatus(
                        PaymentStatus.PROCESSING
                );
            }

            case "payment_intent.succeeded" -> {
                payment.setPaymentStatus(
                        PaymentStatus.SUCCESSFUL
                );

                payment.setPaymentDate(
                        LocalDateTime.now()
                );

                if (order != null) {
                    order.setStatus(
                            OrderStatus.SUCCESSFUL
                    );
                }
            }

            case "payment_intent.payment_failed" -> {
                payment.setPaymentStatus(
                        PaymentStatus.FAILED
                );

                payment.setPaymentDate(
                        LocalDateTime.now()
                );

                if (order != null) {
                    order.setStatus(
                            OrderStatus.PAYMENT_FAILED
                    );
                }
            }

            default -> {
                return;
            }
        }

        paymentRepository.save(payment);

        if (order != null) {
            orderRepository.save(order);
        }
    }
}
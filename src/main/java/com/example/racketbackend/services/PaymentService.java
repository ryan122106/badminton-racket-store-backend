package com.example.racketbackend.services;

import com.example.racketbackend.dto.DemoCardPaymentRequest;
import com.example.racketbackend.dto.DemoPaymentResponse;
import com.example.racketbackend.models.Order;
import com.example.racketbackend.models.OrderStatus;
import com.example.racketbackend.models.Payment;
import com.example.racketbackend.models.PaymentStatus;
import com.example.racketbackend.models.User;
import com.example.racketbackend.repositories.OrderRepository;
import com.example.racketbackend.repositories.PaymentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public PaymentService(
            PaymentRepository paymentRepository,
            OrderRepository orderRepository
    ) {
        this.paymentRepository =
                paymentRepository;

        this.orderRepository =
                orderRepository;
    }

    public ResponseEntity<Object> getPayments() {

        return ResponseEntity.ok(
                paymentRepository.findAll()
        );
    }

    public ResponseEntity<Object> getPaymentByOrderId(
            Integer orderId
    ) {

        return ResponseEntity.ok(
                paymentRepository.findByOrderId(
                        orderId
                )
        );
    }

    public ResponseEntity<Object> getPaymentById(
            Integer id
    ) {

        Payment payment =
                paymentRepository
                        .findById(id)
                        .orElse(null);

        if (payment == null) {

            return ResponseEntity.status(404)
                    .body("Payment not found");
        }

        return ResponseEntity.ok(payment);
    }

    @Transactional
    public ResponseEntity<Object> payWithDemoCard(
            Integer orderId,
            DemoCardPaymentRequest request,
            User loggedInUser
    ) {

        if (loggedInUser == null) {

            return ResponseEntity.status(401)
                    .body("Please log in");
        }

        Order order =
                orderRepository
                        .findById(orderId)
                        .orElse(null);

        if (order == null) {

            return ResponseEntity.status(404)
                    .body("Order not found");
        }

        /*
         * Ensure that the logged-in user owns
         * the order they are trying to pay for.
         */
        if (!order.getUser()
                .getId()
                .equals(
                        loggedInUser.getId()
                )
        ) {

            return ResponseEntity.status(403)
                    .body(
                            "This order does not belong to you"
                    );
        }

        /*
         * Prevent the same order from being
         * successfully paid more than once.
         */
        if (order.getStatus() != OrderStatus.PENDING &&
                order.getStatus() !=
                        OrderStatus.PAYMENT_FAILED) {

            return ResponseEntity.badRequest()
                    .body(
                            "This order has already been paid"
                    );
        }

        if (request == null) {

            return ResponseEntity.badRequest()
                    .body(
                            "Payment information is required"
                    );
        }

        if (request.cardholderName() == null ||
                request.cardholderName()
                        .isBlank()
        ) {

            return ResponseEntity.badRequest()
                    .body(
                            "Cardholder name is required"
                    );
        }

        if (request.cardNumber() == null ||
                request.cardNumber()
                        .isBlank()
        ) {

            return ResponseEntity.badRequest()
                    .body(
                            "Card number is required"
                    );
        }

        /*
         * Remove spaces and hyphens before
         * validating the card number.
         */
        String cardNumber =
                request.cardNumber()
                        .replace(" ", "")
                        .replace("-", "");

        /*
         * Accept any 16-digit number.
         */
        if (!cardNumber.matches("\\d{16}")) {

            return ResponseEntity.badRequest()
                    .body(
                            "Card number must contain 16 digits"
                    );
        }

        if (request.cvv() == null ||
                !request.cvv()
                        .matches("\\d{3}")
        ) {

            return ResponseEntity.badRequest()
                    .body(
                            "CVV must contain 3 digits"
                    );
        }

        if (!isValidExpiry(
                request.expiry()
        )) {

            return ResponseEntity.badRequest()
                    .body(
                            "Expiry must be a future date using MM/YY"
                    );
        }

        Payment payment =
                new Payment();

        payment.setOrderId(
                order.getId()
        );

        /*
         * The payment amount always comes
         * from the saved order.
         */
        payment.setAmount(
                order.getTotalPrice()
        );

        payment.setPaymentMethod(
                "FAKE_CARD"
        );

        payment.setPaymentStatus(
                PaymentStatus.SUCCESSFUL
        );

        /*
         * Never save the full card number.
         * Save only the final four digits.
         */
        payment.setCardLastFour(
                cardNumber.substring(
                        cardNumber.length() - 4
                )
        );

        payment.setPaymentDate(
                LocalDateTime.now()
        );

        /*
         * The order becomes successful
         * after the fake payment succeeds.
         */
        order.setStatus(
                OrderStatus.SUCCESSFUL
        );

        orderRepository.save(order);

        Payment savedPayment =
                paymentRepository.save(
                        payment
                );

        String message =
                "Payment successful";

        DemoPaymentResponse response =
                new DemoPaymentResponse(
                        savedPayment.getId(),
                        order.getId(),
                        savedPayment.getAmount(),
                        savedPayment.getCardLastFour(),
                        savedPayment
                                .getPaymentStatus()
                                .name(),
                        order.getStatus().name(),
                        message
                );

        return ResponseEntity.ok(response);
    }

    private boolean isValidExpiry(
            String expiry
    ) {

        if (expiry == null ||
                !expiry.matches(
                        "^(0[1-9]|1[0-2])/\\d{2}$"
                )
        ) {

            return false;
        }

        try {

            String[] parts =
                    expiry.split("/");

            int month =
                    Integer.parseInt(
                            parts[0]
                    );

            int year =
                    2000 +
                            Integer.parseInt(
                                    parts[1]
                            );

            YearMonth cardExpiry =
                    YearMonth.of(
                            year,
                            month
                    );

            return !cardExpiry.isBefore(
                    YearMonth.now()
            );

        } catch (Exception exception) {

            return false;
        }
    }

    public ResponseEntity<Object> deletePayment(
            Integer id
    ) {

        if (!paymentRepository
                .existsById(id)
        ) {

            return ResponseEntity.status(404)
                    .body(
                            "Payment not found"
                    );
        }

        paymentRepository.deleteById(id);

        return ResponseEntity.ok(
                "Payment deleted successfully"
        );
    }
}
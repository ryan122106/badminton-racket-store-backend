package com.example.racketbackend.services;

import com.example.racketbackend.models.Cart;
import com.example.racketbackend.repositories.CartRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartService {

    private final CartRepository cartRepository;


    public CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }


    public ResponseEntity<Object> getCarts() {
        return ResponseEntity.ok(cartRepository.findAll());
    }


    public ResponseEntity<Object> getCartByUserId(Integer userId) {
        return ResponseEntity.ok(cartRepository.findByUserId(userId));
    }


    public ResponseEntity<Object> addCart(Cart cart) {

        if (cart.getUserId() == null) {
            return ResponseEntity.badRequest().body("User ID cannot be empty");
        }

        if (cart.getRacketId() == null) {
            return ResponseEntity.badRequest().body("Racket ID cannot be empty");
        }

        if (cart.getQuantity() == null || cart.getQuantity() <= 0) {
            return ResponseEntity.badRequest().body("Quantity must be greater than 0");
        }

        Cart savedCart = cartRepository.save(cart);

        return ResponseEntity.ok(savedCart);
    }



    public ResponseEntity<Object> deleteCart(Integer id) {

        if (!cartRepository.existsById(id)) {
            return ResponseEntity.status(404).body("Cart not found");
        }

        cartRepository.deleteById(id);
        return ResponseEntity.ok("Cart deleted successfully");
    }
}
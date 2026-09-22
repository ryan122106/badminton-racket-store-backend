package com.example.racketbackend.controllers;

import com.example.racketbackend.models.Cart;
import com.example.racketbackend.services.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
public class CartController {

    private final CartService cartService;


    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/carts")
    public ResponseEntity<Object> getCarts() {
        return cartService.getCarts();
    }

    @GetMapping("/carts/user/{userId}")
    public ResponseEntity<Object> getUserCart(@PathVariable Integer userId) {
        return cartService.getCartByUserId(userId);
    }
    @PostMapping("/carts")
    public ResponseEntity<Object> addCart(@RequestBody Cart cart) {
        return cartService.addCart(cart);
    }

    @DeleteMapping("/carts/{id}")
    public ResponseEntity<Object> deleteCart(@PathVariable Integer id) {
        return cartService.deleteCart(id);
    }
}
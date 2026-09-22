package com.example.racketbackend.controllers;

import com.example.racketbackend.models.User;
import com.example.racketbackend.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {


    private final UserService userService;


    public UserController(UserService userService){

        this.userService = userService;
    }

    @GetMapping("/users")
    public ResponseEntity<Object> getUsers() {
        return userService.getUsers();
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<Object> getUserById(
            @PathVariable Integer id) {

        return userService.getUserById(id);
    }

    @GetMapping("/admin/products")
    public ResponseEntity<Object> getAdminProducts()
    {
        return ResponseEntity.ok("Admin products");
    }



    @PostMapping("/register")
    public ResponseEntity<Object> register(
            @RequestBody User user){

        return userService.register(user);
    }



    @PostMapping("/login")
    public ResponseEntity<Object> login(
            @RequestBody User user){

        return userService.login(user);
    }


    @PutMapping("/users/{id}/disable")
    public ResponseEntity<Object> disableUser(
            @PathVariable Integer id) {

        return userService.disableUser(id);
    }

    @PutMapping("/users/{id}/make-admin")
    public ResponseEntity<Object> makeUserAdmin(
            @PathVariable Integer id) {

        return userService.makeUserAdmin(id);
    }


}
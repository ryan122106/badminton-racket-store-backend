package com.example.racketbackend.models;


import jakarta.persistence.*;
import lombok.Data;


@Entity
@Data
@Table(name = "Cart")
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer userId;
    private Integer racketId;

    private Integer quantity;
}
package com.example.racketbackend.models;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "Wishlist")
public class Wishlist {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Integer id;

    private Integer userId;
    private Integer racketId;
}
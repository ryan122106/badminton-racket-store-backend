package com.example.racketbackend.models;

import lombok.Data;
import jakarta.persistence.*;

@Data
@Entity
@Table(name = "Racket")
public class Racket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String title;
    private String brand;


    private String description;
    private Double price;
    private Integer stock;

    private String image;
}
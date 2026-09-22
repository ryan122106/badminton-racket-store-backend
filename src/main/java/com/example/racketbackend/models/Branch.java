package com.example.racketbackend.models;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "Branch")
public class Branch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(
            nullable = false,
            unique = true,
            length = 30
    )
    private String code;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 250)
    private String address;

    private Boolean active = true;
}
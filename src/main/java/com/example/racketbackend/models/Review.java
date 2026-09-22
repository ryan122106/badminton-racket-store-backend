package com.example.racketbackend.models;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(
        name = "Review",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UK_Review_User_Racket",
                        columnNames = {
                                "user_id",
                                "racket_id"
                        }
                )
        }
)
public class Review {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Integer id;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "racket_id")
    private Integer racketId;

    private Integer rating;

    private String comment;
}
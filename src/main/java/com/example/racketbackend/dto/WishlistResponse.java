package com.example.racketbackend.dto;

import com.example.racketbackend.models.Racket;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WishlistResponse {

    private Integer wishlistId;
    private Racket racket;
}
package com.example.racketbackend.dto;

public record ReviewResponse(
        Integer id,
        Integer userId,
        String userName,
        Integer racketId,
        Integer rating,
        String comment
) {
}
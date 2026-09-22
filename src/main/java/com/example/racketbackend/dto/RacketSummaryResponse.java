package com.example.racketbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RacketSummaryResponse {

    private Integer id;
    private String title;
    private String brand;
    private String description;
    private Double price;
    private Integer stock;
    private String image;
}
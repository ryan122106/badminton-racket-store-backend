package com.example.racketbackend.dto;

import lombok.Data;

@Data
public class BranchInventoryRequest {

    private Integer stockQuantity;
    private Integer reservedQuantity;
}
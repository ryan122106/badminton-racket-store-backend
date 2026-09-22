package com.example.racketbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BranchAvailabilityResponse {

    private Integer branchId;
    private String branchCode;
    private String branchName;
    private Integer availableQuantity;
    private String status;
}
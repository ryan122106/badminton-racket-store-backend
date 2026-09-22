package com.example.racketbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BranchResponse {

    private Integer id;
    private String code;
    private String name;
    private String address;
}
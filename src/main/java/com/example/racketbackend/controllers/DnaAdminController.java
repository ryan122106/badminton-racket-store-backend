package com.example.racketbackend.controllers;

import com.example.racketbackend.dto.*;
import com.example.racketbackend.models.Branch;
import com.example.racketbackend.models.RacketDnaProfile;
import com.example.racketbackend.services.DnaCatalogService;
import org.springframework.web.bind.annotation.*;

@RestController
public class DnaAdminController {

    private final DnaCatalogService
            catalogService;

    public DnaAdminController(
            DnaCatalogService catalogService
    ) {
        this.catalogService =
                catalogService;
    }

    @PutMapping(
            "/admin/dna/rackets/{racketId}"
    )
    public DnaProfileResponse upsertProfile(
            @PathVariable
            Integer racketId,

            @RequestBody
            RacketDnaProfile request
    ) {
        return catalogService
                .toProfileResponse(
                        catalogService
                                .upsertProfile(
                                        racketId,
                                        request
                                )
                );
    }

    @PostMapping("/admin/branches")
    public BranchResponse saveBranch(
            @RequestBody
            Branch request
    ) {
        return catalogService
                .saveBranch(request);
    }

    @PutMapping(
            "/admin/branches/{branchId}/inventory/{racketId}"
    )
    public BranchAvailabilityResponse
    updateInventory(
            @PathVariable
            Integer branchId,

            @PathVariable
            Integer racketId,

            @RequestBody
            BranchInventoryRequest request
    ) {
        return catalogService
                .updateInventory(
                        branchId,
                        racketId,
                        request
                );
    }
}
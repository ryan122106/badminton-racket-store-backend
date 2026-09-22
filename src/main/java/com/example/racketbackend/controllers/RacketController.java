package com.example.racketbackend.controllers;

import com.example.racketbackend.models.Racket;
import com.example.racketbackend.services.RacketService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class RacketController {


    private final RacketService racketService;


    public RacketController(RacketService racketService){

        this.racketService = racketService;
    }

    @GetMapping("/rackets")
    public ResponseEntity<Object> getRackets(){

        return racketService.getRackets();
    }

    @GetMapping("/rackets/{id}")
    public ResponseEntity<Object> getRacketById(
            @PathVariable Integer id){

        return racketService.getRacketById(id);
    }

    @GetMapping("/rackets/brand/{brand}")
    public ResponseEntity<Object> getByBrand(
            @PathVariable String brand){

        return racketService.getByBrand(brand);
    }



    // Add racket
    @PostMapping("/rackets")
    public ResponseEntity<Object> addRacket(
            @RequestBody Racket racket){

        return racketService.addRacket(racket);
    }



    // Update racket
    @PutMapping("/rackets/{id}")
    public ResponseEntity<Object> updateRacket(
            @PathVariable Integer id,
            @RequestBody Racket racket){

        return racketService.updateRacket(id, racket);
    }



    // Delete racket
    @DeleteMapping("/rackets/{id}")
    public ResponseEntity<Object> deleteRacket(
            @PathVariable Integer id){

        return racketService.deleteRacket(id);
    }

}
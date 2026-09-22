package com.example.racketbackend.services;

import com.example.racketbackend.models.Racket;
import com.example.racketbackend.repositories.RacketRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RacketService {


    private final RacketRepository racketRepository;


    public RacketService(RacketRepository racketRepository) {
        this.racketRepository = racketRepository;
    }



    // Get all rackets
    public ResponseEntity<Object> getRackets() {

        return ResponseEntity.ok(
                racketRepository.findAll()
        );
    }



    // Get racket by id
    public ResponseEntity<Object> getRacketById(Integer id) {


        Racket racket = racketRepository.findById(id)
                .orElse(null);


        if(racket == null){

            return ResponseEntity.status(404)
                    .body("Racket not found");
        }


        return ResponseEntity.ok(racket);
    }



    // Search by brand
    public ResponseEntity<Object> getByBrand(String brand){

        return ResponseEntity.ok(
                racketRepository.findByBrand(brand)
        );
    }



    // Add racket
    public ResponseEntity<Object> addRacket(Racket racket){


        if(racket.getTitle() == null ||
                racket.getTitle().isBlank()){

            return ResponseEntity.badRequest()
                    .body("Title cannot be empty");
        }


        if(racket.getPrice() == null){

            return ResponseEntity.badRequest()
                    .body("Price cannot be empty");
        }


        if(racket.getStock() == null){

            return ResponseEntity.badRequest()
                    .body("Stock cannot be empty");
        }


        Racket savedRacket = racketRepository.save(racket);


        return ResponseEntity.ok(savedRacket);
    }



    // Update racket
    public ResponseEntity<Object> updateRacket(
            Integer id,
            Racket racket){


        Racket existing = racketRepository.findById(id)
                .orElse(null);


        if(existing == null){

            return ResponseEntity.status(404)
                    .body("Racket not found");
        }


        existing.setTitle(racket.getTitle());
        existing.setBrand(racket.getBrand());
        existing.setDescription(racket.getDescription());
        existing.setPrice(racket.getPrice());
        existing.setStock(racket.getStock());
        existing.setImage(racket.getImage());


        return ResponseEntity.ok(
                racketRepository.save(existing)
        );
    }



    // Delete racket
    public ResponseEntity<Object> deleteRacket(Integer id){


        if(!racketRepository.existsById(id)){

            return ResponseEntity.status(404)
                    .body("Racket not found");
        }


        racketRepository.deleteById(id);


        return ResponseEntity.ok(
                "Racket deleted successfully"
        );
    }
}
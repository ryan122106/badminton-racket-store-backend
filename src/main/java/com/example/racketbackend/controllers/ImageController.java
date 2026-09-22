package com.example.racketbackend.controllers;

import com.example.racketbackend.services.ImageService;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.Map;

@RestController
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping("/images/upload")
    public ResponseEntity<Object> uploadImage(
            @RequestParam("image") MultipartFile image) {

        try {
            String filename = imageService.uploadImage(image);

            String imageUrl = ServletUriComponentsBuilder
                    .fromCurrentContextPath()
                    .path("/images/")
                    .path(filename)
                    .toUriString();

            return ResponseEntity.ok(
                    Map.of("imageUrl", imageUrl)
            );

        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest()
                    .body(exception.getMessage());

        } catch (IOException exception) {
            return ResponseEntity.internalServerError()
                    .body("Failed to upload image");
        }
    }

    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<Resource> getImage(
            @PathVariable String filename) {

        try {
            Resource image = imageService.getImage(filename);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(image);

        } catch (IOException | IllegalArgumentException exception) {
            return ResponseEntity.notFound().build();
        }
    }
}
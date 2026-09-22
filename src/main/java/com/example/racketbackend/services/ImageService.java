package com.example.racketbackend.services;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class ImageService {

    private final Path uploadDirectory =
            Paths.get("uploads").toAbsolutePath().normalize();

    public ImageService() throws IOException {
        Files.createDirectories(uploadDirectory);
    }

    public String uploadImage(MultipartFile image)
            throws IOException {

        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException(
                    "Image cannot be empty"
            );
        }

        String contentType = image.getContentType();

        if (contentType == null ||
                !contentType.startsWith("image/")) {

            throw new IllegalArgumentException(
                    "Only image files are allowed"
            );
        }

        String originalFilename = image.getOriginalFilename();
        String extension = "";

        if (originalFilename != null &&
                originalFilename.contains(".")) {

            extension = originalFilename.substring(
                    originalFilename.lastIndexOf(".")
            );
        }

        String filename = UUID.randomUUID() + extension;

        Path destination = uploadDirectory
                .resolve(filename)
                .normalize();

        Files.copy(
                image.getInputStream(),
                destination,
                StandardCopyOption.REPLACE_EXISTING
        );

        return filename;
    }

    public Resource getImage(String filename)
            throws IOException {

        Path imagePath = uploadDirectory
                .resolve(filename)
                .normalize();

        // Prevent access outside the uploads folder
        if (!imagePath.startsWith(uploadDirectory)) {
            throw new IllegalArgumentException(
                    "Invalid filename"
            );
        }

        Resource resource = new UrlResource(
                imagePath.toUri()
        );

        if (!resource.exists() || !resource.isReadable()) {
            throw new NoSuchFileException(
                    "Image not found"
            );
        }

        return resource;
    }
}
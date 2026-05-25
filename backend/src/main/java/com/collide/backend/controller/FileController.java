package com.collide.backend.controller;

import com.collide.backend.dto.FileUploadDto;
import com.collide.backend.service.FileStorageService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/uploads")
public class FileController {
    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public FileUploadDto uploadImage(@RequestParam("file") MultipartFile file) {
        return fileStorageService.saveImage(file);
    }
}

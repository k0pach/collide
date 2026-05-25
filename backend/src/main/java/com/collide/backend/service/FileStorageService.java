package com.collide.backend.service;

import com.collide.backend.dto.FileUploadDto;
import com.collide.backend.exception.BadRequestException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");
    private final Path uploadPath;

    public FileStorageService(@Value("${collide.upload-dir:uploads}") String uploadDir) {
        this.uploadPath = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    public FileUploadDto saveImage(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new BadRequestException("Файл не выбран");
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new BadRequestException("Допустимы только изображения JPEG, PNG, WEBP или GIF");
        }
        try {
            Files.createDirectories(uploadPath);
            String extension = extension(file.getOriginalFilename(), contentType);
            String filename = UUID.randomUUID() + extension;
            Path target = uploadPath.resolve(filename).normalize();
            if (!target.startsWith(uploadPath)) throw new BadRequestException("Некорректное имя файла");
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return new FileUploadDto(filename, "/uploads/" + filename, contentType, file.getSize());
        } catch (IOException ex) {
            throw new BadRequestException("Не удалось сохранить файл");
        }
    }

    private String extension(String originalFilename, String contentType) {
        String clean = StringUtils.cleanPath(originalFilename == null ? "" : originalFilename);
        int dot = clean.lastIndexOf('.');
        if (dot >= 0 && dot < clean.length() - 1) {
            String ext = clean.substring(dot).toLowerCase(Locale.ROOT);
            if (Set.of(".jpg", ".jpeg", ".png", ".webp", ".gif").contains(ext)) return ext;
        }
        return switch (contentType.toLowerCase(Locale.ROOT)) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> ".jpg";
        };
    }
}

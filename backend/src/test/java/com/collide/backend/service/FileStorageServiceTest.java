package com.collide.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.collide.backend.dto.FileUploadDto;
import com.collide.backend.exception.BadRequestException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class FileStorageServiceTest {

    @TempDir
    Path tempDir;

    @Mock
    private MultipartFile file;

    private FileStorageService service;

    @BeforeEach
    void setUp() {
        service = new FileStorageService(tempDir.toString());
    }

    @Test
    void saveImageThrowsWhenFileMissingOrEmpty() {
        assertThatThrownBy(() -> service.saveImage(null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Файл не выбран");

        when(file.isEmpty()).thenReturn(true);
        assertThatThrownBy(() -> service.saveImage(file))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Файл не выбран");
    }

    @Test
    void saveImageThrowsForUnsupportedContentType() {
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("text/plain");

        assertThatThrownBy(() -> service.saveImage(file))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Допустимы только изображения JPEG, PNG, WEBP или GIF");

        when(file.getContentType()).thenReturn(null);
        assertThatThrownBy(() -> service.saveImage(file))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Допустимы только изображения JPEG, PNG, WEBP или GIF");
    }

    @Test
    void saveImageStoresFileWithExtensionFromOriginalName() throws Exception {
        byte[] content = "image-bytes".getBytes(StandardCharsets.UTF_8);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getOriginalFilename()).thenReturn("photo.JPEG");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(content));
        when(file.getSize()).thenReturn((long) content.length);

        FileUploadDto dto = service.saveImage(file);

        assertThat(dto.contentType()).isEqualTo("image/jpeg");
        assertThat(dto.size()).isEqualTo(content.length);
        assertThat(dto.fileName()).endsWith(".jpeg");
        assertThat(dto.url()).isEqualTo("/uploads/" + dto.fileName());
        assertThat(Files.readAllBytes(tempDir.resolve(dto.fileName()))).isEqualTo(content);
    }

    @Test
    void saveImageUsesExtensionFromContentTypeWhenFilenameHasNoAllowedExtension() throws Exception {
        byte[] content = "abc".getBytes(StandardCharsets.UTF_8);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("image/webp");
        when(file.getOriginalFilename()).thenReturn("archive.txt");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(content));
        when(file.getSize()).thenReturn((long) content.length);

        FileUploadDto dto = service.saveImage(file);

        assertThat(dto.fileName()).endsWith(".webp");
    }

    @Test
    void saveImageUsesDefaultJpgExtensionForUnknownNameAndJpegType() throws Exception {
        byte[] content = "abc".getBytes(StandardCharsets.UTF_8);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("IMAGE/JPEG");
        when(file.getOriginalFilename()).thenReturn("file");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(content));
        when(file.getSize()).thenReturn((long) content.length);

        FileUploadDto dto = service.saveImage(file);

        assertThat(dto.fileName()).endsWith(".jpg");
    }

    @Test
    void saveImageUsesGifAndPngFallbackExtensions() throws Exception {
        byte[] content = "xyz".getBytes(StandardCharsets.UTF_8);

        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("image/gif");
        when(file.getOriginalFilename()).thenReturn(null);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(content));
        when(file.getSize()).thenReturn((long) content.length);
        FileUploadDto gifDto = service.saveImage(file);
        assertThat(gifDto.fileName()).endsWith(".gif");

        when(file.getContentType()).thenReturn("image/png");
        when(file.getOriginalFilename()).thenReturn("name.");
        FileUploadDto pngDto = service.saveImage(file);
        assertThat(pngDto.fileName()).endsWith(".png");
    }

    @Test
    void saveImageThrowsWhenIoFails() throws Exception {
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("image/png");
        when(file.getOriginalFilename()).thenReturn("img.png");
        when(file.getInputStream()).thenThrow(new IOException("disk error"));

        assertThatThrownBy(() -> service.saveImage(file))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Не удалось сохранить файл");
    }
}

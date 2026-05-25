package com.collide.backend.dto;

public record FileUploadDto(String fileName, String url, String contentType, long size) {}

package com.urlshortener.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class ShortenUrlRequest {
    @NotBlank(message = "URL é obrigatória")
    private String originalUrl;

    @Min(value = 1, message = "Dias de expiração deve ser no mínimo 1")
    @Max(value = 365, message = "Dias de expiração deve ser no máximo 365")
    private Integer expirationDays;

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public Integer getExpirationDays() {
        return expirationDays;
    }

    public void setExpirationDays(Integer expirationDays) {
        this.expirationDays = expirationDays;
    }

    // Construtores, getters e setters...
}
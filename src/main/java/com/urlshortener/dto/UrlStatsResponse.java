package com.urlshortener.dto;

import java.time.LocalDateTime;

public class UrlStatsResponse {
    private String shortCode;
    private String originalUrl;
    private Long clickCount;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private Boolean isActive;

    public UrlStatsResponse(String shortCode, String originalUrl, Long clickCount, java.time.LocalDateTime createdAt, java.time.LocalDateTime expiresAt, Boolean isActive) {
        this.shortCode = shortCode;
        this.originalUrl = originalUrl;
        this.clickCount = clickCount;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.isActive = isActive;
    }

    public String getShortCode() {
        return shortCode;
    }
    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }
    public String getOriginalUrl() {
        return originalUrl;
    }
    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }
    public Long getClickCount() {
        return clickCount;
    }
    public void setClickCount(Long clickCount) {
        this.clickCount = clickCount;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    public Boolean getIsActive() {
        return isActive;
    }
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    // Construtores, getters e setters...
}
package com.urlshortener.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@Table(name = "urls")
public class UrlEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "short_code", unique = true, nullable = false, length = 10)
    private String shortCode;
    
    @Column(name = "original_url", nullable = false, length = 2000)
    @NotBlank(message = "URL original é obrigatória")
    private String originalUrl;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(name = "click_count", nullable = false)
    private Long clickCount = 0L;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "created_by_ip", length = 45)
    private String createdByIp;
    
    // Construtores
    public UrlEntity() {}
    
    public UrlEntity(String shortCode, String originalUrl, String createdByIp) {
        this.shortCode = shortCode;
        this.originalUrl = originalUrl;
        this.createdByIp = createdByIp;
        this.createdAt = LocalDateTime.now();
        this.clickCount = 0L;
        this.isActive = true;
    }
    
    // Getters e Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
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
    
    public Long getClickCount() {
        return clickCount;
    }
    
    public void setClickCount(Long clickCount) {
        this.clickCount = clickCount;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public String getCreatedByIp() {
        return createdByIp;
    }
    
    public void setCreatedByIp(String createdByIp) {
        this.createdByIp = createdByIp;
    }
    
    // Métodos auxiliares
    public void incrementClickCount() {
        this.clickCount++;
    }
    
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
}
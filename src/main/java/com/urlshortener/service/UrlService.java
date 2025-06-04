package com.urlshortener.service;

import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.urlshortener.dto.ShortenUrlRequest;
import com.urlshortener.dto.UrlStatsResponse;
import com.urlshortener.model.UrlEntity;
import com.urlshortener.repository.UrlRepository;

@Service
public class UrlService {
    
    @Autowired
    private UrlRepository urlRepository;
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    private static final int SHORT_CODE_LENGTH = 6;
    private static final String CACHE_PREFIX = "url:";
    
    @Transactional
    public UrlEntity shortenUrl(ShortenUrlRequest request, String clientIp) {
        // Validar URL
        if (!isValidUrl(request.getOriginalUrl())) {
            throw new IllegalArgumentException("URL inválida");
        }

        // Gerar código curto único
        String shortCode = generateUniqueShortCode();

        // Criar entidade
        UrlEntity urlEntity = new UrlEntity(shortCode, request.getOriginalUrl(), clientIp);

        // Definir expiração se especificada
        if (request.getExpirationDays() != null && request.getExpirationDays() > 0) {
            urlEntity.setExpiresAt(LocalDateTime.now().plusDays(request.getExpirationDays()));
        }

        // Salvar no banco
        UrlEntity savedEntity = urlRepository.save(savedEntity);

        // Tentar cachear no Redis (não falha se Redis estiver indisponível)
        cacheUrl(shortCode, request.getOriginalUrl());

        return savedEntity;
    }

    @Transactional
    public String getOriginalUrl(String shortCode) {
        // Tentar buscar no cache primeiro (falha graciosamente)
        String cachedUrl = getCachedUrl(shortCode);
        if (cachedUrl != null) {
            // Incrementar contador assincronamente
            try {
                urlRepository.incrementClickCount(shortCode);
            } catch (Exception e) {
                System.err.println("Erro ao incrementar contador: " + e.getMessage());
            }
            return cachedUrl;
        }
        
        // Buscar no banco
        Optional<UrlEntity> urlEntity = urlRepository.findByShortCodeAndIsActiveTrue(shortCode);
        
        if (urlEntity.isEmpty()) {
            throw new IllegalArgumentException("URL não encontrada ou expirada");
        }
        
        UrlEntity entity = urlEntity.get();
        
        // Verificar se expirou
        if (entity.isExpired()) {
            entity.setIsActive(false);
            urlRepository.save(entity);
            throw new IllegalArgumentException("URL expirada");
        }
        
        // Incrementar contador
        entity.incrementClickCount();
        urlRepository.save(entity);
        
        // Tentar atualizar cache
        cacheUrl(shortCode, entity.getOriginalUrl());
        
        return entity.getOriginalUrl();
    }
    
    @Cacheable(value = "urlStats", key = "#shortCode")
    public UrlStatsResponse getUrlStats(String shortCode) {
        Optional<UrlEntity> urlEntity = urlRepository.findByShortCodeAndIsActiveTrue(shortCode);
        
        if (urlEntity.isEmpty()) {
            throw new IllegalArgumentException("URL não encontrada");
        }
        
        UrlEntity entity = urlEntity.get();
        return new UrlStatsResponse(
            entity.getShortCode(),
            entity.getOriginalUrl(),
            entity.getClickCount(),
            entity.getCreatedAt(),
            entity.getExpiresAt(),
            entity.getIsActive()
        );
    }
    
    @SuppressWarnings("deprecation")
    private String generateUniqueShortCode() {
        String shortCode;
        int attempts = 0;
        
        do {
            shortCode = RandomStringUtils.randomAlphanumeric(SHORT_CODE_LENGTH);
            attempts++;
            
            if (attempts > 10) {
                throw new RuntimeException("Não foi possível gerar código único");
            }
        } while (urlRepository.existsByShortCode(shortCode));
        
        return shortCode;
    }
    
    private boolean isValidUrl(String url) {
        try {
            java.net.URI uri = new java.net.URI(url);
            return (uri.getScheme() != null) &&
                   (uri.getScheme().equals("http") || uri.getScheme().equals("https"));
        } catch (URISyntaxException e) {
            return false;
        }
    }
    
    private void cacheUrl(String shortCode, String originalUrl) {
        try {
            redisTemplate.opsForValue().set(
                CACHE_PREFIX + shortCode, 
                originalUrl, 
                24, 
                TimeUnit.HOURS
            );
        } catch (Exception e) {
            // Log erro mas não falha a operação - Redis é opcional
            System.err.println("Redis indisponível, continuando sem cache: " + e.getMessage());
        }
    }
    
    private String getCachedUrl(String shortCode) {
        try {
            return redisTemplate.opsForValue().get(CACHE_PREFIX + shortCode);
        } catch (Exception e) {
            // Log erro mas não falha a operação - Redis é opcional
            System.err.println("Redis indisponível para leitura: " + e.getMessage());
            return null;
        }
    }
    
    public long getTotalActiveUrls() {
        try {
            return urlRepository.countActiveUrls();
        } catch (Exception e) {
            System.err.println("Erro ao contar URLs ativas: " + e.getMessage());
            return 0L;
        }
    }
    
    public Long getTotalClicks() {
        try {
            Long total = urlRepository.getTotalClicks();
            return total != null ? total : 0L;
        } catch (Exception e) {
            System.err.println("Erro ao contar cliques: " + e.getMessage());
            return 0L;
        }
    }
    
    @Transactional
    public int cleanupExpiredUrls() {
        try {
            return urlRepository.deactivateExpiredUrls(LocalDateTime.now());
        } catch (Exception e) {
            System.err.println("Erro na limpeza de URLs: " + e.getMessage());
            return 0;
        }
    }
}
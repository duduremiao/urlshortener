package com.urlshortener.repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.urlshortener.model.UrlEntity;

@Repository
public interface UrlRepository extends JpaRepository<UrlEntity, Long> {
    
    /**
     * Busca URL pelo código curto
     */
    Optional<UrlEntity> findByShortCodeAndIsActiveTrue(String shortCode);
    
    /**
     * Verifica se código curto já existe
     */
    boolean existsByShortCode(String shortCode);
    
    /**
     * Busca URLs criadas por IP
     */
    List<UrlEntity> findByCreatedByIpOrderByCreatedAtDesc(String ip);
    
    /**
     * Busca URLs mais acessadas
     */
    @Query("SELECT u FROM UrlEntity u WHERE u.isActive = true ORDER BY u.clickCount DESC")
    List<UrlEntity> findTopUrlsByClickCount();
    
    /**
     * Busca URLs expiradas
     */
    @Query("SELECT u FROM UrlEntity u WHERE u.expiresAt IS NOT NULL AND u.expiresAt < :now")
    List<UrlEntity> findExpiredUrls(@Param("now") LocalDateTime now);
    
    /**
     * Incrementa contador de cliques
     */
    @Modifying
    @Query("UPDATE UrlEntity u SET u.clickCount = u.clickCount + 1 WHERE u.shortCode = :shortCode")
    void incrementClickCount(@Param("shortCode") String shortCode);
    
    /**
     * Desativa URLs expiradas
     */
    @Modifying
    @Query("UPDATE UrlEntity u SET u.isActive = false WHERE u.expiresAt IS NOT NULL AND u.expiresAt < :now")
    int deactivateExpiredUrls(@Param("now") LocalDateTime now);
    
    /**
     * Estatísticas gerais
     */
    @Query("SELECT COUNT(u) FROM UrlEntity u WHERE u.isActive = true")
    long countActiveUrls();
    
    @Query("SELECT SUM(u.clickCount) FROM UrlEntity u WHERE u.isActive = true")
    Long getTotalClicks();
    
    /**
     * URLs criadas hoje
     */
    @Query("SELECT COUNT(u) FROM UrlEntity u WHERE u.createdAt >= :startOfDay AND u.createdAt < :endOfDay")
    long countUrlsCreatedToday(@Param("startOfDay") LocalDateTime startOfDay, 
                              @Param("endOfDay") LocalDateTime endOfDay);
}
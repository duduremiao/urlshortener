package com.urlshortener.scheduled;

import com.urlshortener.service.UrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasks {

    @Autowired
    private UrlService urlService;

    /**
     * Executa limpeza de URLs expiradas todos os dias às 2:00 AM
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredUrls() {
        try {
            int cleanedUrls = urlService.cleanupExpiredUrls();
            System.out.println("Limpeza automática executada: " + cleanedUrls + " URLs desativadas");
        } catch (Exception e) {
            System.err.println("Erro durante limpeza automática: " + e.getMessage());
        }
    }

    /**
     * Log de estatísticas a cada hora
     */
    @Scheduled(fixedRate = 3600000) // 1 hora em millisegundos
    public void logStatistics() {
        try {
            long totalUrls = urlService.getTotalActiveUrls();
            long totalClicks = urlService.getTotalClicks();
            
            System.out.println("=== ESTATÍSTICAS ===");
            System.out.println("URLs ativas: " + totalUrls);
            System.out.println("Total de cliques: " + totalClicks);
            System.out.println("==================");
            
        } catch (Exception e) {
            System.err.println("Erro ao coletar estatísticas: " + e.getMessage());
        }
    }
}
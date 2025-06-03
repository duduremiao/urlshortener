package com.urlshortener.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.urlshortener.dto.ShortenUrlRequest;
import com.urlshortener.dto.ShortenUrlResponse;
import com.urlshortener.dto.UrlStatsResponse;
import com.urlshortener.model.UrlEntity;
import com.urlshortener.service.UrlService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@CrossOrigin(origins = "*")
public class UrlController {
    
    @Autowired
    private UrlService urlService;
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;
    
    /**
     * Endpoint para encurtar URL
     */
    @PostMapping("/api/shorten")
    public ResponseEntity<?> shortenUrl(@Valid @RequestBody ShortenUrlRequest request,
                                       HttpServletRequest httpRequest) {
        try {
            String clientIp = getClientIpAddress(httpRequest);
            UrlEntity urlEntity = urlService.shortenUrl(request, clientIp);
            
            ShortenUrlResponse response = new ShortenUrlResponse(
                urlEntity.getShortCode(),
                baseUrl + "/" + urlEntity.getShortCode(),
                urlEntity.getOriginalUrl(),
                urlEntity.getCreatedAt(),
                urlEntity.getExpiresAt()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erro interno do servidor"));
        }
    }
    
    /**
     * Endpoint para redirecionamento
     */
    @GetMapping("/{shortCode}")
    public void redirectToOriginalUrl(@PathVariable String shortCode,
                                     HttpServletResponse response) throws IOException {
        try {
            String originalUrl = urlService.getOriginalUrl(shortCode);
            response.sendRedirect(originalUrl);
            
        } catch (IllegalArgumentException e) {
            response.sendError(HttpStatus.NOT_FOUND.value(), "URL não encontrada");
        } catch (IOException e) {
            response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Erro interno");
        }
    }
    
    /**
     * Endpoint para estatísticas da URL
     */
    @GetMapping("/api/stats/{shortCode}")
    public ResponseEntity<?> getUrlStats(@PathVariable String shortCode) {
        try {
            UrlStatsResponse stats = urlService.getUrlStats(shortCode);
            return ResponseEntity.ok(stats);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erro interno do servidor"));
        }
    }
    
    /**
     * Endpoint para estatísticas gerais
     */
    @GetMapping("/api/stats")
    public ResponseEntity<Map<String, Object>> getGeneralStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalActiveUrls", urlService.getTotalActiveUrls());
            stats.put("totalClicks", urlService.getTotalClicks());
            stats.put("serverStatus", "online");
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erro interno do servidor"));
        }
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/api/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("timestamp", java.time.Instant.now().toString());
        return ResponseEntity.ok(status);
    }
    
    /**
     * Endpoint para limpeza de URLs expiradas (admin)
     */
    @PostMapping("/api/admin/cleanup")
    public ResponseEntity<Map<String, Object>> cleanupExpiredUrls() {
        try {
            int cleanedCount = urlService.cleanupExpiredUrls();
            Map<String, Object> result = new HashMap<>();
            result.put("cleanedUrls", cleanedCount);
            result.put("message", "Limpeza concluída com sucesso");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erro durante limpeza"));
        }
    }
    
    /**
     * Página inicial simples
     */
    @GetMapping("/")
    public ResponseEntity<String> home() {
        String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>URL Shortener</title>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; max-width: 800px; margin: 50px auto; padding: 20px; }
                    .container { text-align: center; }
                    input, button { padding: 10px; margin: 5px; }
                    input[type="url"] { width: 300px; }
                    .result { margin-top: 20px; padding: 10px; background: #f0f0f0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>🔗 URL Shortener</h1>
                    <p>Encurte suas URLs de forma rápida e segura!</p>
                    
                    <div>
                        <input type="url" id="urlInput" placeholder="Cole sua URL aqui..." />
                        <button onclick="shortenUrl()">Encurtar</button>
                    </div>
                    
                    <div id="result" class="result" style="display:none;"></div>
                    
                    <div style="margin-top: 40px;">
                        <h3>API Endpoints:</h3>
                        <p><strong>POST</strong> /api/shorten - Encurtar URL</p>
                        <p><strong>GET</strong> /{shortCode} - Redirecionamento</p>
                        <p><strong>GET</strong> /api/stats/{shortCode} - Estatísticas</p>
                    </div>
                </div>
                
                <script>
                async function shortenUrl() {
                    const url = document.getElementById('urlInput').value;
                    if (!url) {
                        alert('Por favor, insira uma URL válida');
                        return;
                    }
                    
                    try {
                        const response = await fetch('/api/shorten', {
                            method: 'POST',
                            headers: { 'Content-Type': 'application/json' },
                            body: JSON.stringify({ originalUrl: url })
                        });
                        
                        const data = await response.json();
                        
                        if (response.ok) {
                            document.getElementById('result').innerHTML = `
                                <h3>✅ URL Encurtada com Sucesso!</h3>
                                <p><strong>URL Original:</strong> ${data.originalUrl}</p>
                                <p><strong>URL Encurtada:</strong> <a href="${data.shortUrl}" target="_blank">${data.shortUrl}</a></p>
                                <p><strong>Código:</strong> ${data.shortCode}</p>
                                <button onclick="copyToClipboard('${data.shortUrl}')">Copiar Link</button>
                            `;
                            document.getElementById('result').style.display = 'block';
                        } else {
                            throw new Error(data.error || 'Erro desconhecido');
                        }
                    } catch (error) {
                        document.getElementById('result').innerHTML = `
                            <h3>❌ Erro</h3>
                            <p>${error.message}</p>
                        `;
                        document.getElementById('result').style.display = 'block';
                    }
                }
                
                function copyToClipboard(text) {
                    navigator.clipboard.writeText(text).then(() => {
                        alert('Link copiado para a área de transferência!');
                    });
                }
                </script>
            </body>
            </html>
            """;
        
        return ResponseEntity.ok()
            .header("Content-Type", "text/html; charset=UTF-8")
            .body(html);
    }
    
    /**
     * Extrai o IP real do cliente
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
                
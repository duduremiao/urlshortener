# Dockerfile para URL Shortener
FROM openjdk:17-jdk-slim

LABEL maintainer="URL Shortener App"
LABEL version="1.0"
LABEL description="Cloud URL Shortener with Redis Cache"

# Instalar curl para health check
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Definir diretório de trabalho
WORKDIR /app

# Copiar arquivo JAR (usar nome específico)
COPY target/cloud-url-shortener-0.0.1-SNAPSHOT.jar app.jar

# Criar usuário não-root para segurança
RUN addgroup --system spring && adduser --system spring --ingroup spring \
    && chown -R spring:spring /app

# Expor porta
EXPOSE 8080

# Configurar JVM para container
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:+UseContainerSupport -Djava.security.egd=file:/dev/./urandom"

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/api/health || exit 1

# Executar como usuário não-root
USER spring:spring

# Comando para executar aplicação
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
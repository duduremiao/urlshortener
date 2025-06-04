# Dockerfile para URL Shortener
FROM openjdk:17-jdk-slim

LABEL maintainer="URL Shortener App"
LABEL version="1.0"
LABEL description="Cloud URL Shortener with Redis Cache"

# Definir diretório de trabalho
WORKDIR /app

# Copiar arquivo JAR
COPY target/cloud-url-shortener-*.jar app.jar

# Criar usuário não-root para segurança e garantir permissão
RUN addgroup --system spring && adduser --system spring --ingroup spring \
    && chown -R spring:spring /app
USER spring:spring

# Expor porta
EXPOSE 8080

# Configurar JVM para container
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:+UseContainerSupport"

# Health check (ajuste o endpoint se necessário)
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Comando para executar aplicação
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
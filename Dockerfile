FROM eclipse-temurin:17-jre

# Health check için curl kurulumu
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Environment variables - default profile kullan (production satırı kaldırıldı)

COPY target/*.jar app.jar

# Doğru port (8088)
EXPOSE 8088

# Health check ekle
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:8088/actuator/health || exit 1

# JVM ayarları ile connection pool optimize et
CMD ["java", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Xms256m", \
    "-Xmx512m", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-jar", \
    "app.jar"]
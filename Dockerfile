# Multi-stage build için Maven ve JDK 17 kullan
FROM --platform=$BUILDPLATFORM maven:3.9.9-eclipse-temurin-17 AS build

# Çalışma dizinini ayarla
WORKDIR /app

# POM dosyasını kopyala ve dependency'leri cache'le
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Kaynak kodları kopyala
COPY src ./src

# Uygulamayı build et (test'leri skip et - production için)
RUN mvn clean package -DskipTests -B

# Runtime stage - sadece JRE gerekli
FROM eclipse-temurin:17-jre

# Health check için curl kurulumu
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Security için non-root user oluştur
RUN groupadd -g 1001 spring && \
    useradd -u 1001 -g spring -s /bin/bash -m spring

# Çalışma dizinini ayarla
WORKDIR /app

# Build stage'den JAR dosyasını kopyala
COPY --from=build /app/target/backend-*.jar app.jar

# Dosya sahipliğini spring user'a ver
RUN chown spring:spring app.jar

# Spring user'a geç
USER spring

# Health check ekle
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8088/actuator/health || exit 1

# Port'u expose et
EXPOSE 8088

# JVM parametreleri ile uygulamayı başlat
ENTRYPOINT ["java", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-Dspring.profiles.active=docker", \
    "-Xms512m", \
    "-Xmx1024m", \
    "-jar", \
    "app.jar"] 
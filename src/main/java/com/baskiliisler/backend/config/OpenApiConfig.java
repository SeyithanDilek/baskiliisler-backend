package com.baskiliisler.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Baskılı İşler API")
                        .description("""
                            ## Baskılı İşler Backend REST API
                            
                            Bu API aşağıdaki temel özellikleri sağlar:
                            - 🏭 **Marka Yönetimi** - Brand Management 
                            - 📋 **Teklif Yönetimi** - Quote Management
                            - 📦 **Sipariş Yönetimi** - Order Management
                            - 🏭 **Fabrika Yönetimi** - Factory Management
                            - 👤 **Kullanıcı Yönetimi** - User Management
                            - 🔐 **Kimlik Doğrulama** - Authentication
                            
                            ### Kimlik Doğrulama
                            Tüm API endpoint'leri JWT token ile korunmaktadır.
                            """)
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Baskılı İşler Team")
                                .email("info@baskiliisler.com")
                                .url("https://www.baskiliisler.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(getServerList())
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT token gereklidir. Format: Bearer {token}")))
                .addSecurityItem(new SecurityRequirement()
                        .addList("Bearer Authentication"));
    }
    
    private List<Server> getServerList() {
        return List.of(
                new Server()
                        .url("https://baskili-isler-backend.onrender.com")
                        .description("🌐 Production Server")
        );
    }
} 
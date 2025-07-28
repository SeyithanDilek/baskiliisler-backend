package com.baskiliisler.backend.config;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {

    // @Value("${cloudinary.cloud-name}")
    private String cloudName = "dowcee4sj";

    // @Value("${cloudinary.api-key}")
    private String apiKey = "761866239858883";

    // @Value("${cloudinary.api-secret}")
    private String apiSecret = "W5rAQ9nTpaB-b6jJMryCgatmPa0";

    @Bean
    public Cloudinary cloudinary() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", cloudName);
        config.put("api_key", apiKey);
        config.put("api_secret", apiSecret);
        
        return new Cloudinary(config);
    }
} 
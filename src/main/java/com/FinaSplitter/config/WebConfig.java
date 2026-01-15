package com.FinaSplitter.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Pozwól na wszystkie endpointy
                .allowedOrigins("*") // W produkcji tu wpiszesz konkretny adres
                .allowedMethods("GET", "POST", "PUT", "DELETE");
    }
}
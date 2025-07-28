package it.uniroma3.cyberlab.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // ✅ Handler per avatar uploads
        String uploadPath = Paths.get("uploads").toAbsolutePath().toUri().toString();
        
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath)
                .setCachePeriod(3600); // Cache per 1 ora
        
        System.out.println("✅ Avatar uploads configured: " + uploadPath);
    }
}
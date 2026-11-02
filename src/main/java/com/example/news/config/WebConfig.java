package com.example.news.config;

import com.example.news.service.ImageStorageService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Раздаёт загруженные картинки по адресу /uploads/**.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final ImageStorageService images;

    public WebConfig(ImageStorageService images) {
        this.images = images;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(ImageStorageService.URL_PREFIX + "**")
                .addResourceLocations(images.getDir().toUri().toString());
    }
}

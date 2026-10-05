package com.partshub.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Global Spring MVC configuration.
 * Add shared MVC settings here: CORS policies, resource handlers, etc.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final ModuleSecurityInterceptor moduleSecurityInterceptor;

    public WebConfig(ModuleSecurityInterceptor moduleSecurityInterceptor) {
        this.moduleSecurityInterceptor = moduleSecurityInterceptor;
    }

    @Override
    public void addInterceptors(org.springframework.web.servlet.config.annotation.InterceptorRegistry registry) {
        registry.addInterceptor(moduleSecurityInterceptor)
                .addPathPatterns("/api/**");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // No root redirect; LoginController handles "/" now.
    }

    @Override
    public void addResourceHandlers(org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry registry) {
        java.nio.file.Path baseDir = java.nio.file.Paths.get("src", "main", "resources", "static", "images").toAbsolutePath().normalize();
        if (!java.nio.file.Files.exists(baseDir)) {
            java.nio.file.Path altDir = java.nio.file.Paths.get("Project", "src", "main", "resources", "static", "images").toAbsolutePath().normalize();
            if (java.nio.file.Files.exists(altDir)) {
                baseDir = altDir;
            }
        }

        java.nio.file.Path productsDir = baseDir.resolve("products");
        try {
            if (!java.nio.file.Files.exists(productsDir)) {
                java.nio.file.Files.createDirectories(productsDir);
            }
        } catch (Exception ignored) {
        }

        String baseUri = baseDir.toUri().toString();
        if (!baseUri.endsWith("/")) baseUri += "/";
        String productsUri = productsDir.toUri().toString();
        if (!productsUri.endsWith("/")) productsUri += "/";

        // Map /images/products/** so runtime uploads & disk files appear immediately
        registry.addResourceHandler("/images/products/**")
                .addResourceLocations(productsUri, "file:src/main/resources/static/images/products/", "file:Project/src/main/resources/static/images/products/", "classpath:/static/images/products/")
                .setCachePeriod(0);

        // Map /images/** for root static images (slides, login-bg, placeholder)
        registry.addResourceHandler("/images/**")
                .addResourceLocations(baseUri, "file:src/main/resources/static/images/", "file:Project/src/main/resources/static/images/", "classpath:/static/images/")
                .setCachePeriod(0);
    }
}

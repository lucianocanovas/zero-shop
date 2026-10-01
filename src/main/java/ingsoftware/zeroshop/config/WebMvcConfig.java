package ingsoftware.zeroshop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:uploads/products}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Exponer la carpeta de uploads como recurso estático vía HTTP
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        String uploadUri = uploadPath.toUri().toString();
        if (!uploadUri.endsWith("/")) {
            uploadUri += "/";
        }

        registry.addResourceHandler("/uploads/products/**")
                .addResourceLocations(uploadUri);

        // Mapeo general para cualquier subcarpeta bajo uploads
        Path baseUploadPath = uploadPath.getParent() != null ? uploadPath.getParent() : uploadPath;
        String baseUploadUri = baseUploadPath.toUri().toString();
        if (!baseUploadUri.endsWith("/")) {
            baseUploadUri += "/";
        }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(baseUploadUri);
    }
}

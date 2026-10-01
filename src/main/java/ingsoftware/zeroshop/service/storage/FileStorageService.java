package ingsoftware.zeroshop.service.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    private static final List<String> ALLOWED_EXTENSIONS = List.of(
            ".jpg", ".jpeg", ".png", ".webp", ".gif", ".svg"
    );

    private static final List<String> ALLOWED_MIME_TYPES = List.of(
            "image/jpeg", "image/png", "image/webp", "image/gif", "image/svg+xml"
    );

    @Value("${app.upload.dir:uploads/products}")
    private String uploadDir;

    private Path rootLocation;

    @PostConstruct
    public void init() {
        try {
            this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(this.rootLocation);
            log.info("Directorio de almacenamiento de archivos inicializado en: {}", this.rootLocation);
        } catch (IOException e) {
            log.error("No se pudo inicializar el directorio de almacenamiento: {}", uploadDir, e);
            throw new RuntimeException("No se pudo inicializar el almacenamiento de archivos", e);
        }
    }

    /**
     * Almacena una imagen de producto subida desde la PC y retorna la URL relativa accesible vía web.
     */
    public String storeProductImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("El archivo subido no tiene un nombre válido.");
        }

        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalFilename.substring(dotIndex).toLowerCase();
        }

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Extensión de archivo no permitida (" + extension + "). Se admiten: JPG, JPEG, PNG, WEBP, GIF, SVG.");
        }

        String contentType = file.getContentType();
        if (contentType != null && !contentType.isBlank() && !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Tipo de contenido de archivo no permitido (" + contentType + "). Solo se admiten imágenes.");
        }

        // Generar un nombre único y seguro
        String cleanBaseName = originalFilename.substring(0, dotIndex > 0 ? dotIndex : originalFilename.length())
                .replaceAll("[^a-zA-Z0-9_-]", "_");
        if (cleanBaseName.length() > 30) {
            cleanBaseName = cleanBaseName.substring(0, 30);
        }
        String uniqueFilename = "prod-" + UUID.randomUUID().toString().substring(0, 8) + "-" + cleanBaseName + extension;

        try {
            if (this.rootLocation == null) {
                init();
            }
            Path destinationFile = this.rootLocation.resolve(uniqueFilename).normalize();

            // Verificación de seguridad de ruta (Path Traversal Protection)
            if (!destinationFile.getParent().equals(this.rootLocation)) {
                throw new SecurityException("No se puede almacenar el archivo fuera del directorio designado.");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            log.info("Imagen de producto almacenada con éxito: {}", uniqueFilename);
            return "/uploads/products/" + uniqueFilename;
        } catch (IOException e) {
            log.error("Error al guardar la imagen del producto {}", originalFilename, e);
            throw new RuntimeException("Error al almacenar la imagen en el servidor.", e);
        }
    }

    public Path getRootLocation() {
        return rootLocation;
    }
}

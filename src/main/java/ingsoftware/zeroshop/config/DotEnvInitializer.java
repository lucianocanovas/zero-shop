package ingsoftware.zeroshop.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Inicializador de contexto que carga de forma segura las variables desde el archivo .env
 * al entorno de Spring Boot al arrancar la aplicación o al ejecutar tests,
 * evitando que credenciales privadas queden expuestas en application.properties o en el repositorio.
 */
public class DotEnvInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final Logger log = LoggerFactory.getLogger(DotEnvInitializer.class);

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        Path envPath = Path.of(".env");
        if (!Files.exists(envPath)) {
            envPath = Path.of("../.env");
        }

        if (Files.exists(envPath)) {
            try {
                Map<String, Object> envProperties = new HashMap<>();
                List<String> lines = Files.readAllLines(envPath);
                for (String line : lines) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.startsWith("#") && line.contains("=")) {
                        int separatorIndex = line.indexOf('=');
                        String key = line.substring(0, separatorIndex).trim();
                        String value = line.substring(separatorIndex + 1).trim();

                        envProperties.put(key, value);

                        // Establecer también como System property para componentes que lo lean directamente
                        if (System.getProperty(key) == null && System.getenv(key) == null) {
                            System.setProperty(key, value);
                        }
                    }
                }

                if (!envProperties.isEmpty()) {
                    applicationContext.getEnvironment().getPropertySources().addFirst(
                            new MapPropertySource("dotEnvPropertySource", envProperties)
                    );
                    log.info("Archivo .env cargado con éxito. Se incorporaron {} variables de entorno seguras.", envProperties.size());
                }
            } catch (Exception e) {
                log.warn("No se pudo cargar el archivo .env: {}", e.getMessage());
            }
        } else {
            log.info("No se encontró archivo .env en el directorio de trabajo; se utilizarán variables del sistema.");
        }
    }
}

package ingsoftware.zeroshop.unit;

import ingsoftware.zeroshop.service.storage.FileStorageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class FileStorageServiceUnitTest {

    private FileStorageService fileStorageService;

    @TempDir
    Path tempDir;

    @BeforeEach
    public void setUp() {
        fileStorageService = new FileStorageService();
        ReflectionTestUtils.setField(fileStorageService, "uploadDir", tempDir.toString());
        fileStorageService.init();
    }

    @Test
    @DisplayName("Unit: storeProductImage con archivo nulo o vacío retorna null")
    public void testStoreProductImageNullOrEmpty() {
        assertNull(fileStorageService.storeProductImage(null));

        MockMultipartFile emptyFile = new MockMultipartFile(
                "imageFile", "empty.png", "image/png", new byte[0]
        );
        assertNull(fileStorageService.storeProductImage(emptyFile));
    }

    @Test
    @DisplayName("Unit: storeProductImage con PNG válido guarda el archivo y retorna ruta /uploads/products/...")
    public void testStoreProductImageValidPng() throws IOException {
        byte[] content = "fake image content".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "imageFile", "test-sneaker.png", "image/png", content
        );

        String resultPath = fileStorageService.storeProductImage(file);

        assertNotNull(resultPath);
        assertTrue(resultPath.startsWith("/uploads/products/prod-"));
        assertTrue(resultPath.endsWith(".png"));

        // Verificar que físicamente existe en el directorio temporal
        String filename = resultPath.replace("/uploads/products/", "");
        Path storedFile = tempDir.resolve(filename);
        assertTrue(Files.exists(storedFile));
        assertArrayEquals(content, Files.readAllBytes(storedFile));
    }

    @Test
    @DisplayName("Unit: storeProductImage con extensión no permitida (.exe, .pdf, .txt) lanza IllegalArgumentException")
    public void testStoreProductImageDisallowedExtension() {
        MockMultipartFile exeFile = new MockMultipartFile(
                "imageFile", "malware.exe", "application/octet-stream", "bad content".getBytes()
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                fileStorageService.storeProductImage(exeFile)
        );
        assertTrue(ex.getMessage().contains("Extensión de archivo no permitida"));
    }

    @Test
    @DisplayName("Unit: storeProductImage con tipo MIME incompatible lanza IllegalArgumentException")
    public void testStoreProductImageDisallowedMimeType() {
        MockMultipartFile fakeFile = new MockMultipartFile(
                "imageFile", "test.jpg", "application/pdf", "fake pdf".getBytes()
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                fileStorageService.storeProductImage(fakeFile)
        );
        assertTrue(ex.getMessage().contains("Tipo de contenido de archivo no permitido"));
    }
}

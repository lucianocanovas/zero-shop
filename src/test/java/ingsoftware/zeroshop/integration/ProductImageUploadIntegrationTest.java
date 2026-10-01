package ingsoftware.zeroshop.integration;

import ingsoftware.zeroshop.entity.catalog.Product;
import ingsoftware.zeroshop.entity.catalog.SubCategory;
import ingsoftware.zeroshop.enums.Size;
import ingsoftware.zeroshop.repository.catalog.ProductRepository;
import ingsoftware.zeroshop.repository.catalog.SubCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
public class ProductImageUploadIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SubCategoryRepository subCategoryRepository;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("Dashboard: Crear producto subiendo imagen desde la PC asigna /uploads/products/... a imageUrl")
    @WithMockUser(username = "admin@zeroshop.com", roles = {"ADMIN"})
    public void testCreateProductWithImageUpload() throws Exception {
        SubCategory subCategory = subCategoryRepository.findAllWithCategoryByDeletedFalse().stream().findFirst().orElseThrow();

        String sku = "UP-TEST-" + UUID.randomUUID().toString().substring(0, 6);
        MockMultipartFile imageFile = new MockMultipartFile(
                "imageFile", "running-shoe.jpg", "image/jpeg", "image dummy content".getBytes()
        );

        mockMvc.perform(multipart("/dashboard/products")
                        .file(imageFile)
                        .param("code", sku)
                        .param("name", "Zapatillas Upload Test")
                        .param("description", "Descripción de prueba para upload")
                        .param("size", Size.M.name())
                        .param("basePrice", "55000.00")
                        .param("subCategoryId", subCategory.getId().toString())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard/products"));

        Product created = productRepository.findByCodeAndDeletedFalse(sku).orElseThrow();
        assertNotNull(created.getImageUrl(), "La URL de la imagen no debe ser nula");
        assertTrue(created.getImageUrl().startsWith("/uploads/products/prod-"));
        assertTrue(created.getImageUrl().endsWith(".jpg"));

        // Verificar existencia física del archivo
        Path filePath = Paths.get("uploads/products/" + created.getImageUrl().replace("/uploads/products/", ""));
        assertTrue(Files.exists(filePath), "El archivo de imagen debe existir en el directorio uploads/products/");

        // Limpieza del archivo creado
        Files.deleteIfExists(filePath);
    }
}

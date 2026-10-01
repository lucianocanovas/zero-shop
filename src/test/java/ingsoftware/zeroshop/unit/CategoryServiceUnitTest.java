package ingsoftware.zeroshop.unit;

import ingsoftware.zeroshop.entity.catalog.Category;
import ingsoftware.zeroshop.repository.catalog.CategoryRepository;
import ingsoftware.zeroshop.service.catalog.CategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceUnitTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    @DisplayName("Debe crear una categoría nueva sin asignar ID manualmente para evitar fallos de persistencia JPA")
    void createCategory_NewCategory_ShouldNotPreassignId() {
        when(categoryRepository.findByNameIgnoreCase("Calzado Urbano")).thenReturn(Optional.empty());

        categoryService.createCategory("Calzado Urbano", "Zapatillas urbanas y de paseo");

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).save(captor.capture());

        Category savedCategory = captor.getValue();
        assertNotNull(savedCategory);
        assertNull(savedCategory.getId(), "El ID no debe preasignarse manualmente en creación para permitir que Hibernate lo genere vía @GeneratedValue");
        assertEquals("Calzado Urbano", savedCategory.getName());
        assertEquals("Zapatillas urbanas y de paseo", savedCategory.getDescription());
        assertFalse(savedCategory.getDeleted());
    }

    @Test
    @DisplayName("Debe reactivar una categoría eliminada lógicamente si se intenta crear con el mismo nombre")
    void createCategory_ExistingDeletedCategory_ShouldReactivate() {
        UUID existingId = UUID.randomUUID();
        Category deletedCategory = Category.builder()
                .id(existingId)
                .name("Accesorios")
                .description("Vieja descripción")
                .deleted(true)
                .build();

        when(categoryRepository.findByNameIgnoreCase("Accesorios")).thenReturn(Optional.of(deletedCategory));

        categoryService.createCategory("Accesorios", "Nueva descripción");

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).save(captor.capture());

        Category saved = captor.getValue();
        assertEquals(existingId, saved.getId());
        assertFalse(saved.getDeleted());
        assertEquals("Nueva descripción", saved.getDescription());
    }

    @Test
    @DisplayName("Debe lanzar excepción si ya existe una categoría activa con el mismo nombre")
    void createCategory_DuplicateActiveName_ShouldThrowException() {
        Category activeCategory = Category.builder()
                .id(UUID.randomUUID())
                .name("Ropa")
                .deleted(false)
                .build();

        when(categoryRepository.findByNameIgnoreCase("Ropa")).thenReturn(Optional.of(activeCategory));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                categoryService.createCategory("Ropa", "Colección textil"));

        assertTrue(ex.getMessage().contains("Ya existe una categoría activa"));
        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe actualizar correctamente el nombre y descripción de una categoría activa")
    void updateCategory_ValidData_ShouldUpdateFields() {
        UUID catId = UUID.randomUUID();
        Category existingCategory = Category.builder()
                .id(catId)
                .name("Calzado")
                .description("Desc anterior")
                .deleted(false)
                .build();

        when(categoryRepository.findByIdAndDeletedFalse(catId)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.findByNameIgnoreCase("Calzado Deportivo")).thenReturn(Optional.empty());

        categoryService.updateCategory(catId, "Calzado Deportivo", "Nueva descripción deportiva");

        assertEquals("Calzado Deportivo", existingCategory.getName());
        assertEquals("Nueva descripción deportiva", existingCategory.getDescription());
        verify(categoryRepository).save(existingCategory);
    }

    @Test
    @DisplayName("Debe marcar deleted = true al eliminar lógicamente una categoría")
    void deleteCategory_ActiveCategory_ShouldSoftDelete() {
        UUID catId = UUID.randomUUID();
        Category activeCategory = Category.builder()
                .id(catId)
                .name("Deportes")
                .deleted(false)
                .build();

        when(categoryRepository.findByIdAndDeletedFalse(catId)).thenReturn(Optional.of(activeCategory));

        categoryService.deleteCategory(catId);

        assertTrue(activeCategory.getDeleted());
        verify(categoryRepository).save(activeCategory);
    }
}

package ingsoftware.zeroshop.service.catalog;

import ingsoftware.zeroshop.entity.catalog.Category;
import ingsoftware.zeroshop.repository.catalog.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CategoryService {

    // Lógica de negocio relacionada con categorías y subcategorías de productos

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * Valida el nombre de una categoría para su creación.
     *
     * @param name Nombre de la categoría.
     * @throws IllegalArgumentException si el nombre es nulo, vacío, fuera de los límites de longitud o ya existe.
     */
    public void validateCategory(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la categoría no puede estar vacío.");
        }

        String trimmedName = name.trim();
        if (trimmedName.length() < 2 || trimmedName.length() > 50) {
            throw new IllegalArgumentException("El nombre de la categoría debe tener entre 2 y 50 caracteres.");
        }

        categoryRepository.findByNameIgnoreCase(trimmedName).ifPresent(existing -> {
            if (!Boolean.TRUE.equals(existing.getDeleted())) {
                throw new IllegalArgumentException("Ya existe una categoría activa con el nombre: " + trimmedName);
            }
        });
    }

    /**
     * Valida una entidad Category para creación o actualización.
     * En caso de actualización (si cuenta con ID), excluye a la misma categoría del chequeo de nombre duplicado.
     *
     * @param category Entidad Category a validar.
     * @throws IllegalArgumentException si la categoría es nula o sus datos no son válidos.
     */
    public void validateCategory(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("La categoría no puede ser nula.");
        }

        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la categoría no puede estar vacío.");
        }

        String trimmedName = category.getName().trim();
        if (trimmedName.length() < 2 || trimmedName.length() > 50) {
            throw new IllegalArgumentException("El nombre de la categoría debe tener entre 2 y 50 caracteres.");
        }

        categoryRepository.findByNameIgnoreCase(trimmedName).ifPresent(existing -> {
            boolean isSameCategory = category.getId() != null && category.getId().equals(existing.getId());
            if (!isSameCategory && !Boolean.TRUE.equals(existing.getDeleted())) {
                throw new IllegalArgumentException("Ya existe una categoría activa con el nombre: " + trimmedName);
            }
        });
    }

    /**
     * Valida que una categoría exista por su ID y no haya sido eliminada lógicamente.
     *
     * @param id Identificador UUID de la categoría.
     * @return La categoría encontrada si es válida y está activa.
     * @throws IllegalArgumentException si el ID es nulo o no se encuentra activa.
     */
    public Category validateCategoryExists(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID de la categoría no puede ser nulo.");
        }

        return categoryRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró una categoría activa con el ID: " + id));
    }

    public void createCategory(String name, String description) {
        validateCategory(name);

        Category category = new Category();
        if (category.getId() == null) {
            category.setId(UUID.randomUUID());
        }
        category.setName(name.trim());
        category.setDescription(description != null ? description.trim() : null);
        categoryRepository.save(category);
    }

    public void createCategory(String name) {
        createCategory(name, null);
    }

    public void updateCategory(UUID id, String newName, String newDescription) {
        Category category = validateCategoryExists(id);
        Category toValidate = new Category();
        toValidate.setId(id);
        toValidate.setName(newName);
        validateCategory(toValidate);

        category.setName(newName.trim());
        category.setDescription(newDescription != null ? newDescription.trim() : null);
        categoryRepository.save(category);
    }

    public void deleteCategory(UUID id) {
        Category category = validateCategoryExists(id);
        category.setDeleted(true);
        categoryRepository.save(category);
    }

}

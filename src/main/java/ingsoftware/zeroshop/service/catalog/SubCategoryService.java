package ingsoftware.zeroshop.service.catalog;

import ingsoftware.zeroshop.entity.catalog.Category;
import ingsoftware.zeroshop.entity.catalog.SubCategory;
import ingsoftware.zeroshop.repository.catalog.CategoryRepository;
import ingsoftware.zeroshop.repository.catalog.SubCategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SubCategoryService {

    private final SubCategoryRepository subCategoryRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;

    public SubCategoryService(SubCategoryRepository subCategoryRepository, CategoryRepository categoryRepository, CategoryService categoryService) {
        this.subCategoryRepository = subCategoryRepository;
        this.categoryRepository = categoryRepository;
        this.categoryService = categoryService;
    }

    /**
     * Valida el nombre de una subcategoría y su pertenencia a una categoría activa.
     *
     * @param name Nombre de la subcategoría.
     * @param categoryId ID de la categoría padre.
     * @throws IllegalArgumentException si los datos son inválidos o ya existe la subcategoría en esa categoría.
     */
    public void validateSubCategory(String name, UUID categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("El ID de la categoría padre no puede ser nulo.");
        }

        boolean categoryExists = categoryRepository.findByIdAndDeletedFalse(categoryId).isPresent();
        if (!categoryExists) {
            throw new IllegalArgumentException("No se encontró una categoría activa con el ID: " + categoryId);
        }

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la subcategoría no puede estar vacío.");
        }

        String trimmedName = name.trim();
        if (trimmedName.length() < 2 || trimmedName.length() > 50) {
            throw new IllegalArgumentException("El nombre de la subcategoría debe tener entre 2 y 50 caracteres.");
        }

        subCategoryRepository.findByNameIgnoreCaseAndCategoryId(trimmedName, categoryId).ifPresent(existing -> {
            if (!Boolean.TRUE.equals(existing.getDeleted())) {
                throw new IllegalArgumentException("Ya existe una subcategoría activa con el nombre '" + trimmedName + "' en esta categoría.");
            }
        });
    }

    /**
     * Valida una entidad SubCategory para creación o actualización.
     * Si cuenta con ID (actualización), excluye la misma subcategoría de la verificación de duplicados.
     *
     * @param subCategory Entidad SubCategory a validar.
     * @throws IllegalArgumentException si la subcategoría es nula o inválida.
     */
    public void validateSubCategory(SubCategory subCategory) {
        if (subCategory == null) {
            throw new IllegalArgumentException("La subcategoría no puede ser nula.");
        }

        if (subCategory.getCategory() == null || subCategory.getCategory().getId() == null) {
            throw new IllegalArgumentException("La subcategoría debe estar asociada a una categoría válida.");
        }

        UUID categoryId = subCategory.getCategory().getId();
        boolean categoryExists = categoryRepository.findByIdAndDeletedFalse(categoryId).isPresent();
        if (!categoryExists) {
            throw new IllegalArgumentException("No se encontró una categoría activa con el ID: " + categoryId);
        }

        if (subCategory.getName() == null || subCategory.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la subcategoría no puede estar vacío.");
        }

        String trimmedName = subCategory.getName().trim();
        if (trimmedName.length() < 2 || trimmedName.length() > 50) {
            throw new IllegalArgumentException("El nombre de la subcategoría debe tener entre 2 y 50 caracteres.");
        }

        subCategoryRepository.findByNameIgnoreCaseAndCategoryId(trimmedName, categoryId).ifPresent(existing -> {
            boolean isSameSubCategory = subCategory.getId() != null && subCategory.getId().equals(existing.getId());
            if (!isSameSubCategory && !Boolean.TRUE.equals(existing.getDeleted())) {
                throw new IllegalArgumentException("Ya existe una subcategoría activa con el nombre '" + trimmedName + "' en esta categoría.");
            }
        });
    }

    /**
     * Valida que una subcategoría exista por su ID y no haya sido eliminada lógicamente.
     *
     * @param id Identificador UUID de la subcategoría.
     * @return La subcategoría encontrada si es válida y está activa.
     * @throws IllegalArgumentException si el ID es nulo o no se encuentra activa.
     */
    public SubCategory validateSubCategoryExists(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID de la subcategoría no puede ser nulo.");
        }

        return subCategoryRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró una subcategoría activa con el ID: " + id));
    }

    public void createSubCategory(String name, String description, UUID categoryId) {
        validateSubCategory(name, categoryId);
        Category category = categoryService.validateCategoryExists(categoryId);
        SubCategory subCategory = new SubCategory();
        subCategory.setName(name.trim());
        subCategory.setDescription(description != null ? description.trim() : null);
        subCategory.setCategory(category);
        subCategoryRepository.save(subCategory);
    }

    public void updateSubcategory(UUID id, String name, String description, UUID categoryId) {
        validateSubCategory(name, categoryId);
        SubCategory subCategory = validateSubCategoryExists(id);
        Category category = categoryService.validateCategoryExists(categoryId);

        subCategory.setName(name.trim());
        subCategory.setDescription(description != null ? description.trim() : null);
        subCategory.setCategory(category);
        subCategoryRepository.save(subCategory);
    }

    public Optional<SubCategory> findActiveById(UUID id) {
        if (id == null) {
            return Optional.empty();
        }
        return subCategoryRepository.findByIdAndDeletedFalse(id);
    }

    public List<SubCategory> findByCategoryId(UUID categoryId) {
        if (categoryId == null) {
            return List.of();
        }
        return subCategoryRepository.findByCategoryIdAndDeletedFalse(categoryId);
    }

}

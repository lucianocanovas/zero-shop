package ingsoftware.zeroshop.service.catalog;

import ingsoftware.zeroshop.entity.catalog.Category;
import ingsoftware.zeroshop.entity.catalog.SubCategory;
import ingsoftware.zeroshop.repository.catalog.CategoryRepository;
import ingsoftware.zeroshop.repository.catalog.SubCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Servicio fachada/orquestador del catálogo que centraliza la decisión de crear,
 * actualizar o clasificar entidades como Categoría principal o Subcategoría.
 */
@Service
public class CatalogService {

    private final CategoryService categoryService;
    private final SubCategoryService subCategoryService;
    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;

    public CatalogService(CategoryService categoryService,
                          SubCategoryService subCategoryService,
                          CategoryRepository categoryRepository,
                          SubCategoryRepository subCategoryRepository) {
        this.categoryService = categoryService;
        this.subCategoryService = subCategoryService;
        this.categoryRepository = categoryRepository;
        this.subCategoryRepository = subCategoryRepository;
    }

    /**
     * Lógica principal de negocio que decide si la entrada corresponde a una Categoría
     * principal o a una Subcategoría en función de si se especifica o no un parentId.
     *
     * @param name        Nombre de la categoría o subcategoría.
     * @param description Descripción opcional.
     * @param parentId    ID de la categoría padre. Si es nulo, se persiste como Categoría Principal;
     *                    si está presente, se persiste como Subcategoría asociada a dicho padre.
     */
    @Transactional
    public void createCategoryOrSubCategory(String name, String description, UUID parentId) {
        if (parentId == null) {
            // Sin padre -> Es Categoría Principal
            categoryService.createCategory(name, description);
        } else {
            // Con padre -> Es Subcategoría
            subCategoryService.createSubCategory(name, description, parentId);
        }
    }

    /**
     * Sobrecarga sin descripción.
     */
    @Transactional
    public void createCategoryOrSubCategory(String name, UUID parentId) {
        createCategoryOrSubCategory(name, null, parentId);
    }

    /**
     * Elimina lógicamente una categoría y sus subcategorías asociadas.
     *
     * @param categoryId ID de la categoría a eliminar.
     */
    @Transactional
    public void deleteCategory(UUID categoryId) {
        categoryService.deleteCategory(categoryId);

        // Eliminación lógica en cascada de subcategorías dependientes
        List<SubCategory> subCategories = subCategoryRepository.findByCategoryIdAndDeletedFalse(categoryId);
        for (SubCategory subCategory : subCategories) {
            subCategory.setDeleted(true);
        }
        subCategoryRepository.saveAll(subCategories);
    }

    /**
     * Obtiene todas las categorías principales registradas en la base de datos (activas e inactivas).
     */
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    /**
     * Obtiene todas las categorías principales activas (no eliminadas lógicamente).
     */
    public List<Category> getAllActiveCategories() {
        return categoryRepository.findAllByDeletedFalse();
    }

    /**
     * Obtiene todas las subcategorías activas pertenecientes a una categoría padre.
     */
    public List<SubCategory> getSubCategoriesByCategoryId(UUID categoryId) {
        return subCategoryRepository.findByCategoryIdAndDeletedFalse(categoryId);
    }

    /**
     * Obtiene todas las subcategorías activas generales.
     */
    public List<SubCategory> getAllActiveSubCategories() {
        return subCategoryRepository.findAllByDeletedFalse();
    }
}

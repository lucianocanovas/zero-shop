package ingsoftware.zeroshop.config;

import ingsoftware.zeroshop.entity.catalog.Category;
import ingsoftware.zeroshop.entity.catalog.SubCategory;
import ingsoftware.zeroshop.repository.catalog.CategoryRepository;
import ingsoftware.zeroshop.repository.catalog.SubCategoryRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(2)
public class CatalogInitializer implements ApplicationRunner {

    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;

    public CatalogInitializer(CategoryRepository categoryRepository, SubCategoryRepository subCategoryRepository) {
        this.categoryRepository = categoryRepository;
        this.subCategoryRepository = subCategoryRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        initializeCategoriesAndSubCategories();
    }

    private void initializeCategoriesAndSubCategories() {
        if (categoryRepository.findAllByDeletedFalse().isEmpty()) {
            List<String> categoryNames = List.of("Hombres", "Mujeres", "Niños", "Niñas", "Unisex");
            List<String> subCategoryNames = List.of("Calzado", "Indumentaria", "Accesorios");

            for (String catName : categoryNames) {
                Category category = categoryRepository.save(
                    Category.builder()
                        .name(catName)
                        .deleted(false)
                        .build()
                );

                for (String subName : subCategoryNames) {
                    subCategoryRepository.save(
                        SubCategory.builder()
                            .name(subName)
                            .category(category)
                            .deleted(false)
                            .build()
                    );
                }
            }
        }
    }

}

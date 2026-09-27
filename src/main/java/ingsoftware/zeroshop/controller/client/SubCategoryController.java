package ingsoftware.zeroshop.controller.client;

import ingsoftware.zeroshop.entity.catalog.SubCategory;
import ingsoftware.zeroshop.repository.catalog.SubCategoryRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller("clientSubCategoryController")
@RequestMapping("/subcategories")
public class SubCategoryController {

    private final SubCategoryRepository subCategoryRepository;

    public SubCategoryController(SubCategoryRepository subCategoryRepository) {
        this.subCategoryRepository = subCategoryRepository;
    }

    public record SubCategoryDto(
            UUID id,
            String name,
            UUID categoryId,
            String categoryName
    ) {}

    // GET /subcategories: Redirige al catálogo filtrado si se especifica categoryId, o al listado general de categorías
    @GetMapping
    public String getSubCategories(@RequestParam(value = "categoryId", required = false) UUID categoryId) {
        if (categoryId != null) {
            return "redirect:/products?categoryId=" + categoryId;
        }
        return "redirect:/categories";
    }

    // GET /subcategories/{id}: Redirige a los productos de esa subcategoría
    @GetMapping("/{id}")
    public String getSubCategoryProducts(@PathVariable("id") UUID id) {
        Optional<SubCategory> subCategory = subCategoryRepository.findByIdAndDeletedFalse(id);
        if (subCategory.isPresent()) {
            return "redirect:/products?subCategoryId=" + id;
        }
        return "redirect:/categories";
    }

    // GET /subcategories/by-category/{categoryId}: Endpoint JSON para consultar subcategorías de una categoría
    @GetMapping(value = "/by-category/{categoryId}", produces = "application/json")
    @ResponseBody
    public List<SubCategoryDto> getSubCategoriesByCategoryId(@PathVariable("categoryId") UUID categoryId) {
        return subCategoryRepository.findByCategoryIdAndDeletedFalse(categoryId).stream()
                .map(sub -> new SubCategoryDto(
                        sub.getId(),
                        sub.getName(),
                        sub.getCategory() != null ? sub.getCategory().getId() : categoryId,
                        sub.getCategory() != null ? sub.getCategory().getName() : null
                ))
                .toList();
    }

}

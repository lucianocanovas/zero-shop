package ingsoftware.zeroshop.controller.client;

import ingsoftware.zeroshop.dto.PageResult;
import ingsoftware.zeroshop.entity.catalog.Category;
import ingsoftware.zeroshop.entity.catalog.SubCategory;
import ingsoftware.zeroshop.service.catalog.CatalogService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.UUID;

@Controller("clientCategoryController")
@RequestMapping("/categories")
public class CategoryController {

    private final CatalogService catalogService;

    public CategoryController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    public record CategoryViewDto(
            UUID id,
            String name,
            String description,
            String iconClass,
            String colorClass,
            List<SubCategory> subCategories
    ) {}

    public record SubCategoryDto(
            UUID id,
            String name,
            UUID categoryId
    ) {}

    // GET /categories: Muestra las categorías disponibles junto con sus subcategorías
    @GetMapping
    public String getCategories(
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "hasSubs", required = false) Boolean hasSubs,
            @RequestParam(name = "page", required = false, defaultValue = "1") Integer page,
            Model model) {
        int pageNum = (page != null && page > 0) ? page : 1;
        List<Category> activeCategories = catalogService.getAllActiveCategories();

        List<CategoryViewDto> categoryViews = activeCategories.stream()
                .map(cat -> {
                    List<SubCategory> subCategories = catalogService.getSubCategoriesByCategoryId(cat.getId());
                    String name = cat.getName();
                    String icon = resolveIcon(name);
                    String color = resolveColor(name);
                    String description = (cat.getDescription() != null && !cat.getDescription().trim().isEmpty())
                            ? cat.getDescription().trim()
                            : resolveDescription(name);

                    return new CategoryViewDto(
                            cat.getId(),
                            name,
                            description,
                            icon,
                            color,
                            subCategories
                    );
                })
                .toList();

        // Búsqueda
        if (search != null && !search.trim().isBlank()) {
            String q = search.trim().toLowerCase();
            categoryViews = categoryViews.stream()
                    .filter(c -> (c.name() != null && c.name().toLowerCase().contains(q))
                            || (c.description() != null && c.description().toLowerCase().contains(q))
                            || (c.subCategories() != null && c.subCategories().stream().anyMatch(s -> s.getName().toLowerCase().contains(q))))
                    .toList();
        }

        // Filtro por subcategorías
        if (hasSubs != null) {
            categoryViews = categoryViews.stream()
                    .filter(c -> hasSubs ? (c.subCategories() != null && !c.subCategories().isEmpty()) : (c.subCategories() == null || c.subCategories().isEmpty()))
                    .toList();
        }

        PageResult<CategoryViewDto> pageResult = PageResult.of(categoryViews, pageNum, 4);

        model.addAttribute("categories", pageResult.getContent());
        model.addAttribute("pageResult", pageResult);
        model.addAttribute("search", search);
        model.addAttribute("hasSubs", hasSubs);
        return "client/categories";
    }

    // GET /categories/{id}: Redirige a los productos de esa categoría
    @GetMapping("/{id}")
    public String getCategoryProducts(@PathVariable("id") UUID id) {
        return "redirect:/products?categoryId=" + id;
    }

    // GET /categories/{id}/subcategories: Devuelve las subcategorías en formato JSON
    @GetMapping(value = "/{id}/subcategories", produces = "application/json")
    @ResponseBody
    public List<SubCategoryDto> getSubCategoriesByCategory(@PathVariable("id") UUID id) {
        return catalogService.getSubCategoriesByCategoryId(id).stream()
                .map(sub -> new SubCategoryDto(sub.getId(), sub.getName(), id))
                .toList();
    }

    private String resolveIcon(String categoryName) {
        if (categoryName == null) return "bi bi-tag-fill";
        return switch (categoryName.trim().toLowerCase()) {
            case "hombres" -> "bi bi-gender-male";
            case "mujeres" -> "bi bi-gender-female";
            case "niños", "ninos" -> "bi bi-person";
            case "niñas", "ninas" -> "bi bi-person-heart";
            default -> "bi bi-tag-fill";
        };
    }

    private String resolveColor(String categoryName) {
        if (categoryName == null) return "bg-primary-subtle text-primary";
        return switch (categoryName.trim().toLowerCase()) {
            case "hombres" -> "bg-primary-subtle text-primary";
            case "mujeres" -> "bg-danger-subtle text-danger";
            case "niños", "ninos" -> "bg-success-subtle text-success";
            case "niñas", "ninas" -> "bg-warning-subtle text-warning-emphasis";
            default -> "bg-primary-subtle text-primary";
        };
    }

    private String resolveDescription(String categoryName) {
        if (categoryName == null) return "Descubre los mejores artículos deportivos.";
        return switch (categoryName.trim().toLowerCase()) {
            case "hombres" -> "Indumentaria deportiva, zapatillas de running y accesorios para entrenamiento masculino.";
            case "mujeres" -> "Tops, calzas, zapatillas ultralivianas y accesorios funcionales de alto rendimiento.";
            case "niños", "ninos" -> "Ropa deportiva resistente, zapatillas escolares y complementos para actividades físicas.";
            case "niñas", "ninas" -> "Conjuntos deportivos, calzado cómodo y accesorios diseñados para el movimiento diario.";
            default -> "Descubre la colección deportiva y complementos de " + categoryName + ".";
        };
    }

}

package ingsoftware.zeroshop.controller.dashboard;

import ingsoftware.zeroshop.entity.catalog.Category;
import ingsoftware.zeroshop.entity.catalog.SubCategory;
import ingsoftware.zeroshop.service.catalog.CatalogService;
import ingsoftware.zeroshop.service.catalog.CategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Controller("dashboardCategoryController")
@RequestMapping("/dashboard/categories")
public class CategoryController {

    private final CatalogService catalogService;
    private final CategoryService categoryService;

    public CategoryController(CatalogService catalogService, CategoryService categoryService) {
        this.catalogService = catalogService;
        this.categoryService = categoryService;
    }

    public record CategoryDashboardDto(
            UUID id,
            String name,
            String description,
            List<SubCategory> subCategories
    ) {}

    // GET /dashboard/categories: Lista únicamente las categorías activas en el dashboard con filtros y paginación
    @GetMapping
    public String listCategories(
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "hasSubs", required = false) Boolean hasSubs,
            @RequestParam(name = "page", required = false, defaultValue = "1") Integer page,
            Model model) {
        int pageNum = (page != null && page > 0) ? page : 1;
        List<Category> categories = catalogService.getAllActiveCategories();
        List<CategoryDashboardDto> categoryDtos = categories.stream()
                .map(cat -> new CategoryDashboardDto(
                        cat.getId(),
                        cat.getName(),
                        cat.getDescription(),
                        catalogService.getSubCategoriesByCategoryId(cat.getId())
                ))
                .toList();

        if (search != null && !search.trim().isBlank()) {
            String q = search.trim().toLowerCase();
            categoryDtos = categoryDtos.stream()
                    .filter(c -> (c.name() != null && c.name().toLowerCase().contains(q))
                            || (c.description() != null && c.description().toLowerCase().contains(q))
                            || (c.subCategories() != null && c.subCategories().stream().anyMatch(s -> s.getName().toLowerCase().contains(q))))
                    .toList();
        }

        if (hasSubs != null) {
            categoryDtos = categoryDtos.stream()
                    .filter(c -> hasSubs ? (c.subCategories() != null && !c.subCategories().isEmpty()) : (c.subCategories() == null || c.subCategories().isEmpty()))
                    .toList();
        }

        ingsoftware.zeroshop.dto.PageResult<CategoryDashboardDto> pageResult = ingsoftware.zeroshop.dto.PageResult.of(categoryDtos, pageNum, 10);

        model.addAttribute("categories", pageResult.getContent());
        model.addAttribute("pageResult", pageResult);
        model.addAttribute("search", search);
        model.addAttribute("hasSubs", hasSubs);
        return "dashboard/categories";
    }

    // GET /dashboard/categories/new: Muestra el formulario para crear una nueva categoría o subcategoría
    @GetMapping("/new")
    public String newCategoryForm(Model model) {
        List<Category> categories = catalogService.getAllActiveCategories();
        model.addAttribute("categories", categories);
        return "dashboard/category-new";
    }

    // GET /dashboard/categories/{id}: Muestra la vista para editar una categoría existente activa
    @GetMapping("/{id}")
    public String getCategoryDetail(@PathVariable("id") UUID id, Model model) {
        try {
            Category category = categoryService.validateCategoryExists(id);
            List<Category> categories = catalogService.getAllActiveCategories();

            model.addAttribute("category", category);
            model.addAttribute("categories", categories);
            return "dashboard/category-edit";
        } catch (IllegalArgumentException e) {
            return "redirect:/dashboard/categories";
        }
    }

    // POST /dashboard/categories: Crea una nueva categoría o subcategoría según tenga o no parentId
    @PostMapping
    public String createCategory(@RequestParam("name") String name,
                                 @RequestParam(value = "description", required = false) String description,
                                 @RequestParam(value = "parentId", required = false) String parentIdStr,
                                 RedirectAttributes redirectAttributes) {
        UUID parentId = null;
        if (parentIdStr != null && !parentIdStr.trim().isEmpty()) {
            try {
                parentId = UUID.fromString(parentIdStr.trim());
            } catch (IllegalArgumentException ignored) {
                // Si viene un valor no parseable a UUID, se interpreta como sin padre
            }
        }

        try {
            catalogService.createCategoryOrSubCategory(name, description, parentId);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dashboard/categories";
    }

    // POST /dashboard/categories/{id}: Soporta tanto actualización como eliminación si el navegador envía POST con _method
    @PostMapping("/{id}")
    public String handleCategoryPost(@PathVariable("id") UUID id,
                                     @RequestParam(value = "_method", required = false) String method,
                                     @RequestParam(value = "name", required = false) String name,
                                     @RequestParam(value = "description", required = false) String description,
                                     RedirectAttributes redirectAttributes) {
        if ("DELETE".equalsIgnoreCase(method)) {
            return deleteCategory(id);
        }
        if (name != null) {
            return updateCategory(id, name, description, redirectAttributes);
        }
        return "redirect:/dashboard/categories";
    }

    // POST /dashboard/categories/{id}/delete: Eliminación directa mediante POST para evitar fallos de formularios HTML
    @PostMapping("/{id}/delete")
    public String deleteCategoryPost(@PathVariable("id") UUID id) {
        return deleteCategory(id);
    }

    // PUT /dashboard/categories/{id}: Actualiza una categoría existente
    @PutMapping("/{id}")
    public String updateCategory(@PathVariable("id") UUID id,
                                 @RequestParam("name") String name,
                                 @RequestParam(value = "description", required = false) String description,
                                 RedirectAttributes redirectAttributes) {
        try {
            categoryService.updateCategory(id, name, description);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dashboard/categories";
    }

    // DELETE /dashboard/categories/{id}: Elimina lógicamente una categoría
    @DeleteMapping("/{id}")
    public String deleteCategory(@PathVariable("id") UUID id) {
        try {
            catalogService.deleteCategory(id);
        } catch (IllegalArgumentException ignored) {
        }
        return "redirect:/dashboard/categories";
    }

}

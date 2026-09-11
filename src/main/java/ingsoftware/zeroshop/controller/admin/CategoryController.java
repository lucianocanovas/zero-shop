package ingsoftware.zeroshop.controller.admin;

import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

@Controller("adminCategoryController")
public class CategoryController {

    // GET /admin/categories: Lista todas las categorías en administración
    @GetMapping("/admin/categories")
    public String listCategories() {
        // LOGICA PARA LISTAR CATEGORIAS
        return "admin/categories";
    }

    // GET /admin/categories/new: Muestra el formulario para crear una nueva categoría
    @GetMapping("/admin/categories/new")
    public String newCategoryForm() {
        // LOGICA PARA MOSTRAR FORMULARIO DE ALTA DE CATEGORIA
        return "admin/category-detail";
    }

    // GET /admin/categories/:id: Muestra el detalle o edición de una categoría
    @GetMapping("/admin/categories/{id}")
    public String getCategoryDetail(@PathVariable("id") UUID id) {
        // LOGICA PARA OBTENER DETALLE DE CATEGORIA
        return "admin/category-detail";
    }

    // POST /admin/categories/: Crea una nueva categoría o subcategoría
    @PostMapping("/admin/categories/")
    public String createCategory() {
        // LOGICA PARA CREAR CATEGORIA
        return "redirect:/admin/categories";
    }

    // PUT /admin/categories/:id: Actualiza una categoría existente
    @PutMapping("/admin/categories/{id}")
    public String updateCategory(@PathVariable("id") UUID id) {
        // LOGICA PARA ACTUALIZAR CATEGORIA
        return "redirect:/admin/categories";
    }

    // DELETE /admin/categories/:id: Elimina una categoría
    @DeleteMapping("/admin/categories/{id}")
    public String deleteCategory(@PathVariable("id") UUID id) {
        // LOGICA PARA ELIMINAR CATEGORIA
        return "redirect:/admin/categories";
    }

}

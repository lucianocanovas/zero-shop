package ingsoftware.zeroshop.controller.dashboard;

import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

@Controller("dashboardCategoryController")
public class CategoryController {

    // GET /dashboard/categories: Lista todas las categorías en el dashboard
    @GetMapping("/dashboard/categories")
    public String listCategories() {
        // LOGICA PARA LISTAR CATEGORIAS
        return "dashboard/categories";
    }

    // GET /dashboard/categories/new: Muestra el formulario para crear una nueva categoría
    @GetMapping("/dashboard/categories/new")
    public String newCategoryForm() {
        // LOGICA PARA MOSTRAR FORMULARIO DE ALTA DE CATEGORIA
        return "dashboard/category-new";
    }

    // GET /dashboard/categories/:id: Muestra la vista para editar una categoría existente
    @GetMapping("/dashboard/categories/{id}")
    public String getCategoryDetail(@PathVariable("id") UUID id) {
        // LOGICA PARA OBTENER EDICION DE CATEGORIA
        return "dashboard/category-edit";
    }

    // POST /dashboard/categories: Crea una nueva categoría o subcategoría
    @PostMapping({"/dashboard/categories", "/dashboard/categories/"})
    public String createCategory() {
        // LOGICA PARA CREAR CATEGORIA
        return "redirect:/dashboard/categories";
    }

    // PUT /dashboard/categories/:id: Actualiza una categoría existente
    @PutMapping("/dashboard/categories/{id}")
    public String updateCategory(@PathVariable("id") UUID id) {
        // LOGICA PARA ACTUALIZAR CATEGORIA
        return "redirect:/dashboard/categories";
    }

    // DELETE /dashboard/categories/:id: Elimina una categoría
    @DeleteMapping("/dashboard/categories/{id}")
    public String deleteCategory(@PathVariable("id") UUID id) {
        // LOGICA PARA ELIMINAR CATEGORIA
        return "redirect:/dashboard/categories";
    }

}


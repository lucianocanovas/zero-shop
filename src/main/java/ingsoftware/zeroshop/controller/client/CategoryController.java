package ingsoftware.zeroshop.controller.client;

import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller("clientCategoryController")
public class CategoryController {

    // GET /categories: Muestra las categorías disponibles
    @GetMapping("/categories")
    public String getCategories() {
        // LOGICA PARA LISTAR CATEGORIAS
        return "client/categories";
    }

    // GET /categories/:id: Muestra los productos de una categoría o subcategoría específica
    @GetMapping("/categories/{id}")
    public String getCategoryById(@PathVariable("id") UUID id) {
        // LOGICA PARA OBTENER PRODUCTOS DE LA CATEGORIA
        return "client/category-detail";
    }

}

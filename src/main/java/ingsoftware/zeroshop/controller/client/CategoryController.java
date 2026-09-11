package ingsoftware.zeroshop.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller("clientCategoryController")
public class CategoryController {

    // GET /categories: Muestra las categorías disponibles
    @GetMapping("/categories")
    public String getCategories() {
        // LOGICA PARA LISTAR CATEGORIAS
        return "client/categories";
    }

}

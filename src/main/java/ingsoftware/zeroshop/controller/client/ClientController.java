package ingsoftware.zeroshop.controller.client;

import ingsoftware.zeroshop.entity.catalog.Category;
import ingsoftware.zeroshop.entity.catalog.Product;
import ingsoftware.zeroshop.service.catalog.CatalogService;
import ingsoftware.zeroshop.service.catalog.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class ClientController {

    private final ProductService productService;
    private final CatalogService catalogService;

    public ClientController(ProductService productService, CatalogService catalogService) {
        this.productService = productService;
        this.catalogService = catalogService;
    }

    // GET /: Muestra la página de inicio de la tienda
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("title", "Inicio - Tienda");
        List<Product> products = productService.findAllActive();
        List<Category> categories = catalogService.getAllActiveCategories();

        model.addAttribute("products", products);
        model.addAttribute("categories", categories);
        return "client/index";
    }

    // GET /contact: Muestra la página de contacto e información institucional
    @GetMapping("/contact")
    public String contact(Model model) {
        model.addAttribute("title", "Contacto");
        return "client/contact";
    }

}

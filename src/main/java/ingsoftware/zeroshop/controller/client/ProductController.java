package ingsoftware.zeroshop.controller.client;

import ingsoftware.zeroshop.entity.catalog.Product;
import ingsoftware.zeroshop.repository.catalog.CategoryRepository;
import ingsoftware.zeroshop.service.catalog.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Controller("clientProductController")
public class ProductController {

    private final ProductService productService;
    private final CategoryRepository categoryRepository;

    public ProductController(ProductService productService, CategoryRepository categoryRepository) {
        this.productService = productService;
        this.categoryRepository = categoryRepository;
    }

    // GET /products: Muestra el catálogo general de productos
    @GetMapping("/products")
    public String getAllProducts(
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "categoryId", required = false) String categoryId,
            @RequestParam(name = "subCategory", required = false) String subCategory,
            @RequestParam(name = "maxPrice", required = false) BigDecimal maxPrice,
            Model model) {

        List<Product> products = productService.searchProducts(search, categoryId, subCategory, maxPrice);

        model.addAttribute("products", products);
        model.addAttribute("categories", categoryRepository.findAllByDeletedFalse());
        model.addAttribute("search", search);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("subCategory", subCategory);
        model.addAttribute("maxPrice", maxPrice);

        return "client/products";
    }

    // GET /products/:id: Muestra el detalle de un producto específico
    @GetMapping("/products/{id}")
    public String getProductById(@PathVariable("id") UUID id, Model model) {
        try {
            Product product = productService.findActiveById(id);
            model.addAttribute("product", product);
            return "client/product-detail";
        } catch (IllegalArgumentException e) {
            return "redirect:/products";
        }
    }

    // GET /offers: Muestra la sección de ofertas especiales
    @GetMapping("/offers")
    public String getOffers() {
        // LOGICA PARA OBTENER PRODUCTOS EN OFERTA (será implementada por otra persona)
        return "client/offers";
    }

}

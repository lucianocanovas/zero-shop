package ingsoftware.zeroshop.controller.client;

import ingsoftware.zeroshop.entity.catalog.Product;
import ingsoftware.zeroshop.service.catalog.CatalogService;
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
    private final CatalogService catalogService;

    public ProductController(ProductService productService, CatalogService catalogService) {
        this.productService = productService;
        this.catalogService = catalogService;
    }

    // GET /products: Muestra el catálogo general de productos
    @GetMapping("/products")
    public String getAllProducts(
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "categoryId", required = false) String categoryId,
            @RequestParam(name = "subCategory", required = false) String subCategory,
            @RequestParam(name = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(name = "sort", required = false) String sort,
            @RequestParam(name = "page", required = false, defaultValue = "1") Integer page,
            Model model) {

        int pageNum = (page != null && page > 0) ? page : 1;
        List<Product> products = productService.searchProducts(search, categoryId, subCategory, maxPrice, sort);

        ingsoftware.zeroshop.dto.PageResult<Product> pageResult = ingsoftware.zeroshop.dto.PageResult.of(products, pageNum, 8);

        model.addAttribute("products", pageResult.getContent());
        model.addAttribute("pageResult", pageResult);
        model.addAttribute("categories", catalogService.getAllActiveCategories());
        model.addAttribute("search", search);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("subCategory", subCategory);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sort", sort);

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

    // GET /offers: Muestra la sección de ofertas especiales con filtros y paginación
    @GetMapping("/offers")
    public String getOffers(
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "categoryId", required = false) String categoryId,
            @RequestParam(name = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(name = "page", required = false, defaultValue = "1") Integer page,
            Model model) {
        int pageNum = (page != null && page > 0) ? page : 1;
        List<Product> onSaleProducts = productService.findOnSaleProducts(search, categoryId, maxPrice);

        ingsoftware.zeroshop.dto.PageResult<Product> pageResult = ingsoftware.zeroshop.dto.PageResult.of(onSaleProducts, pageNum, 8);

        model.addAttribute("products", pageResult.getContent());
        model.addAttribute("pageResult", pageResult);
        model.addAttribute("categories", catalogService.getAllActiveCategories());
        model.addAttribute("search", search);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("maxPrice", maxPrice);

        return "client/offers";
    }

}

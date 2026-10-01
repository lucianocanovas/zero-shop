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
        List<Product> products = productService.searchProducts(search, categoryId, subCategory, maxPrice);

        // Ordenamiento
        if ("price_asc".equalsIgnoreCase(sort)) {
            products = products.stream()
                    .sorted(java.util.Comparator.comparing(p -> p.getCurrentPrice() != null ? p.getCurrentPrice() : BigDecimal.ZERO))
                    .toList();
        } else if ("price_desc".equalsIgnoreCase(sort)) {
            products = products.stream()
                    .sorted((p1, p2) -> {
                        BigDecimal pr1 = p1.getCurrentPrice() != null ? p1.getCurrentPrice() : BigDecimal.ZERO;
                        BigDecimal pr2 = p2.getCurrentPrice() != null ? p2.getCurrentPrice() : BigDecimal.ZERO;
                        return pr2.compareTo(pr1);
                    })
                    .toList();
        } else if ("name_asc".equalsIgnoreCase(sort)) {
            products = products.stream()
                    .sorted(java.util.Comparator.comparing(p -> p.getName() != null ? p.getName().toLowerCase() : ""))
                    .toList();
        }

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
        List<Product> onSaleProducts = productService.findOnSaleProducts();

        if (search != null && !search.trim().isBlank()) {
            String q = search.trim().toLowerCase();
            onSaleProducts = onSaleProducts.stream()
                    .filter(p -> (p.getName() != null && p.getName().toLowerCase().contains(q))
                            || (p.getCode() != null && p.getCode().toLowerCase().contains(q))
                            || (p.getDescription() != null && p.getDescription().toLowerCase().contains(q)))
                    .toList();
        }

        if (categoryId != null && !categoryId.trim().isBlank()) {
            try {
                UUID catId = UUID.fromString(categoryId.trim());
                onSaleProducts = onSaleProducts.stream()
                        .filter(p -> p.getCategory() != null && catId.equals(p.getCategory().getId()))
                        .toList();
            } catch (Exception ignored) {}
        }

        if (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) > 0) {
            onSaleProducts = onSaleProducts.stream()
                    .filter(p -> p.getCurrentPrice() != null && p.getCurrentPrice().compareTo(maxPrice) <= 0)
                    .toList();
        }

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

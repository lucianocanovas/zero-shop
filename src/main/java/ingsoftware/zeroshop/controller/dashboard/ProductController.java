package ingsoftware.zeroshop.controller.dashboard;

import ingsoftware.zeroshop.entity.catalog.Product;
import ingsoftware.zeroshop.enums.Size;
import ingsoftware.zeroshop.repository.catalog.CategoryRepository;
import ingsoftware.zeroshop.repository.catalog.SubCategoryRepository;
import ingsoftware.zeroshop.service.catalog.ProductService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.UUID;

@Controller("dashboardProductController")
public class ProductController {

    private final ProductService productService;
    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;

    public ProductController(ProductService productService,
                             CategoryRepository categoryRepository,
                             SubCategoryRepository subCategoryRepository) {
        this.productService = productService;
        this.categoryRepository = categoryRepository;
        this.subCategoryRepository = subCategoryRepository;
    }

    // GET /dashboard/products: Lista todos los productos en el dashboard (Admin y Employee)
    @GetMapping("/dashboard/products")
    public String listProducts(Model model) {
        model.addAttribute("products", productService.findAllActive());
        return "dashboard/products";
    }

    // GET /dashboard/products/new: Formulario para crear un nuevo producto (Solo Admin)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/dashboard/products/new")
    public String newProductForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryRepository.findAllByDeletedFalse());
        model.addAttribute("subCategories", subCategoryRepository.findAllWithCategoryByDeletedFalse());
        model.addAttribute("sizes", Size.values());
        return "dashboard/product-new";
    }

    // GET /dashboard/products/{id}: Muestra vista para ver detalle (Employee) o editar (Admin)
    @GetMapping("/dashboard/products/{id}")
    public String getProductDetail(@PathVariable("id") UUID id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Product product = productService.findActiveById(id);
            model.addAttribute("product", product);
            model.addAttribute("categories", categoryRepository.findAllByDeletedFalse());
            model.addAttribute("subCategories", subCategoryRepository.findAllWithCategoryByDeletedFalse());
            model.addAttribute("sizes", Size.values());
            return "dashboard/product-edit";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/dashboard/products";
        }
    }

    // POST /dashboard/products: Guarda un nuevo producto (Solo Admin)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping({"/dashboard/products", "/dashboard/products/"})
    public String createProduct(@ModelAttribute Product product,
                                @RequestParam(name = "basePrice", required = false) BigDecimal basePrice,
                                @RequestParam(name = "subCategoryId", required = false) UUID subCategoryId,
                                RedirectAttributes redirectAttributes) {
        try {
            productService.createProduct(product, basePrice, subCategoryId);
            redirectAttributes.addFlashAttribute("successMessage", "Producto creado exitosamente.");
            return "redirect:/dashboard/products";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/dashboard/products/new";
        }
    }

    // PUT o POST /dashboard/products/{id}: Actualiza un producto existente (Solo Admin)
    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(value = "/dashboard/products/{id}", method = {RequestMethod.PUT, RequestMethod.POST})
    public String updateProduct(@PathVariable("id") UUID id,
                                @ModelAttribute Product product,
                                @RequestParam(name = "basePrice", required = false) BigDecimal basePrice,
                                @RequestParam(name = "subCategoryId", required = false) UUID subCategoryId,
                                RedirectAttributes redirectAttributes) {
        try {
            productService.updateProduct(id, product, basePrice, subCategoryId);
            redirectAttributes.addFlashAttribute("successMessage", "Producto actualizado exitosamente.");
            return "redirect:/dashboard/products";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/dashboard/products/" + id;
        }
    }

    // DELETE /dashboard/products/{id}: Elimina un producto (Solo Admin)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/dashboard/products/{id}")
    public String deleteProduct(@PathVariable("id") UUID id, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("successMessage", "Producto eliminado exitosamente.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/dashboard/products";
    }

    // POST alternativo para eliminación vía form HTML
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/dashboard/products/{id}/delete")
    public String deleteProductPost(@PathVariable("id") UUID id, RedirectAttributes redirectAttributes) {
        return deleteProduct(id, redirectAttributes);
    }

    // GET /dashboard/products/{id}/prices: Muestra el historial y gestión de precios de un producto
    @GetMapping("/dashboard/products/{id}/prices")
    public String getProductPrices(@PathVariable("id") UUID id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Product product = productService.findActiveById(id);
            model.addAttribute("product", product);
            model.addAttribute("productId", id);
            model.addAttribute("priceHistory", productService.getProductPriceHistory(id));
            return "dashboard/product-prices";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/dashboard/products";
        }
    }

    // POST /dashboard/products/{id}/prices: Registra un nuevo precio para el producto (Solo Admin)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/dashboard/products/{id}/prices")
    public String addProductPrice(@PathVariable("id") UUID id,
                                  @RequestParam("price") BigDecimal price,
                                  RedirectAttributes redirectAttributes) {
        try {
            productService.addProductPrice(id, price);
            redirectAttributes.addFlashAttribute("successMessage", "Precio registrado exitosamente.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/dashboard/products/" + id + "/prices";
    }

    // GET /dashboard/stock: Muestra el panel de gestión de stock general y por sucursales
    @GetMapping("/dashboard/stock")
    public String getStock() {
        return "dashboard/stock";
    }

    // PUT /dashboard/stock/{id}: Realiza un ajuste manual de stock para un producto
    @PutMapping("/dashboard/stock/{id}")
    public String updateStock(@PathVariable("id") UUID id) {
        return "redirect:/dashboard/stock";
    }

}

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
import java.util.List;
import java.util.UUID;

@Controller("dashboardProductController")
public class ProductController {

    private final ProductService productService;
    private final CategoryRepository categoryRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final ingsoftware.zeroshop.service.storage.FileStorageService fileStorageService;

    public ProductController(ProductService productService,
                             CategoryRepository categoryRepository,
                             SubCategoryRepository subCategoryRepository,
                             ingsoftware.zeroshop.service.storage.FileStorageService fileStorageService) {
        this.productService = productService;
        this.categoryRepository = categoryRepository;
        this.subCategoryRepository = subCategoryRepository;
        this.fileStorageService = fileStorageService;
    }

    // GET /dashboard/products: Lista todos los productos en el dashboard con filtros, búsqueda y paginación
    @GetMapping("/dashboard/products")
    public String listProducts(
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "categoryId", required = false) UUID categoryId,
            @RequestParam(name = "subCategoryId", required = false) UUID subCategoryId,
            @RequestParam(name = "size", required = false) Size size,
            @RequestParam(name = "onSale", required = false) Boolean onSale,
            @RequestParam(name = "stockStatus", required = false) String stockStatus,
            @RequestParam(name = "page", required = false, defaultValue = "1") Integer page,
            Model model) {
        int pageNum = (page != null && page > 0) ? page : 1;

        List<Product> products = productService.findAllActive();

        if (search != null && !search.trim().isBlank()) {
            String q = search.trim().toLowerCase();
            products = products.stream()
                    .filter(p -> (p.getName() != null && p.getName().toLowerCase().contains(q))
                            || (p.getCode() != null && p.getCode().toLowerCase().contains(q))
                            || (p.getDescription() != null && p.getDescription().toLowerCase().contains(q)))
                    .toList();
        }

        if (categoryId != null) {
            products = products.stream()
                    .filter(p -> p.getCategory() != null && categoryId.equals(p.getCategory().getId()))
                    .toList();
        }

        if (subCategoryId != null) {
            products = products.stream()
                    .filter(p -> p.getSubCategory() != null && subCategoryId.equals(p.getSubCategory().getId()))
                    .toList();
        }

        if (size != null) {
            products = products.stream()
                    .filter(p -> p.getSize() == size)
                    .toList();
        }

        if (onSale != null) {
            products = products.stream()
                    .filter(p -> Boolean.valueOf(onSale).equals(p.getOnSale()))
                    .toList();
        }

        if (stockStatus != null && !stockStatus.isBlank()) {
            if ("in_stock".equalsIgnoreCase(stockStatus)) {
                products = products.stream()
                        .filter(p -> p.getStock() != null && p.getStock() > 0)
                        .toList();
            } else if ("out_of_stock".equalsIgnoreCase(stockStatus)) {
                products = products.stream()
                        .filter(p -> p.getStock() == null || p.getStock() == 0)
                        .toList();
            }
        }

        ingsoftware.zeroshop.dto.PageResult<Product> pageResult = ingsoftware.zeroshop.dto.PageResult.of(products, pageNum, 10);

        model.addAttribute("products", pageResult.getContent());
        model.addAttribute("pageResult", pageResult);
        model.addAttribute("categories", categoryRepository.findAllByDeletedFalse());
        model.addAttribute("subCategories", subCategoryRepository.findAllWithCategoryByDeletedFalse());
        model.addAttribute("sizes", Size.values());
        model.addAttribute("search", search);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("subCategoryId", subCategoryId);
        model.addAttribute("size", size);
        model.addAttribute("onSale", onSale);
        model.addAttribute("stockStatus", stockStatus);

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
                                @RequestParam(name = "imageFile", required = false) org.springframework.web.multipart.MultipartFile imageFile,
                                @RequestParam(name = "basePrice", required = false) BigDecimal basePrice,
                                @RequestParam(name = "subCategoryId", required = false) UUID subCategoryId,
                                @RequestParam(name = "onSale", defaultValue = "false") Boolean onSale,
                                RedirectAttributes redirectAttributes) {
        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                String uploadedUrl = fileStorageService.storeProductImage(imageFile);
                product.setImageUrl(uploadedUrl);
            }
            product.setOnSale(Boolean.TRUE.equals(onSale));
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
                                @RequestParam(name = "imageFile", required = false) org.springframework.web.multipart.MultipartFile imageFile,
                                @RequestParam(name = "basePrice", required = false) BigDecimal basePrice,
                                @RequestParam(name = "subCategoryId", required = false) UUID subCategoryId,
                                @RequestParam(name = "onSale", defaultValue = "false") Boolean onSale,
                                RedirectAttributes redirectAttributes) {
        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                String uploadedUrl = fileStorageService.storeProductImage(imageFile);
                product.setImageUrl(uploadedUrl);
            }
            product.setOnSale(Boolean.TRUE.equals(onSale));
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
                                  @RequestParam(name = "reason", required = false) String reason,
                                  @RequestParam(name = "onSale", required = false) Boolean onSale,
                                  RedirectAttributes redirectAttributes) {
        try {
            productService.addProductPrice(id, price, reason, onSale);
            redirectAttributes.addFlashAttribute("successMessage", "Precio registrado exitosamente.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/dashboard/products/" + id + "/prices";
    }

}

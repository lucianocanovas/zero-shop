package ingsoftware.zeroshop.controller.dashboard;

import ingsoftware.zeroshop.entity.catalog.Product;
import ingsoftware.zeroshop.entity.org.Office;
import ingsoftware.zeroshop.entity.org.Stock;
import ingsoftware.zeroshop.repository.catalog.ProductRepository;
import ingsoftware.zeroshop.repository.org.OfficeRepository;
import ingsoftware.zeroshop.service.org.StockService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Controller("dashboardStockController")
@PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
public class StockController {

    private final StockService stockService;
    private final ProductRepository productRepository;
    private final OfficeRepository officeRepository;

    public StockController(StockService stockService,
                           ProductRepository productRepository,
                           OfficeRepository officeRepository) {
        this.stockService = stockService;
        this.productRepository = productRepository;
        this.officeRepository = officeRepository;
    }

    // GET /dashboard/stock: Muestra el listado de existencias de inventario
    @GetMapping("/dashboard/stock")
    @Transactional(readOnly = true)
    public String listStock(Model model) {
        List<Stock> stocks = stockService.getAllActiveStock();
        List<Product> products = productRepository.findAllByDeletedFalse();
        List<Office> offices = officeRepository.findAllByDeletedFalse();

        model.addAttribute("stocks", stocks);
        model.addAttribute("products", products);
        model.addAttribute("offices", offices);

        return "dashboard/stock";
    }

    // POST /dashboard/stock/{id}: Ajuste manual de existencias
    @PostMapping("/dashboard/stock/{id}")
    public String adjustStock(@PathVariable("id") UUID id,
                              @RequestParam(value = "adjustment", required = false) Integer adjustment,
                              @RequestParam(value = "quantity", required = false) Integer quantity,
                              RedirectAttributes redirectAttributes) {
        try {
            if (quantity != null) {
                stockService.updateStockQuantity(id, quantity);
                redirectAttributes.addFlashAttribute("successMessage", "Stock actualizado correctamente a " + quantity + " unidades.");
            } else if (adjustment != null) {
                Stock updated = stockService.adjustStock(id, adjustment);
                redirectAttributes.addFlashAttribute("successMessage",
                        "Stock ajustado en " + (adjustment >= 0 ? "+" : "") + adjustment +
                        " unidades. Total actual: " + updated.getQuantity());
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Debe ingresar una cantidad o valor de ajuste.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar stock: " + e.getMessage());
        }
        return "redirect:/dashboard/stock";
    }

    // PUT /dashboard/stock/{id}: Para compatibilidad con formularios HTTP PUT
    @PutMapping("/dashboard/stock/{id}")
    public String adjustStockPut(@PathVariable("id") UUID id,
                                 @RequestParam(value = "adjustment", required = false) Integer adjustment,
                                 @RequestParam(value = "quantity", required = false) Integer quantity,
                                 RedirectAttributes redirectAttributes) {
        return adjustStock(id, adjustment, quantity, redirectAttributes);
    }

    // POST /dashboard/stock: Asignación o creación directa de stock
    @PostMapping("/dashboard/stock")
    public String createOrSetStock(@RequestParam("productId") UUID productId,
                                   @RequestParam("officeId") UUID officeId,
                                   @RequestParam("quantity") Integer quantity,
                                   RedirectAttributes redirectAttributes) {
        try {
            stockService.setStock(productId, officeId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Stock inicial asignado exitosamente (" + quantity + " unidades).");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al asignar stock: " + e.getMessage());
        }
        return "redirect:/dashboard/stock";
    }
}

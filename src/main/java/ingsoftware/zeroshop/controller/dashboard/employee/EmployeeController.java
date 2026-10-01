package ingsoftware.zeroshop.controller.dashboard.employee;

import com.fasterxml.jackson.databind.ObjectMapper;
import ingsoftware.zeroshop.dto.DeskProductDTO;
import ingsoftware.zeroshop.entity.catalog.Product;
import ingsoftware.zeroshop.entity.org.Office;
import ingsoftware.zeroshop.entity.transaction.SaleOrder;
import ingsoftware.zeroshop.enums.OrderStatus;
import ingsoftware.zeroshop.enums.PaymentMethod;
import ingsoftware.zeroshop.repository.org.OfficeRepository;
import ingsoftware.zeroshop.repository.org.StockRepository;
import ingsoftware.zeroshop.repository.transaction.SaleOrderRepository;
import ingsoftware.zeroshop.service.catalog.ProductService;
import ingsoftware.zeroshop.service.transaction.SaleOrderService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.*;

@Controller("dashboardEmployeeController")
public class EmployeeController {

    private final OfficeRepository officeRepository;
    private final ProductService productService;
    private final StockRepository stockRepository;
    private final SaleOrderService saleOrderService;
    private final SaleOrderRepository saleOrderRepository;
    private final ingsoftware.zeroshop.repository.actor.ClientRepository clientRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public EmployeeController(OfficeRepository officeRepository,
                              ProductService productService,
                              StockRepository stockRepository,
                              SaleOrderService saleOrderService,
                              SaleOrderRepository saleOrderRepository,
                              ingsoftware.zeroshop.repository.actor.ClientRepository clientRepository) {
        this.officeRepository = officeRepository;
        this.productService = productService;
        this.stockRepository = stockRepository;
        this.saleOrderService = saleOrderService;
        this.saleOrderRepository = saleOrderRepository;
        this.clientRepository = clientRepository;
    }

    // GET /dashboard/employee: Panel principal o escritorio del empleado
    @GetMapping({"/dashboard/employee", "/dashboard/employee/"})
    public String employeeDesk(Model model) {
        model.addAttribute("title", "Escritorio del Empleado - Zero Shop");
        Office office = officeRepository.findAllByDeletedFalse().stream().findFirst().orElse(null);
        model.addAttribute("officeName", office != null ? office.getName() : "Sucursal Central");
        return "dashboard/employee/index";
    }

    // GET /dashboard/employee/desk: Punto de Venta (POS) y Mostrador en tienda física
    @GetMapping("/dashboard/employee/desk")
    public String employeeDeskDetails(@RequestParam(value = "officeId", required = false) UUID officeId,
                                      Model model) {
        List<Office> offices = officeRepository.findAllByDeletedFalse();
        Office currentOffice = null;
        if (officeId != null) {
            currentOffice = officeRepository.findActive(officeId).orElse(null);
        }
        if (currentOffice == null) {
            currentOffice = offices.stream().findFirst().orElse(null);
        }

        UUID activeOfficeId = currentOffice != null ? currentOffice.getId() : null;

        // Cargar productos activos con el stock específico de esta sucursal
        List<Product> rawProducts = productService.findAllActive();
        List<DeskProductDTO> deskProducts = new ArrayList<>();
        for (Product p : rawProducts) {
            int officeStock = 0;
            if (activeOfficeId != null) {
                officeStock = stockRepository.findByProductIdAndOfficeIdAndDeletedFalse(p.getId(), activeOfficeId)
                        .map(s -> s.getQuantity())
                        .orElse(0);
            }
            deskProducts.add(DeskProductDTO.builder()
                    .id(p.getId())
                    .code(p.getCode())
                    .name(p.getName())
                    .category(p.getCategory() != null ? p.getCategory().getName() : "")
                    .price(p.getCurrentPrice() != null ? p.getCurrentPrice() : BigDecimal.ZERO)
                    .stock(officeStock)
                    .imageUrl(p.getImageUrl())
                    .onSale(Boolean.TRUE.equals(p.getOnSale()))
                    .build());
        }

        // Serializar a JSON para búsqueda en vivo y autocompletado ultra veloz en el mostrador
        String productsJson = "[]";
        try {
            productsJson = objectMapper.writeValueAsString(deskProducts);
        } catch (Exception ignored) {
        }

        // Cargar últimas órdenes registradas en esta sucursal
        List<SaleOrder> recentSales = new ArrayList<>();
        if (activeOfficeId != null) {
            recentSales = saleOrderRepository.findByOfficeIdAndDeletedFalse(activeOfficeId).stream()
                    .filter(o -> o.getStatus() == OrderStatus.DELIVERED || o.getStatus() == OrderStatus.PAID)
                    .sorted((o1, o2) -> {
                        if (o1.getDate() == null) return 1;
                        if (o2.getDate() == null) return -1;
                        return o2.getDate().compareTo(o1.getDate());
                    })
                    .limit(10)
                    .toList();
        }

        model.addAttribute("title", "Mostrador y Punto de Venta (POS) - Zero Shop");
        model.addAttribute("offices", offices);
        model.addAttribute("currentOffice", currentOffice);
        model.addAttribute("officeName", currentOffice != null ? currentOffice.getName() : "Sucursal Central");
        model.addAttribute("products", deskProducts);
        model.addAttribute("productsJson", productsJson);
        model.addAttribute("recentSales", recentSales);
        model.addAttribute("paymentMethods", PaymentMethod.values());

        return "dashboard/employee/desk";
    }

    // POST /dashboard/employee/desk/sale: Procesar y registrar venta de mostrador
    @PostMapping("/dashboard/employee/desk/sale")
    public String registerDeskSale(@RequestParam("officeId") UUID officeId,
                                   @RequestParam("paymentMethod") PaymentMethod paymentMethod,
                                   @RequestParam(value = "clientDni", required = false) String clientDni,
                                   @RequestParam(value = "clientName", required = false) String clientName,
                                   @RequestParam("productIds") List<UUID> productIds,
                                   @RequestParam("quantities") List<Integer> quantities,
                                   @RequestParam(value = "amountReceived", required = false) BigDecimal amountReceived,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes) {
        if (clientDni == null || clientDni.trim().isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "El DNI del cliente es obligatorio para registrar la venta en mostrador.");
            return "redirect:/dashboard/employee/desk?officeId=" + officeId;
        }

        try {
            String employeeUser = authentication != null ? authentication.getName() : null;
            SaleOrder order = saleOrderService.createDeskSale(
                    officeId,
                    employeeUser,
                    clientDni,
                    clientName,
                    productIds,
                    quantities,
                    paymentMethod
            );

            // Calcular vuelto si fue en efectivo
            if (paymentMethod == PaymentMethod.CASH && amountReceived != null && amountReceived.compareTo(order.getTotalAmount()) >= 0) {
                BigDecimal change = amountReceived.subtract(order.getTotalAmount());
                redirectAttributes.addFlashAttribute("changeAmount", change);
                redirectAttributes.addFlashAttribute("amountReceived", amountReceived);
            }

            redirectAttributes.addFlashAttribute("saleSuccess", true);
            redirectAttributes.addFlashAttribute("ticketOrder", order);
            redirectAttributes.addFlashAttribute("ticketDetails", saleOrderService.getOrderDetails(order.getId()));
            redirectAttributes.addFlashAttribute("successMessage", "¡Venta #" + order.getId().toString().substring(0, 8).toUpperCase()
                    + " cobrada con éxito! Stock descontado de " + (order.getOffice() != null ? order.getOffice().getName() : "la sucursal") + ".");

        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al procesar la venta: " + e.getMessage());
        }

        return "redirect:/dashboard/employee/desk?officeId=" + officeId;
    }

    // GET /dashboard/employee/desk/search: API de búsqueda rápida por término y sucursal
    @GetMapping("/dashboard/employee/desk/search")
    @ResponseBody
    public List<DeskProductDTO> searchDeskProducts(@RequestParam("query") String query,
                                                   @RequestParam("officeId") UUID officeId) {
        if (query == null || query.trim().isBlank()) {
            return Collections.emptyList();
        }
        String clean = query.trim().toLowerCase();
        List<Product> products = productService.findAllActive();
        List<DeskProductDTO> result = new ArrayList<>();
        for (Product p : products) {
            boolean matches = (p.getName() != null && p.getName().toLowerCase().contains(clean))
                    || (p.getCode() != null && p.getCode().toLowerCase().contains(clean))
                    || (p.getCategory() != null && p.getCategory().getName() != null && p.getCategory().getName().toLowerCase().contains(clean));

            if (matches) {
                int stock = stockRepository.findByProductIdAndOfficeIdAndDeletedFalse(p.getId(), officeId)
                        .map(s -> s.getQuantity())
                        .orElse(0);

                result.add(DeskProductDTO.builder()
                        .id(p.getId())
                        .code(p.getCode())
                        .name(p.getName())
                        .category(p.getCategory() != null ? p.getCategory().getName() : "")
                        .price(p.getCurrentPrice() != null ? p.getCurrentPrice() : BigDecimal.ZERO)
                        .stock(stock)
                        .imageUrl(p.getImageUrl())
                        .onSale(Boolean.TRUE.equals(p.getOnSale()))
                        .build());
            }
        }
        return result;
    }

    // GET /dashboard/employee/desk/client-lookup: Autocompletado de cliente por DNI
    @GetMapping("/dashboard/employee/desk/client-lookup")
    @ResponseBody
    public Map<String, Object> lookupClientByDni(@RequestParam("dni") String dni) {
        if (dni == null || dni.trim().isBlank()) {
            return Map.of("found", false);
        }
        String cleanDni = dni.trim();
        return clientRepository.findAllByDeletedFalse().stream()
                .filter(c -> cleanDni.equalsIgnoreCase(c.getIdNumber()))
                .findFirst()
                .map(c -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("found", true);
                    map.put("firstName", c.getFirstName() != null ? c.getFirstName() : "");
                    map.put("lastName", c.getLastName() != null ? c.getLastName() : "");
                    map.put("clientNumber", c.getClientNumber() != null ? c.getClientNumber() : "");
                    return map;
                })
                .orElse(Map.of("found", false));
    }
}

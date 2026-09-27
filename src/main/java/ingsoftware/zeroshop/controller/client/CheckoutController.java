package ingsoftware.zeroshop.controller.client;

import ingsoftware.zeroshop.entity.org.Office;
import ingsoftware.zeroshop.entity.transaction.OrderDetail;
import ingsoftware.zeroshop.entity.transaction.SaleOrder;
import ingsoftware.zeroshop.enums.PaymentMethod;
import ingsoftware.zeroshop.repository.org.OfficeRepository;
import ingsoftware.zeroshop.service.transaction.SaleOrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller
public class CheckoutController {

    private final SaleOrderService saleOrderService;
    private final OfficeRepository officeRepository;

    public CheckoutController(SaleOrderService saleOrderService, OfficeRepository officeRepository) {
        this.saleOrderService = saleOrderService;
        this.officeRepository = officeRepository;
    }

    // GET /checkout: Muestra la página de finalización de compra con el carrito del cliente
    @GetMapping("/checkout")
    public String checkoutPage(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        SaleOrder cart = saleOrderService.getOrCreateCart(principal.getName());
        List<OrderDetail> cartItems = saleOrderService.getOrderDetails(cart.getId());
        List<Office> offices = officeRepository.findAllByDeletedFalse();

        model.addAttribute("cart", cart);
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", cart.getTotalAmount());
        model.addAttribute("offices", offices);

        return "client/checkout";
    }

    // POST /checkout/add: Agrega un producto al carrito
    @PostMapping("/checkout/add")
    public String addToCart(@RequestParam("productId") UUID productId,
                            @RequestParam(value = "quantity", defaultValue = "1") Integer quantity,
                            Principal principal,
                            RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }

        try {
            saleOrderService.addProductToCart(principal.getName(), productId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Producto agregado al carrito con éxito.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/checkout";
    }

    // POST /checkout/update: Actualiza la cantidad de un ítem en el carrito
    @PostMapping("/checkout/update")
    public String updateQuantity(@RequestParam("detailId") UUID detailId,
                                 @RequestParam("quantity") Integer quantity,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }

        try {
            saleOrderService.updateCartItemQuantity(principal.getName(), detailId, quantity);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/checkout";
    }

    // POST /checkout/remove: Elimina un ítem del carrito
    @PostMapping("/checkout/remove")
    public String removeItem(@RequestParam("detailId") UUID detailId,
                             Principal principal,
                             RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }

        try {
            saleOrderService.removeCartItem(principal.getName(), detailId);
            redirectAttributes.addFlashAttribute("infoMessage", "Ítem eliminado del carrito.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/checkout";
    }

    // POST /checkout: Procesa la compra y redirige a pago o éxito
    @PostMapping("/checkout")
    public String processCheckout(@RequestParam(value = "officeId", required = false) UUID officeId,
                                  @RequestParam(value = "street", defaultValue = "Av. San Martín") String street,
                                  @RequestParam(value = "number", defaultValue = "1250") String number,
                                  @RequestParam(value = "floor", required = false) String floor,
                                  @RequestParam(value = "apartment", required = false) String apartment,
                                  @RequestParam(value = "zipCode", defaultValue = "5500") String zipCode,
                                  @RequestParam(value = "city", defaultValue = "Mendoza") String city,
                                  @RequestParam(value = "phone", defaultValue = "+54 9 261 455-1234") String phone,
                                  @RequestParam("paymentMethod") PaymentMethod paymentMethod,
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }

        try {
            String redirectUrl = saleOrderService.processCheckout(
                    principal.getName(),
                    officeId,
                    street,
                    number,
                    floor,
                    apartment,
                    zipCode,
                    city,
                    phone,
                    paymentMethod
            );

            if (redirectUrl.startsWith("http")) {
                return "redirect:" + redirectUrl;
            } else {
                return "redirect:" + redirectUrl;
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al procesar el pedido: " + e.getMessage());
            return "redirect:/checkout";
        }
    }

    // GET /checkout/mp/success: Retorno exitoso de Mercado Pago
    @GetMapping("/checkout/mp/success")
    public String mpSuccess(@RequestParam("orderId") UUID orderId,
                            RedirectAttributes redirectAttributes) {
        try {
            saleOrderService.handleMercadoPagoSuccess(orderId);
            redirectAttributes.addFlashAttribute("successMessage", "¡Pago acreditado por Mercado Pago con éxito!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al confirmar con Mercado Pago: " + e.getMessage());
        }
        return "redirect:/checkout/success?orderId=" + orderId;
    }

    // GET /checkout/mp/failure: Retorno de pago fallido desde Mercado Pago
    @GetMapping("/checkout/mp/failure")
    public String mpFailure(@RequestParam("orderId") UUID orderId,
                            RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", "El pago a través de Mercado Pago no fue completado.");
        return "redirect:/checkout";
    }

    // GET /checkout/mp/pending: Retorno de pago pendiente desde Mercado Pago
    @GetMapping("/checkout/mp/pending")
    public String mpPending(@RequestParam("orderId") UUID orderId,
                            RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("infoMessage", "El pago de Mercado Pago está pendiente de acreditación.");
        return "redirect:/checkout/success?orderId=" + orderId;
    }

    // GET /checkout/success: Muestra la confirmación de la compra realizada con éxito
    @GetMapping("/checkout/success")
    public String checkoutSuccess(@RequestParam(value = "orderId", required = false) UUID orderId,
                                  Model model) {
        if (orderId != null) {
            saleOrderService.getOrderById(orderId).ifPresent(order -> {
                model.addAttribute("order", order);
                model.addAttribute("details", saleOrderService.getOrderDetails(order.getId()));
            });
        }
        return "client/checkout-success";
    }

}

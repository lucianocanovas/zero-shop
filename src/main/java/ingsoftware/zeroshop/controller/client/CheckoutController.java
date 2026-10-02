package ingsoftware.zeroshop.controller.client;

import ingsoftware.zeroshop.entity.location.Address;
import ingsoftware.zeroshop.entity.org.Office;
import ingsoftware.zeroshop.entity.transaction.OrderDetail;
import ingsoftware.zeroshop.entity.transaction.SaleOrder;
import ingsoftware.zeroshop.enums.PaymentMethod;
import ingsoftware.zeroshop.service.org.OfficeService;
import ingsoftware.zeroshop.service.transaction.MercadoPagoService;
import ingsoftware.zeroshop.service.transaction.SaleOrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
public class CheckoutController {

    private final SaleOrderService saleOrderService;
    private final OfficeService officeService;
    private final MercadoPagoService mercadoPagoService;

    public CheckoutController(SaleOrderService saleOrderService,
                              OfficeService officeService,
                              MercadoPagoService mercadoPagoService) {
        this.saleOrderService = saleOrderService;
        this.officeService = officeService;
        this.mercadoPagoService = mercadoPagoService;
    }

    // GET /checkout: Muestra la página de finalización de compra con el carrito del cliente
    @GetMapping("/checkout")
    public String checkoutPage(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        SaleOrder cart = saleOrderService.getOrCreateCart(principal.getName());
        List<OrderDetail> cartItems = saleOrderService.getOrderDetails(cart.getId());
        List<Office> offices = officeService.getAllOffices();
        List<Address> userAddresses = saleOrderService.getUserAddresses(principal.getName());

        model.addAttribute("cart", cart);
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", cart.getTotalAmount());
        model.addAttribute("offices", offices);
        model.addAttribute("userAddresses", userAddresses);

        return "client/checkout";
    }

    // POST /checkout/add: Agrega un producto al carrito manteniéndose en la misma página
    @PostMapping("/checkout/add")
    public String addToCart(@RequestParam("productId") UUID productId,
                            @RequestParam(value = "quantity", defaultValue = "1") Integer quantity,
                            @RequestParam(value = "redirectUrl", required = false) String redirectUrl,
                            jakarta.servlet.http.HttpServletRequest request,
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

        if (redirectUrl != null && !redirectUrl.isBlank()) {
            return "redirect:" + redirectUrl;
        }

        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isBlank()) {
            return "redirect:" + referer;
        }

        return "redirect:/products/" + productId;
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
    public String processCheckout(@RequestParam(value = "addressId", required = false) UUID addressId,
                                  @RequestParam(value = "saveNewAddress", defaultValue = "false") Boolean saveNewAddress,
                                  @RequestParam(value = "officeId", required = false) UUID officeId,
                                  @RequestParam(value = "street", required = false) String street,
                                  @RequestParam(value = "number", required = false) String number,
                                  @RequestParam(value = "floor", required = false) String floor,
                                  @RequestParam(value = "apartment", required = false) String apartment,
                                  @RequestParam(value = "zipCode", required = false) String zipCode,
                                  @RequestParam(value = "city", required = false) String city,
                                  @RequestParam(value = "phone", required = false) String phone,
                                  @RequestParam("paymentMethod") PaymentMethod paymentMethod,
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }

        try {
            String redirectUrl = saleOrderService.processCheckout(
                    principal.getName(),
                    addressId,
                    Boolean.TRUE.equals(saveNewAddress),
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

            return "redirect:" + redirectUrl;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al procesar el pedido: " + e.getMessage());
            return "redirect:/checkout";
        }
    }

    // GET /checkout/mp/success: Retorno exitoso de Mercado Pago
    @GetMapping("/checkout/mp/success")
    public String mpSuccess(@RequestParam(value = "orderId", required = false) UUID orderId,
                            @RequestParam(value = "external_reference", required = false) String externalReference,
                            @RequestParam(value = "payment_id", required = false) String paymentId,
                            @RequestParam(value = "collection_id", required = false) String collectionId,
                            @RequestParam(value = "collection_status", required = false) String collectionStatus,
                            RedirectAttributes redirectAttributes) {
        UUID targetOrderId = orderId;
        if (targetOrderId == null && externalReference != null && !externalReference.isBlank()) {
            try {
                targetOrderId = UUID.fromString(externalReference.trim());
            } catch (Exception ignored) {
            }
        }

        if (targetOrderId == null) {
            String pId = (paymentId != null && !paymentId.isBlank()) ? paymentId : collectionId;
            if (pId != null && !pId.isBlank()) {
                targetOrderId = mercadoPagoService.verifyPaymentApproved(pId).orElse(null);
            }
        }

        if (targetOrderId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "No se encontró el identificador del pedido.");
            return "redirect:/orders";
        }

        try {
            saleOrderService.handleMercadoPagoSuccess(targetOrderId);
            redirectAttributes.addFlashAttribute("successMessage", "¡Pago acreditado por Mercado Pago con éxito!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al confirmar con Mercado Pago: " + e.getMessage());
        }
        return "redirect:/checkout/success?orderId=" + targetOrderId;
    }

    // GET /checkout/mp/failure: Retorno de pago fallido desde Mercado Pago
    @GetMapping("/checkout/mp/failure")
    public String mpFailure(@RequestParam(value = "orderId", required = false) UUID orderId,
                            @RequestParam(value = "external_reference", required = false) String externalReference,
                            RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", "El pago a través de Mercado Pago no fue completado.");
        return "redirect:/checkout";
    }

    // GET /checkout/mp/pending: Retorno de pago pendiente desde Mercado Pago
    @GetMapping("/checkout/mp/pending")
    public String mpPending(@RequestParam(value = "orderId", required = false) UUID orderId,
                            @RequestParam(value = "external_reference", required = false) String externalReference,
                            RedirectAttributes redirectAttributes) {
        UUID targetOrderId = orderId;
        if (targetOrderId == null && externalReference != null && !externalReference.isBlank()) {
            try {
                targetOrderId = UUID.fromString(externalReference.trim());
            } catch (Exception ignored) {
            }
        }

        redirectAttributes.addFlashAttribute("infoMessage", "El pago de Mercado Pago está pendiente de acreditación.");
        return targetOrderId != null ? "redirect:/checkout/success?orderId=" + targetOrderId : "redirect:/orders";
    }

    // Webhook / IPN de Mercado Pago (recibe notificaciones POST/GET de Mercado Pago)
    @RequestMapping(value = "/checkout/mp/webhook", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public ResponseEntity<String> mpWebhook(
            @RequestParam(value = "topic", required = false) String topic,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "id", required = false) String id,
            @RequestParam(value = "data.id", required = false) String dataId,
            @RequestBody(required = false) Map<String, Object> body) {
        try {
            String paymentId = null;
            if ("payment".equalsIgnoreCase(topic) || "payment".equalsIgnoreCase(type)) {
                paymentId = id != null ? id : dataId;
            }
            if (paymentId == null && body != null) {
                if (body.get("data") instanceof Map<?, ?> dataMap) {
                    Object idObj = dataMap.get("id");
                    if (idObj != null) {
                        paymentId = idObj.toString();
                    }
                }
                if (paymentId == null && body.get("id") != null) {
                    paymentId = body.get("id").toString();
                }
            }

            if (paymentId != null && !paymentId.isBlank()) {
                mercadoPagoService.verifyPaymentApproved(paymentId)
                        .ifPresent(saleOrderService::handleMercadoPagoSuccess);
            }
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            return ResponseEntity.ok("IGNORED");
        }
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

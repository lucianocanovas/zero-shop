package ingsoftware.zeroshop.controller.admin;

import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

@Controller("adminProductController")
public class ProductController {

    // GET /admin/products: Lista todos los productos en el panel de administración
    @GetMapping("/admin/products")
    public String listProducts() {
        // LOGICA PARA LISTAR PRODUCTOS
        return "admin/products";
    }

    // GET /admin/products/:id: Muestra el detalle o edición de un producto
    @GetMapping("/admin/products/{id}")
    public String getProductDetail(@PathVariable("id") UUID id) {
        // LOGICA PARA OBTENER DETALLE DEL PRODUCTO
        return "admin/product-detail";
    }

    // POST /admin/products/: Guarda un nuevo producto
    @PostMapping("/admin/products/")
    public String createProduct() {
        // LOGICA PARA GUARDAR NUEVO PRODUCTO
        return "redirect:/admin/products";
    }

    // PUT /admin/products/:id: Actualiza un producto existente
    @PutMapping("/admin/products/{id}")
    public String updateProduct(@PathVariable("id") UUID id) {
        // LOGICA PARA ACTUALIZAR PRODUCTO
        return "redirect:/admin/products";
    }

    // DELETE /admin/products/:id: Elimina un producto
    @DeleteMapping("/admin/products/{id}")
    public String deleteProduct(@PathVariable("id") UUID id) {
        // LOGICA PARA ELIMINAR PRODUCTO
        return "redirect:/admin/products";
    }

    // GET /admin/products/:id/prices: Muestra el historial y gestión de precios de un producto
    @GetMapping("/admin/products/{id}/prices")
    public String getProductPrices(@PathVariable("id") UUID id) {
        // LOGICA PARA OBTENER HISTORIAL DE PRECIOS
        return "admin/product-prices";
    }

    // POST /admin/products/:id/prices: Registra un nuevo precio para el producto
    @PostMapping("/admin/products/{id}/prices")
    public String addProductPrice(@PathVariable("id") UUID id) {
        // LOGICA PARA REGISTRAR NUEVO PRECIO
        return "redirect:/admin/products/" + id + "/prices";
    }

    // PUT /admin/products/:id/prices/:priceId: Modifica un precio registrado
    @PutMapping("/admin/products/{id}/prices/{priceId}")
    public String updateProductPrice(@PathVariable("id") UUID id, @PathVariable("priceId") UUID priceId) {
        // LOGICA PARA ACTUALIZAR PRECIO
        return "redirect:/admin/products/" + id + "/prices";
    }

    // DELETE /admin/products/:id/prices/:priceId: Elimina un registro de precio
    @DeleteMapping("/admin/products/{id}/prices/{priceId}")
    public String deleteProductPrice(@PathVariable("id") UUID id, @PathVariable("priceId") UUID priceId) {
        // LOGICA PARA ELIMINAR PRECIO
        return "redirect:/admin/products/" + id + "/prices";
    }

    // GET /admin/stock: Muestra el panel de gestión de stock general y por sucursales
    @GetMapping("/admin/stock")
    public String getStock() {
        // LOGICA PARA OBTENER GESTION DE STOCK
        return "admin/stock";
    }

    // PUT /admin/stock/:id: Realiza un ajuste manual de stock para un producto
    @PutMapping("/admin/stock/{id}")
    public String updateStock(@PathVariable("id") UUID id) {
        // LOGICA PARA AJUSTAR STOCK DE PRODUCTO
        return "redirect:/admin/stock";
    }

}

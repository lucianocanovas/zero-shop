package ingsoftware.zeroshop.controller.dashboard;

import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

@Controller("dashboardProductController")
public class ProductController {

    // GET /dashboard/products: Lista todos los productos en el dashboard
    @GetMapping("/dashboard/products")
    public String listProducts() {
        // LOGICA PARA LISTAR PRODUCTOS
        return "dashboard/products";
    }

    // GET /dashboard/products/:id: Muestra el detalle o edición de un producto
    @GetMapping("/dashboard/products/{id}")
    public String getProductDetail(@PathVariable("id") UUID id) {
        // LOGICA PARA OBTENER DETALLE DEL PRODUCTO
        return "dashboard/product-detail";
    }

    // POST /dashboard/products/: Guarda un nuevo producto
    @PostMapping("/dashboard/products/")
    public String createProduct() {
        // LOGICA PARA GUARDAR NUEVO PRODUCTO
        return "redirect:/dashboard/products";
    }

    // PUT /dashboard/products/:id: Actualiza un producto existente
    @PutMapping("/dashboard/products/{id}")
    public String updateProduct(@PathVariable("id") UUID id) {
        // LOGICA PARA ACTUALIZAR PRODUCTO
        return "redirect:/dashboard/products";
    }

    // DELETE /dashboard/products/:id: Elimina un producto
    @DeleteMapping("/dashboard/products/{id}")
    public String deleteProduct(@PathVariable("id") UUID id) {
        // LOGICA PARA ELIMINAR PRODUCTO
        return "redirect:/dashboard/products";
    }

    // GET /dashboard/products/:id/prices: Muestra el historial y gestión de precios de un producto
    @GetMapping("/dashboard/products/{id}/prices")
    public String getProductPrices(@PathVariable("id") UUID id) {
        // LOGICA PARA OBTENER HISTORIAL DE PRECIOS
        return "dashboard/product-prices";
    }

    // POST /dashboard/products/{id}/prices: Registra un nuevo precio para el producto
    @PostMapping("/dashboard/products/{id}/prices")
    public String addProductPrice(@PathVariable("id") UUID id) {
        // LOGICA PARA REGISTRAR NUEVO PRECIO
        return "redirect:/dashboard/products/" + id + "/prices";
    }

    // PUT /dashboard/products/:id/prices/:priceId: Modifica un precio registrado
    @PutMapping("/dashboard/products/{id}/prices/{priceId}")
    public String updateProductPrice(@PathVariable("id") UUID id, @PathVariable("priceId") UUID priceId) {
        // LOGICA PARA ACTUALIZAR PRECIO
        return "redirect:/dashboard/products/" + id + "/prices";
    }

    // DELETE /dashboard/products/:id/prices/:priceId: Elimina un registro de precio
    @DeleteMapping("/dashboard/products/{id}/prices/{priceId}")
    public String deleteProductPrice(@PathVariable("id") UUID id, @PathVariable("priceId") UUID priceId) {
        // LOGICA PARA ELIMINAR PRECIO
        return "redirect:/dashboard/products/" + id + "/prices";
    }

    // GET /dashboard/stock: Muestra el panel de gestión de stock general y por sucursales
    @GetMapping("/dashboard/stock")
    public String getStock() {
        // LOGICA PARA OBTENER GESTION DE STOCK
        return "dashboard/stock";
    }

    // PUT /dashboard/stock/:id: Realiza un ajuste manual de stock para un producto
    @PutMapping("/dashboard/stock/{id}")
    public String updateStock(@PathVariable("id") UUID id) {
        // LOGICA PARA AJUSTAR STOCK DE PRODUCTO
        return "redirect:/dashboard/stock";
    }

}


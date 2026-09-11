package ingsoftware.zeroshop.controller.client;

import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller("clientProductController")
public class ProductController {

    // GET /products: Muestra el catálogo general de productos
    @GetMapping("/products")
    public String getAllProducts() {
        // LOGICA PARA OBTENER Y FILTRAR PRODUCTOS
        return "client/products";
    }

    // GET /products/:id: Muestra el detalle de un producto específico
    @GetMapping("/products/{id}")
    public String getProductById(@PathVariable("id") UUID id) {
        // LOGICA PARA OBTENER EL DETALLE DEL PRODUCTO
        return "client/product-detail";
    }

    // GET /offers: Muestra la sección de ofertas especiales
    @GetMapping("/offers")
    public String getOffers() {
        // LOGICA PARA OBTENER PRODUCTOS EN OFERTA
        return "client/offers";
    }

}

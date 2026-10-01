package ingsoftware.zeroshop.integration;

import ingsoftware.zeroshop.dto.PageResult;
import ingsoftware.zeroshop.entity.catalog.Product;
import ingsoftware.zeroshop.repository.catalog.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
public class ListingPaginationAndFilterIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ingsoftware.zeroshop.repository.location.CountryRepository countryRepository;

    @Autowired
    private ingsoftware.zeroshop.repository.location.StateRepository stateRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    // --- PageResult Unit Verification ---

    @Test
    @DisplayName("PageResult: Cálculo correcto de páginas, límites y navegación")
    void testPageResultCalculations() {
        List<Integer> items = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);
        PageResult<Integer> page1 = PageResult.of(items, 1, 6);

        assertEquals(1, page1.getPageNumber());
        assertEquals(6, page1.getPageSize());
        assertEquals(15, page1.getTotalElements());
        assertEquals(3, page1.getTotalPages());
        assertTrue(page1.isFirst());
        assertFalse(page1.isLast());
        assertFalse(page1.isHasPrevious());
        assertTrue(page1.isHasNext());
        assertEquals(6, page1.getContent().size());
        assertEquals(1, page1.getFromIndex());
        assertEquals(6, page1.getToIndex());

        PageResult<Integer> page3 = PageResult.of(items, 3, 6);
        assertEquals(3, page3.getPageNumber());
        assertTrue(page3.isLast());
        assertFalse(page3.isFirst());
        assertTrue(page3.isHasPrevious());
        assertFalse(page3.isHasNext());
        assertEquals(3, page3.getContent().size()); // remaining 13, 14, 15
        assertEquals(13, page3.getFromIndex());
        assertEquals(15, page3.getToIndex());

        // Empty list
        PageResult<String> empty = PageResult.of(Collections.emptyList(), 1, 10);
        assertEquals(0, empty.getTotalElements());
        assertEquals(1, empty.getTotalPages());
        assertEquals(0, empty.getContent().size());

        // Low / negative page index clamped to 1
        PageResult<Integer> clampedLow = PageResult.of(items, -5, 5);
        assertEquals(1, clampedLow.getPageNumber());

        // High page index clamped to max totalPages
        PageResult<Integer> clampedHigh = PageResult.of(items, 999, 5);
        assertEquals(3, clampedHigh.getPageNumber());
    }

    // --- Client Storefront Listings ---

    @Test
    @DisplayName("GET /products: Carga exitosa con atributos de paginación y búsqueda")
    void testClientProductsListing() throws Exception {
        mockMvc.perform(get("/products")
                        .param("search", "Zapatillas")
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("client/products"))
                .andExpect(model().attributeExists("products"))
                .andExpect(model().attributeExists("pageResult"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(model().attribute("search", "Zapatillas"));
    }

    @Test
    @DisplayName("GET /offers: Carga exitosa con paginación y filtro de ofertas")
    void testClientOffersListing() throws Exception {
        mockMvc.perform(get("/offers")
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("client/offers"))
                .andExpect(model().attributeExists("products"))
                .andExpect(model().attributeExists("pageResult"));
    }

    @Test
    @DisplayName("GET /categories: Carga exitosa con búsqueda y filtro de subcategorías")
    void testClientCategoriesListing() throws Exception {
        mockMvc.perform(get("/categories")
                        .param("search", "Calzado")
                        .param("hasSubs", "true")
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("client/categories"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(model().attributeExists("pageResult"));
    }

    @Test
    @WithMockUser(username = "client@gmail.com", roles = {"CLIENT"})
    @DisplayName("GET /orders: Carga del historial de compras de client@gmail.com")
    void testClientOrdersWithPurchases() throws Exception {
        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("client/orders"))
                .andExpect(model().attributeExists("orders"))
                .andExpect(model().attributeExists("pageResult"));
    }

    // --- Dashboard Listings (Employee & Admin) ---

    @Test
    @WithMockUser(username = "admin@zeroshop.com", roles = {"ADMIN"})
    @DisplayName("GET /dashboard/products: Carga con filtros de inventario y paginación")
    void testDashboardProductsListing() throws Exception {
        mockMvc.perform(get("/dashboard/products")
                        .param("search", "")
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/products"))
                .andExpect(model().attributeExists("products"))
                .andExpect(model().attributeExists("pageResult"));
    }

    @Test
    @WithMockUser(username = "admin@zeroshop.com", roles = {"ADMIN"})
    @DisplayName("GET /dashboard/categories: Carga con paginación y búsqueda")
    void testDashboardCategoriesListing() throws Exception {
        mockMvc.perform(get("/dashboard/categories")
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/categories"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(model().attributeExists("pageResult"));
    }

    @Test
    @WithMockUser(username = "admin@zeroshop.com", roles = {"ADMIN"})
    @DisplayName("GET /dashboard/stock: Carga con filtros por sucursal y nivel de stock")
    void testDashboardStockListing() throws Exception {
        mockMvc.perform(get("/dashboard/stock")
                        .param("stockLevel", "all")
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/stock"))
                .andExpect(model().attributeExists("stocks"))
                .andExpect(model().attributeExists("pageResult"));
    }

    @Test
    @WithMockUser(username = "admin@zeroshop.com", roles = {"ADMIN"})
    @DisplayName("GET /dashboard/sale-orders: Carga con búsqueda por cliente y estado")
    void testDashboardSaleOrdersListing() throws Exception {
        mockMvc.perform(get("/dashboard/sale-orders")
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/sale-orders"))
                .andExpect(model().attributeExists("orders"))
                .andExpect(model().attributeExists("pageResult"));
    }

    @Test
    @WithMockUser(username = "admin@zeroshop.com", roles = {"ADMIN"})
    @DisplayName("GET /dashboard/purchase-orders: Carga con filtro de proveedor y estado")
    void testDashboardPurchaseOrdersListing() throws Exception {
        mockMvc.perform(get("/dashboard/purchase-orders")
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/purchase-orders"))
                .andExpect(model().attributeExists("orders"))
                .andExpect(model().attributeExists("pageResult"));
    }

    @Test
    @WithMockUser(username = "admin@zeroshop.com", roles = {"ADMIN"})
    @DisplayName("GET /dashboard/providers: Carga con búsqueda y filtro de contacto")
    void testDashboardProvidersListing() throws Exception {
        mockMvc.perform(get("/dashboard/providers")
                        .param("hasContact", "true")
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/providers"))
                .andExpect(model().attributeExists("providers"))
                .andExpect(model().attributeExists("pageResult"));
    }

    @Test
    @WithMockUser(username = "admin@zeroshop.com", roles = {"ADMIN"})
    @DisplayName("GET /dashboard/admin/users: Carga con filtros por rol y estado")
    void testDashboardUsersListing() throws Exception {
        mockMvc.perform(get("/dashboard/admin/users")
                        .param("role", "ADMIN")
                        .param("status", "active")
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/admin/users"))
                .andExpect(model().attributeExists("users"))
                .andExpect(model().attributeExists("pageResult"))
                .andExpect(model().attributeExists("roles"));
    }

    @Test
    @WithMockUser(username = "admin@zeroshop.com", roles = {"ADMIN"})
    @DisplayName("GET /dashboard/admin/offices: Carga con filtro de tipo de sucursal")
    void testDashboardOfficesListing() throws Exception {
        mockMvc.perform(get("/dashboard/admin/offices")
                        .param("type", "HEADQUARTERS")
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/admin/offices"))
                .andExpect(model().attributeExists("offices"))
                .andExpect(model().attributeExists("pageResult"))
                .andExpect(model().attributeExists("officeTypes"));
    }

    // --- Cart Addition Without Redirecting to Checkout (Point 4) ---

    @Test
    @WithMockUser(username = "agustin.rodriguez@gmail.com", roles = {"CLIENT"})
    @DisplayName("POST /checkout/add: Mantiene la página actual con redirectUrl y muestra mensaje de éxito")
    void testAddToCartStaysOnCurrentPageWithRedirectUrl() throws Exception {
        Product product = productRepository.findAll().stream()
                .filter(p -> !Boolean.TRUE.equals(p.getDeleted()))
                .findFirst()
                .orElseThrow();

        mockMvc.perform(post("/checkout/add")
                        .with(csrf())
                        .param("productId", product.getId().toString())
                        .param("quantity", "1")
                        .param("redirectUrl", "/products/" + product.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products/" + product.getId()))
                .andExpect(flash().attribute("successMessage", "Producto agregado al carrito con éxito."));
    }

    @Test
    @WithMockUser(username = "agustin.rodriguez@gmail.com", roles = {"CLIENT"})
    @DisplayName("POST /checkout/add: Mantiene la página referer sin redirigir a /checkout")
    void testAddToCartStaysOnRefererPage() throws Exception {
        Product product = productRepository.findAll().stream()
                .filter(p -> !Boolean.TRUE.equals(p.getDeleted()))
                .findFirst()
                .orElseThrow();

        mockMvc.perform(post("/checkout/add")
                        .with(csrf())
                        .header("Referer", "http://localhost:8080/products")
                        .param("productId", product.getId().toString())
                        .param("quantity", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost:8080/products"))
                .andExpect(flash().attribute("successMessage", "Producto agregado al carrito con éxito."));
    }

    @Test
    @WithMockUser(username = "agustin.rodriguez@gmail.com", roles = {"CLIENT"})
    @DisplayName("POST /checkout/add: Sin referer ni redirectUrl vuelve a la página del producto y no a /checkout")
    void testAddToCartDefaultsToProductDetail() throws Exception {
        Product product = productRepository.findAll().stream()
                .filter(p -> !Boolean.TRUE.equals(p.getDeleted()))
                .findFirst()
                .orElseThrow();

        mockMvc.perform(post("/checkout/add")
                        .with(csrf())
                        .param("productId", product.getId().toString())
                        .param("quantity", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products/" + product.getId()))
                .andExpect(flash().attribute("successMessage", "Producto agregado al carrito con éxito."));
    }

    // --- Task 5 & Parameter Robustness: Mis Compras (/orders) sin error 500 con page vacía ---

    @Test
    @WithMockUser(username = "agustin.rodriguez@gmail.com", roles = {"CLIENT"})
    @DisplayName("GET /orders?page=: Devuelve 200 OK y no lanza error 500 cuando page está vacío o presente")
    void testClientOrdersReturns200WithEmptyPageParam() throws Exception {
        mockMvc.perform(get("/orders").param("page", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("client/orders"))
                .andExpect(model().attributeExists("orders"))
                .andExpect(model().attributeExists("pageResult"));

        mockMvc.perform(get("/orders").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("client/orders"));
    }

    // --- Task 7: Cascada de Ubicaciones (/api/locations/states y /api/locations/cities) ---

    @Test
    @DisplayName("GET /api/locations/states y /cities: Devuelven JSON 200 OK permitiendo habilitar provincias y ciudades")
    void testLocationsCascadeEndpoints() throws Exception {
        var countries = countryRepository.findAll();
        assertFalse(countries.isEmpty(), "Debe existir al menos un país cargado");
        var country = countries.get(0);

        mockMvc.perform(get("/api/locations/states").param("countryId", country.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"));

        var states = stateRepository.findByCountryIdAndDeletedFalse(country.getId());
        if (!states.isEmpty()) {
            var state = states.get(0);
            mockMvc.perform(get("/api/locations/cities").param("stateId", state.getId().toString()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith("application/json"));
        }
    }

    // --- Listings Robustness: Dashboard endpoints con page vacío ---

    @Test
    @WithMockUser(username = "admin@zeroshop.com", roles = {"ADMIN"})
    @DisplayName("GET /dashboard listings con page=: No lanzan error 500")
    void testDashboardEndpointsWithEmptyPageParam() throws Exception {
        mockMvc.perform(get("/dashboard/categories").param("page", ""))
                .andExpect(status().isOk());

        mockMvc.perform(get("/dashboard/stock").param("page", ""))
                .andExpect(status().isOk());

        mockMvc.perform(get("/dashboard/sale-orders").param("page", ""))
                .andExpect(status().isOk());

        mockMvc.perform(get("/dashboard/purchase-orders").param("page", ""))
                .andExpect(status().isOk());

        mockMvc.perform(get("/dashboard/providers").param("page", ""))
                .andExpect(status().isOk());

        mockMvc.perform(get("/dashboard/users").param("page", ""))
                .andExpect(status().isOk());

        mockMvc.perform(get("/dashboard/offices").param("page", ""))
                .andExpect(status().isOk());
    }
}

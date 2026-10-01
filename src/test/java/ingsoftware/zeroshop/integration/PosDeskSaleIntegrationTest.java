package ingsoftware.zeroshop.integration;

import ingsoftware.zeroshop.entity.actor.Client;
import ingsoftware.zeroshop.entity.catalog.Product;
import ingsoftware.zeroshop.entity.location.City;
import ingsoftware.zeroshop.entity.org.Office;
import ingsoftware.zeroshop.entity.org.Stock;
import ingsoftware.zeroshop.entity.transaction.SaleOrder;
import ingsoftware.zeroshop.enums.IDType;
import ingsoftware.zeroshop.enums.OfficeType;
import ingsoftware.zeroshop.enums.PaymentMethod;
import ingsoftware.zeroshop.enums.Size;
import ingsoftware.zeroshop.repository.actor.ClientRepository;
import ingsoftware.zeroshop.repository.catalog.ProductRepository;
import ingsoftware.zeroshop.repository.location.CityRepository;
import ingsoftware.zeroshop.repository.org.OfficeRepository;
import ingsoftware.zeroshop.repository.org.StockRepository;
import ingsoftware.zeroshop.repository.transaction.SaleOrderRepository;
import ingsoftware.zeroshop.service.catalog.ProductService;
import ingsoftware.zeroshop.service.transaction.SaleOrderService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
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
public class PosDeskSaleIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private SaleOrderService saleOrderService;

    @Autowired
    private OfficeRepository officeRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private SaleOrderRepository saleOrderRepository;

    private MockMvc mockMvc;
    private Office testOffice;
    private Product testProduct;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        testOffice = officeRepository.findAllByDeletedFalse().stream().findFirst().orElseGet(() -> {
            Office o = new Office();
            o.setName("Sucursal Test POS");
            o.setCuit("30-99887766-9");
            o.setType(OfficeType.BRANCH);
            o.setDeleted(false);
            return officeRepository.save(o);
        });

        testProduct = productService.createProduct(
                Product.builder()
                        .code("POS-TEST-" + UUID.randomUUID().toString().substring(0, 5))
                        .name("Zapatillas Running POS Test")
                        .size(Size.L)
                        .build(),
                new BigDecimal("35000.00"),
                null
        );

        Stock stock = stockRepository.findByProductIdAndOfficeIdAndDeletedFalse(testProduct.getId(), testOffice.getId())
                .orElseGet(() -> {
                    Stock s = new Stock();
                    s.setProduct(testProduct);
                    s.setOffice(testOffice);
                    s.setQuantity(0);
                    s.setDeleted(false);
                    return s;
                });
        stock.setQuantity(50);
        stockRepository.save(stock);
    }

    @Test
    @DisplayName("POS: Venta sin DNI de cliente debe lanzar excepción en el servicio")
    public void testSaleWithoutClientDniFailsInService() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            saleOrderService.createDeskSale(
                    testOffice.getId(),
                    "admin@zeroshop.com",
                    null, // DNI nulo
                    "Cliente Anonimo",
                    List.of(testProduct.getId()),
                    List.of(1),
                    PaymentMethod.CASH
            );
        });

        assertTrue(ex.getMessage().contains("obligatorio registrar los datos del cliente"));
    }

    @Test
    @DisplayName("POS: Venta con DNI y nombre registra un nuevo cliente y completa la orden")
    public void testSaleWithNewClientSuccess() {
        String testDni = "40" + (int)(Math.random() * 900000 + 100000);
        String clientFullName = "Carlos Tevez";

        SaleOrder order = saleOrderService.createDeskSale(
                testOffice.getId(),
                "admin@zeroshop.com",
                testDni,
                clientFullName,
                List.of(testProduct.getId()),
                List.of(2),
                PaymentMethod.CASH
        );

        assertNotNull(order.getId());
        assertNotNull(order.getClient(), "La orden debe tener un cliente asociado");
        assertEquals(testDni, order.getClient().getIdNumber());
        assertEquals("Carlos", order.getClient().getFirstName());
        assertEquals("Tevez", order.getClient().getLastName());
        assertEquals(new BigDecimal("70000.00"), order.getTotalAmount());
    }

    @Test
    @DisplayName("POS: Venta con DNI de cliente existente asocia el cliente ya registrado sin duplicar")
    public void testSaleWithExistingClientSuccess() {
        String testDni = "35" + (int)(Math.random() * 900000 + 100000);
        Client existing = new Client();
        existing.setFirstName("Lionel");
        existing.setLastName("Messi");
        existing.setIdType(IDType.DNI);
        existing.setIdNumber(testDni);
        existing.setDateOfBirth(LocalDate.of(1987, 6, 24));
        existing.setClientNumber("CLI-MESSI-" + testDni);
        existing.setDeleted(false);
        existing = clientRepository.save(existing);

        SaleOrder order = saleOrderService.createDeskSale(
                testOffice.getId(),
                "admin@zeroshop.com",
                testDni,
                "Lionel Messi",
                List.of(testProduct.getId()),
                List.of(1),
                PaymentMethod.DEBIT
        );

        assertNotNull(order.getId());
        assertEquals(existing.getId(), order.getClient().getId());
        assertEquals("Lionel", order.getClient().getFirstName());
        assertEquals("Messi", order.getClient().getLastName());
    }

    @Test
    @DisplayName("POS Controller: Endpoint /dashboard/employee/desk/sale sin DNI redirige con error")
    @WithMockUser(username = "admin@zeroshop.com", roles = {"ADMIN", "EMPLOYEE"})
    public void testControllerDeskSaleWithoutDniRedirectsWithError() throws Exception {
        mockMvc.perform(post("/dashboard/employee/desk/sale")
                        .param("officeId", testOffice.getId().toString())
                        .param("paymentMethod", "CASH")
                        .param("clientDni", "")
                        .param("productIds", testProduct.getId().toString())
                        .param("quantities", "1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    @DisplayName("POS Controller: Endpoint /dashboard/employee/desk/client-lookup encuentra cliente existente")
    @WithMockUser(username = "admin@zeroshop.com", roles = {"ADMIN", "EMPLOYEE"})
    public void testClientLookupEndpoint() throws Exception {
        String testDni = "28" + (int)(Math.random() * 900000 + 100000);
        Client c = new Client();
        c.setFirstName("Diego");
        c.setLastName("Maradona");
        c.setIdType(IDType.DNI);
        c.setIdNumber(testDni);
        c.setDateOfBirth(LocalDate.of(1960, 10, 30));
        c.setClientNumber("CLI-DIEGO-" + testDni);
        c.setDeleted(false);
        clientRepository.save(c);

        mockMvc.perform(get("/dashboard/employee/desk/client-lookup")
                        .param("dni", testDni))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.found").value(true))
                .andExpect(jsonPath("$.firstName").value("Diego"))
                .andExpect(jsonPath("$.lastName").value("Maradona"));

        // DNI inexistente
        mockMvc.perform(get("/dashboard/employee/desk/client-lookup")
                        .param("dni", "99999999999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.found").value(false));
    }
}

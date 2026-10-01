package ingsoftware.zeroshop.integration;

import ingsoftware.zeroshop.entity.catalog.Product;
import ingsoftware.zeroshop.entity.org.Office;
import ingsoftware.zeroshop.entity.transaction.SaleOrder;
import ingsoftware.zeroshop.enums.OfficeType;
import ingsoftware.zeroshop.enums.PaymentMethod;
import ingsoftware.zeroshop.enums.Size;
import ingsoftware.zeroshop.repository.catalog.ProductRepository;
import ingsoftware.zeroshop.repository.org.OfficeRepository;
import ingsoftware.zeroshop.service.catalog.ProductService;
import ingsoftware.zeroshop.service.org.StockService;
import ingsoftware.zeroshop.service.transaction.SaleOrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@Transactional
public class OrderDetailViewIntegrationTest {

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
    private StockService stockService;

    private MockMvc mockMvc;
    private Office office;
    private Product product;
    private final String clientUsername = "client@gmail.com";

    @BeforeEach
    public void setUp() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        List<Office> offices = officeRepository.findAllByDeletedFalse();
        if (offices.isEmpty()) {
            Office newOffice = new Office();
            newOffice.setName("Sucursal Mendoza Test");
            newOffice.setCuit("30-99887766-5");
            newOffice.setType(OfficeType.BRANCH);
            newOffice.setDeleted(false);
            office = officeRepository.save(newOffice);
        } else {
            office = offices.get(0);
        }

        List<Product> products = productRepository.findAllByDeletedFalse();
        if (products.isEmpty()) {
            product = productService.createProduct(
                    Product.builder()
                            .code("DETAIL-PROD-" + UUID.randomUUID().toString().substring(0, 5))
                            .name("Zapatilla Running Zero Test")
                            .size(Size.M)
                            .build(),
                    new BigDecimal("35000.00"),
                    null
            );
        } else {
            product = products.get(0);
        }

        stockService.incrementStock(product.getId(), office.getId(), 20);
    }

    @Test
    @DisplayName("Renderizado de client/order-detail sin errores de template parsing")
    public void testRenderOrderDetailTemplateSuccessfully() throws Exception {
        // 1. Agregar al carrito y procesar checkout
        SaleOrder cart = saleOrderService.addProductToCart(clientUsername, product.getId(), 1);

        saleOrderService.processCheckout(
                clientUsername,
                office.getId(),
                "San Martín",
                "1234",
                null,
                null,
                "5500",
                "Mendoza Capital",
                "2615555555",
                PaymentMethod.MERCADO_PAGO
        );

        // 2. Probar GET /orders/{id}
        mockMvc.perform(get("/orders/{id}", cart.getId())
                        .with(user(clientUsername).roles("CLIENT")))
                .andExpect(status().isOk())
                .andExpect(view().name("client/order-detail"));
    }
}

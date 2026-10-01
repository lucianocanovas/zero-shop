package ingsoftware.zeroshop.service.transaction;

import ingsoftware.zeroshop.entity.actor.Client;
import ingsoftware.zeroshop.entity.actor.Employee;
import ingsoftware.zeroshop.entity.actor.Person;
import ingsoftware.zeroshop.entity.actor.User;
import ingsoftware.zeroshop.entity.catalog.PriceHistory;
import ingsoftware.zeroshop.entity.catalog.Product;
import ingsoftware.zeroshop.entity.location.Address;
import ingsoftware.zeroshop.entity.org.Office;
import ingsoftware.zeroshop.entity.transaction.OrderDetail;
import ingsoftware.zeroshop.entity.transaction.SaleOrder;
import ingsoftware.zeroshop.enums.IDType;
import ingsoftware.zeroshop.enums.OrderStatus;
import ingsoftware.zeroshop.enums.PaymentMethod;
import ingsoftware.zeroshop.repository.actor.ClientRepository;
import ingsoftware.zeroshop.repository.actor.UserRepository;
import ingsoftware.zeroshop.repository.catalog.PriceHistoryRepository;
import ingsoftware.zeroshop.repository.catalog.ProductRepository;
import ingsoftware.zeroshop.repository.location.AddressRepository;
import ingsoftware.zeroshop.repository.org.OfficeRepository;
import ingsoftware.zeroshop.repository.transaction.OrderDetailRepository;
import ingsoftware.zeroshop.repository.transaction.SaleOrderRepository;
import ingsoftware.zeroshop.service.notification.EmailService;
import ingsoftware.zeroshop.service.org.StockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SaleOrderService {

    private static final Logger log = LoggerFactory.getLogger(SaleOrderService.class);

    private final SaleOrderRepository saleOrderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductRepository productRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final OfficeRepository officeRepository;
    private final AddressRepository addressRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final PaymentService paymentService;
    private final StockService stockService;
    private final MercadoPagoService mercadoPagoService;
    private final EmailService emailService;

    public SaleOrderService(SaleOrderRepository saleOrderRepository,
                            OrderDetailRepository orderDetailRepository,
                            ProductRepository productRepository,
                            PriceHistoryRepository priceHistoryRepository,
                            ClientRepository clientRepository,
                            UserRepository userRepository,
                            OfficeRepository officeRepository,
                            AddressRepository addressRepository,
                            PaymentService paymentService,
                            StockService stockService,
                            MercadoPagoService mercadoPagoService,
                            EmailService emailService) {
        this.saleOrderRepository = saleOrderRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.productRepository = productRepository;
        this.priceHistoryRepository = priceHistoryRepository;
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
        this.officeRepository = officeRepository;
        this.addressRepository = addressRepository;
        this.paymentService = paymentService;
        this.stockService = stockService;
        this.mercadoPagoService = mercadoPagoService;
        this.emailService = emailService;
    }

    /**
     * Obtiene o crea el cliente/persona asociado al usuario autenticado.
     */
    @Transactional
    public Person getOrCreateClientForUser(String username) {
        User user = userRepository.findByUsernameIgnoreCaseAndDeletedFalse(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + username));

        if (user.getPerson() != null) {
            return user.getPerson();
        }

        // Si la persona aún no está inicializada, se crea automáticamente como cliente
        Client newClient = new Client();
        newClient.setFirstName(user.getUsername().split("@")[0]);
        newClient.setLastName("Cliente");
        newClient.setClientNumber("CLI-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        newClient.setIdType(IDType.DNI);
        newClient.setIdNumber(String.valueOf(Math.abs(UUID.randomUUID().getMostSignificantBits())).substring(0, 8));
        newClient.setDateOfBirth(LocalDate.of(2000, 1, 1));
        newClient.setDeleted(false);
        Client savedClient = clientRepository.save(newClient);

        user.setPerson(savedClient);
        userRepository.save(user);

        return savedClient;
    }

    /**
     * Obtiene o crea el carrito activo (SaleOrder con status ON_CART).
     */
    @Transactional
    public SaleOrder getOrCreateCart(String username) {
        Person client = getOrCreateClientForUser(username);

        return saleOrderRepository.findByClientIdAndStatusAndDeletedFalse(client.getId(), OrderStatus.ON_CART)
                .orElseGet(() -> {
                    Office defaultOffice = officeRepository.findAllByDeletedFalse().stream().findFirst().orElse(null);
                    SaleOrder cart = SaleOrder.builder()
                            .client(client)
                            .office(defaultOffice)
                            .date(LocalDateTime.now())
                            .status(OrderStatus.ON_CART)
                            .totalAmount(BigDecimal.ZERO)
                            .deleted(false)
                            .build();
                    return saleOrderRepository.save(cart);
                });
    }

    /**
     * Obtiene los detalles de un carrito u orden.
     */
    public List<OrderDetail> getOrderDetails(UUID orderId) {
        return orderDetailRepository.findByOrderIdAndDeletedFalse(orderId);
    }

    /**
     * Agrega un producto al carrito de compras del usuario.
     */
    @Transactional
    public SaleOrder addProductToCart(String username, UUID productId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            quantity = 1;
        }

        SaleOrder cart = getOrCreateCart(username);
        Product product = productRepository.findActive(productId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + productId));

        BigDecimal price = priceHistoryRepository
                .findFirstByProductIdAndDeletedFalseOrderByStartDateDesc(product.getId())
                .map(ph -> ph.getPrice())
                .orElse(product.getCurrentPrice() != null ? product.getCurrentPrice() : new BigDecimal("24990.00"));

        Optional<OrderDetail> existingDetailOpt = orderDetailRepository.findByOrderIdAndProductIdAndDeletedFalse(cart.getId(), productId);

        if (existingDetailOpt.isPresent()) {
            OrderDetail detail = existingDetailOpt.get();
            detail.setQuantity(detail.getQuantity() + quantity);
            detail.setTotal(detail.getUnitPrice().multiply(BigDecimal.valueOf(detail.getQuantity())));
            orderDetailRepository.save(detail);
        } else {
            OrderDetail newDetail = OrderDetail.builder()
                    .order(cart)
                    .product(product)
                    .quantity(quantity)
                    .unitPrice(price)
                    .total(price.multiply(BigDecimal.valueOf(quantity)))
                    .deleted(false)
                    .build();
            orderDetailRepository.save(newDetail);
        }

        recalculateCartTotal(cart);
        return cart;
    }

    /**
     * Actualiza la cantidad de un ítem en el carrito.
     */
    @Transactional
    public void updateCartItemQuantity(String username, UUID detailId, Integer quantity) {
        SaleOrder cart = getOrCreateCart(username);
        OrderDetail detail = orderDetailRepository.findActive(detailId)
                .orElseThrow(() -> new IllegalArgumentException("Detalle no encontrado con ID: " + detailId));

        if (!detail.getOrder().getId().equals(cart.getId())) {
            throw new SecurityException("No tienes permiso para modificar este ítem.");
        }

        if (quantity == null || quantity <= 0) {
            detail.setDeleted(true);
        } else {
            detail.setQuantity(quantity);
            detail.setTotal(detail.getUnitPrice().multiply(BigDecimal.valueOf(quantity)));
        }
        orderDetailRepository.save(detail);
        recalculateCartTotal(cart);
    }

    /**
     * Elimina un ítem del carrito.
     */
    @Transactional
    public void removeCartItem(String username, UUID detailId) {
        SaleOrder cart = getOrCreateCart(username);
        OrderDetail detail = orderDetailRepository.findActive(detailId)
                .orElseThrow(() -> new IllegalArgumentException("Detalle no encontrado con ID: " + detailId));

        if (!detail.getOrder().getId().equals(cart.getId())) {
            throw new SecurityException("No tienes permiso para eliminar este ítem.");
        }

        detail.setDeleted(true);
        orderDetailRepository.save(detail);
        recalculateCartTotal(cart);
    }

    private void recalculateCartTotal(SaleOrder cart) {
        List<OrderDetail> details = orderDetailRepository.findByOrderIdAndDeletedFalse(cart.getId());
        BigDecimal total = details.stream()
                .filter(d -> d != null && d.getTotal() != null)
                .map(d -> d.getTotal())
                .reduce(BigDecimal.ZERO, (a, b) -> a.add(b));
        cart.setTotalAmount(total);
        saleOrderRepository.save(cart);
    }

    /**
     * Procesa la confirmación de compra y prepara el pago según el método seleccionado.
     */
    @Transactional
    public String processCheckout(String username,
                                  UUID officeId,
                                  String street,
                                  String number,
                                  String floor,
                                  String apartment,
                                  String zipCode,
                                  String city,
                                  String phone,
                                  PaymentMethod paymentMethod) {

        SaleOrder order = getOrCreateCart(username);
        List<OrderDetail> details = orderDetailRepository.findByOrderIdAndDeletedFalse(order.getId());

        if (details.isEmpty() || order.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("El carrito de compras se encuentra vacío.");
        }

        // Sucursal de despacho/retiro
        Office office = null;
        if (officeId != null) {
            office = officeRepository.findActive(officeId).orElse(null);
        }
        if (office == null) {
            List<Office> offices = officeRepository.findAllByDeletedFalse();
            if (!offices.isEmpty()) {
                office = offices.get(0);
            }
        }
        order.setOffice(office);

        // Dirección de entrega
        Address shippingAddress = Address.builder()
                .street(street != null && !street.isBlank() ? street : "Dirección Cliente")
                .number(number != null && !number.isBlank() ? number : "S/N")
                .floor(floor)
                .apartment(apartment)
                .zipCode(zipCode != null && !zipCode.isBlank() ? zipCode : "5500")
                .observations(phone != null ? "Tel: " + phone : null)
                .deleted(false)
                .build();
        Address savedAddress = addressRepository.save(shippingAddress);
        order.setShippingAddress(savedAddress);
        order.setDate(LocalDateTime.now());

        if (paymentMethod == PaymentMethod.MERCADO_PAGO) {
            saleOrderRepository.save(order);
            // Crea preferencia y obtiene URL de pago en Mercado Pago
            return mercadoPagoService.createPreference(order, details);
        } else if (paymentMethod == PaymentMethod.CASH) {
            order.setStatus(OrderStatus.PENDING_PAYMENT);
            saleOrderRepository.save(order);

            paymentService.registerPayment(order, order.getTotalAmount(), PaymentMethod.CASH);
            sendOrderEmailNotification(order, details, username);

            return "/checkout/success?orderId=" + order.getId();
        } else {
            // Tarjeta de Crédito / Débito directa
            order.setStatus(OrderStatus.PAID);
            saleOrderRepository.save(order);

            paymentService.registerPayment(order, order.getTotalAmount(), paymentMethod != null ? paymentMethod : PaymentMethod.CREDIT);
            paymentService.createInvoiceForOrder(order, details);
            decrementStockForOrder(order, details);
            sendOrderEmailNotification(order, details, username);

            return "/checkout/success?orderId=" + order.getId();
        }
    }

    /**
     * Callback exitoso desde Mercado Pago.
     */
    @Transactional
    public SaleOrder handleMercadoPagoSuccess(UUID orderId) {
        SaleOrder order = saleOrderRepository.findActive(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada: " + orderId));

        if (order.getStatus() != OrderStatus.PAID) {
            order.setStatus(OrderStatus.PAID);
            saleOrderRepository.save(order);

            List<OrderDetail> details = orderDetailRepository.findByOrderIdAndDeletedFalse(order.getId());
            paymentService.registerPayment(order, order.getTotalAmount(), PaymentMethod.MERCADO_PAGO);
            paymentService.createInvoiceForOrder(order, details);
            decrementStockForOrder(order, details);

            // Buscar email real del cliente si existe
            userRepository.findAllByDeletedFalse().stream()
                    .filter(u -> u.getPerson() != null && u.getPerson().getId().equals(order.getClient().getId()))
                    .findFirst()
                    .ifPresent(u -> sendOrderEmailNotification(order, details, u.getUsername()));
        }
        return order;
    }

    /**
     * Registra el cobro en efectivo de una orden existente y descuenta inmediatamente el stock.
     */
    @Transactional
    public SaleOrder payOrderWithCash(UUID orderId) {
        SaleOrder order = saleOrderRepository.findActive(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada: " + orderId));

        if (order.getStatus() == OrderStatus.PAID) {
            return order;
        }

        order.setStatus(OrderStatus.PAID);
        saleOrderRepository.save(order);

        List<OrderDetail> details = orderDetailRepository.findByOrderIdAndDeletedFalse(order.getId());
        paymentService.registerPayment(order, order.getTotalAmount(), PaymentMethod.CASH);
        paymentService.createInvoiceForOrder(order, details);
        decrementStockForOrder(order, details);

        if (order.getClient() != null) {
            userRepository.findAllByDeletedFalse().stream()
                    .filter(u -> u.getPerson() != null && u.getPerson().getId().equals(order.getClient().getId()))
                    .findFirst()
                    .ifPresent(u -> sendOrderEmailNotification(order, details, u.getUsername()));
        }

        return order;
    }

    private void decrementStockForOrder(SaleOrder order, List<OrderDetail> details) {
        if (order.getOffice() == null) {
            return;
        }
        UUID officeId = order.getOffice().getId();
        for (OrderDetail detail : details) {
            try {
                stockService.decrementStock(detail.getProduct().getId(), officeId, detail.getQuantity());
            } catch (Exception e) {
                log.warn("No se pudo descontar stock automáticamente para el producto {}: {}", detail.getProduct().getName(), e.getMessage());
            }
        }
    }

    private void sendOrderEmailNotification(SaleOrder order, List<OrderDetail> details, String recipientEmail) {
        try {
            String customerName = (order.getClient() != null && order.getClient().getFirstName() != null)
                    ? order.getClient().getFirstName() + " " + (order.getClient().getLastName() != null ? order.getClient().getLastName() : "")
                    : "Cliente";

            String shippingAddress = (order.getShippingAddress() != null)
                    ? order.getShippingAddress().getStreet() + " " + order.getShippingAddress().getNumber()
                    : "Retiro en sucursal";

            List<String> items = details.stream()
                    .map(d -> d.getProduct().getName() + " x " + d.getQuantity() + " ($" + d.getTotal() + ")")
                    .toList();

            emailService.sendOrderReceiptEmail(
                    recipientEmail,
                    customerName.trim(),
                    order.getId(),
                    order.getTotalAmount(),
                    order.getStatus() != null ? order.getStatus().getDisplayName() : "En proceso",
                    shippingAddress,
                    items
            );
        } catch (Exception e) {
            log.warn("No se pudo enviar correo de confirmación: {}", e.getMessage());
        }
    }

    /**
     * Lista todas las órdenes confirmadas para el panel de empleados.
     */
    public List<SaleOrder> getAllConfirmedOrders() {
        return saleOrderRepository.findByStatusNotAndDeletedFalseOrderByDateDesc(OrderStatus.ON_CART);
    }

    /**
     * Lista todas las órdenes de un cliente (excluyendo el carrito en preparación).
     */
    public List<SaleOrder> getClientOrders(String username) {
        Person client = getOrCreateClientForUser(username);
        return saleOrderRepository.findByClientIdAndStatusNotAndDeletedFalseOrderByDateDesc(client.getId(), OrderStatus.ON_CART);
    }

    public Optional<SaleOrder> getOrderById(UUID id) {
        return saleOrderRepository.findActive(id);
    }

    /**
     * Actualiza el estado de la orden (utilizado por los empleados en el panel de control).
     */
    @Transactional
    public void updateOrderStatus(UUID orderId, OrderStatus newStatus) {
        SaleOrder order = saleOrderRepository.findActive(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada: " + orderId));

        OrderStatus previousStatus = order.getStatus();
        order.setStatus(newStatus);
        saleOrderRepository.save(order);

        // Si se cambia de PENDIENTE DE PAGO a PAGO REALIZADO (o estados posteriores), descontar stock
        if (previousStatus == OrderStatus.PENDING_PAYMENT && (newStatus == OrderStatus.PAID 
                || newStatus == OrderStatus.PENDING_SHIPPING 
                || newStatus == OrderStatus.PENDING_DELIVERY 
                || newStatus == OrderStatus.DELIVERED)) {
            List<OrderDetail> details = orderDetailRepository.findByOrderIdAndDeletedFalse(order.getId());
            decrementStockForOrder(order, details);
            paymentService.createInvoiceForOrder(order, details);
        } else if (newStatus == OrderStatus.PAID 
                || newStatus == OrderStatus.PENDING_SHIPPING 
                || newStatus == OrderStatus.PENDING_DELIVERY 
                || newStatus == OrderStatus.DELIVERED) {
            // Asegurar que si la orden está en estado pagado o posterior, tenga su factura emitida
            List<OrderDetail> details = orderDetailRepository.findByOrderIdAndDeletedFalse(order.getId());
            paymentService.createInvoiceForOrder(order, details);
        }
    }

    /**
     * Permite cancelar una orden al cliente o empleado.
     */
    @Transactional
    public void cancelOrder(UUID orderId, String username) {
        SaleOrder order = saleOrderRepository.findActive(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Orden no encontrada: " + orderId));

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("No se puede cancelar una orden que ya ha sido entregada.");
        }

        // Si ya estaba pagada, se repone el stock
        if (order.getStatus() == OrderStatus.PAID && order.getOffice() != null) {
            List<OrderDetail> details = orderDetailRepository.findByOrderIdAndDeletedFalse(order.getId());
            for (OrderDetail detail : details) {
                try {
                    stockService.incrementStock(detail.getProduct().getId(), order.getOffice().getId(), detail.getQuantity());
                } catch (Exception e) {
                    log.warn("No se pudo reponer stock para el producto {}: {}", detail.getProduct().getName(), e.getMessage());
                }
            }
        }

        order.setStatus(OrderStatus.CANCELLED);
        saleOrderRepository.save(order);
        paymentService.cancelInvoiceForOrder(order.getId());
    }

    /**
     * Registra una venta directa de mostrador (POS) en tienda física.
     * Crea la orden con estado DELIVERED, asocia los ítems, descuenta el stock
     * de la sucursal especificada y registra el cobro.
     */
    @Transactional
    public SaleOrder createDeskSale(UUID officeId,
                                   String employeeUsername,
                                   String clientDni,
                                   String clientName,
                                   List<UUID> productIds,
                                   List<Integer> quantities,
                                   PaymentMethod paymentMethod) {
        if (productIds == null || productIds.isEmpty()) {
            throw new IllegalArgumentException("Debe agregar al menos un producto a la venta.");
        }
        if (quantities == null || quantities.size() != productIds.size()) {
            throw new IllegalArgumentException("La cantidad de productos e ítems no coincide.");
        }

        // 1. Obtener sucursal
        Office office = null;
        if (officeId != null) {
            office = officeRepository.findActive(officeId).orElse(null);
        }
        if (office == null) {
            office = officeRepository.findAllByDeletedFalse().stream().findFirst()
                    .orElseThrow(() -> new IllegalStateException("No hay sucursales activas registradas."));
        }

        // 2. Resolver empleado autenticado
        Employee employee = null;
        if (employeeUsername != null) {
            Optional<User> empUserOpt = userRepository.findByUsernameIgnoreCaseAndDeletedFalse(employeeUsername);
            if (empUserOpt.isPresent() && empUserOpt.get().getPerson() instanceof Employee emp) {
                employee = emp;
            }
        }

        // 3. Resolver cliente (OBLIGATORIO: Se deben registrar los datos del cliente para vender)
        if (clientDni == null || clientDni.trim().isBlank()) {
            throw new IllegalArgumentException("Es obligatorio registrar los datos del cliente (DNI) para poder realizar una venta.");
        }

        String cleanDni = clientDni.trim();
        Client client = clientRepository.findAllByDeletedFalse().stream()
                .filter(c -> cleanDni.equalsIgnoreCase(c.getIdNumber()))
                .findFirst()
                .orElse(null);

        if (client == null) {
            if (clientName == null || clientName.trim().isBlank()) {
                throw new IllegalArgumentException("Es obligatorio ingresar el nombre del cliente para registrarlo en el sistema.");
            }
            String trimmedName = clientName.trim();
            String[] parts = trimmedName.split("\\s+", 2);
            String fName = parts[0];
            String lName = parts.length > 1 ? parts[1] : "Mostrador";

            client = new Client();
            client.setFirstName(fName);
            client.setLastName(lName);
            client.setIdType(IDType.DNI);
            client.setIdNumber(cleanDni);
            client.setDateOfBirth(LocalDate.of(2000, 1, 1));
            client.setClientNumber("CLI-" + cleanDni);
            client.setDeleted(false);
            client = clientRepository.save(client);
        }

        // 4. Verificar stock y calcular total
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<Product> products = new ArrayList<>();
        for (int i = 0; i < productIds.size(); i++) {
            UUID pId = productIds.get(i);
            int qty = quantities.get(i);
            if (qty <= 0) {
                throw new IllegalArgumentException("La cantidad de cada ítem debe ser mayor a cero.");
            }

            Product product = productRepository.findActive(pId)
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + pId));

            // Verificar stock en la sucursal
            if (!stockService.hasAvailableStock(pId, office.getId(), qty)) {
                throw new IllegalStateException("Stock insuficiente en " + office.getName() + " para el producto: " + product.getName());
            }

            Optional<PriceHistory> priceHistory = priceHistoryRepository
                    .findFirstByProductIdAndDeletedFalseOrderByStartDateDesc(product.getId());
            BigDecimal unitPrice = priceHistory.map(ph -> ph.getPrice()).orElse(BigDecimal.ZERO);
            product.setCurrentPrice(unitPrice);
            products.add(product);

            totalAmount = totalAmount.add(unitPrice.multiply(BigDecimal.valueOf(qty)));
        }

        // 5. Crear orden
        SaleOrder order = SaleOrder.builder()
                .client(client)
                .employee(employee)
                .office(office)
                .date(LocalDateTime.now())
                .totalAmount(totalAmount)
                .status(OrderStatus.DELIVERED)
                .deleted(false)
                .build();
        SaleOrder savedOrder = saleOrderRepository.save(order);

        // 6. Crear detalles de orden y descontar stock
        for (int i = 0; i < products.size(); i++) {
            Product p = products.get(i);
            int qty = quantities.get(i);
            BigDecimal unitPrice = p.getCurrentPrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(qty));

            OrderDetail detail = OrderDetail.builder()
                    .order(savedOrder)
                    .product(p)
                    .quantity(qty)
                    .unitPrice(unitPrice)
                    .total(subtotal)
                    .deleted(false)
                    .build();
            orderDetailRepository.save(detail);

            // Descontar stock inmediatamente
            stockService.decrementStock(p.getId(), office.getId(), qty);
        }

        // 7. Registrar pago y emitir factura
        PaymentMethod finalMethod = paymentMethod != null ? paymentMethod : PaymentMethod.CASH;
        paymentService.registerPayment(savedOrder, totalAmount, finalMethod);
        paymentService.createInvoiceForOrder(savedOrder);

        return savedOrder;
    }
}

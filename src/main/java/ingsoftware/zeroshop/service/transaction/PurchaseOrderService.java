package ingsoftware.zeroshop.service.transaction;

import ingsoftware.zeroshop.entity.actor.Employee;
import ingsoftware.zeroshop.entity.actor.Supplier;
import ingsoftware.zeroshop.entity.actor.User;
import ingsoftware.zeroshop.entity.catalog.Product;
import ingsoftware.zeroshop.entity.org.Office;
import ingsoftware.zeroshop.entity.transaction.OrderDetail;
import ingsoftware.zeroshop.entity.transaction.PurchaseOrder;
import ingsoftware.zeroshop.enums.OrderStatus;
import ingsoftware.zeroshop.repository.actor.SupplierRepository;
import ingsoftware.zeroshop.repository.actor.UserRepository;
import ingsoftware.zeroshop.repository.catalog.ProductRepository;
import ingsoftware.zeroshop.repository.org.OfficeRepository;
import ingsoftware.zeroshop.repository.transaction.OrderDetailRepository;
import ingsoftware.zeroshop.repository.transaction.PurchaseOrderRepository;
import ingsoftware.zeroshop.service.org.StockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Servicio de negocio encargado de la gestión integral de órdenes de compra a proveedores,
 * recepción física de mercadería y actualización automatizada del inventario en las sucursales.
 */
@Service
public class PurchaseOrderService {

    private static final Logger log = LoggerFactory.getLogger(PurchaseOrderService.class);

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final SupplierRepository supplierRepository;
    private final OfficeRepository officeRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final StockService stockService;

    public PurchaseOrderService(PurchaseOrderRepository purchaseOrderRepository,
                                OrderDetailRepository orderDetailRepository,
                                SupplierRepository supplierRepository,
                                OfficeRepository officeRepository,
                                ProductRepository productRepository,
                                UserRepository userRepository,
                                StockService stockService) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.supplierRepository = supplierRepository;
        this.officeRepository = officeRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.stockService = stockService;
    }

    /**
     * Lista todas las órdenes de compra activas ordenadas cronológicamente de forma descendente.
     */
    public List<PurchaseOrder> getAllPurchaseOrders() {
        return purchaseOrderRepository.findAllByDeletedFalseOrderByDateDesc();
    }

    /**
     * Busca una orden de compra por su identificador único.
     */
    public Optional<PurchaseOrder> getPurchaseOrderById(UUID id) {
        return purchaseOrderRepository.findActive(id);
    }

    /**
     * Obtiene los ítems/detalles de una orden de compra.
     */
    public List<OrderDetail> getOrderDetails(UUID orderId) {
        return orderDetailRepository.findByOrderIdAndDeletedFalse(orderId);
    }

    /**
     * Emite una nueva orden de compra a un proveedor con un producto inicial.
     */
    @Transactional
    public PurchaseOrder createPurchaseOrder(UUID supplierId,
                                             UUID officeId,
                                             UUID productId,
                                             Integer quantity,
                                             BigDecimal unitPrice,
                                             String username) {
        if (supplierId == null) {
            throw new IllegalArgumentException("Debe seleccionar un proveedor válido.");
        }
        if (officeId == null) {
            throw new IllegalArgumentException("Debe seleccionar una sucursal de destino.");
        }
        if (productId == null) {
            throw new IllegalArgumentException("Debe seleccionar un producto a reabastecer.");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("La cantidad solicitada debe ser mayor a cero.");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio o costo unitario debe ser mayor a cero.");
        }

        Supplier supplier = supplierRepository.findActive(supplierId)
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado con ID: " + supplierId));

        Office office = officeRepository.findActive(officeId)
                .orElseThrow(() -> new IllegalArgumentException("Sucursal no encontrada con ID: " + officeId));

        Product product = productRepository.findActive(productId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + productId));

        Employee employee = null;
        if (username != null && !username.isBlank()) {
            Optional<User> userOpt = userRepository.findByUsernameIgnoreCaseAndDeletedFalse(username);
            if (userOpt.isPresent() && userOpt.get().getPerson() instanceof Employee emp) {
                employee = emp;
            }
        }

        BigDecimal totalAmount = unitPrice.multiply(BigDecimal.valueOf(quantity));

        PurchaseOrder purchaseOrder = PurchaseOrder.builder()
                .supplier(supplier)
                .office(office)
                .employee(employee)
                .date(LocalDateTime.now())
                .status(OrderStatus.PENDING_DELIVERY)
                .totalAmount(totalAmount)
                .deleted(false)
                .build();

        PurchaseOrder savedOrder = purchaseOrderRepository.save(purchaseOrder);

        OrderDetail detail = OrderDetail.builder()
                .order(savedOrder)
                .product(product)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .total(totalAmount)
                .deleted(false)
                .build();

        orderDetailRepository.save(detail);

        log.info("Orden de compra creada exitosamente con ID: {} para el proveedor: {}", savedOrder.getId(), supplier.getName());
        return savedOrder;
    }

    /**
     * Marca la orden de compra como recibida (DELIVERED) e ingresa automáticamente el stock al depósito de la sucursal.
     */
    @Transactional
    public PurchaseOrder receivePurchaseOrder(UUID orderId) {
        PurchaseOrder order = purchaseOrderRepository.findActive(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Orden de compra no encontrada con ID: " + orderId));

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Esta orden de compra ya fue recibida anteriormente.");
        }
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("No se puede recibir una orden de compra cancelada.");
        }

        List<OrderDetail> details = orderDetailRepository.findByOrderIdAndDeletedFalse(order.getId());
        if (details.isEmpty()) {
            throw new IllegalStateException("La orden de compra no contiene productos para ingresar al inventario.");
        }

        UUID officeId = order.getOffice().getId();
        for (OrderDetail detail : details) {
            stockService.incrementStock(detail.getProduct().getId(), officeId, detail.getQuantity());
            log.info("Stock incrementado: Producto '{}' (+{} unidades) en sucursal '{}'",
                    detail.getProduct().getName(), detail.getQuantity(), order.getOffice().getName());
        }

        order.setStatus(OrderStatus.DELIVERED);
        PurchaseOrder updatedOrder = purchaseOrderRepository.save(order);

        log.info("Orden de compra {} marcada como RECIBIDA exitosamente.", orderId);
        return updatedOrder;
    }

    /**
     * Cancela o elimina lógicamente una orden de compra pendiente.
     */
    @Transactional
    public void cancelPurchaseOrder(UUID orderId) {
        PurchaseOrder order = purchaseOrderRepository.findActive(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Orden de compra no encontrada con ID: " + orderId));

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("No es posible cancelar una orden que ya ha sido recibida e ingresada al inventario.");
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setDeleted(true);
        purchaseOrderRepository.save(order);

        List<OrderDetail> details = orderDetailRepository.findByOrderIdAndDeletedFalse(order.getId());
        for (OrderDetail detail : details) {
            detail.setDeleted(true);
            orderDetailRepository.save(detail);
        }

        log.info("Orden de compra {} cancelada y eliminada lógicamente.", orderId);
    }
}

package ingsoftware.zeroshop.service.transaction;

import ingsoftware.zeroshop.entity.transaction.Invoice;
import ingsoftware.zeroshop.entity.transaction.InvoiceDetail;
import ingsoftware.zeroshop.entity.transaction.Order;
import ingsoftware.zeroshop.entity.transaction.OrderDetail;
import ingsoftware.zeroshop.entity.transaction.Payment;
import ingsoftware.zeroshop.enums.InvoiceStatus;
import ingsoftware.zeroshop.enums.PaymentMethod;
import ingsoftware.zeroshop.repository.transaction.InvoiceDetailRepository;
import ingsoftware.zeroshop.repository.transaction.InvoiceRepository;
import ingsoftware.zeroshop.repository.transaction.OrderDetailRepository;
import ingsoftware.zeroshop.repository.transaction.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceDetailRepository invoiceDetailRepository;
    private final OrderDetailRepository orderDetailRepository;

    public PaymentService(PaymentRepository paymentRepository,
                          InvoiceRepository invoiceRepository,
                          InvoiceDetailRepository invoiceDetailRepository,
                          OrderDetailRepository orderDetailRepository) {
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
        this.invoiceDetailRepository = invoiceDetailRepository;
        this.orderDetailRepository = orderDetailRepository;
    }

    public List<Payment> getPaymentsByOrder(UUID orderId) {
        return paymentRepository.findByOrderIdAndDeletedFalse(orderId);
    }

    public Optional<Payment> getPaymentById(UUID paymentId) {
        return paymentRepository.findActive(paymentId);
    }

    @Transactional
    public Payment registerPayment(Order order, BigDecimal amount, PaymentMethod method) {
        Payment payment = Payment.builder()
                .order(order)
                .amount(amount != null ? amount : BigDecimal.ZERO)
                .date(LocalDateTime.now())
                .method(method)
                .deleted(false)
                .build();
        return paymentRepository.save(payment);
    }

    /**
     * Emite y persiste una Factura electrónica para una orden confirmada y pagada.
     * Es idempotente: si la factura ya existe, asegura su estado PAID y la retorna.
     */
    @Transactional
    public Invoice createInvoiceForOrder(Order order, List<OrderDetail> details) {
        if (order == null) {
            throw new IllegalArgumentException("La orden no puede ser nula para emitir la factura.");
        }

        // 1. Verificar si ya existe una factura para esta orden
        Optional<Invoice> existing = invoiceRepository.findByOrderIdAndDeletedFalse(order.getId());
        if (existing.isPresent()) {
            Invoice inv = existing.get();
            if (inv.getStatus() != InvoiceStatus.PAID) {
                inv.setStatus(InvoiceStatus.PAID);
                return invoiceRepository.save(inv);
            }
            return inv;
        }

        // 2. Resolver detalles de la orden si no fueron provistos
        if (details == null || details.isEmpty()) {
            details = orderDetailRepository.findByOrderIdAndDeletedFalse(order.getId());
        }

        // 3. Generar número de factura único consecutivo
        long nextSeq = invoiceRepository.count() + 1;
        String invoiceNumber;
        do {
            invoiceNumber = String.format("FC-0001-%08d", nextSeq++);
        } while (invoiceRepository.findByNumberAndDeletedFalse(invoiceNumber).isPresent());

        BigDecimal total = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;
        if (total.compareTo(BigDecimal.ZERO) <= 0 && details != null && !details.isEmpty()) {
            total = details.stream()
                    .map(d -> d.getTotal() != null ? d.getTotal() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        // 4. Crear cabecera de Factura
        Invoice invoice = Invoice.builder()
                .number(invoiceNumber)
                .date(LocalDateTime.now())
                .totalAmount(total)
                .status(InvoiceStatus.PAID)
                .order(order)
                .deleted(false)
                .build();
        Invoice savedInvoice = invoiceRepository.save(invoice);

        // 5. Crear líneas de detalle de factura
        if (details != null && !details.isEmpty()) {
            for (OrderDetail od : details) {
                InvoiceDetail invDetail = InvoiceDetail.builder()
                        .invoice(savedInvoice)
                        .product(od.getProduct())
                        .quantity(od.getQuantity())
                        .unitPrice(od.getUnitPrice())
                        .total(od.getTotal())
                        .deleted(false)
                        .build();
                invoiceDetailRepository.save(invDetail);
            }
        }

        log.info("Factura {} generada exitosamente para la orden ID: {}", savedInvoice.getNumber(), order.getId());
        return savedInvoice;
    }

    /**
     * Sobrecarga conveniente para emitir factura cargando automáticamente los detalles de la orden.
     */
    @Transactional
    public Invoice createInvoiceForOrder(Order order) {
        return createInvoiceForOrder(order, null);
    }

    public Optional<Invoice> getInvoiceByOrderId(UUID orderId) {
        return invoiceRepository.findByOrderIdAndDeletedFalse(orderId);
    }

    public Optional<Invoice> getInvoiceById(UUID invoiceId) {
        return invoiceRepository.findActive(invoiceId);
    }

    public List<InvoiceDetail> getInvoiceDetails(UUID invoiceId) {
        return invoiceDetailRepository.findByInvoiceIdAndDeletedFalse(invoiceId);
    }

    @Transactional
    public void cancelInvoiceForOrder(UUID orderId) {
        invoiceRepository.findByOrderIdAndDeletedFalse(orderId).ifPresent(inv -> {
            inv.setStatus(InvoiceStatus.CANCELLED);
            invoiceRepository.save(inv);
            log.info("Factura {} cancelada para orden {}", inv.getNumber(), orderId);
        });
    }
}

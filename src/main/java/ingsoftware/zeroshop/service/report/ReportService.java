package ingsoftware.zeroshop.service.report;

import ingsoftware.zeroshop.dto.SalesReportDTO;
import ingsoftware.zeroshop.dto.StockReportDTO;
import ingsoftware.zeroshop.dto.SupplierReportDTO;
import ingsoftware.zeroshop.entity.actor.ContactPhone;
import ingsoftware.zeroshop.entity.actor.Supplier;
import ingsoftware.zeroshop.entity.actor.SupplierProduct;
import ingsoftware.zeroshop.entity.catalog.Product;
import ingsoftware.zeroshop.entity.org.Office;
import ingsoftware.zeroshop.entity.org.Stock;
import ingsoftware.zeroshop.entity.transaction.OrderDetail;
import ingsoftware.zeroshop.entity.transaction.Payment;
import ingsoftware.zeroshop.entity.transaction.SaleOrder;
import ingsoftware.zeroshop.enums.OrderStatus;
import ingsoftware.zeroshop.repository.actor.SupplierProductRepository;
import ingsoftware.zeroshop.repository.actor.SupplierRepository;
import ingsoftware.zeroshop.repository.catalog.ProductRepository;
import ingsoftware.zeroshop.repository.org.OfficeRepository;
import ingsoftware.zeroshop.repository.org.StockRepository;
import ingsoftware.zeroshop.repository.transaction.OrderDetailRepository;
import ingsoftware.zeroshop.repository.transaction.PaymentRepository;
import ingsoftware.zeroshop.repository.transaction.SaleOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ReportService {

    private final SaleOrderRepository saleOrderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final PaymentRepository paymentRepository;
    private final StockRepository stockRepository;
    private final ProductRepository productRepository;
    private final OfficeRepository officeRepository;
    private final SupplierProductRepository supplierProductRepository;
    private final SupplierRepository supplierRepository;

    public ReportService(SaleOrderRepository saleOrderRepository,
                         OrderDetailRepository orderDetailRepository,
                         PaymentRepository paymentRepository,
                         StockRepository stockRepository,
                         ProductRepository productRepository,
                         OfficeRepository officeRepository,
                         SupplierProductRepository supplierProductRepository,
                         SupplierRepository supplierRepository) {
        this.saleOrderRepository = saleOrderRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.paymentRepository = paymentRepository;
        this.stockRepository = stockRepository;
        this.productRepository = productRepository;
        this.officeRepository = officeRepository;
        this.supplierProductRepository = supplierProductRepository;
        this.supplierRepository = supplierRepository;
    }

    /**
     * Reporte de Ventas por rango de fechas (especificación integrador).
     */
    @Transactional(readOnly = true)
    public SalesReportDTO getSalesReport(LocalDate startDate, LocalDate endDate) {
        LocalDate actualStart = startDate != null ? startDate : LocalDate.now().withDayOfMonth(1);
        LocalDate actualEnd = endDate != null ? endDate : LocalDate.now();

        LocalDateTime startDateTime = actualStart.atStartOfDay();
        LocalDateTime endDateTime = actualEnd.atTime(23, 59, 59);

        List<SaleOrder> orders = saleOrderRepository.findByDateBetweenAndDeletedFalse(startDateTime, endDateTime);

        // Excluir órdenes canceladas y carritos no confirmados
        List<SaleOrder> confirmedOrders = orders.stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED && o.getStatus() != OrderStatus.ON_CART)
                .toList();

        BigDecimal totalBilled = confirmedOrders.stream()
                .map(o -> o.getTotalAmount())
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, (a, b) -> a.add(b));

        List<SalesReportDTO.SaleItemDTO> itemDetails = new ArrayList<>();

        for (SaleOrder order : confirmedOrders) {
            String orderCode = "#" + order.getId().toString().substring(0, Math.min(8, order.getId().toString().length()));
            LocalDateTime orderDate = order.getDate() != null ? order.getDate() : LocalDateTime.now();

            // Buscar método de pago registrado
            List<Payment> payments = paymentRepository.findByOrderIdAndDeletedFalse(order.getId());
            String paymentMethod = "Mercado Pago";
            if (!payments.isEmpty() && payments.get(0).getMethod() != null) {
                paymentMethod = switch (payments.get(0).getMethod()) {
                    case MERCADO_PAGO -> "Mercado Pago";
                    case CASH -> "Efectivo";
                    case CREDIT -> "Tarjeta Crédito";
                    case DEBIT -> "Tarjeta Débito";
                    default -> payments.get(0).getMethod().name();
                };
            }

            List<OrderDetail> details = orderDetailRepository.findByOrderIdAndDeletedFalse(order.getId());
            if (details.isEmpty()) {
                itemDetails.add(new SalesReportDTO.SaleItemDTO(
                        orderCode,
                        orderDate,
                        "Compra General",
                        "Deportiva",
                        1,
                        paymentMethod,
                        order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO
                ));
            } else {
                for (OrderDetail d : details) {
                    String prodName = d.getProduct() != null ? d.getProduct().getName() : "Artículo";
                    String catName = "Deportivo";
                    if (d.getProduct() != null && d.getProduct().getSubCategory() != null) {
                        String mainCat = d.getProduct().getCategory() != null ? d.getProduct().getCategory().getName() + " • " : "";
                        catName = mainCat + d.getProduct().getSubCategory().getName();
                    }

                    itemDetails.add(new SalesReportDTO.SaleItemDTO(
                            orderCode,
                            orderDate,
                            prodName,
                            catName,
                            d.getQuantity() != null ? d.getQuantity() : 1,
                            paymentMethod,
                            d.getTotal() != null ? d.getTotal() : (d.getUnitPrice() != null ? d.getUnitPrice() : BigDecimal.ZERO)
                    ));
                }
            }
        }

        return new SalesReportDTO(actualStart, actualEnd, totalBilled, confirmedOrders.size(), itemDetails);
    }

    /**
     * Reporte de Stock con semáforo de reposición por sucursal y link a WhatsApp.
     */
    @Transactional(readOnly = true)
    public StockReportDTO getStockReport() {
        List<Product> products = productRepository.findAllByDeletedFalse();
        List<Office> offices = officeRepository.findAllByDeletedFalse();

        if (offices.isEmpty()) {
            Office defaultOffice = new Office();
            defaultOffice.setName("Sucursal Central");
            offices = List.of(defaultOffice);
        }

        List<StockReportDTO.StockItemDTO> items = new ArrayList<>();
        int totalUnits = 0;
        int goodCount = 0;
        int regularCount = 0;
        int badCount = 0;

        final int IDEAL_CAPACITY = 50;

        for (Product product : products) {
            // Obtener proveedor preferido para reposición
            List<SupplierProduct> supProducts = supplierProductRepository
                    .findByProductIdAndDeletedFalseOrderByCostPriceAsc(product.getId());

            String supplierName = "Distribuidora Textil S.A.";
            String supplierPhone = "+54 9 11 4444-1234";

            if (!supProducts.isEmpty() && supProducts.get(0).getSupplier() != null) {
                Supplier sup = supProducts.get(0).getSupplier();
                supplierName = sup.getName();
                if (sup.getContact() != null) {
                    for (var c : sup.getContact()) {
                        if (c instanceof ContactPhone cp && cp.getPhoneNumber() != null && !cp.getPhoneNumber().isBlank()) {
                            supplierPhone = cp.getPhoneNumber();
                            break;
                        }
                    }
                }
            } else {
                List<Supplier> allSuppliers = supplierRepository.findAllByDeletedFalse();
                if (!allSuppliers.isEmpty()) {
                    Supplier s = allSuppliers.get(0);
                    supplierName = s.getName();
                    if (s.getContact() != null) {
                        for (var c : s.getContact()) {
                            if (c instanceof ContactPhone cp && cp.getPhoneNumber() != null && !cp.getPhoneNumber().isBlank()) {
                                supplierPhone = cp.getPhoneNumber();
                                break;
                            }
                        }
                    }
                }
            }

            for (Office office : offices) {
                int quantity = 0;
                if (office.getId() != null) {
                    Optional<Stock> stockOpt = stockRepository
                            .findByProductIdAndOfficeIdAndDeletedFalse(product.getId(), office.getId());
                    if (stockOpt.isPresent() && stockOpt.get().getQuantity() != null) {
                        quantity = stockOpt.get().getQuantity();
                    }
                }

                totalUnits += quantity;
                int percentage = Math.min(100, (int) Math.round((quantity * 100.0) / IDEAL_CAPACITY));

                String trafficLight;
                int neededUnits = 0;

                if (percentage > 50) {
                    trafficLight = "BIEN";
                    goodCount++;
                } else if (percentage >= 20) {
                    trafficLight = "REGULAR";
                    regularCount++;
                } else {
                    trafficLight = "MALO";
                    badCount++;
                    // Cantidad requerida para alcanzar el 50% (25 unidades)
                    neededUnits = Math.max(0, 25 - quantity);
                }

                // Generar mensaje preformateado de WhatsApp
                String phoneClean = supplierPhone.replaceAll("[^0-9]", "");
                String message = String.format(
                        "Hola, desde Zero Shop Mendoza te solicitamos el envio urgente de %d unidades de %s (%s) para alcanzar el 50%% de stock en %s.",
                        neededUnits > 0 ? neededUnits : 15,
                        product.getName(),
                        product.getCode(),
                        office.getName()
                );
                String whatsappUrl = "https://wa.me/" + phoneClean + "?text=" + URLEncoder.encode(message, StandardCharsets.UTF_8);

                items.add(new StockReportDTO.StockItemDTO(
                        office.getName(),
                        product.getName(),
                        product.getCode(),
                        product.getSize() != null ? product.getSize().name() : "Unico",
                        quantity,
                        IDEAL_CAPACITY,
                        percentage,
                        trafficLight,
                        neededUnits,
                        supplierName,
                        supplierPhone,
                        whatsappUrl
                ));
            }
        }

        int totalEntries = items.size();
        double pctGood = totalEntries > 0 ? Math.round((goodCount * 100.0) / totalEntries) : 0;
        double pctRegular = totalEntries > 0 ? Math.round((regularCount * 100.0) / totalEntries) : 0;
        double pctBad = totalEntries > 0 ? Math.round((badCount * 100.0) / totalEntries) : 0;

        return new StockReportDTO(totalUnits, pctGood, pctRegular, pctBad, items);
    }

    /**
     * Reporte comparativo de proveedores para encontrar el más económico.
     */
    @Transactional(readOnly = true)
    public SupplierReportDTO getSuppliersReport() {
        List<Product> products = productRepository.findAllByDeletedFalse();
        List<SupplierReportDTO.SupplierComparisonDTO> comparisons = new ArrayList<>();

        for (Product product : products) {
            String catName = "Deportivo";
            if (product.getSubCategory() != null) {
                String mainCat = product.getCategory() != null ? product.getCategory().getName() + " • " : "";
                catName = mainCat + product.getSubCategory().getName();
            }

            List<SupplierProduct> supProducts = supplierProductRepository
                    .findByProductIdAndDeletedFalseOrderByCostPriceAsc(product.getId());

            if (supProducts.isEmpty()) {
                comparisons.add(new SupplierReportDTO.SupplierComparisonDTO(
                        product.getId(),
                        product.getName(),
                        product.getCode(),
                        catName,
                        "Sin proveedor asignado",
                        null,
                        "-",
                        null,
                        null,
                        null
                ));
            } else {
                SupplierProduct best = supProducts.get(0);
                SupplierProduct alt = supProducts.size() > 1 ? supProducts.get(1) : null;

                BigDecimal unitSavings = null;
                Double savingPct = null;

                if (alt != null && alt.getCostPrice() != null && best.getCostPrice() != null) {
                    unitSavings = alt.getCostPrice().subtract(best.getCostPrice());
                    if (alt.getCostPrice().compareTo(BigDecimal.ZERO) > 0) {
                        savingPct = Math.round((unitSavings.doubleValue() / alt.getCostPrice().doubleValue() * 1000.0)) / 10.0;
                    }
                }

                comparisons.add(new SupplierReportDTO.SupplierComparisonDTO(
                        product.getId(),
                        product.getName(),
                        product.getCode(),
                        catName,
                        best.getSupplier() != null ? best.getSupplier().getName() : "Proveedor Principal",
                        best.getCostPrice(),
                        alt != null && alt.getSupplier() != null ? alt.getSupplier().getName() : "-",
                        alt != null ? alt.getCostPrice() : null,
                        unitSavings,
                        savingPct
                ));
            }
        }

        return new SupplierReportDTO(comparisons);
    }

}

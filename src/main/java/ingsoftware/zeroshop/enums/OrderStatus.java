package ingsoftware.zeroshop.enums;

public enum OrderStatus {
    ON_CART("En Carrito"),
    PENDING_PAYMENT("PENDIENTE DE PAGO"),
    PAID("PAGO REALIZADO"),
    PENDING_SHIPPING("PENDIENTE DE ENVIO"),
    PENDING_DELIVERY("PENDIENTE DE ENTREGA"),
    DELIVERED("ENTREGADO"),
    CANCELLED("CANCELADO");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}


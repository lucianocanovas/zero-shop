package ingsoftware.zeroshop.enums;

public enum PaymentMethod {
    CREDIT("Tarjeta de Crédito"),
    DEBIT("Tarjeta de Débito"),
    CASH("Efectivo"),
    MERCADO_PAGO("Mercado Pago"),
    OTHER("Otro");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

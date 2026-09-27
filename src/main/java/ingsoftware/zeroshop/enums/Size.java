package ingsoftware.zeroshop.enums;

public enum Size {
    XS,
    S,
    M,
    L,
    XL,
    XXL;

    public String getDisplayName() {
        return this.name();
    }
}

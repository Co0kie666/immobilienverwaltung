package de.hsbi.immobilienverwaltung.domain.enums;

public enum Zahlungseingangtyp {
    KALTMIETE("Kaltmite"),
    NEBENKOSTEN("Nebenkosten"),
    KAUTION("Kaution"),
    SONSTIGES("Sonstiges");

    private final String label;

    Zahlungseingangtyp(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

package de.hsbi.immobilienverwaltung.domain.enums;

public enum Ausgabenkategorie {
    STROM("Strom"),
    WASSER("Wasser"),
    HEIZUNG("Heizung"),
    INTERNET("Internet"),

    VERSICHERUNG("Versicherung"),
    REPARATUR("Reparatur / Handwerker"),
    INSTANDHALTUNG("Instandhaltung"),
    RENOVIERUNG("Renovierung"),
    REINIGUNG("Reinigung"),
    GRUNDSTEUER("Grundsteuer"),
    MUELLABFUHR("Müllabfuhr"),
    VERWALTUNG("Verwaltungskosten"),
    SONSTIGES("Sonstiges");

    private final String label;

    Ausgabenkategorie(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

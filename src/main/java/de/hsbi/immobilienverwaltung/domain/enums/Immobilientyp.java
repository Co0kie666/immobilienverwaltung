package de.hsbi.immobilienverwaltung.domain.enums;

public enum Immobilientyp {
    WOHNGEBAEUDE("Wohngebäude"),
    MEHRFAMILIENHAUS("Mehrfamilienhaus"),
    GEWERBEIMMOBILIE("Gewerbeimmobilie");

    private final String label;

    Immobilientyp(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
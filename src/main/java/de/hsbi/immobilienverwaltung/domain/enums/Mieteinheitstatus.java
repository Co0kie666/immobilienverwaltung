package de.hsbi.immobilienverwaltung.domain.enums;

public enum Mieteinheitstatus {
    FREI("Frei"),
    VERMIETET("Vermietet"),
    IN_RENOVIERUNG("In Renovierung");

    private final String label;

    Mieteinheitstatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
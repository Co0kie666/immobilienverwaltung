package de.hsbi.immobilienverwaltung.domain.enums;

public enum MieteinheitTyp {
    WOHNUNG("Wohnung"),
    BUERO("Büro"),
    LAGERHALLE("Lagerhalle"),
    GEWERBEFLAECHE("Gewerbefläche"),
    GESAMTOBJEKT("Gesamtobjekt");

    private final String label;

    MieteinheitTyp(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
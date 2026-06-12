package de.hsbi.immobilienverwaltung.domain.enums;

public enum Vertragsstatus {
        AKTIV("Aktiv"),
        GEKUENDIGT("Läuft aus"),
        BEENDET("Beendet");

        private final String label;

        Vertragsstatus(String label) {
                this.label = label;
        }

        public String getLabel() {
                return label;
        }
}
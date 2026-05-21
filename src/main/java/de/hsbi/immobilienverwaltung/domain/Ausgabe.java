package de.hsbi.immobilienverwaltung.domain;
import de.hsbi.immobilienverwaltung.domain.enums.Ausgabenkategorie;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
public class Ausgabe {

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Ausgabenkategorie typ;

    @Setter
    @Getter
    private String titel;
    @Setter
    @Getter
    private Double betrag;
    @Setter
    @Getter
    private LocalDate datum;
    @Setter
    @Getter
    private String beschreibung;

    @Setter
    @Getter
    @ManyToOne
    private Immobilie immobilie;

    public Ausgabe() {}

    public Ausgabe(String titel, Ausgabenkategorie typ, Double betrag, LocalDate datum, String beschreibung) {
        this.titel = titel;
        this.typ = typ;
        this.betrag = betrag;
        this.datum = datum;
        this.beschreibung = beschreibung;
    }
}
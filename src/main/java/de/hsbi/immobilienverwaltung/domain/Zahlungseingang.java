package de.hsbi.immobilienverwaltung.domain;

import de.hsbi.immobilienverwaltung.domain.enums.Zahlungseingangtyp;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
public class Zahlungseingang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Zahlungseingangtyp typ;

    private BigDecimal betrag;

    private LocalDate zahlungsdatum;

    private LocalDate leistungsmonat;

    private String beschreibung;

    @ManyToOne(optional = false)
    private Mietvertrag mietvertrag;

    public Zahlungseingang() {
    }

    public Zahlungseingang(Zahlungseingangtyp typ,
                           BigDecimal betrag,
                           LocalDate zahlungsdatum,
                           LocalDate leistungsmonat,
                           String beschreibung,
                           Mietvertrag mietvertrag) {
        this.typ = typ;
        this.betrag = betrag;
        this.zahlungsdatum = zahlungsdatum;
        this.leistungsmonat = leistungsmonat;
        this.beschreibung = beschreibung;
        this.mietvertrag = mietvertrag;
    }
}
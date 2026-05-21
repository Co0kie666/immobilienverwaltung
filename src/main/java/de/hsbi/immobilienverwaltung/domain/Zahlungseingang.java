package de.hsbi.immobilienverwaltung.domain;
import de.hsbi.immobilienverwaltung.domain.enums.Zahlungseingangtyp;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Zahlungseingang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated (EnumType.STRING)
    private Zahlungseingangtyp typ;

    private Double betrag;
    private LocalDate zahlungsdatum;

    @ManyToOne
    private Mieter mieter;

    public Zahlungseingang() {}

    public Zahlungseingang(Zahlungseingangtyp typ, Double betrag, LocalDate zahlungsdatum) {
        this.typ = typ;
        this.betrag = betrag;
        this.zahlungsdatum = zahlungsdatum;
    }

    public Long getId() {
        return id;
    }

    public Zahlungseingangtyp getTyp() {
        return typ;
    }

    public void setTyp(Zahlungseingangtyp typ) {
        this.typ = typ;
    }

    public Double getBetrag() {
        return betrag;
    }

    public void setBetrag(Double betrag) {
        this.betrag = betrag;
    }

    public LocalDate getZahlungsdatum() {
        return zahlungsdatum;
    }

    public void setZahlungsdatum(LocalDate zahlungsdatum) {
        this.zahlungsdatum = zahlungsdatum;
    }

    public Mieter getMieter() {
        return mieter;
    }

    public void setMieter(Mieter mieter) {
        this.mieter = mieter;
    }
}
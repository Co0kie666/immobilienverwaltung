package de.hsbi.immobilienverwaltung.domain;

import de.hsbi.immobilienverwaltung.domain.enums.MieteinheitTyp;
import de.hsbi.immobilienverwaltung.domain.enums.Mieteinheitstatus;
import jakarta.persistence.*;

/**
 * Repräsentiert eine einzelne vermietbare Einheit innerhalb einer Immobilie.
 * Das kann z. B. eine Wohnung, ein Büro oder ein Gesamtobjekt sein.
 */
@Entity
public class Mieteinheit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Neue Mieteinheiten sind standardmäßig frei,
     * solange kein anderer Status gesetzt wird.
     */
    @Enumerated(EnumType.STRING)
    private Mieteinheitstatus status = Mieteinheitstatus.FREI;

    @Enumerated(EnumType.STRING)
    private MieteinheitTyp typ;

    private String bezeichnung;
    private Integer groesse;
    private Integer zimmerzahl;
    private String stockwerk;

    /**
     * Viele Mieteinheiten können zu einer Immobilie gehören.
     * nullable = false stellt sicher, dass jede Mieteinheit immer einer Immobilie zugeordnet ist.
     */
    @ManyToOne
    @JoinColumn(name = "immobilie_id", nullable = false)
    private Immobilie immobilie;

    public Mieteinheit() {}

    public Mieteinheit(String bezeichnung, Mieteinheitstatus status, MieteinheitTyp typ, Integer groesse, Integer zimmerzahl, String stockwerk) {
        this.bezeichnung = bezeichnung;
        this.status = status;
        this.typ = typ;
        this.groesse = groesse;
        this.zimmerzahl = zimmerzahl;
        this.stockwerk = stockwerk;
    }

    public Long getId() {
        return id;
    }

    public String getBezeichnung() {
        return bezeichnung;
    }

    public void setBezeichnung(String bezeichnung) {
        this.bezeichnung = bezeichnung;
    }

    public Immobilie getImmobilie() {
        return immobilie;
    }

    public void setImmobilie(Immobilie immobilie) {
        this.immobilie = immobilie;
    }

    public Mieteinheitstatus getStatus() {return status;}

    public void setStatus(Mieteinheitstatus status) {this.status = status;}

    public Integer getGroesse() {
        return groesse;
    }

    public void setGroesse(Integer groesse) {
        this.groesse = groesse;
    }

    public Integer getZimmerzahl() {
        return zimmerzahl;
    }

    public void setZimmerzahl(Integer zimmerzahl) {
        this.zimmerzahl = zimmerzahl;
    }

    public String getStockwerk() {
        return stockwerk;
    }

    public void setStockwerk(String stockwerk) {
        this.stockwerk = stockwerk;
    }

    public MieteinheitTyp getTyp() {
        return typ;
    }

    public void setTyp(MieteinheitTyp typ) {
        this.typ = typ;
    }
}
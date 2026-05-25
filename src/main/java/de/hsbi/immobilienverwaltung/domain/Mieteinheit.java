package de.hsbi.immobilienverwaltung.domain;

import de.hsbi.immobilienverwaltung.domain.enums.MieteinheitTyp;
import de.hsbi.immobilienverwaltung.domain.enums.Mieteinheitstatus;
import jakarta.persistence.*;

@Entity
public class Mieteinheit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Mieteinheitstatus status = Mieteinheitstatus.FREI;

    @Enumerated(EnumType.STRING)
    private MieteinheitTyp typ;

    private String bezeichnung;
    private Integer groesse;
    private Integer zimmerzahl;
    private Integer stockwerk;

    @ManyToOne
    @JoinColumn(name = "immobilie_id", nullable = false) // foreign key der nicht NULL sein darf
    private Immobilie immobilie;

    public Mieteinheit() {}

    public Mieteinheit(String bezeichnung, Mieteinheitstatus status, MieteinheitTyp typ, Integer groesse, Integer zimmerzahl, Integer stockwerk, Immobilie immobilie) {
        this.bezeichnung = bezeichnung;
        this.status = status;
        this.typ = typ;
        this.groesse = groesse;
        this.zimmerzahl = zimmerzahl;
        this.stockwerk = stockwerk;
        this.immobilie = immobilie;
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

    public Integer getStockwerk() {
        return stockwerk;
    }

    public void setStockwerk(Integer stockwerk) {
        this.stockwerk = stockwerk;
    }

    public MieteinheitTyp getTyp() {
        return typ;
    }

    public void setTyp(MieteinheitTyp typ) {
        this.typ = typ;
    }
}
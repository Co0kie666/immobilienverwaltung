package de.hsbi.immobilienverwaltung.domain;

import jakarta.persistence.*;

@Entity
public class Immobilie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String bezeichnung;
    private String typ;
    private Integer baujahr;

    public Immobilie() {
    }

    public Immobilie(String bezeichnung, String typ, Integer baujahr) {
        this.bezeichnung = bezeichnung;
        this.typ = typ;
        this.baujahr = baujahr;
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

    public String getTyp() {
        return typ;
    }

    public void setTyp(String typ) {
        this.typ = typ;
    }

    public Integer getBaujahr() {
        return baujahr;
    }

    public void setBaujahr(Integer baujahr) {
        this.baujahr = baujahr;
    }
}
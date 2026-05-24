package de.hsbi.immobilienverwaltung.domain;
import de.hsbi.immobilienverwaltung.domain.enums.Immobilientyp;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class Immobilie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Immobilientyp typ;

    private String bezeichnung;
    private Integer baujahr;
    private Integer flaeche;

    @OneToOne(cascade = CascadeType.ALL) // stellt Datenkonsistenz sicher
    private Adresse adresse;

    public Immobilie() {}

    public Immobilie(String bezeichnung, Immobilientyp typ, Integer baujahr, Integer flaeche, Adresse adresse) {
        this.bezeichnung = bezeichnung;
        this.typ = typ;
        this.baujahr = baujahr;
        this.flaeche = flaeche;
        this.adresse = adresse;
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

    public Immobilientyp getTyp() {
        return typ;
    }

    public void setTyp(Immobilientyp typ) {
        this.typ = typ;
    }

    public Integer getBaujahr() {
        return baujahr;
    }

    public void setBaujahr(Integer baujahr) {
        this.baujahr = baujahr;
    }

    public Integer getFlaeche() {return flaeche;}

    public void setFlaeche(Integer flaeche) {this.flaeche = flaeche;}

    public Adresse getAdresse() {return adresse;}

    public void setAdresse(Adresse adresse) {this.adresse = adresse;}
}
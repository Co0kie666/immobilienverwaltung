package de.hsbi.immobilienverwaltung.domain;

import de.hsbi.immobilienverwaltung.domain.enums.Mieteinheitstatus;
import jakarta.persistence.*;

@Entity
public class Mieteinheit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Mieteinheitstatus status = Mieteinheitstatus.FREI;

    private String bezeichnung;
    private Double kaltmiete;

    @ManyToOne
    private Immobilie immobilie;

    @OneToOne
    private Mieter mieter;

    public Mieteinheit() {}

    public Mieteinheit(String bezeichnung, Mieteinheitstatus status, Double kaltmiete) {
        this.bezeichnung = bezeichnung;
        this.status = status;
        this.kaltmiete = kaltmiete;
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

    public Double getKaltmiete() {
        return kaltmiete;
    }

    public void setKaltmiete(Double kaltmiete) {
        this.kaltmiete = kaltmiete;
    }

    public Immobilie getImmobilie() {
        return immobilie;
    }

    public void setImmobilie(Immobilie immobilie) {
        this.immobilie = immobilie;
    }

    public Mieter getMieter() {
        return mieter;
    }

    public void setMieter(Mieter mieter) {
        this.mieter = mieter;
    }

    public Mieteinheitstatus getStatus() {return status;}

    public void setStatus(Mieteinheitstatus status) {this.status = status;}
}
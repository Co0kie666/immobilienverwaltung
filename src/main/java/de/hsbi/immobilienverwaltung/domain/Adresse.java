package de.hsbi.immobilienverwaltung.domain;
import jakarta.persistence.*;

@Embeddable
public class Adresse {

    private String strasse;
    private String hausnummer;
    private String plz;
    private String stadt;
    private String land;

    public Adresse() {}

    public Adresse(String strasse, String hausnummer, String plz, String stadt, String land) {
        this.strasse = strasse;
        this.hausnummer = hausnummer;
        this.plz = plz;
        this.stadt = stadt;
        this.land = land;
    }

    public String getStrasse() {
        return strasse;
    }

    public void setStrasse(String strasse) { this.strasse = strasse; }

    public String getHausnummer() {
        return hausnummer;
    }

    public void setHausnummer(String hausnummer) {
        this.hausnummer = hausnummer;
    }

    public String getPlz() {
        return plz;
    }

    public void setPlz(String plz) {
        this.plz = plz;
    }

    public String getStadt() {
        return stadt;
    }

    public void setStadt(String stadt) {
        this.stadt = stadt;
    }

    public String getLand() {
        return land;
    }

    public void setLand(String land) {
        this.land = land;
    }
}
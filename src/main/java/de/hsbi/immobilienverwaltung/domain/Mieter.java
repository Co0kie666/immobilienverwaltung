package de.hsbi.immobilienverwaltung.domain;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class Mieter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String anrede;
    private String titel;

    private String vorname;
    private String nachname;

    private LocalDate geburtsdatum;
    private String beruf;

    private String email;
    private String telefonnummer;

    @Embedded
    private Adresse adresse;

    private boolean bankdatenAktiv;
    private String kontoinhaber;
    private String iban;
    private String bic;

    private boolean archiviert = false;

    public Mieter() {}

    public Mieter(String vorname, String nachname, String email, String telefonnummer) {
        this.vorname = vorname;
        this.nachname = nachname;
        this.email = email;
        this.telefonnummer = telefonnummer;
    }

    public Mieter(
            String anrede,
            String titel,
            String vorname,
            String nachname,
            LocalDate geburtsdatum,
            String beruf,
            String email,
            String telefonnummer,
            Adresse adresse,
            boolean bankdatenAktiv,
            String kontoinhaber,
            String iban,
            String bic
    ) {
        this.anrede = anrede;
        this.titel = titel;
        this.vorname = vorname;
        this.nachname = nachname;
        this.geburtsdatum = geburtsdatum;
        this.beruf = beruf;
        this.email = email;
        this.telefonnummer = telefonnummer;
        this.adresse = adresse;
        this.bankdatenAktiv = bankdatenAktiv;
        this.kontoinhaber = kontoinhaber;
        this.iban = iban;
        this.bic = bic;
    }

    public Long getId() {
        return id;
    }

    public String getAnrede() {
        return anrede;
    }

    public void setAnrede(String anrede) {
        this.anrede = anrede;
    }

    public String getTitel() {
        return titel;
    }

    public void setTitel(String titel) {
        this.titel = titel;
    }

    public String getVorname() {
        return vorname;
    }

    public void setVorname(String vorname) {
        this.vorname = vorname;
    }

    public String getNachname() {
        return nachname;
    }

    public void setNachname(String nachname) {
        this.nachname = nachname;
    }

    public LocalDate getGeburtsdatum() {
        return geburtsdatum;
    }

    public void setGeburtsdatum(LocalDate geburtsdatum) {
        this.geburtsdatum = geburtsdatum;
    }

    public String getBeruf() {
        return beruf;
    }

    public void setBeruf(String beruf) {
        this.beruf = beruf;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefonnummer() {
        return telefonnummer;
    }

    public void setTelefonnummer(String telefonnummer) {
        this.telefonnummer = telefonnummer;
    }

    public Adresse getAdresse() {
        return adresse;
    }

    public void setAdresse(Adresse adresse) {
        this.adresse = adresse;
    }

    public boolean isBankdatenAktiv() {
        return bankdatenAktiv;
    }

    public void setBankdatenAktiv(boolean bankdatenAktiv) {
        this.bankdatenAktiv = bankdatenAktiv;
    }

    public String getKontoinhaber() {
        return kontoinhaber;
    }

    public void setKontoinhaber(String kontoinhaber) {
        this.kontoinhaber = kontoinhaber;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public String getBic() {
        return bic;
    }

    public void setBic(String bic) {
        this.bic = bic;
    }

    public boolean isArchiviert() {
        return archiviert;
    }

    public void setArchiviert(boolean archiviert) {
        this.archiviert = archiviert;
    }
}
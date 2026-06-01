package de.hsbi.immobilienverwaltung.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Entity
public class Nutzer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String vorname;
    private String nachname;

    @NotBlank(message = "E-Mail darf nicht leer sein.")
    @Email(message = "Bitte gib eine gültige E-Mail-Adresse ein.")
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwort;

    @Column(nullable = false)
    private String rolle = "USER";

    public Nutzer() {}

    public Long getId() {return id;}

    public String getVorname() {return vorname;}

    public void setVorname(String vorname) {this.vorname = vorname;}

    public String getNachname() {return nachname;}

    public void setNachname(String nachname) {this.nachname = nachname;}

    public String getEmail() {return email;}

    public void setEmail(String email) {this.email = email;}

    public String getPasswort() {return passwort;}

    public void setPasswort(String passwort) {this.passwort = passwort;}

    public String getRolle() {return rolle;}

    public void setRolle(String rolle) {this.rolle = rolle;}
}
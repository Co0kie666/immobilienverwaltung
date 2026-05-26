package de.hsbi.immobilienverwaltung.service.interfaces;

import de.hsbi.immobilienverwaltung.domain.Nutzer;

public interface AuthService {

    Nutzer registrieren(
            String vorname,
            String nachname,
            String email,
            String passwort,
            String passwortWiederholen
    );

    Nutzer anmelden(
            String email,
            String passwort
    );

    void logout();
    boolean isLoggedIn();
    Nutzer getCurrentUser();
}
package de.hsbi.immobilienverwaltung.service.interfaces;

import de.hsbi.immobilienverwaltung.domain.Nutzer;

import java.util.Optional;

public interface AuthService {

    Nutzer registrieren(
            String vorname,
            String nachname,
            String email,
            String passwort,
            String passwortWiederholen
    );

    Optional<Nutzer> getCurrentUser();
}
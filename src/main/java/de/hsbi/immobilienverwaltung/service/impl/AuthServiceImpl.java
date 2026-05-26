package de.hsbi.immobilienverwaltung.service.impl;

import de.hsbi.immobilienverwaltung.domain.Nutzer;
import de.hsbi.immobilienverwaltung.repository.NutzerRepository;
import de.hsbi.immobilienverwaltung.service.interfaces.AuthService;
import org.springframework.stereotype.Service;
import org.mindrot.jbcrypt.BCrypt;

@Service
public class AuthServiceImpl implements AuthService {

    private final NutzerRepository nutzerRepository;

    public AuthServiceImpl(NutzerRepository nutzerRepository) {
        this.nutzerRepository = nutzerRepository;
    }

    @Override
    public Nutzer registrieren(String vorname,
                               String nachname,
                               String email,
                               String passwort,
                               String passwortWiederholen) {

        if (vorname == null || vorname.isBlank()) {
            throw new IllegalArgumentException("Vorname darf nicht leer sein.");
        }

        if (nachname == null || nachname.isBlank()) {
            throw new IllegalArgumentException("Nachname darf nicht leer sein.");
        }

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("E-Mail darf nicht leer sein.");
        }

        if (passwort == null || passwort.isBlank()) {
            throw new IllegalArgumentException("Passwort darf nicht leer sein.");
        }

        if (!passwort.equals(passwortWiederholen)) {
            throw new IllegalArgumentException("Passwörter stimmen nicht überein.");
        }

        String normalizedEmail = email.trim().toLowerCase();

        if (nutzerRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Diese E-Mail ist bereits registriert.");
        }

        Nutzer nutzer = new Nutzer();
        nutzer.setVorname(vorname);
        nutzer.setNachname(nachname);
        nutzer.setEmail(normalizedEmail);

        String hashedPassword = BCrypt.hashpw(passwort, BCrypt.gensalt());
        nutzer.setPasswort(hashedPassword);

        return nutzerRepository.save(nutzer);
    }

    @Override
    public Nutzer anmelden(String email, String passwort) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("E-Mail darf nicht leer sein.");
        }

        if (passwort == null || passwort.isBlank()) {
            throw new IllegalArgumentException("Passwort darf nicht leer sein.");
        }

        Nutzer nutzer = nutzerRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException("Kein Benutzer mit dieser E-Mail gefunden.")
                );

        if (!BCrypt.checkpw(passwort, nutzer.getPasswort())) {
            throw new IllegalArgumentException("Passwort ist falsch.");
        }
        return nutzer;
    }
}
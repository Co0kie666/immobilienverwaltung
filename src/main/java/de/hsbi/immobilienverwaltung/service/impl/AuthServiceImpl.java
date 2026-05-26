package de.hsbi.immobilienverwaltung.service.impl;

import com.vaadin.flow.server.VaadinSession;
import de.hsbi.immobilienverwaltung.domain.Nutzer;
import de.hsbi.immobilienverwaltung.repository.NutzerRepository;
import de.hsbi.immobilienverwaltung.service.interfaces.AuthService;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final NutzerRepository nutzerRepository;
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

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

        validateEmail(email);

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

        String hashedPassword = BCrypt.hashpw(passwort, BCrypt.gensalt());

        Nutzer nutzer = new Nutzer();
        nutzer.setVorname(vorname.trim());
        nutzer.setNachname(nachname.trim());
        nutzer.setEmail(normalizedEmail);
        nutzer.setPasswort(hashedPassword);

        return nutzerRepository.save(nutzer);
    }

    @Override
    public Nutzer anmelden(String email, String passwort) {

        validateEmail(email);

        if (passwort == null || passwort.isBlank()) {
            throw new IllegalArgumentException("Passwort darf nicht leer sein.");
        }

        String normalizedEmail = email.trim().toLowerCase();

        Nutzer nutzer = nutzerRepository.findByEmail(normalizedEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException("E-Mail oder Passwort ist falsch.")
                );

        if (!BCrypt.checkpw(passwort, nutzer.getPasswort())) {
            throw new IllegalArgumentException("E-Mail oder Passwort ist falsch.");
        }

        VaadinSession.getCurrent().setAttribute(Nutzer.class, nutzer);

        return nutzer;
    }

    @Override
    public void logout() {
        VaadinSession.getCurrent().setAttribute(Nutzer.class, null);
        VaadinSession.getCurrent().close();
    }

    @Override
    public boolean isLoggedIn() {
        return getCurrentUser() != null;
    }

    @Override
    public Nutzer getCurrentUser() {
        return VaadinSession.getCurrent().getAttribute(Nutzer.class);
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("E-Mail darf nicht leer sein.");
        }

        if (!email.matches(EMAIL_REGEX)) {
            throw new IllegalArgumentException("Bitte gib eine gültige E-Mail-Adresse ein.");
        }
    }
}
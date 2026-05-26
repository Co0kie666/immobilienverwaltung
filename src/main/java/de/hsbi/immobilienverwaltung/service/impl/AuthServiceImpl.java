package de.hsbi.immobilienverwaltung.service.impl;

import com.vaadin.flow.server.VaadinSession;
import de.hsbi.immobilienverwaltung.domain.Nutzer;
import de.hsbi.immobilienverwaltung.repository.NutzerRepository;
import de.hsbi.immobilienverwaltung.service.interfaces.AuthService;
import jakarta.validation.constraints.Pattern;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;

@Service
public class AuthServiceImpl implements AuthService {

    private final NutzerRepository nutzerRepository;
    private final Validator validator;

    public AuthServiceImpl(NutzerRepository nutzerRepository,
                           Validator validator) {
        this.nutzerRepository = nutzerRepository;
        this.validator = validator;
    }

    private void validateEmail(String email) {
        EmailCheck emailCheck = new EmailCheck(email);

        Set<ConstraintViolation<EmailCheck>> violations =
                validator.validate(emailCheck);

        if (!violations.isEmpty()) {
            throw new IllegalArgumentException(
                    violations.iterator().next().getMessage()
            );
        }
    }

    private record EmailCheck(
            @NotBlank(message = "E-Mail darf nicht leer sein.")
            @Email(message = "Bitte gib eine gültige E-Mail-Adresse ein.")
            @Pattern(
                    regexp = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$",
                    message = "Bitte gib eine gültige E-Mail-Adresse ein."
            )
            String email
    ) {}

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
}
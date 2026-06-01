package de.hsbi.immobilienverwaltung.service.impl;

import de.hsbi.immobilienverwaltung.domain.Nutzer;
import de.hsbi.immobilienverwaltung.repository.NutzerRepository;
import de.hsbi.immobilienverwaltung.service.interfaces.AuthService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
public class AuthServiceImpl implements AuthService {

    private final NutzerRepository nutzerRepository;
    private final PasswordEncoder passwordEncoder;
    private final Validator validator;

    public AuthServiceImpl(NutzerRepository nutzerRepository,
                           PasswordEncoder passwordEncoder,
                           Validator validator) {
        this.nutzerRepository = nutzerRepository;
        this.passwordEncoder = passwordEncoder;
        this.validator = validator;
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

        Nutzer nutzer = new Nutzer();
        nutzer.setVorname(vorname.trim());
        nutzer.setNachname(nachname.trim());
        nutzer.setEmail(normalizedEmail);
        nutzer.setPasswort(passwordEncoder.encode(passwort));
        nutzer.setRolle("USER");

        return nutzerRepository.save(nutzer);
    }

    @Override
    public Optional<Nutzer> getCurrentUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        String email = authentication.getName();

        if (email == null || email.equals("anonymousUser")) {
            return Optional.empty();
        }

        return nutzerRepository.findByEmail(email);
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
            String email
    ) {
    }
}
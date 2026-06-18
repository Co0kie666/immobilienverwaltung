package de.hsbi.immobilienverwaltung.service.impl;

import de.hsbi.immobilienverwaltung.domain.Nutzer;
import de.hsbi.immobilienverwaltung.repository.NutzerRepository;
import jakarta.validation.Validator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceImplTest {

    private NutzerRepository nutzerRepository;
    private PasswordEncoder passwordEncoder;
    private Validator validator;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        nutzerRepository = mock(NutzerRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        validator = mock(Validator.class);

        authService = new AuthServiceImpl(
                nutzerRepository,
                passwordEncoder,
                validator
        );

        when(validator.validate(any()))
                .thenReturn(Collections.emptySet());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();

        nutzerRepository = null;
        passwordEncoder = null;
        validator = null;
        authService = null;
    }

    @Test
    void registrierenSpeichertNeuenNutzerMitVerschluesseltemPasswort() {
        when(nutzerRepository.existsByEmail("max@test.de"))
                .thenReturn(false);

        when(passwordEncoder.encode("Passwort123!"))
                .thenReturn("verschluesseltes-passwort");

        when(nutzerRepository.save(any(Nutzer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Nutzer result = authService.registrieren(
                " Max ",
                " Mustermann ",
                " MAX@TEST.DE ",
                "Passwort123!",
                "Passwort123!"
        );

        ArgumentCaptor<Nutzer> nutzerCaptor =
                ArgumentCaptor.forClass(Nutzer.class);

        verify(nutzerRepository).save(nutzerCaptor.capture());

        Nutzer gespeicherterNutzer = nutzerCaptor.getValue();

        assertEquals("Max", gespeicherterNutzer.getVorname());
        assertEquals("Mustermann", gespeicherterNutzer.getNachname());
        assertEquals("max@test.de", gespeicherterNutzer.getEmail());
        assertEquals("verschluesseltes-passwort", gespeicherterNutzer.getPasswort());
        assertEquals("USER", gespeicherterNutzer.getRolle());

        assertEquals(gespeicherterNutzer, result);

        verify(passwordEncoder).encode("Passwort123!");
        verify(nutzerRepository).existsByEmail("max@test.de");
    }

    @Test
    void registrierenWirftExceptionWennEmailBereitsRegistriertIst() {
        when(nutzerRepository.existsByEmail("max@test.de"))
                .thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.registrieren(
                        "Max",
                        "Mustermann",
                        "max@test.de",
                        "Passwort123!",
                        "Passwort123!"
                )
        );

        assertEquals(
                "Diese E-Mail ist bereits registriert.",
                exception.getMessage()
        );

        verify(nutzerRepository).existsByEmail("max@test.de");
        verify(nutzerRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void getCurrentUserGibtAngemeldetenNutzerZurueck() {
        Nutzer nutzer = new Nutzer();
        nutzer.setEmail("max@test.de");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "max@test.de",
                        "irrelevant",
                        List.of()
                )
        );

        when(nutzerRepository.findByEmail("max@test.de"))
                .thenReturn(Optional.of(nutzer));

        Optional<Nutzer> result = authService.getCurrentUser();

        assertTrue(result.isPresent());
        assertEquals("max@test.de", result.get().getEmail());

        verify(nutzerRepository).findByEmail("max@test.de");
    }
}
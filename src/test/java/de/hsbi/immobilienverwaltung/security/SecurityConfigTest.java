package de.hsbi.immobilienverwaltung.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class SecurityConfigTest {

    private SecurityConfig securityConfig;
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig();
        passwordEncoder = securityConfig.passwordEncoder();
    }

    @AfterEach
    void tearDown() {
        passwordEncoder = null;
        securityConfig = null;
    }

    @Test
    void passwordEncoderVerschluesseltPasswortMitBCrypt() {
        String klaresPasswort = "Passwort123!";

        String verschluesseltesPasswort =
                passwordEncoder.encode(klaresPasswort);

        assertNotNull(verschluesseltesPasswort);

        assertNotEquals(
                klaresPasswort,
                verschluesseltesPasswort
        );

        assertTrue(
                passwordEncoder.matches(
                        klaresPasswort,
                        verschluesseltesPasswort
                )
        );

        assertInstanceOf(
                BCryptPasswordEncoder.class,
                passwordEncoder
        );
    }

    @Test
    void passwordEncoderLehntFalschesPasswortAb() {
        String verschluesseltesPasswort =
                passwordEncoder.encode("Passwort123!");

        assertFalse(
                passwordEncoder.matches(
                        "FalschesPasswort!",
                        verschluesseltesPasswort
                )
        );
    }
}
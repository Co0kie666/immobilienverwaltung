package de.hsbi.immobilienverwaltung.security;

import de.hsbi.immobilienverwaltung.domain.Nutzer;
import de.hsbi.immobilienverwaltung.repository.NutzerRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomUserDetailsServiceTest {

    private NutzerRepository nutzerRepository;
    private CustomUserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        nutzerRepository = mock(NutzerRepository.class);

        userDetailsService = new CustomUserDetailsService(
                nutzerRepository
        );
    }

    @AfterEach
    void tearDown() {
        nutzerRepository = null;
        userDetailsService = null;
    }

    @Test
    void loadUserByUsernameLaedtNutzerMitRolleUser() {
        Nutzer nutzer = new Nutzer();
        nutzer.setEmail("max@test.de");
        nutzer.setPasswort("$2a$10$verschluesseltesPasswort");
        nutzer.setRolle("USER");

        when(nutzerRepository.findByEmail("max@test.de"))
                .thenReturn(Optional.of(nutzer));

        UserDetails result = userDetailsService.loadUserByUsername(
                " MAX@TEST.DE "
        );

        assertEquals("max@test.de", result.getUsername());
        assertEquals(
                "$2a$10$verschluesseltesPasswort",
                result.getPassword()
        );

        assertTrue(
                result.getAuthorities().stream()
                        .anyMatch(authority ->
                                authority.getAuthority().equals("ROLE_USER")
                        )
        );

        verify(nutzerRepository).findByEmail("max@test.de");
    }

    @Test
    void loadUserByUsernameWirftExceptionBeiUnbekannterEmail() {
        when(nutzerRepository.findByEmail("unbekannt@test.de"))
                .thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername(
                        " UNBEKANNT@TEST.DE "
                )
        );

        assertEquals(
                "Nutzer nicht gefunden.",
                exception.getMessage()
        );

        verify(nutzerRepository).findByEmail("unbekannt@test.de");
    }
}
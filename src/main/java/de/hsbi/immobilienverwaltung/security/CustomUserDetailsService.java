package de.hsbi.immobilienverwaltung.security;

import de.hsbi.immobilienverwaltung.domain.Nutzer;
import de.hsbi.immobilienverwaltung.repository.NutzerRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final NutzerRepository nutzerRepository;

    public CustomUserDetailsService(NutzerRepository nutzerRepository) {
        this.nutzerRepository = nutzerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) //Laedt einen Nutzer anhand seiner E-Mail-Adresse.
            throws UsernameNotFoundException {

        String normalizedEmail = email.trim().toLowerCase();

        Nutzer nutzer = nutzerRepository.findByEmail(normalizedEmail)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Nutzer nicht gefunden.")
                );

        return User.builder()
                .username(nutzer.getEmail())
                .password(nutzer.getPasswort())
                .roles(nutzer.getRolle())
                .build();
    }
}
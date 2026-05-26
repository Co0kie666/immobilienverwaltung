package de.hsbi.immobilienverwaltung.repository;

import de.hsbi.immobilienverwaltung.domain.Nutzer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NutzerRepository extends JpaRepository<Nutzer, Long> {

    Optional<Nutzer> findByEmail(String email);

    boolean existsByEmail(String email);
}
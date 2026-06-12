package de.hsbi.immobilienverwaltung.repository;

import de.hsbi.immobilienverwaltung.domain.Mieter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MieterRepository extends JpaRepository<Mieter, Long> {

    List<Mieter> findByArchiviertFalse();

    List<Mieter> findByArchiviertTrue();

    List<Mieter> findByVornameContainingIgnoreCaseOrNachnameContainingIgnoreCase(
            String vorname,
            String nachname
    );

    Optional<Mieter> findByEmailIgnoreCase(String email);
}
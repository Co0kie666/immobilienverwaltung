package de.hsbi.immobilienverwaltung.repository;

import de.hsbi.immobilienverwaltung.domain.Ausgabe;
import de.hsbi.immobilienverwaltung.domain.Immobilie;
import de.hsbi.immobilienverwaltung.domain.Mieteinheit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AusgabeRepository extends JpaRepository<Ausgabe, Long> {

    List<Ausgabe> findByImmobilie(Immobilie immobilie);

    List<Ausgabe> findByMieteinheit(Mieteinheit mieteinheit);

    List<Ausgabe> findByImmobilieAndDatumBetween(
            Immobilie immobilie,
            LocalDate startDatum,
            LocalDate endDatum
    );

    List<Ausgabe> findByStatus(String status);

    long countByStatus(String status);

    List<Ausgabe> findByDatumBetween(
            LocalDate startDatum,
            LocalDate endDatum
    );
}
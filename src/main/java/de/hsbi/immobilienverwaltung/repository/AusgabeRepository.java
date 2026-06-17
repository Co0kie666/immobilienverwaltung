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

    /**
     * Findet alle Ausgaben einer Immobilie innerhalb eines Datumsbereichs.
     * Start- und Enddatum werden von Spring Data inklusive berücksichtigt.
     */
    List<Ausgabe> findByImmobilieAndDatumBetween(
            Immobilie immobilie,
            LocalDate startDatum,
            LocalDate endDatum
    );

    /**
     * Findet alle Ausgaben mit einem bestimmten Zahlungsstatus.
     */
    List<Ausgabe> findByStatus(String status);

    /**
     * Zählt alle Ausgaben mit einem bestimmten Zahlungsstatus.
     */
    long countByStatus(String status);

    /**
     * Findet alle Ausgaben innerhalb eines Datumsbereichs.
     * Start- und Enddatum werden von Spring Data inklusive berücksichtigt.
     */
    List<Ausgabe> findByDatumBetween(
            LocalDate startDatum,
            LocalDate endDatum
    );
}
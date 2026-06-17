package de.hsbi.immobilienverwaltung.repository;

import de.hsbi.immobilienverwaltung.domain.Mietvertrag;
import de.hsbi.immobilienverwaltung.domain.Zahlungseingang;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ZahlungsEingangRepository extends JpaRepository<Zahlungseingang, Long> {

    List<Zahlungseingang> findByMietvertrag(Mietvertrag mietvertrag);

    /**
     * Findet alle Zahlungseingänge eines Mietvertrags innerhalb eines Datumsbereichs.
     * Start- und Enddatum werden von Spring Data inklusive berücksichtigt.
     */
    List<Zahlungseingang> findByMietvertragAndZahlungsdatumBetween(
            Mietvertrag mietvertrag,
            LocalDate startDatum,
            LocalDate endDatum
    );

    /**
     * Prüft, ob für einen Mietvertrag im angegebenen Zeitraum mindestens eine Zahlung existiert.
     */
    boolean existsByMietvertragAndZahlungsdatumBetween(
            Mietvertrag mietvertrag,
            LocalDate startDatum,
            LocalDate endDatum
    );

    /**
     * Findet Zahlungseingänge zu einer Immobilie über die Beziehung
     * Zahlungseingang -> Mietvertrag -> Mieteinheit -> Immobilie.
     */
    List<Zahlungseingang> findByMietvertrag_Mieteinheit_Immobilie_IdAndStatus(
            Long immobilieId,
            String status
    );

    /**
     * Zählt Zahlungseingänge zu einer Immobilie über die Beziehung
     * Zahlungseingang -> Mietvertrag -> Mieteinheit -> Immobilie.
     */
    long countByMietvertrag_Mieteinheit_Immobilie_IdAndStatus(
            Long immobilieId,
            String status
    );

    /**
     * Findet alle Zahlungseingänge mit einem bestimmten Zahlungsstatus.
     */
    List<Zahlungseingang> findByStatus(String status);

    /**
     * Findet alle Zahlungseingänge mit einem bestimmten Status innerhalb eines Datumsbereichs.
     * Start- und Enddatum werden von Spring Data inklusive berücksichtigt.
     */
    List<Zahlungseingang> findByStatusAndZahlungsdatumBetween(
            String status,
            LocalDate startDatum,
            LocalDate endDatum
    );
}
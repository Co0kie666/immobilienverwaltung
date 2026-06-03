package de.hsbi.immobilienverwaltung.repository;

import de.hsbi.immobilienverwaltung.domain.Mietvertrag;
import de.hsbi.immobilienverwaltung.domain.Zahlungseingang;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ZahlungsEingangRepository extends JpaRepository<Zahlungseingang, Long> {

    List<Zahlungseingang> findByMietvertrag(Mietvertrag mietvertrag);

    List<Zahlungseingang> findByMietvertragAndZahlungsdatumBetween(
            Mietvertrag mietvertrag,
            LocalDate startDatum,
            LocalDate endDatum
    );

    boolean existsByMietvertragAndZahlungsdatumBetween(
            Mietvertrag mietvertrag,
            LocalDate startDatum,
            LocalDate endDatum
    );
}
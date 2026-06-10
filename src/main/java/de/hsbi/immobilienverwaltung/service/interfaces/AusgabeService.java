package de.hsbi.immobilienverwaltung.service.interfaces;
import de.hsbi.immobilienverwaltung.domain.Ausgabe;
import de.hsbi.immobilienverwaltung.domain.Immobilie;
import de.hsbi.immobilienverwaltung.domain.Mieteinheit;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface AusgabeService {

    Ausgabe speichereAusgabe(Ausgabe ausgabe);

    List<Ausgabe> findeAlleAusgaben();

    Optional<Ausgabe> findeAusgabeNachId(Long id);

    List<Ausgabe> findeAusgabenNachImmobilie(Immobilie immobilie);

    List<Ausgabe> findeAusgabenNachMieteinheit(Mieteinheit mieteinheit);

    List<Ausgabe> findeAusgabenNachImmobilieUndZeitraum(
            Immobilie immobilie,
            LocalDate startDatum,
            LocalDate endDatum
    );

    BigDecimal berechneSummeAusgabenFuerImmobilie(
            Immobilie immobilie,
            LocalDate startDatum,
            LocalDate endDatum
    );

    void loescheAusgabe(Long id);

    BigDecimal berechneOffeneAusgaben();

    long zaehleOffeneAusgaben();

    BigDecimal berechneAusgabenImZeitraum(
            LocalDate startDatum,
            LocalDate endDatum
    );

    BigDecimal berechneBezahlteAusgaben();

    BigDecimal berechneBezahlteAusgabenImZeitraum(
            LocalDate startDatum,
            LocalDate endDatum,
            Long immobilieId
    );

    Map<String, BigDecimal> berechneKostenverteilungImZeitraum(
            LocalDate startDatum,
            LocalDate endDatum,
            Long immobilieId
    );

    List<Ausgabe> findeAusgabenImZeitraum(
            LocalDate startDatum,
            LocalDate endDatum,
            Long immobilieId
    );
}

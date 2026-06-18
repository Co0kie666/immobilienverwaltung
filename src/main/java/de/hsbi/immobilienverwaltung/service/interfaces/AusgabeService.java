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

    /**
     * Findet alle Ausgaben einer Immobilie innerhalb eines Datumsbereichs.
     */
    List<Ausgabe> findeAusgabenNachImmobilieUndZeitraum(
            Immobilie immobilie,
            LocalDate startDatum,
            LocalDate endDatum
    );

    /**
     * Berechnet die Summe aller Ausgaben einer Immobilie innerhalb eines Datumsbereichs.
     */
    BigDecimal berechneSummeAusgabenFuerImmobilie(
            Immobilie immobilie,
            LocalDate startDatum,
            LocalDate endDatum
    );

    void loescheAusgabe(Long id);

    /**
     * Berechnet die Summe aller Ausgaben mit dem Status "Offen / Ausstehend".
     */
    BigDecimal berechneOffeneAusgaben();

    /**
     * Zählt alle Ausgaben mit dem Status "Offen / Ausstehend".
     */
    long zaehleOffeneAusgaben();

    /**
     * Berechnet die Summe aller Ausgaben innerhalb eines Datumsbereichs.
     */
    BigDecimal berechneAusgabenImZeitraum(
            LocalDate startDatum,
            LocalDate endDatum
    );

    /**
     * Berechnet die Summe aller Ausgaben mit dem Status "Bezahlt / Erledigt".
     */
    BigDecimal berechneBezahlteAusgaben();

    /**
     * Berechnet die Summe bezahlter Ausgaben innerhalb eines Datumsbereichs.
     * Wenn immobilieId null ist, werden alle Immobilien berücksichtigt.
     */
    BigDecimal berechneBezahlteAusgabenImZeitraum(
            LocalDate startDatum,
            LocalDate endDatum,
            Long immobilieId
    );

    /**
     * Gruppiert Ausgaben innerhalb eines Datumsbereichs nach Kategorie
     * und berechnet pro Kategorie die Gesamtsumme.
     * Wenn immobilieId null ist, werden alle Immobilien berücksichtigt.
     */
    Map<String, BigDecimal> berechneKostenverteilungImZeitraum(
            LocalDate startDatum,
            LocalDate endDatum,
            Long immobilieId
    );

    /**
     * Findet Ausgaben innerhalb eines Datumsbereichs.
     * Wenn immobilieId null ist, werden alle Immobilien berücksichtigt.
     */
    List<Ausgabe> findeAusgabenImZeitraum(
            LocalDate startDatum,
            LocalDate endDatum,
            Long immobilieId
    );
}
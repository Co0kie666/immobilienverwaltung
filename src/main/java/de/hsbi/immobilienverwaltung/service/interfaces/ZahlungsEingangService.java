package de.hsbi.immobilienverwaltung.service.interfaces;

import de.hsbi.immobilienverwaltung.domain.Mietvertrag;
import de.hsbi.immobilienverwaltung.domain.Zahlungseingang;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ZahlungsEingangService {

    Zahlungseingang speichereZahlungseingang(Zahlungseingang zahlungseingang);

    List<Zahlungseingang> findeAlleZahlungseingaenge();

    Optional<Zahlungseingang> findeZahlungseingangNachId(Long id);

    List<Zahlungseingang> findeZahlungseingaengeNachMietvertrag(Mietvertrag mietvertrag);

    /**
     * Findet alle Zahlungseingänge eines Mietvertrags innerhalb eines Datumsbereichs.
     */
    List<Zahlungseingang> findeZahlungseingaengeNachMietvertragUndZeitraum(
            Mietvertrag mietvertrag,
            LocalDate startDatum,
            LocalDate endDatum
    );

    /**
     * Berechnet die Summe aller Zahlungseingänge eines Mietvertrags innerhalb eines Datumsbereichs.
     */
    BigDecimal berechneSummeZahlungseingaengeFuerMietvertrag(
            Mietvertrag mietvertrag,
            LocalDate startDatum,
            LocalDate endDatum
    );

    /**
     * Prüft, ob für einen Mietvertrag im angegebenen Zeitraum mindestens eine Zahlung vorhanden ist.
     */
    boolean hatZahlungImZeitraum(
            Mietvertrag mietvertrag,
            LocalDate startDatum,
            LocalDate endDatum
    );

    void loescheZahlungseingang(Long id);

    /**
     * Berechnet die Summe offener Zahlungseingänge für eine Immobilie.
     */
    BigDecimal berechneOffeneZahlungenFuerImmobilie(Long immobilieId);

    /**
     * Zählt offene Zahlungseingänge für eine Immobilie.
     */
    long zaehleOffeneZahlungenFuerImmobilie(Long immobilieId);

    /**
     * Berechnet die Summe aller Zahlungseingänge mit dem Status "Bezahlt / Erledigt".
     */
    BigDecimal berechneGesamteBezahlteZahlungseingaenge();

    /**
     * Berechnet die Summe bezahlter Zahlungseingänge innerhalb eines Datumsbereichs.
     * Wenn eine der IDs null ist, wird der jeweilige Filter nicht angewendet.
     */
    BigDecimal berechneBezahlteZahlungseingaengeImZeitraum(
            LocalDate startDatum,
            LocalDate endDatum,
            Long immobilieId,
            Long mieteinheitId,
            Long mieterId
    );

    /**
     * Findet alle Zahlungseingänge mit dem Status "Offen / Ausstehend".
     */
    List<Zahlungseingang> findeOffeneZahlungseingaenge();

    /**
     * Berechnet die Summe aller Zahlungseingänge mit dem Status "Offen / Ausstehend".
     */
    BigDecimal berechneOffeneZahlungseingaenge();

    /**
     * Berechnet die Summe offener Zahlungseingänge innerhalb eines Datumsbereichs.
     * Wenn eine der IDs null ist, wird der jeweilige Filter nicht angewendet.
     */
    BigDecimal berechneOffeneZahlungseingaengeImZeitraum(
            LocalDate startDatum,
            LocalDate endDatum,
            Long immobilieId,
            Long mieteinheitId,
            Long mieterId
    );

    /**
     * Findet Zahlungseingänge innerhalb eines Datumsbereichs.
     * Wenn eine der IDs null ist, wird der jeweilige Filter nicht angewendet.
     */
    List<Zahlungseingang> findeZahlungseingaengeImZeitraum(
            LocalDate startDatum,
            LocalDate endDatum,
            Long immobilieId,
            Long mieteinheitId,
            Long mieterId
    );
}
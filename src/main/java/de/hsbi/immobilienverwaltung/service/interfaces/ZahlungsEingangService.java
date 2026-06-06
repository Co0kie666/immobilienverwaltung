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

    List<Zahlungseingang> findeZahlungseingaengeNachMietvertragUndZeitraum(
            Mietvertrag mietvertrag,
            LocalDate startDatum,
            LocalDate endDatum
    );

    BigDecimal berechneSummeZahlungseingaengeFuerMietvertrag(
            Mietvertrag mietvertrag,
            LocalDate startDatum,
            LocalDate endDatum
    );

    boolean hatZahlungImZeitraum(
            Mietvertrag mietvertrag,
            LocalDate startDatum,
            LocalDate endDatum
    );

    void loescheZahlungseingang(Long id);
}
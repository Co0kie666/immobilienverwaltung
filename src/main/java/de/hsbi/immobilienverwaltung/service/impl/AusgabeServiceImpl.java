package de.hsbi.immobilienverwaltung.service.impl;

import de.hsbi.immobilienverwaltung.domain.Ausgabe;
import de.hsbi.immobilienverwaltung.domain.Immobilie;
import de.hsbi.immobilienverwaltung.domain.Mieteinheit;
import de.hsbi.immobilienverwaltung.repository.AusgabeRepository;
import de.hsbi.immobilienverwaltung.service.interfaces.AusgabeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AusgabeServiceImpl implements AusgabeService {

    private final AusgabeRepository ausgabeRepository;

    public AusgabeServiceImpl(AusgabeRepository ausgabeRepository) {
        this.ausgabeRepository = ausgabeRepository;
    }

    @Override
    @Transactional
    public Ausgabe speichereAusgabe(Ausgabe ausgabe) {

        // Eine Ausgabe benötigt alle fachlich notwendigen Pflichtfelder,
        // bevor sie gespeichert werden darf.
        if (ausgabe == null) {
            throw new IllegalArgumentException("Ausgabe darf nicht leer sein.");
        }

        if (ausgabe.getKategorie() == null) {
            throw new IllegalArgumentException("Kategorie muss ausgewählt werden.");
        }

        if (ausgabe.getBetrag() == null ||
                ausgabe.getBetrag().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Betrag muss größer als 0 sein.");
        }

        if (ausgabe.getDatum() == null) {
            throw new IllegalArgumentException("Datum muss angegeben werden.");
        }

        if (ausgabe.getImmobilie() == null && ausgabe.getMieteinheit() == null) {
            throw new IllegalArgumentException("Bitte Immobilie oder Mieteinheit auswählen.");
        }

        return ausgabeRepository.save(ausgabe);
    }

    @Override
    public List<Ausgabe> findeAlleAusgaben() {
        return ausgabeRepository.findAll();
    }

    @Override
    public Optional<Ausgabe> findeAusgabeNachId(Long id) {
        return ausgabeRepository.findById(id);
    }

    @Override
    public List<Ausgabe> findeAusgabenNachImmobilie(Immobilie immobilie) {
        return ausgabeRepository.findByImmobilie(immobilie);
    }

    @Override
    public List<Ausgabe> findeAusgabenNachMieteinheit(Mieteinheit mieteinheit) {
        return ausgabeRepository.findByMieteinheit(mieteinheit);
    }

    @Override
    public List<Ausgabe> findeAusgabenNachImmobilieUndZeitraum(
            Immobilie immobilie,
            LocalDate startDatum,
            LocalDate endDatum
    ) {
        return ausgabeRepository.findByImmobilieAndDatumBetween(
                immobilie,
                startDatum,
                endDatum
        );
    }

    @Override
    public BigDecimal berechneSummeAusgabenFuerImmobilie(
            Immobilie immobilie,
            LocalDate startDatum,
            LocalDate endDatum
    ) {
        List<Ausgabe> ausgaben = ausgabeRepository.findByImmobilieAndDatumBetween(
                immobilie,
                startDatum,
                endDatum
        );

        return ausgaben.stream()
                .map(Ausgabe::getBetrag)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Transactional
    public void loescheAusgabe(Long id) {
        ausgabeRepository.deleteById(id);
    }

    @Override
    public BigDecimal berechneOffeneAusgaben() {
        return ausgabeRepository.findByStatus("Offen / Ausstehend")
                .stream()
                .map(Ausgabe::getBetrag)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal berechneBezahlteAusgaben() {
        return ausgabeRepository.findByStatus("Bezahlt / Erledigt")
                .stream()
                .map(Ausgabe::getBetrag)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public long zaehleOffeneAusgaben() {
        return ausgabeRepository.countByStatus("Offen / Ausstehend");
    }

    @Override
    public BigDecimal berechneAusgabenImZeitraum(
            LocalDate startDatum,
            LocalDate endDatum
    ) {
        return ausgabeRepository
                .findByDatumBetween(
                        startDatum,
                        endDatum
                )
                .stream()
                .map(Ausgabe::getBetrag)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal berechneBezahlteAusgabenImZeitraum(
            LocalDate startDatum,
            LocalDate endDatum,
            Long immobilieId
    ) {
        return ausgabeRepository.findAll()
                .stream()
                // Das Dashboard betrachtet nur Ausgaben im gewählten Zeitraum.
                .filter(a -> !a.getDatum().isBefore(startDatum))
                .filter(a -> !a.getDatum().isAfter(endDatum))

                // Für Einnahmen-/Ausgaben-KPIs zählen nur erledigte Ausgaben.
                .filter(a -> a.getStatus().equals("Bezahlt / Erledigt"))

                // null bedeutet: Es wurde kein Immobilienfilter gesetzt.
                .filter(a -> immobilieId == null ||
                        a.getImmobilie()
                                .getId()
                                .equals(immobilieId))
                .map(Ausgabe::getBetrag)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public Map<String, BigDecimal> berechneKostenverteilungImZeitraum(
            LocalDate startDatum,
            LocalDate endDatum,
            Long immobilieId
    ) {
        return ausgabeRepository.findAll()
                .stream()
                .filter(a -> a.getDatum() != null)
                .filter(a -> !a.getDatum().isBefore(startDatum))
                .filter(a -> !a.getDatum().isAfter(endDatum))

                // null bedeutet: Es wurde kein Immobilienfilter gesetzt.
                .filter(a -> immobilieId == null ||
                        (
                                a.getImmobilie() != null &&
                                        a.getImmobilie()
                                                .getId()
                                                .equals(immobilieId)
                        ))
                .filter(a -> a.getBetrag() != null)

                // LinkedHashMap behält die Reihenfolge der ermittelten Kategorien bei.
                .collect(Collectors.groupingBy(
                        this::ermittleKategorieName,
                        LinkedHashMap::new,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                Ausgabe::getBetrag,
                                BigDecimal::add
                        )
                ));
    }

    private String ermittleKategorieName(Ausgabe ausgabe) {
        if (ausgabe.getKategorie() == null) {
            return "Ohne Kategorie";
        }

        return ausgabe.getKategorie().getLabel();
    }

    @Override
    public List<Ausgabe> findeAusgabenImZeitraum(
            LocalDate startDatum,
            LocalDate endDatum,
            Long immobilieId
    ) {
        return ausgabeRepository.findAll()
                .stream()
                .filter(a -> a.getDatum() != null)
                .filter(a -> !a.getDatum().isBefore(startDatum))
                .filter(a -> !a.getDatum().isAfter(endDatum))

                // null bedeutet: Es wurde kein Immobilienfilter gesetzt.
                .filter(a -> immobilieId == null ||
                        (
                                a.getImmobilie() != null &&
                                        a.getImmobilie()
                                                .getId()
                                                .equals(immobilieId)
                        ))
                .toList();
    }
}
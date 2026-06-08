package de.hsbi.immobilienverwaltung.service.impl;

import de.hsbi.immobilienverwaltung.domain.Mietvertrag;
import de.hsbi.immobilienverwaltung.domain.Zahlungseingang;
import de.hsbi.immobilienverwaltung.repository.ZahlungsEingangRepository;
import de.hsbi.immobilienverwaltung.service.interfaces.ZahlungsEingangService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ZahlungsEingangServiceImpl implements ZahlungsEingangService {

    private final ZahlungsEingangRepository zahlungsEingangRepository;

    public ZahlungsEingangServiceImpl(ZahlungsEingangRepository zahlungsEingangRepository) {
        this.zahlungsEingangRepository = zahlungsEingangRepository;
    }

    @Override
    @Transactional
    public Zahlungseingang speichereZahlungseingang(Zahlungseingang zahlungseingang) {

        if (zahlungseingang == null) {
            throw new IllegalArgumentException("Zahlungseingang darf nicht leer sein.");
        }

        if (zahlungseingang.getTyp() == null) {
            throw new IllegalArgumentException("Zahlungstyp muss ausgewählt werden.");
        }

        if (zahlungseingang.getBetrag() == null ||
                zahlungseingang.getBetrag().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Betrag muss größer als 0 sein.");
        }

        if (zahlungseingang.getZahlungsdatum() == null) {
            throw new IllegalArgumentException("Zahlungsdatum muss angegeben werden.");
        }

        if (zahlungseingang.getMietvertrag() == null) {
            throw new IllegalArgumentException("Mietvertrag muss ausgewählt werden.");
        }

        return zahlungsEingangRepository.save(zahlungseingang);
    }

    @Override
    public List<Zahlungseingang> findeAlleZahlungseingaenge() {
        return zahlungsEingangRepository.findAll();
    }

    @Override
    public Optional<Zahlungseingang> findeZahlungseingangNachId(Long id) {
        return zahlungsEingangRepository.findById(id);
    }

    @Override
    public List<Zahlungseingang> findeZahlungseingaengeNachMietvertrag(Mietvertrag mietvertrag) {
        return zahlungsEingangRepository.findByMietvertrag(mietvertrag);
    }

    @Override
    public List<Zahlungseingang> findeZahlungseingaengeNachMietvertragUndZeitraum(
            Mietvertrag mietvertrag,
            LocalDate startDatum,
            LocalDate endDatum
    ) {
        return zahlungsEingangRepository.findByMietvertragAndZahlungsdatumBetween(
                mietvertrag,
                startDatum,
                endDatum
        );
    }

    @Override
    public BigDecimal berechneSummeZahlungseingaengeFuerMietvertrag(
            Mietvertrag mietvertrag,
            LocalDate startDatum,
            LocalDate endDatum
    ) {
        List<Zahlungseingang> zahlungen =
                zahlungsEingangRepository.findByMietvertragAndZahlungsdatumBetween(
                        mietvertrag,
                        startDatum,
                        endDatum
                );

        BigDecimal summe = BigDecimal.ZERO;

        for (Zahlungseingang zahlung : zahlungen) {
            if (zahlung.getBetrag() != null) {
                summe = summe.add(zahlung.getBetrag());
            }
        }

        return summe;
    }

    @Override
    public boolean hatZahlungImZeitraum(
            Mietvertrag mietvertrag,
            LocalDate startDatum,
            LocalDate endDatum
    ) {
        return zahlungsEingangRepository.existsByMietvertragAndZahlungsdatumBetween(
                mietvertrag,
                startDatum,
                endDatum
        );
    }

    @Override
    @Transactional
    public void loescheZahlungseingang(Long id) {
        zahlungsEingangRepository.deleteById(id);
    }

    @Override
    public BigDecimal berechneOffeneZahlungenFuerImmobilie(Long immobilieId) {
        List<Zahlungseingang> offeneZahlungen =
                zahlungsEingangRepository.findByMietvertrag_Mieteinheit_Immobilie_IdAndStatus(
                        immobilieId,
                        "Offen / Ausstehend"
                );

        BigDecimal summe = BigDecimal.ZERO;

        for (Zahlungseingang zahlung : offeneZahlungen) {
            if (zahlung.getBetrag() != null) {
                summe = summe.add(zahlung.getBetrag());
            }
        }

        return summe;
    }

    @Override
    public long zaehleOffeneZahlungenFuerImmobilie(Long immobilieId) {
        return zahlungsEingangRepository.countByMietvertrag_Mieteinheit_Immobilie_IdAndStatus(
                immobilieId,
                "Offen / Ausstehend"
        );
    }

    @Override
    public BigDecimal berechneGesamteBezahlteZahlungseingaenge() {
        return zahlungsEingangRepository.findByStatus("Bezahlt / Erledigt")
                .stream()
                .map(Zahlungseingang::getBetrag)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal berechneOffeneZahlungseingaenge() {
        return zahlungsEingangRepository.findByStatus("Offen / Ausstehend")
                .stream()
                .map(Zahlungseingang::getBetrag)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal berechneBezahlteZahlungseingaengeImZeitraum(
            LocalDate startDatum,
            LocalDate endDatum,
            Long immobilieId,
            Long mieteinheitId,
            Long mieterId
    ) {
        return zahlungsEingangRepository.findAll()
                .stream()
                .filter(z -> !z.getZahlungsdatum().isBefore(startDatum))
                .filter(z -> !z.getZahlungsdatum().isAfter(endDatum))
                .filter(z -> z.getStatus().equals("Bezahlt / Erledigt"))
                .filter(z -> immobilieId == null ||
                        z.getMietvertrag()
                                .getMieteinheit()
                                .getImmobilie()
                                .getId()
                                .equals(immobilieId))
                .filter(z -> mieteinheitId == null ||
                        z.getMietvertrag()
                                .getMieteinheit()
                                .getId()
                                .equals(mieteinheitId))
                .filter(z -> mieterId == null ||
                        z.getMietvertrag()
                                .getMieter()
                                .getId()
                                .equals(mieterId))
                .map(Zahlungseingang::getBetrag)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    @Override
    public BigDecimal berechneOffeneZahlungseingaengeImZeitraum(
            LocalDate startDatum,
            LocalDate endDatum,
            Long immobilieId,
            Long mieteinheitId,
            Long mieterId
    ) {
        return zahlungsEingangRepository.findAll()
                .stream()
                .filter(z -> !z.getZahlungsdatum().isBefore(startDatum))
                .filter(z -> !z.getZahlungsdatum().isAfter(endDatum))
                .filter(z -> !z.getStatus().equals("Bezahlt / Erledigt"))
                .filter(z -> immobilieId == null ||
                        z.getMietvertrag()
                                .getMieteinheit()
                                .getImmobilie()
                                .getId()
                                .equals(immobilieId))
                .filter(z -> mieteinheitId == null ||
                        z.getMietvertrag()
                                .getMieteinheit()
                                .getId()
                                .equals(mieteinheitId))
                .filter(z -> mieterId == null ||
                        z.getMietvertrag()
                                .getMieter()
                                .getId()
                                .equals(mieterId))
                .map(Zahlungseingang::getBetrag)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    @Override
    public List<Zahlungseingang> findeOffeneZahlungseingaenge() {
        return zahlungsEingangRepository.findByStatus("Offen / Ausstehend");
    }
}
package de.hsbi.immobilienverwaltung.service.impl;

import de.hsbi.immobilienverwaltung.domain.Adresse;
import de.hsbi.immobilienverwaltung.domain.Immobilie;
import de.hsbi.immobilienverwaltung.domain.Mieteinheit;
import de.hsbi.immobilienverwaltung.domain.enums.Immobilientyp;
import de.hsbi.immobilienverwaltung.domain.enums.Mieteinheitstatus;
import de.hsbi.immobilienverwaltung.domain.enums.Vertragsstatus;
import de.hsbi.immobilienverwaltung.repository.ImmobilieRepository;
import de.hsbi.immobilienverwaltung.repository.MieteinheitRepository;
import de.hsbi.immobilienverwaltung.repository.MietvertragRepository;
import de.hsbi.immobilienverwaltung.service.interfaces.ImmobilieService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ImmobilieServiceImpl implements ImmobilieService {

    private final ImmobilieRepository immobilieRepository;
    private final MieteinheitRepository mieteinheitRepository;
    private final MietvertragRepository mietvertragRepository;

    public ImmobilieServiceImpl(
            ImmobilieRepository immobilieRepository,
            MieteinheitRepository mieteinheitRepository,
            MietvertragRepository mietvertragRepository
    ) {
        this.immobilieRepository = immobilieRepository;
        this.mieteinheitRepository = mieteinheitRepository;
        this.mietvertragRepository = mietvertragRepository;
    }

    @Override
    @Transactional
    public Immobilie speichereImmobilie(Immobilie immobilie) {

        // Die wichtigsten Pflichtfelder werden zusätzlich im Service geprüft,
        // damit die Geschäftsregeln nicht nur von der Benutzeroberfläche abhängen.
        if (immobilie == null) {
            throw new IllegalArgumentException("Immobilie darf nicht leer sein.");
        }

        Adresse adresse = immobilie.getAdresse();

        if (immobilie.getBezeichnung() == null || immobilie.getBezeichnung().isBlank()) {
            throw new IllegalArgumentException("Bezeichnung darf nicht leer sein.");
        }

        if (immobilie.getTyp() == null) {
            throw new IllegalArgumentException("Immobilientyp muss ausgewählt werden.");
        }

        if (adresse == null) {
            throw new IllegalArgumentException("Adresse muss angegeben werden.");
        }

        if (adresse.getStrasse() == null || adresse.getStrasse().isBlank()) {
            throw new IllegalArgumentException("Straße darf nicht leer sein.");
        }

        if (adresse.getHausnummer() == null || adresse.getHausnummer().isBlank()) {
            throw new IllegalArgumentException("Hausnummer darf nicht leer sein.");
        }

        if (adresse.getPlz() == null || adresse.getPlz().isBlank()) {
            throw new IllegalArgumentException("PLZ darf nicht leer sein.");
        }

        if (adresse.getStadt() == null || adresse.getStadt().isBlank()) {
            throw new IllegalArgumentException("Stadt darf nicht leer sein.");
        }

        // Optionale Zahlenfelder dürfen leer bleiben, aber keine negativen Werte enthalten.
        if (immobilie.getBaujahr() != null && immobilie.getBaujahr() < 0) {
            throw new IllegalArgumentException("Baujahr darf nicht negativ sein.");
        }

        if (immobilie.getFlaeche() != null && immobilie.getFlaeche() < 0) {
            throw new IllegalArgumentException("Fläche darf nicht negativ sein.");
        }

        return immobilieRepository.save(immobilie);
    }

    @Override
    public List<Immobilie> findeAlleImmobilien() {
        return immobilieRepository.findAll();
    }

    @Override
    public Optional<Immobilie> findeImmobilieNachId(Long id) {
        return immobilieRepository.findById(id);
    }

    @Override
    @Transactional
    public void loescheImmobilie(Long id) {
        // Eine Immobilie darf nicht gelöscht werden, wenn mindestens eine ihrer
        // Mieteinheiten noch durch einen aktiven Mietvertrag belegt ist.
        boolean hatAktiveMietvertraege = mietvertragRepository.existsByMieteinheit_Immobilie_IdAndStatus(id, Vertragsstatus.AKTIV);

        if (hatAktiveMietvertraege) {
            throw new IllegalStateException(
                    "Diese Immobilie kann nicht gelöscht werden, da mindestens eine Mieteinheit noch einen aktiven Mietvertrag hat."
            );
        }

        immobilieRepository.deleteById(id);
    }

    @Override
    public List<Immobilie> findeGefilterteImmobilien(
            String ortOderPlz,
            Immobilientyp typ,
            String einheitenFilter,
            String leerstandFilter
    ) {
        List<Immobilie> alleImmobilien = immobilieRepository.findAll();
        List<Immobilie> gefilterteImmobilien = new ArrayList<>();

        // Die Filter werden nacheinander geprüft. Sobald eine Immobilie ein Kriterium
        // nicht erfüllt, wird sie übersprungen.
        for (Immobilie immobilie : alleImmobilien) {

            if (!passtOrtOderPlzFilter(immobilie, ortOderPlz)) {
                continue;
            }

            if (!passtTypFilter(immobilie, typ)) {
                continue;
            }

            List<Mieteinheit> mieteinheiten = mieteinheitRepository.findByImmobilieId(immobilie.getId());

            if (!passtEinheitenFilter(mieteinheiten.size(), einheitenFilter)) {
                continue;
            }

            if (!passtLeerstandFilter(mieteinheiten, leerstandFilter)) {
                continue;
            }

            gefilterteImmobilien.add(immobilie);
        }

        return gefilterteImmobilien;
    }

    private boolean passtOrtOderPlzFilter(Immobilie immobilie, String ortOderPlz) {
        if (ortOderPlz == null || ortOderPlz.isBlank()) {
            return true;
        }

        if (immobilie.getAdresse() == null) {
            return false;
        }

        String suchtext = ortOderPlz.trim().toLowerCase();

        String stadt = immobilie.getAdresse().getStadt();
        String plz = immobilie.getAdresse().getPlz();

        boolean stadtPasst = stadt != null && stadt.toLowerCase().contains(suchtext);
        boolean plzPasst = plz != null && plz.contains(suchtext);

        return stadtPasst || plzPasst;
    }

    private boolean passtTypFilter(Immobilie immobilie, Immobilientyp typ) {
        // null steht hier für "Alle Typen".
        if (typ == null) {
            return true;
        }

        return immobilie.getTyp() == typ;
    }

    private boolean passtEinheitenFilter(int anzahlEinheiten, String einheitenFilter) {
        if (einheitenFilter == null || einheitenFilter.equals("Alle Größen")) {
            return true;
        }

        if (einheitenFilter.equals("1-5 Einheiten")) {
            return anzahlEinheiten >= 1 && anzahlEinheiten <= 5;
        }

        if (einheitenFilter.equals("6-20 Einheiten")) {
            return anzahlEinheiten >= 6 && anzahlEinheiten <= 20;
        }

        if (einheitenFilter.equals("20+ Einheiten")) {
            return anzahlEinheiten > 20;
        }

        return true;
    }

    private boolean passtLeerstandFilter(List<Mieteinheit> mieteinheiten, String leerstandFilter) {
        if (leerstandFilter == null || leerstandFilter.equals("Alle anzeigen")) {
            return true;
        }

        int leerstand = 0;

        // Für die Übersicht zählen sowohl freie Einheiten als auch Einheiten in Renovierung
        // als Leerstand, weil beide aktuell nicht regulär vermietet sind.
        for (Mieteinheit mieteinheit : mieteinheiten) {
            if (mieteinheit.getStatus() == Mieteinheitstatus.FREI || mieteinheit.getStatus() == Mieteinheitstatus.IN_RENOVIERUNG) {
                leerstand++;
            }
        }

        if (leerstandFilter.equals("Mit Leerstand")) {
            return leerstand > 0;
        }

        if (leerstandFilter.equals("Ohne Leerstand")) {
            return leerstand == 0;
        }

        return true;
    }
}
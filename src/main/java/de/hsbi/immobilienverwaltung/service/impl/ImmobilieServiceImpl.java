package de.hsbi.immobilienverwaltung.service.impl;

import de.hsbi.immobilienverwaltung.domain.Adresse;
import de.hsbi.immobilienverwaltung.domain.Immobilie;
import de.hsbi.immobilienverwaltung.repository.ImmobilieRepository;
import de.hsbi.immobilienverwaltung.service.interfaces.ImmobilieService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ImmobilieServiceImpl implements ImmobilieService {

    private final ImmobilieRepository immobilieRepository;

    public ImmobilieServiceImpl(ImmobilieRepository immobilieRepository) {
        this.immobilieRepository = immobilieRepository;
    }

    @Override
    @Transactional
    public Immobilie speichereImmobilie(Immobilie immobilie) {

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
        immobilieRepository.deleteById(id);
    }
}
package de.hsbi.immobilienverwaltung.service.impl;

import de.hsbi.immobilienverwaltung.domain.Mieter;
import de.hsbi.immobilienverwaltung.repository.MieterRepository;
import de.hsbi.immobilienverwaltung.service.interfaces.MieterService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import de.hsbi.immobilienverwaltung.domain.Mietvertrag;
import de.hsbi.immobilienverwaltung.domain.enums.Vertragsstatus;
import de.hsbi.immobilienverwaltung.repository.MietvertragRepository;

import java.time.LocalDate;

import java.util.List;
import java.util.Optional;

@Service
public class MieterServiceImpl implements MieterService {

    private final MieterRepository mieterRepository;
    private final MietvertragRepository mietvertragRepository;

    public MieterServiceImpl(
            MieterRepository mieterRepository,
            MietvertragRepository mietvertragRepository
    ) {
        this.mieterRepository = mieterRepository;
        this.mietvertragRepository = mietvertragRepository;
    }

    @Override
    @Transactional
    public Mieter speichereMieter(Mieter mieter) {

        if (mieter.getVorname() == null || mieter.getVorname().isBlank()) {
            throw new IllegalArgumentException("Vorname darf nicht leer sein.");
        }

        if (mieter.getNachname() == null || mieter.getNachname().isBlank()) {
            throw new IllegalArgumentException("Nachname darf nicht leer sein.");
        }

        if (mieter.getEmail() == null || mieter.getEmail().isBlank()) {
            throw new IllegalArgumentException("E-Mail darf nicht leer sein.");
        }

        if (!mieter.getEmail().contains("@")) {
            throw new IllegalArgumentException("E-Mail ist ungültig.");
        }

        if (mieter.getTelefonnummer() != null
                && !mieter.getTelefonnummer().isBlank()
                && !mieter.getTelefonnummer().matches("\\+?[0-9]*")) {
            throw new IllegalArgumentException("Telefonnummer darf nur Zahlen und optional ein + am Anfang enthalten.");
        }

        Optional<Mieter> vorhandenerMieter = mieterRepository.findByEmailIgnoreCase(mieter.getEmail());

        if (vorhandenerMieter.isPresent()) {
            Mieter andererMieter = vorhandenerMieter.get();

            if (mieter.getId() == null || !mieter.getId().equals(andererMieter.getId())) {
                throw new IllegalArgumentException("Diese E-Mail wird bereits verwendet.");
            }
        }

        if (mieter.isBankdatenAktiv()) {
            if (mieter.getKontoinhaber() == null || mieter.getKontoinhaber().isBlank()) {
                throw new IllegalArgumentException("Kontoinhaber darf nicht leer sein.");
            }

            if (mieter.getIban() == null || mieter.getIban().isBlank()) {
                throw new IllegalArgumentException("IBAN darf nicht leer sein.");
            }
        }

        return mieterRepository.save(mieter);
    }

    @Override
    public List<Mieter> findeAlleMieter() {
        return mieterRepository.findByArchiviertFalse();
    }

    @Override
    public List<Mieter> findeArchivierteMieter() {
        return mieterRepository.findByArchiviertTrue();
    }

    @Override
    public List<Mieter> sucheMieter(String suchbegriff) {
        if (suchbegriff == null || suchbegriff.isBlank()) {
            return findeAlleMieter();
        }

        return mieterRepository.findByVornameContainingIgnoreCaseOrNachnameContainingIgnoreCase(
                        suchbegriff,
                        suchbegriff
                )
                .stream()
                .filter(mieter -> !mieter.isArchiviert())
                .toList();
    }

    @Override
    public Optional<Mieter> findeMieterNachId(Long id) {
        return mieterRepository.findById(id);
    }

    @Override
    @Transactional
    public void archiviereMieter(Long id) {
        Mieter mieter = mieterRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Mieter wurde nicht gefunden."));

        if (hatLaufendenMietvertrag(id)) {
            throw new IllegalStateException(
                    "Mieter kann nicht archiviert werden, da noch ein aktiver oder auslaufender Mietvertrag besteht."
            );
        }

        mieter.setArchiviert(true);
        mieterRepository.save(mieter);
    }

    private boolean hatLaufendenMietvertrag(Long mieterId) {
        return mietvertragRepository.findByMieterId(mieterId)
                .stream()
                .anyMatch(this::istLaufenderMietvertrag);
    }

    private boolean istLaufenderMietvertrag(Mietvertrag mietvertrag) {
        if (mietvertrag == null || mietvertrag.getStatus() == null) {
            return false;
        }

        if (mietvertrag.getStatus() == Vertragsstatus.AKTIV) {
            return true;
        }

        return mietvertrag.getStatus() == Vertragsstatus.GEKUENDIGT
                && (
                mietvertrag.getEnddatum() == null
                        || !mietvertrag.getEnddatum().isBefore(LocalDate.now())
        );
    }

    @Override
    @Transactional
    public void loescheMieter(Long id) {
        mieterRepository.deleteById(id);
    }
}
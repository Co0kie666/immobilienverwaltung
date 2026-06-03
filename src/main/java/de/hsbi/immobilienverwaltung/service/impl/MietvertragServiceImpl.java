package de.hsbi.immobilienverwaltung.service.impl;

import de.hsbi.immobilienverwaltung.domain.Mieter;
import de.hsbi.immobilienverwaltung.domain.Mieteinheit;
import de.hsbi.immobilienverwaltung.domain.Mietvertrag;
import de.hsbi.immobilienverwaltung.domain.enums.Mieteinheitstatus;
import de.hsbi.immobilienverwaltung.domain.enums.Vertragsstatus;
import de.hsbi.immobilienverwaltung.repository.MieterRepository;
import de.hsbi.immobilienverwaltung.repository.MieteinheitRepository;
import de.hsbi.immobilienverwaltung.repository.MietvertragRepository;
import de.hsbi.immobilienverwaltung.service.interfaces.MietvertragService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class MietvertragServiceImpl implements MietvertragService {

    private final MietvertragRepository mietvertragRepository;
    private final MieterRepository mieterRepository;
    private final MieteinheitRepository mieteinheitRepository;

    public MietvertragServiceImpl(
            MietvertragRepository mietvertragRepository,
            MieterRepository mieterRepository,
            MieteinheitRepository mieteinheitRepository
    ) {
        this.mietvertragRepository = mietvertragRepository;
        this.mieterRepository = mieterRepository;
        this.mieteinheitRepository = mieteinheitRepository;
    }

    @Override
    @Transactional
    public Mietvertrag speichereMietvertrag(Long mieterId, Long mieteinheitId, Mietvertrag mietvertrag) {

        Mieter mieter = mieterRepository.findById(mieterId)
                .orElseThrow(() -> new IllegalArgumentException("Mieter wurde nicht gefunden."));

        Mieteinheit mieteinheit = mieteinheitRepository.findById(mieteinheitId)
                .orElseThrow(() -> new IllegalArgumentException("Mieteinheit wurde nicht gefunden."));

        if (mietvertrag.getStartdatum() == null) {
            throw new IllegalArgumentException("Startdatum muss angegeben werden.");
        }

        if (mietvertrag.getEnddatum() != null
                && mietvertrag.getEnddatum().isBefore(mietvertrag.getStartdatum())) {
            throw new IllegalArgumentException("Enddatum darf nicht vor dem Startdatum liegen.");
        }

        if (mietvertrag.getKaltmiete() == null) {
            throw new IllegalArgumentException("Kaltmiete muss angegeben werden.");
        }

        if (mietvertrag.getKaltmiete() < 0) {
            throw new IllegalArgumentException("Kaltmiete darf nicht negativ sein.");
        }

        if (mietvertrag.getNebenkosten() == null) {
            throw new IllegalArgumentException("Nebenkosten müssen angegeben werden.");
        }

        if (mietvertrag.getNebenkosten() < 0) {
            throw new IllegalArgumentException("Nebenkosten dürfen nicht negativ sein.");
        }

        if (mietvertrag.getKaution() != null && mietvertrag.getKaution() < 0) {
            throw new IllegalArgumentException("Kaution darf nicht negativ sein.");
        }

        if (mietvertrag.getStatus() == null) {
            mietvertrag.setStatus(Vertragsstatus.AKTIV);
        }

        pruefeAktivenVertragFuerMieteinheit(mieteinheitId, mietvertrag);

        mietvertrag.setMieter(mieter);
        mietvertrag.setMieteinheit(mieteinheit);

        if (mietvertrag.getStatus() == Vertragsstatus.AKTIV) {
            mieteinheit.setStatus(Mieteinheitstatus.VERMIETET);
        }

        return mietvertragRepository.save(mietvertrag);
    }

    private void pruefeAktivenVertragFuerMieteinheit(Long mieteinheitId, Mietvertrag mietvertrag) {
        List<Mietvertrag> aktiveVertraege =
                mietvertragRepository.findByMieteinheitIdAndStatus(mieteinheitId, Vertragsstatus.AKTIV);

        for (Mietvertrag aktiverVertrag : aktiveVertraege) {

            if (mietvertrag.getId() != null && mietvertrag.getId().equals(aktiverVertrag.getId())) {
                continue;
            }

            throw new IllegalArgumentException("Für diese Mieteinheit existiert bereits ein aktiver Mietvertrag.");
        }
    }

    @Override
    public List<Mietvertrag> findeAlleMietvertraege() {
        return mietvertragRepository.findAll();
    }

    @Override
    public List<Mietvertrag> findeMietvertraegeNachMieter(Long mieterId) {
        return mietvertragRepository.findByMieterId(mieterId);
    }

    @Override
    public List<Mietvertrag> findeMietvertraegeNachMieteinheit(Long mieteinheitId) {
        return mietvertragRepository.findByMieteinheitId(mieteinheitId);
    }

    @Override
    public Optional<Mietvertrag> findeMietvertragNachId(Long id) {
        return mietvertragRepository.findById(id);
    }

    @Override
    @Transactional
    public void kuendigeMietvertrag(Long id) {
        Mietvertrag mietvertrag = mietvertragRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Mietvertrag wurde nicht gefunden."));

        if (mietvertrag.getStatus() == Vertragsstatus.BEENDET) {
            throw new IllegalArgumentException("Beendete Mietverträge können nicht gekündigt werden.");
        }

        mietvertrag.setStatus(Vertragsstatus.GEKUENDIGT);

        if (mietvertrag.getEnddatum() == null) {
            mietvertrag.setEnddatum(LocalDate.now());
        }

        if (mietvertrag.getMieteinheit() != null) {
            mietvertrag.getMieteinheit().setStatus(Mieteinheitstatus.FREI);
        }

        mietvertragRepository.save(mietvertrag);
    }

    @Override
    @Transactional
    public void loescheMietvertrag(Long id) {
        mietvertragRepository.deleteById(id);
    }
}
package de.hsbi.immobilienverwaltung.service.impl;

import de.hsbi.immobilienverwaltung.domain.Immobilie;
import de.hsbi.immobilienverwaltung.domain.Mieteinheit;
import de.hsbi.immobilienverwaltung.domain.enums.Mieteinheitstatus;
import de.hsbi.immobilienverwaltung.repository.ImmobilieRepository;
import de.hsbi.immobilienverwaltung.repository.MieteinheitRepository;
import de.hsbi.immobilienverwaltung.service.interfaces.MieteinheitService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MieteinheitServiceImpl implements MieteinheitService {

    private final MieteinheitRepository mieteinheitRepository;
    private final ImmobilieRepository immobilieRepository;

    public MieteinheitServiceImpl(MieteinheitRepository mieteinheitRepository, ImmobilieRepository immobilieRepository) {
        this.mieteinheitRepository = mieteinheitRepository;
        this.immobilieRepository = immobilieRepository;
    }

    @Override
    @Transactional
    public Mieteinheit speichereMieteinheit(Long immobilieId, Mieteinheit mieteinheit) {
        Immobilie immobilie = immobilieRepository.findById(immobilieId)
                .orElseThrow(() -> new IllegalArgumentException("Immobilie wurde nicht gefunden."));

        if (mieteinheit.getBezeichnung() == null || mieteinheit.getBezeichnung().isBlank()) {
            throw new IllegalArgumentException("Bezeichnung darf nicht leer sein.");
        }

        if (mieteinheit.getTyp() == null) {
            throw new IllegalArgumentException("Mieteinheittyp muss ausgewählt werden.");
        }

        if (mieteinheit.getGroesse() != null && mieteinheit.getGroesse() < 0) {
            throw new IllegalArgumentException("Größe darf nicht negativ sein.");
        }

        if (mieteinheit.getZimmerzahl() != null && mieteinheit.getZimmerzahl() < 0) {
            throw new IllegalArgumentException("Zimmerzahl darf nicht negativ sein.");
        }

        if (mieteinheit.getStatus() == null) {
            mieteinheit.setStatus(Mieteinheitstatus.FREI);
        }

        mieteinheit.setImmobilie(immobilie);

        return mieteinheitRepository.save(mieteinheit);
    }

    @Override
    public List<Mieteinheit> findeMieteinheitenNachImmobilie(Long immobilieId) {
        return mieteinheitRepository.findByImmobilieId(immobilieId);
    }

    @Override
    public Optional<Mieteinheit> findeMieteinheitNachId(Long id) {
        return mieteinheitRepository.findById(id);
    }

    @Override
    @Transactional
    public void loescheMieteinheit(Long id) {
        mieteinheitRepository.deleteById(id);
    }
}
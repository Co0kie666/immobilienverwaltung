package de.hsbi.immobilienverwaltung.service.impl;

import de.hsbi.immobilienverwaltung.domain.Immobilie;
import de.hsbi.immobilienverwaltung.domain.Mieteinheit;
import de.hsbi.immobilienverwaltung.domain.enums.MieteinheitTyp;
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

        pruefeGesamtobjektRegel(immobilieId, mieteinheit);
        mieteinheit.setImmobilie(immobilie);

        return mieteinheitRepository.save(mieteinheit);
    }

    // Prüfen, ob für diese Immobilie bereits Mieteinheiten existieren.
    private void pruefeGesamtobjektRegel(Long immobilieId, Mieteinheit mieteinheit) {
        List<Mieteinheit> vorhandeneEinheiten = mieteinheitRepository.findByImmobilieId(immobilieId);

        for (Mieteinheit e : vorhandeneEinheiten) {

            // Beim Bearbeiten soll die aktuelle Mieteinheit nicht gegen sich selbst geprüft werden.
            if (mieteinheit.getId() != null && mieteinheit.getId().equals(e.getId())) {
                continue;
            }

            // Wenn bereits ein Gesamtobjekt existiert, dürfen keine weiteren Mieteinheiten angelegt werden.
            if (e.getTyp() == MieteinheitTyp.GESAMTOBJEKT) {
                throw new IllegalArgumentException("Diese Immobilie ist bereits als Gesamtobjekt angelegt.");
            }

            // Ein Gesamtobjekt darf nur angelegt werden, wenn noch keine anderen Mieteinheiten existieren.
            if (mieteinheit.getTyp() == MieteinheitTyp.GESAMTOBJEKT) {
                throw new IllegalArgumentException("Ein Gesamtobjekt kann nur angelegt werden, wenn keine anderen Mieteinheiten existieren.");
            }
        }
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

    @Override
    public long zaehleMieteinheiten(Long immobilieId) {
        return mieteinheitRepository.countByImmobilieId(immobilieId);
    }

    @Override
    public long zaehleFreieMieteinheiten(Long immobilieId) {
        long freieEinheiten = mieteinheitRepository.countByImmobilieIdAndStatus(immobilieId, Mieteinheitstatus.FREI);
        long einheitenInRenovierung = mieteinheitRepository.countByImmobilieIdAndStatus(
                immobilieId, Mieteinheitstatus.IN_RENOVIERUNG);
        return freieEinheiten + einheitenInRenovierung;
    }

    @Override
    public long zaehleVermieteteMieteinheiten(Long immobilieId) {
        return mieteinheitRepository.countByImmobilieIdAndStatus(immobilieId, Mieteinheitstatus.VERMIETET);
    }

    @Override
    public double berechneLeerstandsquote(Long immobilieId) {
        long gesamt = zaehleMieteinheiten(immobilieId);

        if (gesamt == 0) {
            return 0;
        }

        long leerstand = zaehleFreieMieteinheiten(immobilieId);

        return leerstand * 100.0 / gesamt;
    }

    @Override
    public long zaehleMieteinheitenInRenovierung(Long immobilieId) {
        return mieteinheitRepository.countByImmobilieIdAndStatus(immobilieId, Mieteinheitstatus.IN_RENOVIERUNG);
    }
}
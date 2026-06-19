package de.hsbi.immobilienverwaltung.service.impl;

import de.hsbi.immobilienverwaltung.domain.Mieteinheit;
import de.hsbi.immobilienverwaltung.domain.Mieter;
import de.hsbi.immobilienverwaltung.domain.Mietvertrag;
import de.hsbi.immobilienverwaltung.domain.enums.Mieteinheitstatus;
import de.hsbi.immobilienverwaltung.domain.enums.Vertragsstatus;
import de.hsbi.immobilienverwaltung.repository.MieteinheitRepository;
import de.hsbi.immobilienverwaltung.repository.MieterRepository;
import de.hsbi.immobilienverwaltung.repository.MietvertragRepository;
import de.hsbi.immobilienverwaltung.service.interfaces.MietvertragService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.temporal.ChronoUnit;

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

        if (mieteinheit.getStatus() == Mieteinheitstatus.IN_RENOVIERUNG) {
            throw new IllegalStateException(
                    "Für diese Mieteinheit kann kein Mietvertrag erstellt werden, da sie sich aktuell in Renovierung befindet."
            );
        }

        validiereMietvertrag(mietvertrag);

        if (mietvertrag.getStatus() == null) {
            mietvertrag.setStatus(Vertragsstatus.AKTIV);
        }

        aktualisiereStatusNachDatum(mietvertrag);

        mietvertrag.setMieter(mieter);
        mietvertrag.setMieteinheit(mieteinheit);

        pruefeLaufendenVertragFuerMieteinheit(mieteinheitId, mietvertrag);
        aktualisiereMieteinheitStatus(mietvertrag);

        return mietvertragRepository.save(mietvertrag);
    }

    @Override
    @Transactional
    public List<Mietvertrag> findeAlleMietvertraege() {
        List<Mietvertrag> vertraege = mietvertragRepository.findAll();
        vertraege.forEach(this::aktualisierePersistiertenVertragNachDatum);
        return vertraege;
    }

    @Override
    @Transactional
    public List<Mietvertrag> findeMietvertraegeNachMieter(Long mieterId) {
        List<Mietvertrag> vertraege = mietvertragRepository.findByMieterId(mieterId);
        vertraege.forEach(this::aktualisierePersistiertenVertragNachDatum);
        return vertraege;
    }

    @Override
    @Transactional
    public List<Mietvertrag> findeMietvertraegeNachMieteinheit(Long mieteinheitId) {
        List<Mietvertrag> vertraege = mietvertragRepository.findByMieteinheitId(mieteinheitId);
        vertraege.forEach(this::aktualisierePersistiertenVertragNachDatum);
        return vertraege;
    }

    @Override
    @Transactional
    public Optional<Mietvertrag> findeMietvertragNachId(Long id) {
        return mietvertragRepository.findById(id)
                .map(this::aktualisierePersistiertenVertragNachDatum);
    }

    @Override
    @Transactional
    public void kuendigeMietvertrag(Long id) {
        Mietvertrag mietvertrag = mietvertragRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Mietvertrag wurde nicht gefunden."));

        if (mietvertrag.getStatus() == Vertragsstatus.BEENDET) {
            throw new IllegalArgumentException("Beendete Mietverträge können nicht gekündigt werden.");
        }

        LocalDate fristEnde = LocalDate.now().plusMonths(ermittleKuendigungsfristInMonaten(mietvertrag));

        mietvertrag.setStatus(Vertragsstatus.GEKUENDIGT);

        if (mietvertrag.getEnddatum() == null || mietvertrag.getEnddatum().isAfter(fristEnde)) {
            mietvertrag.setEnddatum(fristEnde);
        }

        aktualisiereStatusNachDatum(mietvertrag);
        aktualisiereMieteinheitStatus(mietvertrag);

        mietvertragRepository.save(mietvertrag);
    }

    private int ermittleKuendigungsfristInMonaten(Mietvertrag mietvertrag) {
        if (mietvertrag.getStartdatum() == null || mietvertrag.getKuendigungsfrist() == null) {
            return 3;
        }

        for (int monate : new int[]{1, 3, 6}) {
            if (mietvertrag.getStartdatum().plusMonths(monate).equals(mietvertrag.getKuendigungsfrist())) {
                return monate;
            }
        }

        long monate = ChronoUnit.MONTHS.between(
                mietvertrag.getStartdatum(),
                mietvertrag.getKuendigungsfrist()
        );

        if (monate <= 0) {
            return 3;
        }

        return (int) monate;
    }

    @Override
    @Transactional
    public void loescheMietvertrag(Long id) {
        mietvertragRepository.deleteById(id);
    }

    private void validiereMietvertrag(Mietvertrag mietvertrag) {
        if (mietvertrag.getStartdatum() == null) {
            throw new IllegalArgumentException("Startdatum muss angegeben werden.");
        }

        if (mietvertrag.getEnddatum() != null && mietvertrag.getEnddatum().isBefore(mietvertrag.getStartdatum())) {
            throw new IllegalArgumentException("Enddatum darf nicht vor dem Startdatum liegen.");
        }

        if (mietvertrag.getEnddatum() != null && mietvertrag.getEnddatum().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Enddatum darf nicht vor dem heutigen Datum liegen.");
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
    }

    private Mietvertrag aktualisierePersistiertenVertragNachDatum(Mietvertrag mietvertrag) {
        Vertragsstatus alterStatus = mietvertrag.getStatus();

        aktualisiereStatusNachDatum(mietvertrag);

        if (alterStatus != mietvertrag.getStatus()) {
            aktualisiereMieteinheitStatus(mietvertrag);
            return mietvertragRepository.save(mietvertrag);
        }

        return mietvertrag;
    }

    private void aktualisiereStatusNachDatum(Mietvertrag mietvertrag) {
        if (mietvertrag.getStatus() == Vertragsstatus.GEKUENDIGT
                && mietvertrag.getEnddatum() != null
                && mietvertrag.getEnddatum().isBefore(LocalDate.now())) {
            mietvertrag.setStatus(Vertragsstatus.BEENDET);
        }
    }

    private void pruefeLaufendenVertragFuerMieteinheit(Long mieteinheitId, Mietvertrag aktuellerVertrag) {
        if (!istLaufenderVertrag(aktuellerVertrag)) {
            return;
        }

        List<Mietvertrag> vorhandeneVertraege = mietvertragRepository.findByMieteinheitId(mieteinheitId);

        for (Mietvertrag vorhandenerVertrag : vorhandeneVertraege) {
            aktualisiereStatusNachDatum(vorhandenerVertrag);

            if (aktuellerVertrag.getId() != null
                    && aktuellerVertrag.getId().equals(vorhandenerVertrag.getId())) {
                continue;
            }

            if (istLaufenderVertrag(vorhandenerVertrag)) {
                throw new IllegalArgumentException("Für diese Mieteinheit existiert bereits ein laufender Mietvertrag.");
            }
        }
    }

    private boolean istLaufenderVertrag(Mietvertrag mietvertrag) {
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

    private void aktualisiereMieteinheitStatus(Mietvertrag mietvertrag) {
        if (mietvertrag.getMieteinheit() == null) {
            return;
        }

        Mieteinheit mieteinheit = mietvertrag.getMieteinheit();

        if (istLaufenderVertrag(mietvertrag)) {
            mieteinheit.setStatus(Mieteinheitstatus.VERMIETET);
            mieteinheitRepository.save(mieteinheit);
            return;
        }

        if (!hatAnderenLaufendenVertragFuerMieteinheit(mietvertrag)) {
            mieteinheit.setStatus(Mieteinheitstatus.FREI);
            mieteinheitRepository.save(mieteinheit);
        }
    }

    private boolean hatAnderenLaufendenVertragFuerMieteinheit(Mietvertrag aktuellerVertrag) {
        if (aktuellerVertrag.getMieteinheit() == null || aktuellerVertrag.getMieteinheit().getId() == null) {
            return false;
        }

        return mietvertragRepository.findByMieteinheitId(aktuellerVertrag.getMieteinheit().getId())
                .stream()
                .filter(vertrag -> aktuellerVertrag.getId() == null
                        || !aktuellerVertrag.getId().equals(vertrag.getId())
                )
                .anyMatch(this::istLaufenderVertrag);
    }
}
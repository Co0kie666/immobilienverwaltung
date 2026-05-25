package de.hsbi.immobilienverwaltung.service.interfaces;

import de.hsbi.immobilienverwaltung.domain.Mieteinheit;

import java.util.List;
import java.util.Optional;

public interface MieteinheitService {

    Mieteinheit speichereMieteinheit(Long immobilieId, Mieteinheit mieteinheit);

    List<Mieteinheit> findeMieteinheitenNachImmobilie(Long immobilieId);

    // fuer MieteinheitDetailView
    Optional<Mieteinheit> findeMieteinheitNachId(Long id);

    void loescheMieteinheit(Long id);
}
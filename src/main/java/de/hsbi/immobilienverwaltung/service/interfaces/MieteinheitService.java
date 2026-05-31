package de.hsbi.immobilienverwaltung.service.interfaces;

import de.hsbi.immobilienverwaltung.domain.Mieteinheit;

import java.util.List;
import java.util.Optional;

public interface MieteinheitService {

    Mieteinheit speichereMieteinheit(Long immobilieId, Mieteinheit mieteinheit);

    List<Mieteinheit> findeMieteinheitenNachImmobilie(Long immobilieId);

    Optional<Mieteinheit> findeMieteinheitNachId(Long id);

    void loescheMieteinheit(Long id);

    long zaehleMieteinheiten(Long immobilieId);

    long zaehleFreieMieteinheiten(Long immobilieId);

    long zaehleVermieteteMieteinheiten(Long immobilieId);

    double berechneLeerstandsquote(Long immobilieId);

    long zaehleMieteinheitenInRenovierung(Long immobilieId);

}
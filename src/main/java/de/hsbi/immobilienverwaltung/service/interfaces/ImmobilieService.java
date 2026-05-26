package de.hsbi.immobilienverwaltung.service.interfaces;

import de.hsbi.immobilienverwaltung.domain.Immobilie;

import java.util.List;
import java.util.Optional;

public interface ImmobilieService {

    Immobilie speichereImmobilie(Immobilie immobilie);

    List<Immobilie> findeAlleImmobilien();

    // fuer ImmobilieDetailView
    Optional<Immobilie> findeImmobilieNachId(Long id);

    void loescheImmobilie(Long id);
}
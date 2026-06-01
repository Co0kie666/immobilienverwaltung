package de.hsbi.immobilienverwaltung.service.interfaces;

import de.hsbi.immobilienverwaltung.domain.Immobilie;
import de.hsbi.immobilienverwaltung.domain.enums.Immobilientyp;

import java.util.List;
import java.util.Optional;

public interface ImmobilieService {

    Immobilie speichereImmobilie(Immobilie immobilie);

    List<Immobilie> findeAlleImmobilien();

    // fuer ImmobilieDetailView
    Optional<Immobilie> findeImmobilieNachId(Long id);

    void loescheImmobilie(Long id);

    List<Immobilie> findeGefilterteImmobilien(String ortOderPlz, Immobilientyp typ, String einheitenFilter, String leerstandFilter);
}
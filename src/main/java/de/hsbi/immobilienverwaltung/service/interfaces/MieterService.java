package de.hsbi.immobilienverwaltung.service.interfaces;

import de.hsbi.immobilienverwaltung.domain.Mieter;

import java.util.List;
import java.util.Optional;

public interface MieterService {

    Mieter speichereMieter(Mieter mieter);

    List<Mieter> findeAlleMieter();

    List<Mieter> findeArchivierteMieter();

    List<Mieter> sucheMieter(String suchbegriff);

    Optional<Mieter> findeMieterNachId(Long id);

    void archiviereMieter(Long id);

    void loescheMieter(Long id);
}
package de.hsbi.immobilienverwaltung.service.interfaces;

import de.hsbi.immobilienverwaltung.domain.Mietvertrag;

import java.util.List;
import java.util.Optional;

public interface MietvertragService {

    Mietvertrag speichereMietvertrag(Long mieterId, Long mieteinheitId, Mietvertrag mietvertrag);

    List<Mietvertrag> findeAlleMietvertraege();

    List<Mietvertrag> findeMietvertraegeNachMieter(Long mieterId);

    List<Mietvertrag> findeMietvertraegeNachMieteinheit(Long mieteinheitId);

    Optional<Mietvertrag> findeMietvertragNachId(Long id);

    void kuendigeMietvertrag(Long id);

    boolean istLaufenderVertrag(Mietvertrag mietvertrag);

    boolean istHistorischerVertrag(Mietvertrag mietvertrag);

    List<Mietvertrag> findeHistorischeMietvertraegeNachMieteinheit(Long mieteinheitId);

    void loescheMietvertrag(Long id);
}
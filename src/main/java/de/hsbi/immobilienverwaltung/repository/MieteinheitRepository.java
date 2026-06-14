package de.hsbi.immobilienverwaltung.repository;

import de.hsbi.immobilienverwaltung.domain.Mieteinheit;
import de.hsbi.immobilienverwaltung.domain.enums.Mieteinheitstatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

/**
 * Repository für den Datenbankzugriff auf Mieteinheiten.
 * Spring Data JPA erzeugt die Abfragen anhand der Methodennamen automatisch.
 */
public interface MieteinheitRepository extends JpaRepository<Mieteinheit, Long> {

    /**
     * Findet alle Mieteinheiten, die zu einer bestimmten Immobilie gehören.
     */
    List<Mieteinheit> findByImmobilieId(Long immobilieId);

    /**
     * Zählt alle Mieteinheiten, deren Status in der übergebenen Status Liste enthalten ist.
     * Wird für globale Auswertungen verwendet.
     */
    long countByStatusIn(Collection<Mieteinheitstatus> status);

    /**
     * Zählt alle Mieteinheiten einer bestimmten Immobilie.
     */
    long countByImmobilieId(Long immobilieId);

    /**
     * Zählt alle Mieteinheiten einer Immobilie mit einem bestimmten Status.
     * Wird z. B. für freie, vermietete oder renovierte Einheiten genutzt.
     */
    long countByImmobilieIdAndStatus(Long immobilieId, Mieteinheitstatus status);
}
package de.hsbi.immobilienverwaltung.repository;

import de.hsbi.immobilienverwaltung.domain.Mietvertrag;
import de.hsbi.immobilienverwaltung.domain.enums.Vertragsstatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository für den Datenbankzugriff auf Mietverträge.
 * Spring Data JPA erzeugt die passenden Abfragen direkt aus den Methodennamen.
 */
public interface MietvertragRepository extends JpaRepository<Mietvertrag, Long> {

    /**
     * Lädt alle Mietverträge eines bestimmten Mieters.
     * Das wird zum Beispiel in der Mieter-Detailansicht gebraucht.
     */
    List<Mietvertrag> findByMieterId(Long mieterId);

    /**
     * Lädt alle Mietverträge einer bestimmten Mieteinheit.
     * Damit kann man aktuelle Verträge und die Vertragshistorie einer Einheit anzeigen.
     */
    List<Mietvertrag> findByMieteinheitId(Long mieteinheitId);

    /**
     * Lädt alle Mietverträge mit einem bestimmten Status.
     * Das ist praktisch für Übersichten oder Auswertungen nach Vertragsstatus.
     */
    List<Mietvertrag> findByStatus(Vertragsstatus status);

    /**
     * Lädt alle Mietverträge einer Mieteinheit mit einem bestimmten Status.
     * Wird genutzt, wenn gezielt aktive, gekündigte oder beendete Verträge gesucht werden.
     */
    List<Mietvertrag> findByMieteinheitIdAndStatus(Long mieteinheitId, Vertragsstatus status);

    /**
     * Zählt Mietverträge mit einem bestimmten Status.
     */
    long countByStatus(Vertragsstatus status);

    /**
     * Prüft, ob eine Mieteinheit bereits einen Mietvertrag mit einem bestimmten Status hat.
     * Wichtig ist das vor allem, um doppelte laufende Verträge zu verhindern.
     */
    boolean existsByMieteinheit_IdAndStatus(Long mieteinheitId, Vertragsstatus status);

    /**
     * Prüft, ob in einer Immobilie mindestens eine Mieteinheit
     * einen Mietvertrag mit einem bestimmten Status hat.
     */
    boolean existsByMieteinheit_Immobilie_IdAndStatus(Long immobilieId, Vertragsstatus status);

    /**
     * Prüft, ob zu irgendeiner Mieteinheit einer Immobilie schon ein Mietvertrag existiert.
     * Dadurch kann eine Immobilie nicht gelöscht werden, wenn Verträge daran hängen.
     */
    boolean existsByMieteinheit_Immobilie_Id(Long immobilieId);

    /**
     * Prüft, ob eine Mieteinheit überhaupt schon mit einem Mietvertrag verknüpft ist.
     * Das wird für die Löschsperre bei Mieteinheiten genutzt.
     */
    boolean existsByMieteinheit_Id(Long mieteinheitId);
}
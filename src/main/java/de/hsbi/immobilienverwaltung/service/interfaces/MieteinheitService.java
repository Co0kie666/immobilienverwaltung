package de.hsbi.immobilienverwaltung.service.interfaces;

import de.hsbi.immobilienverwaltung.domain.Mieteinheit;

import java.util.List;
import java.util.Optional;

/**
 * Service Schnittstelle für die fachliche Verwaltung von Mieteinheiten.
 * Mieteinheiten sind immer einer Immobilie zugeordnet.
 */
public interface MieteinheitService {

    /**
     * Speichert eine neue oder bestehende Mieteinheit für eine bestimmte Immobilie.
     * Dabei werden Validierungen und fachliche Regeln geprüft, z. B. zur Gesamtobjekt Logik.
     *
     * @param immobilieId ID der zugehörigen Immobilie
     * @param mieteinheit Mieteinheit, die gespeichert werden soll
     * @return gespeicherte Mieteinheit
     */
    Mieteinheit speichereMieteinheit(Long immobilieId, Mieteinheit mieteinheit);

    /**
     * Gibt alle Mieteinheiten einer bestimmten Immobilie zurück.
     *
     * @param immobilieId ID der Immobilie
     * @return Liste der zugehörigen Mieteinheiten
     */
    List<Mieteinheit> findeMieteinheitenNachImmobilie(Long immobilieId);

    /**
     * Gibt alle gespeicherten Mieteinheiten zurück.
     *
     * @return Liste aller Mieteinheiten
     */
    List<Mieteinheit> findeAlleMieteinheiten();

    /**
     * Sucht eine Mieteinheit anhand ihrer ID.
     *
     * @param id ID der Mieteinheit
     * @return Optional mit Mieteinheit, falls sie existiert
     */
    Optional<Mieteinheit> findeMieteinheitNachId(Long id);

    /**
     * Löscht eine Mieteinheit, sofern keine fachlichen Regeln dagegen sprechen.
     * Eine Mieteinheit darf z. B. nicht gelöscht werden, wenn noch ein aktiver Mietvertrag besteht.
     *
     * @param id ID der zu löschenden Mieteinheit
     */
    void loescheMieteinheit(Long id);

    /**
     * Zählt alle Mieteinheiten einer Immobilie.
     *
     * @param immobilieId ID der Immobilie
     * @return Anzahl der Mieteinheiten
     */
    long zaehleMieteinheiten(Long immobilieId);

    /**
     * Zählt alle freien Mieteinheiten einer Immobilie
     *
     * @param immobilieId ID der Immobilie
     * @return Anzahl freier Mieteinheiten
     */
    long zaehleFreieMieteinheiten(Long immobilieId);

    /**
     * Zählt alle vermieteten Mieteinheiten einer Immobilie
     *
     * @param immobilieId ID der Immobilie
     * @return Anzahl vermieteter Mieteinheiten
     */
    long zaehleVermieteteMieteinheiten(Long immobilieId);

    /**
     * Berechnet die Leerstandsquote einer Immobilie in Prozent
     * Als Leerstand gelten freie Mieteinheiten und Mieteinheiten in Renovierung
     *
     * @param immobilieId ID der Immobilie
     * @return Leerstandsquote in Prozent
     */
    double berechneLeerstandsquote(Long immobilieId);

    /**
     * Zählt alle Mieteinheiten einer Immobilie, die sich in Renovierung befinden
     *
     * @param immobilieId ID der Immobilie
     * @return Anzahl der Mieteinheiten in Renovierung
     */
    long zaehleMieteinheitenInRenovierung(Long immobilieId);

    /**
     * Sucht innerhalb der Mieteinheiten einer Immobilie nach einem Suchtext
     * Durchsucht werden z. B. Bezeichnung, Typ, Status, Stockwerk, Größe und Zimmeranzahl
     *
     * @param immobilieId ID der Immobilie
     * @param suchtext Suchtext aus der Detailansicht
     * @return passende Mieteinheiten der Immobilie
     */
    List<Mieteinheit> sucheMieteinheitenDerImmobilie(Long immobilieId, String suchtext);
}
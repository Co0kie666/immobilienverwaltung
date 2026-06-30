package de.hsbi.immobilienverwaltung.service.interfaces;

import de.hsbi.immobilienverwaltung.domain.Mietvertrag;

import java.util.List;
import java.util.Optional;

/**
 * Service Schnittstelle für die fachliche Verwaltung von Mietverträgen.
 * Hier liegen die Methoden für Anlegen, Bearbeiten, Kündigen und die Vertragshistorie.
 */
public interface MietvertragService {

    /**
     * Speichert einen neuen oder bestehenden Mietvertrag.
     * Dabei wird der Vertrag mit einem Mieter und einer Mieteinheit verbunden.
     *
     * @param mieterId ID des Mieters
     * @param mieteinheitId ID der Mieteinheit
     * @param mietvertrag Mietvertrag, der gespeichert werden soll
     * @return gespeicherter Mietvertrag
     */
    Mietvertrag speichereMietvertrag(Long mieterId, Long mieteinheitId, Mietvertrag mietvertrag);

    /**
     * Gibt alle Mietverträge zurück.
     * In der Implementierung werden dabei auch abgelaufene Verträge aktualisiert.
     *
     * @return Liste aller Mietverträge
     */
    List<Mietvertrag> findeAlleMietvertraege();

    /**
     * Gibt alle Mietverträge eines bestimmten Mieters zurück.
     *
     * @param mieterId ID des Mieters
     * @return Mietverträge des Mieters
     */
    List<Mietvertrag> findeMietvertraegeNachMieter(Long mieterId);

    /**
     * Gibt alle Mietverträge einer bestimmten Mieteinheit zurück.
     * Das wird zum Beispiel für die Historie einer Einheit gebraucht.
     *
     * @param mieteinheitId ID der Mieteinheit
     * @return Mietverträge der Mieteinheit
     */
    List<Mietvertrag> findeMietvertraegeNachMieteinheit(Long mieteinheitId);

    /**
     * Sucht einen einzelnen Mietvertrag über seine ID.
     *
     * @param id ID des Mietvertrags
     * @return Optional mit Mietvertrag, falls er existiert
     */
    Optional<Mietvertrag> findeMietvertragNachId(Long id);

    /**
     * Kündigt einen Mietvertrag.
     * Das genaue Enddatum wird in der Implementierung anhand der Kündigungsfrist gesetzt.
     *
     * @param id ID des Mietvertrags
     */
    void kuendigeMietvertrag(Long id);

    /**
     * Prüft, ob ein Vertrag aktuell noch läuft.
     * Auch ein gekündigter Vertrag kann noch laufen, solange das Enddatum nicht vorbei ist.
     *
     * @param mietvertrag Mietvertrag, der geprüft werden soll
     * @return true, wenn der Vertrag noch läuft
     */
    boolean istLaufenderVertrag(Mietvertrag mietvertrag);

    /**
     * Prüft, ob ein Vertrag zur Historie gehört.
     * Das ist der Fall, wenn er beendet oder bereits abgelaufen ist.
     *
     * @param mietvertrag Mietvertrag, der geprüft werden soll
     * @return true, wenn der Vertrag historisch ist
     */
    boolean istHistorischerVertrag(Mietvertrag mietvertrag);

    /**
     * Gibt die historischen Mietverträge einer Mieteinheit zurück.
     * Diese Liste wird für die Vertragshistorie in der Detailansicht genutzt.
     *
     * @param mieteinheitId ID der Mieteinheit
     * @return historische Mietverträge der Mieteinheit
     */
    List<Mietvertrag> findeHistorischeMietvertraegeNachMieteinheit(Long mieteinheitId);

    /**
     * Löscht einen Mietvertrag endgültig aus der Datenbank.
     *
     * @param id ID des Mietvertrags
     */
    void loescheMietvertrag(Long id);
}
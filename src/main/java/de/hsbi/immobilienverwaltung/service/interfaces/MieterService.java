package de.hsbi.immobilienverwaltung.service.interfaces;

import de.hsbi.immobilienverwaltung.domain.Mieter;

import java.util.List;
import java.util.Optional;

/**
 * Service Schnittstelle für die fachliche Verwaltung von Mietern.
 * Hier geht es um Anlegen, Suchen, Archivieren und Löschen von Mieterdaten.
 */
public interface MieterService {

    /**
     * Speichert einen neuen oder bestehenden Mieter.
     * Die genaue Prüfung der Pflichtfelder passiert in der Implementierung.
     *
     * @param mieter Mieter, der gespeichert werden soll
     * @return gespeicherter Mieter
     */
    Mieter speichereMieter(Mieter mieter);

    /**
     * Gibt alle Mieter zurück, die nicht archiviert sind.
     *
     * @return Liste aller aktiven Mieter
     */
    List<Mieter> findeAlleMieter();

    /**
     * Gibt alle archivierten Mieter zurück.
     * Diese Mieter werden in der normalen Übersicht nicht mehr angezeigt.
     *
     * @return Liste aller archivierten Mieter
     */
    List<Mieter> findeArchivierteMieter();

    /**
     * Sucht Mieter anhand eines Suchbegriffs.
     * Gesucht wird zum Beispiel nach Vorname oder Nachname.
     *
     * @param suchbegriff Text aus der Suchleiste
     * @return passende Mieter
     */
    List<Mieter> sucheMieter(String suchbegriff);

    /**
     * Sucht einen einzelnen Mieter über seine ID.
     *
     * @param id ID des Mieters
     * @return Optional mit Mieter, falls er existiert
     */
    Optional<Mieter> findeMieterNachId(Long id);

    /**
     * Archiviert einen Mieter.
     * In der Implementierung wird vorher geprüft, ob noch ein laufender Mietvertrag existiert.
     *
     * @param id ID des Mieters, der archiviert werden soll
     */
    void archiviereMieter(Long id);

    /**
     * Löscht einen Mieter endgültig aus der Datenbank.
     *
     * @param id ID des Mieters, der gelöscht werden soll
     */
    void loescheMieter(Long id);
}
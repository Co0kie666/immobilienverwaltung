package de.hsbi.immobilienverwaltung.service.interfaces;

import de.hsbi.immobilienverwaltung.domain.Immobilie;
import de.hsbi.immobilienverwaltung.domain.enums.Immobilientyp;

import java.util.List;
import java.util.Optional;

/**
 * Service Schnittstelle für die fachliche Verwaltung von Immobilien.
 * Die Implementierung enthält Validierungen und Geschäftsregeln
 */
public interface ImmobilieService {

    /**
     * Speichert eine neue oder bestehende Immobilie
     * Pflichtfelder und fachliche Regeln werden in der Implementierung geprüft
     *
     * @param immobilie Immobilie, die gespeichert werden soll
     * @return gespeicherte Immobilie
     */
    Immobilie speichereImmobilie(Immobilie immobilie);

    /**
     * Gibt alle gespeicherten Immobilien zurück
     * @return Liste aller Immobilien
     */
    List<Immobilie> findeAlleImmobilien();

    /**
     * Sucht eine Immobilie anhand ihrer ID
     *
     * @param id ID der Immobilie
     * @return Optional mit Immobilie, falls sie existiert
     */
    Optional<Immobilie> findeImmobilieNachId(Long id);

    /**
     * Löscht eine Immobilie, sofern keine fachlichen Regeln dagegen sprechen
     * Eine Immobilie darf z. B. nicht gelöscht werden, wenn noch aktive Mietverträge bestehen
     *
     * @param id ID der zu löschenden Immobilie
     */
    void loescheImmobilie(Long id);

    /**
     * Filtert Immobilien nach Ort/PLZ, Immobilientyp, Anzahl der Einheiten und Leerstand
     * Die Filterwerte stammen aus der Immobilienübersicht
     *
     * @param ortOderPlz Suchtext für Ort oder PLZ
     * @param typ gewählter Immobilientyp oder null für alle Typen
     * @param einheitenFilter Filter für die Anzahl der Einheiten
     * @param leerstandFilter Filter für Leerstand
     * @return gefilterte Liste von Immobilien
     */
    List<Immobilie> findeGefilterteImmobilien(
            String ortOderPlz,
            Immobilientyp typ,
            String einheitenFilter,
            String leerstandFilter
    );
}
package de.hsbi.immobilienverwaltung.repository;

import de.hsbi.immobilienverwaltung.domain.Mieter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository für den Datenbankzugriff auf Mieter.
 * Die Abfragen werden von Spring Data JPA aus den Methodennamen gebaut.
 */
public interface MieterRepository extends JpaRepository<Mieter, Long> {

    /**
     * Lädt alle Mieter, die noch normal in der Übersicht angezeigt werden sollen.
     */
    List<Mieter> findByArchiviertFalse();

    /**
     * Lädt alle Mieter, die archiviert wurden.
     * Diese Liste wird für die Archivansicht genutzt.
     */
    List<Mieter> findByArchiviertTrue();

    /**
     * Sucht Mieter über Vorname oder Nachname.
     * Die Groß- und Kleinschreibung spielt dabei keine Rolle.
     */
    List<Mieter> findByVornameContainingIgnoreCaseOrNachnameContainingIgnoreCase(
            String vorname,
            String nachname
    );

    /**
     * Sucht einen Mieter über die E-Mail-Adresse.
     * Wird vor allem gebraucht, um doppelte E-Mail-Adressen zu verhindern.
     */
    Optional<Mieter> findByEmailIgnoreCase(String email);
}
package de.hsbi.immobilienverwaltung.repository;

import de.hsbi.immobilienverwaltung.domain.Immobilie;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository für den Datenbankzugriff auf Immobilien.
 * Durch JpaRepository stehen Standardmethoden wie save, findAll,
 * findById und deleteById automatisch zur Verfügung.
 */
public interface ImmobilieRepository extends JpaRepository<Immobilie, Long> {
}
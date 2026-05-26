package de.hsbi.immobilienverwaltung.repository;

import de.hsbi.immobilienverwaltung.domain.Mieteinheit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MieteinheitRepository extends JpaRepository<Mieteinheit, Long> {

    List<Mieteinheit> findByImmobilieId(Long immobilieId);

}
package de.hsbi.immobilienverwaltung.repository;

import de.hsbi.immobilienverwaltung.domain.Mieteinheit;
import de.hsbi.immobilienverwaltung.domain.enums.Mieteinheitstatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface MieteinheitRepository extends JpaRepository<Mieteinheit, Long> {

    List<Mieteinheit> findByImmobilieId(Long immobilieId);

    long countByStatusIn(Collection<Mieteinheitstatus> status);

    long countByImmobilieId(Long immobilieId);

    long countByImmobilieIdAndStatus(Long immobilieId, Mieteinheitstatus status);
}
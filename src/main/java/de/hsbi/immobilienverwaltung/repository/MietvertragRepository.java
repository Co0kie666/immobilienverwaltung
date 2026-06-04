package de.hsbi.immobilienverwaltung.repository;

import de.hsbi.immobilienverwaltung.domain.Mietvertrag;
import de.hsbi.immobilienverwaltung.domain.enums.Vertragsstatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MietvertragRepository extends JpaRepository<Mietvertrag, Long> {

    List<Mietvertrag> findByMieterId(Long mieterId);

    List<Mietvertrag> findByMieteinheitId(Long mieteinheitId);

    List<Mietvertrag> findByStatus(Vertragsstatus status);

    List<Mietvertrag> findByMieteinheitIdAndStatus(Long mieteinheitId, Vertragsstatus status);
}
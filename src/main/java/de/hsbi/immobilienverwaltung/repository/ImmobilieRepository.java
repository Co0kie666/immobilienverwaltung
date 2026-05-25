package de.hsbi.immobilienverwaltung.repository;

import de.hsbi.immobilienverwaltung.domain.Immobilie;
import de.hsbi.immobilienverwaltung.domain.enums.Immobilientyp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImmobilieRepository extends JpaRepository<Immobilie, Long> {

    List<Immobilie> findByTyp(Immobilientyp typ);

    List<Immobilie> findByAdresseStadtContainingIgnoreCaseOrAdressePlzContainingIgnoreCase(
            String stadt,
            String plz
    );
}
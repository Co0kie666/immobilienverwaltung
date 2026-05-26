package de.hsbi.immobilienverwaltung.service.impl;

import de.hsbi.immobilienverwaltung.domain.enums.Mieteinheitstatus;
import de.hsbi.immobilienverwaltung.repository.MieteinheitRepository;
import de.hsbi.immobilienverwaltung.service.interfaces.GesamtAuswertungService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GesamtAuswertungServiceImpl implements GesamtAuswertungService {

    private final MieteinheitRepository mieteinheitRepository;

    public GesamtAuswertungServiceImpl(MieteinheitRepository mieteinheitRepository) {
        this.mieteinheitRepository = mieteinheitRepository;
    }

    @Override
    public double berechneLeerstandsquote() {
        long gesamt = mieteinheitRepository.count();

        if (gesamt == 0) {
            return 0.0;
        }

        long leerstand = berechneAnzahlLeerstehendeMieteinheiten();

        return (double) leerstand / gesamt * 100;
    }

    @Override
    public long berechneAnzahlLeerstehendeMieteinheiten() {
        return mieteinheitRepository.countByStatusIn(
                List.of(
                        Mieteinheitstatus.FREI,
                        Mieteinheitstatus.IN_RENOVIERUNG
                )
        );
    }

    @Override
    public long berechneAnzahlMieteinheiten() {
        return mieteinheitRepository.count();
    }
}
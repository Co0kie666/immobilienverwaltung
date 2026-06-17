package de.hsbi.immobilienverwaltung.service.impl;

import de.hsbi.immobilienverwaltung.domain.enums.Mieteinheitstatus;
import de.hsbi.immobilienverwaltung.domain.enums.Vertragsstatus;
import de.hsbi.immobilienverwaltung.repository.MieteinheitRepository;
import de.hsbi.immobilienverwaltung.repository.MietvertragRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GesamtAuswertungServiceImplTest {

    @Mock
    private MieteinheitRepository mieteinheitRepository;

    @Mock
    private MietvertragRepository mietvertragRepository;

    @InjectMocks
    private GesamtAuswertungServiceImpl gesamtAuswertungService;

    // Testet, ob die Leerstandsquote korrekt berechnet wird,
    // wenn Mieteinheiten vorhanden sind.
    @Test
    void berechnetLeerstandsquote() {
        when(mieteinheitRepository.count())
                .thenReturn(10L);

        when(mieteinheitRepository.countByStatusIn(
                List.of(
                        Mieteinheitstatus.FREI,
                        Mieteinheitstatus.IN_RENOVIERUNG
                )
        )).thenReturn(2L);

        double ergebnis =
                gesamtAuswertungService.berechneLeerstandsquote();

        assertEquals(20.0, ergebnis);

        verify(mieteinheitRepository).count();

        verify(mieteinheitRepository).countByStatusIn(
                List.of(
                        Mieteinheitstatus.FREI,
                        Mieteinheitstatus.IN_RENOVIERUNG
                )
        );
    }

    // Testet, ob die Anzahl aktiver Mietverträge korrekt aus dem Repository gelesen wird.
    @Test
    void berechnetAnzahlAktiveVertraege() {
        when(mietvertragRepository.countByStatus(Vertragsstatus.AKTIV))
                .thenReturn(8L);

        long ergebnis =
                gesamtAuswertungService.berechneAnzahlAktiveVertraege();

        assertEquals(8L, ergebnis);

        verify(mietvertragRepository).countByStatus(Vertragsstatus.AKTIV);
    }
}
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GesamtAuswertungServiceImplTest {

    @Mock
    private MieteinheitRepository mieteinheitRepository;

    @Mock
    private MietvertragRepository mietvertragRepository;

    @InjectMocks
    private GesamtAuswertungServiceImpl gesamtAuswertungService;

    // Testet, ob die Anzahl aller Mieteinheiten korrekt aus dem Repository gelesen wird.
    @Test
    void berechnetAnzahlMieteinheiten() {
        when(mieteinheitRepository.count()).thenReturn(10L);

        long ergebnis = gesamtAuswertungService.berechneAnzahlMieteinheiten();

        assertEquals(10L, ergebnis);
        verify(mieteinheitRepository).count();
    }

    // Testet, ob die Anzahl leerstehender Mieteinheiten korrekt berechnet wird.
    // Dabei zählen Mieteinheiten mit dem Status FREI und IN_RENOVIERUNG als Leerstand.
    @Test
    void berechnetAnzahlLeerstehendeMieteinheiten() {
        when(mieteinheitRepository.countByStatusIn(
                List.of(
                        Mieteinheitstatus.FREI,
                        Mieteinheitstatus.IN_RENOVIERUNG
                )
        )).thenReturn(3L);

        long ergebnis =
                gesamtAuswertungService.berechneAnzahlLeerstehendeMieteinheiten();

        assertEquals(3L, ergebnis);

        verify(mieteinheitRepository).countByStatusIn(
                List.of(
                        Mieteinheitstatus.FREI,
                        Mieteinheitstatus.IN_RENOVIERUNG
                )
        );
    }

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

        double ergebnis = gesamtAuswertungService.berechneLeerstandsquote();

        assertEquals(20.0, ergebnis);

        verify(mieteinheitRepository).count();

        verify(mieteinheitRepository).countByStatusIn(
                List.of(
                        Mieteinheitstatus.FREI,
                        Mieteinheitstatus.IN_RENOVIERUNG
                )
        );
    }

    // Testet, ob die Leerstandsquote 0.0 zurückgibt,
    // wenn keine Mieteinheiten vorhanden sind.
    @Test
    void gibtNullZurueckWennKeineMieteinheitenVorhandenSind() {
        when(mieteinheitRepository.count())
                .thenReturn(0L);

        double ergebnis = gesamtAuswertungService.berechneLeerstandsquote();

        assertEquals(0.0, ergebnis);

        verify(mieteinheitRepository).count();

        verify(mieteinheitRepository, never()).countByStatusIn(anyList());
    }

    // Testet, ob die Anzahl aktiver Mietverträge korrekt aus dem Repository gelesen wird.
    @Test
    void berechnetAnzahlAktiveVertraege() {
        when(mietvertragRepository.countByStatus(Vertragsstatus.AKTIV))
                .thenReturn(8L);

        long ergebnis = gesamtAuswertungService.berechneAnzahlAktiveVertraege();

        assertEquals(8L, ergebnis);

        verify(mietvertragRepository).countByStatus(Vertragsstatus.AKTIV);
    }
}
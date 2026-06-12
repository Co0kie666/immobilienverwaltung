package de.hsbi.immobilienverwaltung.service.impl;

import de.hsbi.immobilienverwaltung.domain.Adresse;
import de.hsbi.immobilienverwaltung.domain.Immobilie;
import de.hsbi.immobilienverwaltung.domain.enums.Immobilientyp;
import de.hsbi.immobilienverwaltung.domain.enums.Vertragsstatus;
import de.hsbi.immobilienverwaltung.repository.ImmobilieRepository;
import de.hsbi.immobilienverwaltung.repository.MietvertragRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImmobilieServiceImplTest {

    @Mock
    private ImmobilieRepository immobilieRepository;

    @Mock
    private MietvertragRepository mietvertragRepository;

    @InjectMocks
    private ImmobilieServiceImpl immobilieService;

    // Testet, ob eine Immobilie mit gültigen Daten erfolgreich gespeichert wird.
    // Dabei wird geprüft, ob das Repository aufgerufen wird und das gespeicherte Objekt zurückkommt.
    @Test
    void speichertGueltigeImmobilie() {
        Immobilie immobilie = new Immobilie(
                "Testimmobilie",
                Immobilientyp.MEHRFAMILIENHAUS,
                2005,
                500,
                new Adresse("Teststraße", "1", "33602", "Bielefeld")
        );

        when(immobilieRepository.save(immobilie)).thenReturn(immobilie);

        Immobilie ergebnis = immobilieService.speichereImmobilie(immobilie);

        assertNotNull(ergebnis);
        assertEquals("Testimmobilie", ergebnis.getBezeichnung());
        verify(immobilieRepository).save(immobilie);
    }

    // Testet, ob eine Immobilie nicht gelöscht wird, wenn zu einer ihrer Mieteinheiten
    // noch ein aktiver Mietvertrag existiert.
    // Erwartet wird eine Exception und kein Löschaufruf im Repository.
    @Test
    void verhindertLoeschenBeiAktivemMietvertrag() {
        Long immobilieId = 1L;

        when(mietvertragRepository.existsByMieteinheit_Immobilie_IdAndStatus(
                immobilieId,
                Vertragsstatus.AKTIV
        )).thenReturn(true);

        assertThrows(IllegalStateException.class, () ->
                immobilieService.loescheImmobilie(immobilieId)
        );

        verify(immobilieRepository, never()).deleteById(immobilieId);
    }
}
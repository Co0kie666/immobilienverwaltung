package de.hsbi.immobilienverwaltung.service.impl;

import de.hsbi.immobilienverwaltung.domain.Adresse;
import de.hsbi.immobilienverwaltung.domain.Immobilie;
import de.hsbi.immobilienverwaltung.domain.Mieteinheit;
import de.hsbi.immobilienverwaltung.domain.enums.Immobilientyp;
import de.hsbi.immobilienverwaltung.domain.enums.MieteinheitTyp;
import de.hsbi.immobilienverwaltung.domain.enums.Mieteinheitstatus;
import de.hsbi.immobilienverwaltung.domain.enums.Vertragsstatus;
import de.hsbi.immobilienverwaltung.repository.ImmobilieRepository;
import de.hsbi.immobilienverwaltung.repository.MieteinheitRepository;
import de.hsbi.immobilienverwaltung.repository.MietvertragRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MieteinheitServiceImplTest {

    @Mock
    private MieteinheitRepository mieteinheitRepository;

    @Mock
    private ImmobilieRepository immobilieRepository;

    @Mock
    private MietvertragRepository mietvertragRepository;

    @InjectMocks
    private MieteinheitServiceImpl mieteinheitService;

    // Testet, ob eine gültige Mieteinheit gespeichert wird
    // Dabei wird geprüft, ob die Immobilie gefunden und gesetzt wird
    @Test
    void speichertGueltigeMieteinheit() {
        Long immobilieId = 1L;

        Immobilie immobilie = new Immobilie(
                "Testimmobilie",
                Immobilientyp.MEHRFAMILIENHAUS,
                2005,
                500,
                new Adresse("Teststraße", "1", "33602", "Bielefeld")
        );

        Mieteinheit mieteinheit = new Mieteinheit(
                "Wohnung 1",
                Mieteinheitstatus.FREI,
                MieteinheitTyp.WOHNUNG,
                80,
                3,
                "EG"
        );

        when(immobilieRepository.findById(immobilieId)).thenReturn(Optional.of(immobilie));

        when(mieteinheitRepository.findByImmobilieId(immobilieId)).thenReturn(List.of());

        when(mieteinheitRepository.save(mieteinheit)).thenReturn(mieteinheit);

        Mieteinheit ergebnis = mieteinheitService.speichereMieteinheit(immobilieId, mieteinheit);

        assertNotNull(ergebnis);
        assertEquals("Wohnung 1", ergebnis.getBezeichnung());
        assertEquals(immobilie, ergebnis.getImmobilie());

        verify(mieteinheitRepository).save(mieteinheit);
    }

    // Testet, ob der Status einer Mieteinheit nicht auf FREI gesetzt werden darf,
    // wenn für diese Mieteinheit noch ein aktiver Mietvertrag existiert.
    // Erwartet wird eine Exception und kein Speichern im Repository.
    @Test
    void verhindertStatusFreiBeiAktivemMietvertrag() {
        Long immobilieId = 1L;
        Long mieteinheitId = 10L;

        Immobilie immobilie = new Immobilie(
                "Testimmobilie",
                Immobilientyp.MEHRFAMILIENHAUS,
                2005,
                500,
                new Adresse("Teststraße", "1", "33602", "Bielefeld")
        );

        Mieteinheit mieteinheit = new Mieteinheit(
                "Wohnung 1",
                Mieteinheitstatus.FREI,
                MieteinheitTyp.WOHNUNG,
                80,
                3,
                "EG"
        );

        // Da die Entity keinen setId()-Setter hat, wird die ID nur für den Test gesetzt.
        ReflectionTestUtils.setField(mieteinheit, "id", mieteinheitId);

        when(immobilieRepository.findById(immobilieId)).thenReturn(Optional.of(immobilie));

        when(mietvertragRepository.existsByMieteinheit_IdAndStatus(
                mieteinheitId,
                Vertragsstatus.AKTIV
        )).thenReturn(true);

        assertThrows(IllegalStateException.class, () ->
                mieteinheitService.speichereMieteinheit(immobilieId, mieteinheit)
        );

        verify(mieteinheitRepository, never()).save(mieteinheit);
    }
}
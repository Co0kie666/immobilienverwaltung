package de.hsbi.immobilienverwaltung.service.impl;

import de.hsbi.immobilienverwaltung.domain.Mieteinheit;
import de.hsbi.immobilienverwaltung.domain.Mieter;
import de.hsbi.immobilienverwaltung.domain.Mietvertrag;
import de.hsbi.immobilienverwaltung.domain.enums.Mieteinheitstatus;
import de.hsbi.immobilienverwaltung.domain.enums.Vertragsstatus;
import de.hsbi.immobilienverwaltung.repository.MieteinheitRepository;
import de.hsbi.immobilienverwaltung.repository.MieterRepository;
import de.hsbi.immobilienverwaltung.repository.MietvertragRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MietvertragServiceImplTest {

    @Mock
    private MietvertragRepository mietvertragRepository;

    @Mock
    private MieterRepository mieterRepository;

    @Mock
    private MieteinheitRepository mieteinheitRepository;

    @InjectMocks
    private MietvertragServiceImpl mietvertragService;

    // Testet, ob ein gültiger Mietvertrag gespeichert wird.
    // Dabei wird geprüft, ob Mieter und Mieteinheit gesetzt werden
    // und die Mieteinheit danach als vermietet markiert wird.
    @Test
    void speichertGueltigenMietvertragUndSetztMieteinheitAufVermietet() {
        Long mieterId = 1L;
        Long mieteinheitId = 10L;

        Mieter mieter = new Mieter();
        ReflectionTestUtils.setField(mieter, "id", mieterId);

        Mieteinheit mieteinheit = new Mieteinheit();
        ReflectionTestUtils.setField(mieteinheit, "id", mieteinheitId);
        mieteinheit.setBezeichnung("Wohnung 1");
        mieteinheit.setStatus(Mieteinheitstatus.FREI);

        Mietvertrag mietvertrag = new Mietvertrag();
        mietvertrag.setStartdatum(LocalDate.now());
        mietvertrag.setKaltmiete(700.0);
        mietvertrag.setNebenkosten(150.0);

        when(mieterRepository.findById(mieterId))
                .thenReturn(Optional.of(mieter));

        when(mieteinheitRepository.findById(mieteinheitId))
                .thenReturn(Optional.of(mieteinheit));

        when(mietvertragRepository.findByMieteinheitId(mieteinheitId))
                .thenReturn(List.of());

        when(mietvertragRepository.save(mietvertrag))
                .thenReturn(mietvertrag);

        Mietvertrag ergebnis =
                mietvertragService.speichereMietvertrag(mieterId, mieteinheitId, mietvertrag);

        assertNotNull(ergebnis);
        assertEquals(Vertragsstatus.AKTIV, ergebnis.getStatus());
        assertEquals(mieter, ergebnis.getMieter());
        assertEquals(mieteinheit, ergebnis.getMieteinheit());
        assertEquals(Mieteinheitstatus.VERMIETET, mieteinheit.getStatus());

        verify(mieteinheitRepository).save(mieteinheit);
        verify(mietvertragRepository).save(mietvertrag);
    }

    // Testet, ob für eine Mieteinheit kein zweiter laufender Mietvertrag
    // gespeichert werden darf.
    @Test
    void verhindertZweitenLaufendenMietvertragFuerDieselbeMieteinheit() {
        Long mieterId = 1L;
        Long mieteinheitId = 10L;

        Mieter mieter = new Mieter();

        Mieteinheit mieteinheit = new Mieteinheit();
        ReflectionTestUtils.setField(mieteinheit, "id", mieteinheitId);
        mieteinheit.setStatus(Mieteinheitstatus.FREI);

        Mietvertrag neuerVertrag = new Mietvertrag();
        neuerVertrag.setStartdatum(LocalDate.now());
        neuerVertrag.setKaltmiete(800.0);
        neuerVertrag.setNebenkosten(200.0);
        neuerVertrag.setStatus(Vertragsstatus.AKTIV);

        Mietvertrag vorhandenerVertrag = new Mietvertrag();
        vorhandenerVertrag.setStatus(Vertragsstatus.AKTIV);

        when(mieterRepository.findById(mieterId))
                .thenReturn(Optional.of(mieter));

        when(mieteinheitRepository.findById(mieteinheitId))
                .thenReturn(Optional.of(mieteinheit));

        when(mietvertragRepository.findByMieteinheitId(mieteinheitId))
                .thenReturn(List.of(vorhandenerVertrag));

        assertThrows(IllegalArgumentException.class, () ->
                mietvertragService.speichereMietvertrag(mieterId, mieteinheitId, neuerVertrag)
        );

        verify(mietvertragRepository, never()).save(neuerVertrag);
    }
}
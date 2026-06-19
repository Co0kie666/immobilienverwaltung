package de.hsbi.immobilienverwaltung.service.impl;

import de.hsbi.immobilienverwaltung.domain.Mieter;
import de.hsbi.immobilienverwaltung.domain.Mietvertrag;
import de.hsbi.immobilienverwaltung.domain.enums.Vertragsstatus;
import de.hsbi.immobilienverwaltung.repository.MieterRepository;
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
class MieterServiceImplTest {

    @Mock
    private MieterRepository mieterRepository;

    @Mock
    private MietvertragRepository mietvertragRepository;

    @InjectMocks
    private MieterServiceImpl mieterService;

    // Testet, ob ein Mieter mit gültigen Daten gespeichert wird.
    // Wichtig: Die Telefonnummer darf auch Leerzeichen enthalten.
    @Test
    void speichertGueltigenMieterMitTelefonnummerMitLeerzeichen() {
        Mieter mieter = new Mieter();
        mieter.setVorname("Max");
        mieter.setNachname("Mustermann");
        mieter.setEmail("max@test.de");
        mieter.setTelefonnummer("+49 173 77788899");

        when(mieterRepository.findByEmailIgnoreCase("max@test.de"))
                .thenReturn(Optional.empty());

        when(mieterRepository.save(mieter))
                .thenReturn(mieter);

        Mieter ergebnis = mieterService.speichereMieter(mieter);

        assertNotNull(ergebnis);
        assertEquals("Max", ergebnis.getVorname());
        assertEquals("+49 173 77788899", ergebnis.getTelefonnummer());

        verify(mieterRepository).save(mieter);
    }

    // Testet, ob ein Mieter nicht archiviert werden darf,
    // wenn noch ein aktiver Mietvertrag vorhanden ist.
    @Test
    void verhindertArchivierenBeiAktivemMietvertrag() {
        Long mieterId = 1L;

        Mieter mieter = new Mieter();
        ReflectionTestUtils.setField(mieter, "id", mieterId);

        Mietvertrag aktiverVertrag = new Mietvertrag();
        aktiverVertrag.setStatus(Vertragsstatus.AKTIV);

        when(mieterRepository.findById(mieterId))
                .thenReturn(Optional.of(mieter));

        when(mietvertragRepository.findByMieterId(mieterId))
                .thenReturn(List.of(aktiverVertrag));

        assertThrows(IllegalStateException.class, () ->
                mieterService.archiviereMieter(mieterId)
        );

        verify(mieterRepository, never()).save(mieter);
    }
}
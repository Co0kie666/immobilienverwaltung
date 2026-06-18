package de.hsbi.immobilienverwaltung.service.impl;

import de.hsbi.immobilienverwaltung.domain.Immobilie;
import de.hsbi.immobilienverwaltung.domain.Mieteinheit;
import de.hsbi.immobilienverwaltung.domain.Mieter;
import de.hsbi.immobilienverwaltung.domain.Mietvertrag;
import de.hsbi.immobilienverwaltung.domain.Zahlungseingang;
import de.hsbi.immobilienverwaltung.domain.enums.Zahlungseingangtyp;
import de.hsbi.immobilienverwaltung.repository.ZahlungsEingangRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ZahlungsEingangServiceImplTest {

    @Mock
    private ZahlungsEingangRepository zahlungsEingangRepository;

    @InjectMocks
    private ZahlungsEingangServiceImpl zahlungsEingangService;

    // Testet, ob ein Zahlungseingang mit gültigen Daten erfolgreich gespeichert wird.
    // Dabei wird geprüft, ob das Repository aufgerufen wird und das gespeicherte Objekt zurückkommt.
    @Test
    void speichertGueltigenZahlungseingang() {
        Mietvertrag mietvertrag = new Mietvertrag();

        Zahlungseingang zahlungseingang = new Zahlungseingang();
        zahlungseingang.setTyp(Zahlungseingangtyp.KALTMIETE);
        zahlungseingang.setBetrag(BigDecimal.valueOf(850));
        zahlungseingang.setZahlungsdatum(LocalDate.of(2026, 1, 5));
        zahlungseingang.setMietvertrag(mietvertrag);

        when(zahlungsEingangRepository.save(zahlungseingang))
                .thenReturn(zahlungseingang);

        Zahlungseingang ergebnis =
                zahlungsEingangService.speichereZahlungseingang(zahlungseingang);

        assertNotNull(ergebnis);
        assertEquals(Zahlungseingangtyp.KALTMIETE, ergebnis.getTyp());
        assertEquals(BigDecimal.valueOf(850), ergebnis.getBetrag());

        verify(zahlungsEingangRepository).save(zahlungseingang);
    }

    // Testet, ob bezahlte Zahlungseingänge im Zeitraum mit mehreren Filtern korrekt summiert werden.
    // Es sollen nur Zahlungen mit passender Immobilie, Mieteinheit, Mieter und bezahltem Status zählen.
    @Test
    void berechnetBezahlteZahlungseingaengeImZeitraumMitFiltern() {
        LocalDate startDatum = LocalDate.of(2026, 1, 1);
        LocalDate endDatum = LocalDate.of(2026, 1, 31);

        Immobilie immobilie = mock(Immobilie.class);
        when(immobilie.getId()).thenReturn(1L);

        Mieteinheit mieteinheit = mock(Mieteinheit.class);
        when(mieteinheit.getId()).thenReturn(10L);
        when(mieteinheit.getImmobilie()).thenReturn(immobilie);

        Mieter mieter = mock(Mieter.class);
        when(mieter.getId()).thenReturn(100L);

        Mietvertrag mietvertrag = mock(Mietvertrag.class);
        when(mietvertrag.getMieteinheit()).thenReturn(mieteinheit);
        when(mietvertrag.getMieter()).thenReturn(mieter);

        Zahlungseingang passendeZahlung = new Zahlungseingang();
        passendeZahlung.setZahlungsdatum(LocalDate.of(2026, 1, 5));
        passendeZahlung.setStatus("Bezahlt / Erledigt");
        passendeZahlung.setBetrag(BigDecimal.valueOf(850));
        passendeZahlung.setMietvertrag(mietvertrag);

        Zahlungseingang offeneZahlung = new Zahlungseingang();
        offeneZahlung.setZahlungsdatum(LocalDate.of(2026, 1, 6));
        offeneZahlung.setStatus("Offen / Ausstehend");
        offeneZahlung.setBetrag(BigDecimal.valueOf(850));
        offeneZahlung.setMietvertrag(mietvertrag);

        when(zahlungsEingangRepository.findAll())
                .thenReturn(List.of(passendeZahlung, offeneZahlung));

        BigDecimal ergebnis =
                zahlungsEingangService.berechneBezahlteZahlungseingaengeImZeitraum(
                        startDatum,
                        endDatum,
                        1L,
                        10L,
                        100L
                );

        assertEquals(BigDecimal.valueOf(850), ergebnis);

        verify(zahlungsEingangRepository).findAll();
    }
}
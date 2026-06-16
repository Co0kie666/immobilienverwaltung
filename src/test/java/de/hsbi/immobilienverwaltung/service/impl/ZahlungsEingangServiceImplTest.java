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
import java.util.Optional;

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

    // Testet, ob kein Zahlungseingang gespeichert wird, wenn der Zahlungseingang null ist.
    // Erwartet wird eine Exception und kein Speicheraufruf im Repository.
    @Test
    void verhindertSpeichernWennZahlungseingangNullIst() {
        assertThrows(IllegalArgumentException.class, () ->
                zahlungsEingangService.speichereZahlungseingang(null)
        );

        verify(zahlungsEingangRepository, never()).save(any());
    }

    // Testet, ob kein Zahlungseingang gespeichert wird, wenn kein Typ gesetzt ist.
    // Erwartet wird eine Exception und kein Speicheraufruf im Repository.
    @Test
    void verhindertSpeichernOhneZahlungstyp() {
        Zahlungseingang zahlungseingang = new Zahlungseingang();

        zahlungseingang.setBetrag(BigDecimal.valueOf(850));
        zahlungseingang.setZahlungsdatum(LocalDate.of(2026, 1, 5));
        zahlungseingang.setMietvertrag(new Mietvertrag());

        assertThrows(IllegalArgumentException.class, () ->
                zahlungsEingangService.speichereZahlungseingang(zahlungseingang)
        );

        verify(zahlungsEingangRepository, never()).save(any());
    }

    // Testet, ob kein Zahlungseingang gespeichert wird, wenn der Betrag null oder kleiner gleich 0 ist.
    // Erwartet wird eine Exception und kein Speicheraufruf im Repository.
    @Test
    void verhindertSpeichernMitUngueltigemBetrag() {
        Zahlungseingang zahlungseingang = new Zahlungseingang();

        zahlungseingang.setTyp(Zahlungseingangtyp.KALTMIETE);
        zahlungseingang.setBetrag(BigDecimal.ZERO);
        zahlungseingang.setZahlungsdatum(LocalDate.of(2026, 1, 5));
        zahlungseingang.setMietvertrag(new Mietvertrag());

        assertThrows(IllegalArgumentException.class, () ->
                zahlungsEingangService.speichereZahlungseingang(zahlungseingang)
        );

        verify(zahlungsEingangRepository, never()).save(any());
    }

    // Testet, ob kein Zahlungseingang gespeichert wird, wenn kein Zahlungsdatum gesetzt ist.
    // Erwartet wird eine Exception und kein Speicheraufruf im Repository.
    @Test
    void verhindertSpeichernOhneZahlungsdatum() {
        Zahlungseingang zahlungseingang = new Zahlungseingang();

        zahlungseingang.setTyp(Zahlungseingangtyp.KALTMIETE);
        zahlungseingang.setBetrag(BigDecimal.valueOf(850));
        zahlungseingang.setMietvertrag(new Mietvertrag());

        assertThrows(IllegalArgumentException.class, () ->
                zahlungsEingangService.speichereZahlungseingang(zahlungseingang)
        );

        verify(zahlungsEingangRepository, never()).save(any());
    }

    // Testet, ob kein Zahlungseingang gespeichert wird, wenn kein Mietvertrag gesetzt ist.
    // Erwartet wird eine Exception und kein Speicheraufruf im Repository.
    @Test
    void verhindertSpeichernOhneMietvertrag() {
        Zahlungseingang zahlungseingang = new Zahlungseingang();

        zahlungseingang.setTyp(Zahlungseingangtyp.KALTMIETE);
        zahlungseingang.setBetrag(BigDecimal.valueOf(850));
        zahlungseingang.setZahlungsdatum(LocalDate.of(2026, 1, 5));

        assertThrows(IllegalArgumentException.class, () ->
                zahlungsEingangService.speichereZahlungseingang(zahlungseingang)
        );

        verify(zahlungsEingangRepository, never()).save(any());
    }

    // Testet, ob alle Zahlungseingänge korrekt aus dem Repository gelesen werden.
    @Test
    void findetAlleZahlungseingaenge() {
        Zahlungseingang zahlung1 = new Zahlungseingang();
        Zahlungseingang zahlung2 = new Zahlungseingang();

        when(zahlungsEingangRepository.findAll())
                .thenReturn(List.of(zahlung1, zahlung2));

        List<Zahlungseingang> ergebnis =
                zahlungsEingangService.findeAlleZahlungseingaenge();

        assertEquals(2, ergebnis.size());
        verify(zahlungsEingangRepository).findAll();
    }

    // Testet, ob ein Zahlungseingang anhand seiner ID gefunden wird.
    @Test
    void findetZahlungseingangNachId() {
        Long id = 1L;
        Zahlungseingang zahlungseingang = new Zahlungseingang();

        when(zahlungsEingangRepository.findById(id))
                .thenReturn(Optional.of(zahlungseingang));

        Optional<Zahlungseingang> ergebnis =
                zahlungsEingangService.findeZahlungseingangNachId(id);

        assertTrue(ergebnis.isPresent());
        assertSame(zahlungseingang, ergebnis.get());

        verify(zahlungsEingangRepository).findById(id);
    }

    // Testet, ob Zahlungseingänge zu einem Mietvertrag korrekt gelesen werden.
    @Test
    void findetZahlungseingaengeNachMietvertrag() {
        Mietvertrag mietvertrag = new Mietvertrag();

        Zahlungseingang zahlung1 = new Zahlungseingang();
        Zahlungseingang zahlung2 = new Zahlungseingang();

        when(zahlungsEingangRepository.findByMietvertrag(mietvertrag))
                .thenReturn(List.of(zahlung1, zahlung2));

        List<Zahlungseingang> ergebnis =
                zahlungsEingangService.findeZahlungseingaengeNachMietvertrag(mietvertrag);

        assertEquals(2, ergebnis.size());
        verify(zahlungsEingangRepository).findByMietvertrag(mietvertrag);
    }

    // Testet, ob Zahlungseingänge zu einem Mietvertrag im Zeitraum korrekt gelesen werden.
    @Test
    void findetZahlungseingaengeNachMietvertragUndZeitraum() {
        Mietvertrag mietvertrag = new Mietvertrag();

        LocalDate startDatum = LocalDate.of(2026, 1, 1);
        LocalDate endDatum = LocalDate.of(2026, 1, 31);

        Zahlungseingang zahlung = new Zahlungseingang();

        when(zahlungsEingangRepository.findByMietvertragAndZahlungsdatumBetween(
                mietvertrag,
                startDatum,
                endDatum
        )).thenReturn(List.of(zahlung));

        List<Zahlungseingang> ergebnis =
                zahlungsEingangService.findeZahlungseingaengeNachMietvertragUndZeitraum(
                        mietvertrag,
                        startDatum,
                        endDatum
                );

        assertEquals(1, ergebnis.size());

        verify(zahlungsEingangRepository).findByMietvertragAndZahlungsdatumBetween(
                mietvertrag,
                startDatum,
                endDatum
        );
    }

    // Testet, ob die Summe der Zahlungseingänge für einen Mietvertrag im Zeitraum korrekt berechnet wird.
    // Dabei werden null-Beträge ignoriert.
    @Test
    void berechnetSummeZahlungseingaengeFuerMietvertrag() {
        Mietvertrag mietvertrag = new Mietvertrag();

        LocalDate startDatum = LocalDate.of(2026, 1, 1);
        LocalDate endDatum = LocalDate.of(2026, 1, 31);

        Zahlungseingang zahlung1 = new Zahlungseingang();
        zahlung1.setBetrag(BigDecimal.valueOf(850));

        Zahlungseingang zahlung2 = new Zahlungseingang();
        zahlung2.setBetrag(BigDecimal.valueOf(150));

        Zahlungseingang zahlung3 = new Zahlungseingang();
        zahlung3.setBetrag(null);

        when(zahlungsEingangRepository.findByMietvertragAndZahlungsdatumBetween(
                mietvertrag,
                startDatum,
                endDatum
        )).thenReturn(List.of(zahlung1, zahlung2, zahlung3));

        BigDecimal ergebnis =
                zahlungsEingangService.berechneSummeZahlungseingaengeFuerMietvertrag(
                        mietvertrag,
                        startDatum,
                        endDatum
                );

        assertEquals(BigDecimal.valueOf(1000), ergebnis);

        verify(zahlungsEingangRepository).findByMietvertragAndZahlungsdatumBetween(
                mietvertrag,
                startDatum,
                endDatum
        );
    }

    // Testet, ob erkannt wird, dass eine Zahlung im Zeitraum vorhanden ist.
    @Test
    void erkenntZahlungImZeitraum() {
        Mietvertrag mietvertrag = new Mietvertrag();

        LocalDate startDatum = LocalDate.of(2026, 1, 1);
        LocalDate endDatum = LocalDate.of(2026, 1, 31);

        when(zahlungsEingangRepository.existsByMietvertragAndZahlungsdatumBetween(
                mietvertrag,
                startDatum,
                endDatum
        )).thenReturn(true);

        boolean ergebnis =
                zahlungsEingangService.hatZahlungImZeitraum(
                        mietvertrag,
                        startDatum,
                        endDatum
                );

        assertTrue(ergebnis);

        verify(zahlungsEingangRepository).existsByMietvertragAndZahlungsdatumBetween(
                mietvertrag,
                startDatum,
                endDatum
        );
    }

    // Testet, ob offene Zahlungen für eine Immobilie korrekt summiert werden.
    // Dabei werden null-Beträge ignoriert.
    @Test
    void berechnetOffeneZahlungenFuerImmobilie() {
        Long immobilieId = 1L;

        Zahlungseingang zahlung1 = new Zahlungseingang();
        zahlung1.setBetrag(BigDecimal.valueOf(300));

        Zahlungseingang zahlung2 = new Zahlungseingang();
        zahlung2.setBetrag(BigDecimal.valueOf(200));

        Zahlungseingang zahlung3 = new Zahlungseingang();
        zahlung3.setBetrag(null);

        when(zahlungsEingangRepository.findByMietvertrag_Mieteinheit_Immobilie_IdAndStatus(
                immobilieId,
                "Offen / Ausstehend"
        )).thenReturn(List.of(zahlung1, zahlung2, zahlung3));

        BigDecimal ergebnis =
                zahlungsEingangService.berechneOffeneZahlungenFuerImmobilie(immobilieId);

        assertEquals(BigDecimal.valueOf(500), ergebnis);

        verify(zahlungsEingangRepository).findByMietvertrag_Mieteinheit_Immobilie_IdAndStatus(
                immobilieId,
                "Offen / Ausstehend"
        );
    }

    // Testet, ob offene Zahlungen für eine Immobilie korrekt gezählt werden.
    @Test
    void zaehltOffeneZahlungenFuerImmobilie() {
        Long immobilieId = 1L;

        when(zahlungsEingangRepository.countByMietvertrag_Mieteinheit_Immobilie_IdAndStatus(
                immobilieId,
                "Offen / Ausstehend"
        )).thenReturn(4L);

        long ergebnis =
                zahlungsEingangService.zaehleOffeneZahlungenFuerImmobilie(immobilieId);

        assertEquals(4L, ergebnis);

        verify(zahlungsEingangRepository).countByMietvertrag_Mieteinheit_Immobilie_IdAndStatus(
                immobilieId,
                "Offen / Ausstehend"
        );
    }

    // Testet, ob bezahlte Zahlungseingänge korrekt summiert werden.
    // Dabei werden null-Beträge ignoriert.
    @Test
    void berechnetGesamteBezahlteZahlungseingaenge() {
        Zahlungseingang zahlung1 = new Zahlungseingang();
        zahlung1.setBetrag(BigDecimal.valueOf(1000));

        Zahlungseingang zahlung2 = new Zahlungseingang();
        zahlung2.setBetrag(BigDecimal.valueOf(500));

        Zahlungseingang zahlung3 = new Zahlungseingang();
        zahlung3.setBetrag(null);

        when(zahlungsEingangRepository.findByStatus("Bezahlt / Erledigt"))
                .thenReturn(List.of(zahlung1, zahlung2, zahlung3));

        BigDecimal ergebnis =
                zahlungsEingangService.berechneGesamteBezahlteZahlungseingaenge();

        assertEquals(BigDecimal.valueOf(1500), ergebnis);
        verify(zahlungsEingangRepository).findByStatus("Bezahlt / Erledigt");
    }

    // Testet, ob offene Zahlungseingänge korrekt summiert werden.
    // Dabei werden nur Zahlungseingänge mit dem Status "Offen / Ausstehend" verwendet.
    @Test
    void berechnetOffeneZahlungseingaenge() {
        Zahlungseingang zahlung1 = new Zahlungseingang();
        zahlung1.setBetrag(BigDecimal.valueOf(300));

        Zahlungseingang zahlung2 = new Zahlungseingang();
        zahlung2.setBetrag(BigDecimal.valueOf(200));

        when(zahlungsEingangRepository.findByStatus("Offen / Ausstehend"))
                .thenReturn(List.of(zahlung1, zahlung2));

        BigDecimal ergebnis =
                zahlungsEingangService.berechneOffeneZahlungseingaenge();

        assertEquals(BigDecimal.valueOf(500), ergebnis);
        verify(zahlungsEingangRepository).findByStatus("Offen / Ausstehend");
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

    // Testet, ob offene Zahlungseingänge im Zeitraum mit mehreren Filtern korrekt summiert werden.
    // Bezahlte Zahlungseingänge dürfen hier nicht mitgezählt werden.
    @Test
    void berechnetOffeneZahlungseingaengeImZeitraumMitFiltern() {
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

        Zahlungseingang offeneZahlung = new Zahlungseingang();
        offeneZahlung.setZahlungsdatum(LocalDate.of(2026, 1, 5));
        offeneZahlung.setStatus("Offen / Ausstehend");
        offeneZahlung.setBetrag(BigDecimal.valueOf(400));
        offeneZahlung.setMietvertrag(mietvertrag);

        Zahlungseingang bezahlteZahlung = new Zahlungseingang();
        bezahlteZahlung.setZahlungsdatum(LocalDate.of(2026, 1, 6));
        bezahlteZahlung.setStatus("Bezahlt / Erledigt");
        bezahlteZahlung.setBetrag(BigDecimal.valueOf(900));
        bezahlteZahlung.setMietvertrag(mietvertrag);

        when(zahlungsEingangRepository.findAll())
                .thenReturn(List.of(offeneZahlung, bezahlteZahlung));

        BigDecimal ergebnis =
                zahlungsEingangService.berechneOffeneZahlungseingaengeImZeitraum(
                        startDatum,
                        endDatum,
                        1L,
                        10L,
                        100L
                );

        assertEquals(BigDecimal.valueOf(400), ergebnis);
        verify(zahlungsEingangRepository).findAll();
    }

    // Testet, ob offene Zahlungseingänge korrekt aus dem Repository gelesen werden.
    @Test
    void findetOffeneZahlungseingaenge() {
        Zahlungseingang zahlung1 = new Zahlungseingang();
        Zahlungseingang zahlung2 = new Zahlungseingang();

        when(zahlungsEingangRepository.findByStatus("Offen / Ausstehend"))
                .thenReturn(List.of(zahlung1, zahlung2));

        List<Zahlungseingang> ergebnis =
                zahlungsEingangService.findeOffeneZahlungseingaenge();

        assertEquals(2, ergebnis.size());
        verify(zahlungsEingangRepository).findByStatus("Offen / Ausstehend");
    }

    // Testet, ob Zahlungseingänge im Zeitraum mit mehreren Filtern korrekt gefunden werden.
// Es sollen nur Zahlungen zurückgegeben werden, die Zeitraum, Immobilie, Mieteinheit und Mieter erfüllen.
    @Test
    void findetZahlungseingaengeImZeitraumMitFiltern() {
        LocalDate startDatum = LocalDate.of(2026, 1, 1);
        LocalDate endDatum = LocalDate.of(2026, 1, 31);

        Immobilie immobilie1 = mock(Immobilie.class);
        when(immobilie1.getId()).thenReturn(1L);

        Immobilie immobilie2 = mock(Immobilie.class);
        when(immobilie2.getId()).thenReturn(2L);

        Mieteinheit passendeMieteinheit = mock(Mieteinheit.class);
        when(passendeMieteinheit.getId()).thenReturn(10L);
        when(passendeMieteinheit.getImmobilie()).thenReturn(immobilie1);

        Mieteinheit falscheMieteinheit = mock(Mieteinheit.class);
        when(falscheMieteinheit.getImmobilie()).thenReturn(immobilie2);

        Mieter passenderMieter = mock(Mieter.class);
        when(passenderMieter.getId()).thenReturn(100L);

        Mietvertrag passenderMietvertrag = mock(Mietvertrag.class);
        when(passenderMietvertrag.getMieteinheit()).thenReturn(passendeMieteinheit);
        when(passenderMietvertrag.getMieter()).thenReturn(passenderMieter);

        Mietvertrag falscherMietvertrag = mock(Mietvertrag.class);
        when(falscherMietvertrag.getMieteinheit()).thenReturn(falscheMieteinheit);

        Zahlungseingang passendeZahlung = new Zahlungseingang();
        passendeZahlung.setZahlungsdatum(LocalDate.of(2026, 1, 10));
        passendeZahlung.setMietvertrag(passenderMietvertrag);

        Zahlungseingang falscheZahlung = new Zahlungseingang();
        falscheZahlung.setZahlungsdatum(LocalDate.of(2026, 1, 10));
        falscheZahlung.setMietvertrag(falscherMietvertrag);

        Zahlungseingang ausserhalb = new Zahlungseingang();
        ausserhalb.setZahlungsdatum(LocalDate.of(2026, 2, 1));
        ausserhalb.setMietvertrag(passenderMietvertrag);

        when(zahlungsEingangRepository.findAll())
                .thenReturn(List.of(
                        passendeZahlung,
                        falscheZahlung,
                        ausserhalb
                ));

        List<Zahlungseingang> ergebnis =
                zahlungsEingangService.findeZahlungseingaengeImZeitraum(
                        startDatum,
                        endDatum,
                        1L,
                        10L,
                        100L
                );

        assertEquals(1, ergebnis.size());
        assertSame(passendeZahlung, ergebnis.getFirst());

        verify(zahlungsEingangRepository).findAll();
    }

    // Testet, ob ein Zahlungseingang gelöscht wird.
    // Dabei wird geprüft, ob deleteById im Repository mit der richtigen ID aufgerufen wird.
    @Test
    void loeschtZahlungseingang() {
        Long id = 1L;

        zahlungsEingangService.loescheZahlungseingang(id);

        verify(zahlungsEingangRepository).deleteById(id);
    }
}
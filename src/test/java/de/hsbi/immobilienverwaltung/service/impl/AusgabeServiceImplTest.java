package de.hsbi.immobilienverwaltung.service.impl;

import de.hsbi.immobilienverwaltung.domain.Ausgabe;
import de.hsbi.immobilienverwaltung.domain.Immobilie;
import de.hsbi.immobilienverwaltung.domain.enums.Ausgabenkategorie;
import de.hsbi.immobilienverwaltung.repository.AusgabeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AusgabeServiceImplTest {

    @Mock
    private AusgabeRepository ausgabeRepository;

    @InjectMocks
    private AusgabeServiceImpl ausgabeService;

    // Testet, ob eine Ausgabe mit gültigen Daten erfolgreich gespeichert wird.
    // Dabei wird geprüft, ob das Repository aufgerufen wird und das gespeicherte Objekt zurückkommt.
    @Test
    void speichertGueltigeAusgabe() {
        Immobilie immobilie = mock(Immobilie.class);

        Ausgabe ausgabe = new Ausgabe();
        ausgabe.setKategorie(Ausgabenkategorie.REPARATUR);
        ausgabe.setBetrag(BigDecimal.valueOf(250));
        ausgabe.setDatum(LocalDate.of(2026, 1, 10));
        ausgabe.setImmobilie(immobilie);

        when(ausgabeRepository.save(ausgabe)).thenReturn(ausgabe);

        Ausgabe ergebnis = ausgabeService.speichereAusgabe(ausgabe);

        assertNotNull(ergebnis);
        assertEquals(Ausgabenkategorie.REPARATUR, ergebnis.getKategorie());
        assertEquals(BigDecimal.valueOf(250), ergebnis.getBetrag());

        verify(ausgabeRepository).save(ausgabe);
    }

    // Testet, ob keine Ausgabe gespeichert wird, wenn die Ausgabe null ist.
    // Erwartet wird eine Exception und kein Speicheraufruf im Repository.
    @Test
    void verhindertSpeichernWennAusgabeNullIst() {
        assertThrows(IllegalArgumentException.class, () ->
                ausgabeService.speichereAusgabe(null)
        );

        verify(ausgabeRepository, never()).save(any());
    }

    // Testet, ob keine Ausgabe gespeichert wird, wenn keine Kategorie gesetzt ist.
    // Erwartet wird eine Exception und kein Speicheraufruf im Repository.
    @Test
    void verhindertSpeichernOhneKategorie() {
        Ausgabe ausgabe = new Ausgabe();

        ausgabe.setBetrag(BigDecimal.valueOf(100));
        ausgabe.setDatum(LocalDate.of(2026, 1, 10));
        ausgabe.setImmobilie(mock(Immobilie.class));

        assertThrows(IllegalArgumentException.class, () ->
                ausgabeService.speichereAusgabe(ausgabe)
        );

        verify(ausgabeRepository, never()).save(any());
    }

    // Testet, ob keine Ausgabe gespeichert wird, wenn der Betrag null oder kleiner gleich 0 ist.
    // Erwartet wird eine Exception und kein Speicheraufruf im Repository.
    @Test
    void verhindertSpeichernMitUngueltigemBetrag() {
        Ausgabe ausgabe = new Ausgabe();

        ausgabe.setKategorie(Ausgabenkategorie.REPARATUR);
        ausgabe.setBetrag(BigDecimal.ZERO);
        ausgabe.setDatum(LocalDate.of(2026, 1, 10));
        ausgabe.setImmobilie(mock(Immobilie.class));

        assertThrows(IllegalArgumentException.class, () ->
                ausgabeService.speichereAusgabe(ausgabe)
        );

        verify(ausgabeRepository, never()).save(any());
    }

    // Testet, ob keine Ausgabe gespeichert wird, wenn kein Datum gesetzt ist.
    // Erwartet wird eine Exception und kein Speicheraufruf im Repository.
    @Test
    void verhindertSpeichernOhneDatum() {
        Ausgabe ausgabe = new Ausgabe();

        ausgabe.setKategorie(Ausgabenkategorie.REPARATUR);
        ausgabe.setBetrag(BigDecimal.valueOf(100));
        ausgabe.setImmobilie(mock(Immobilie.class));

        assertThrows(IllegalArgumentException.class, () ->
                ausgabeService.speichereAusgabe(ausgabe)
        );

        verify(ausgabeRepository, never()).save(any());
    }

    // Testet, ob keine Ausgabe gespeichert wird, wenn weder Immobilie noch Mieteinheit gesetzt ist.
    // Erwartet wird eine Exception und kein Speicheraufruf im Repository.
    @Test
    void verhindertSpeichernOhneImmobilieUndMieteinheit() {
        Ausgabe ausgabe = new Ausgabe();

        ausgabe.setKategorie(Ausgabenkategorie.REPARATUR);
        ausgabe.setBetrag(BigDecimal.valueOf(100));
        ausgabe.setDatum(LocalDate.of(2026, 1, 10));

        assertThrows(IllegalArgumentException.class, () ->
                ausgabeService.speichereAusgabe(ausgabe)
        );

        verify(ausgabeRepository, never()).save(any());
    }

    // Testet, ob alle Ausgaben korrekt aus dem Repository gelesen werden.
    @Test
    void findetAlleAusgaben() {
        Ausgabe ausgabe1 = new Ausgabe();
        Ausgabe ausgabe2 = new Ausgabe();

        when(ausgabeRepository.findAll())
                .thenReturn(List.of(ausgabe1, ausgabe2));

        List<Ausgabe> ergebnis = ausgabeService.findeAlleAusgaben();

        assertEquals(2, ergebnis.size());
        verify(ausgabeRepository).findAll();
    }

    // Testet, ob offene Ausgaben korrekt summiert werden.
    // Dabei werden null-Beträge ignoriert.
    @Test
    void berechnetOffeneAusgaben() {
        Ausgabe ausgabe1 = new Ausgabe();
        ausgabe1.setBetrag(BigDecimal.valueOf(100));

        Ausgabe ausgabe2 = new Ausgabe();
        ausgabe2.setBetrag(BigDecimal.valueOf(200));

        Ausgabe ausgabe3 = new Ausgabe();
        ausgabe3.setBetrag(null);

        when(ausgabeRepository.findByStatus("Offen / Ausstehend"))
                .thenReturn(List.of(ausgabe1, ausgabe2, ausgabe3));

        BigDecimal ergebnis = ausgabeService.berechneOffeneAusgaben();

        assertEquals(BigDecimal.valueOf(300), ergebnis);
        verify(ausgabeRepository).findByStatus("Offen / Ausstehend");
    }

    // Testet, ob bezahlte Ausgaben korrekt summiert werden.
    // Dabei werden nur Ausgaben mit dem Status "Bezahlt / Erledigt" verwendet.
    @Test
    void berechnetBezahlteAusgaben() {
        Ausgabe ausgabe1 = new Ausgabe();
        ausgabe1.setBetrag(BigDecimal.valueOf(150));

        Ausgabe ausgabe2 = new Ausgabe();
        ausgabe2.setBetrag(BigDecimal.valueOf(350));

        when(ausgabeRepository.findByStatus("Bezahlt / Erledigt"))
                .thenReturn(List.of(ausgabe1, ausgabe2));

        BigDecimal ergebnis = ausgabeService.berechneBezahlteAusgaben();

        assertEquals(BigDecimal.valueOf(500), ergebnis);
        verify(ausgabeRepository).findByStatus("Bezahlt / Erledigt");
    }

    // Testet, ob die Anzahl offener Ausgaben korrekt aus dem Repository gelesen wird.
    @Test
    void zaehltOffeneAusgaben() {
        when(ausgabeRepository.countByStatus("Offen / Ausstehend"))
                .thenReturn(3L);

        long ergebnis = ausgabeService.zaehleOffeneAusgaben();

        assertEquals(3L, ergebnis);
        verify(ausgabeRepository).countByStatus("Offen / Ausstehend");
    }

    // Testet, ob Ausgaben in einem Zeitraum korrekt summiert werden.
    // Dabei werden null-Beträge ignoriert.
    @Test
    void berechnetAusgabenImZeitraum() {
        LocalDate startDatum = LocalDate.of(2026, 1, 1);
        LocalDate endDatum = LocalDate.of(2026, 1, 31);

        Ausgabe ausgabe1 = new Ausgabe();
        ausgabe1.setBetrag(BigDecimal.valueOf(120));

        Ausgabe ausgabe2 = new Ausgabe();
        ausgabe2.setBetrag(BigDecimal.valueOf(80));

        Ausgabe ausgabe3 = new Ausgabe();
        ausgabe3.setBetrag(null);

        when(ausgabeRepository.findByDatumBetween(startDatum, endDatum))
                .thenReturn(List.of(ausgabe1, ausgabe2, ausgabe3));

        BigDecimal ergebnis =
                ausgabeService.berechneAusgabenImZeitraum(startDatum, endDatum);

        assertEquals(BigDecimal.valueOf(200), ergebnis);
        verify(ausgabeRepository).findByDatumBetween(startDatum, endDatum);
    }

    // Testet, ob bezahlte Ausgaben im Zeitraum mit Immobilienfilter korrekt summiert werden.
    // Es sollen nur bezahlte Ausgaben der gewählten Immobilie berücksichtigt werden.
    @Test
    void berechnetBezahlteAusgabenImZeitraumMitImmobilienFilter() {
        LocalDate startDatum = LocalDate.of(2026, 1, 1);
        LocalDate endDatum = LocalDate.of(2026, 1, 31);

        Immobilie immobilie1 = mock(Immobilie.class);
        when(immobilie1.getId()).thenReturn(1L);

        Immobilie immobilie2 = mock(Immobilie.class);
        when(immobilie2.getId()).thenReturn(2L);

        Ausgabe passendeAusgabe = new Ausgabe();
        passendeAusgabe.setDatum(LocalDate.of(2026, 1, 10));
        passendeAusgabe.setStatus("Bezahlt / Erledigt");
        passendeAusgabe.setBetrag(BigDecimal.valueOf(300));
        passendeAusgabe.setImmobilie(immobilie1);

        Ausgabe falscheImmobilie = new Ausgabe();
        falscheImmobilie.setDatum(LocalDate.of(2026, 1, 10));
        falscheImmobilie.setStatus("Bezahlt / Erledigt");
        falscheImmobilie.setBetrag(BigDecimal.valueOf(500));
        falscheImmobilie.setImmobilie(immobilie2);

        Ausgabe offeneAusgabe = new Ausgabe();
        offeneAusgabe.setDatum(LocalDate.of(2026, 1, 10));
        offeneAusgabe.setStatus("Offen / Ausstehend");
        offeneAusgabe.setBetrag(BigDecimal.valueOf(700));
        offeneAusgabe.setImmobilie(immobilie1);

        when(ausgabeRepository.findAll())
                .thenReturn(List.of(
                        passendeAusgabe,
                        falscheImmobilie,
                        offeneAusgabe
                ));

        BigDecimal ergebnis =
                ausgabeService.berechneBezahlteAusgabenImZeitraum(
                        startDatum,
                        endDatum,
                        1L
                );

        assertEquals(BigDecimal.valueOf(300), ergebnis);
        verify(ausgabeRepository).findAll();
    }

    // Testet, ob die Kostenverteilung im Zeitraum korrekt nach Kategorien gruppiert wird.
    // Zusätzlich wird geprüft, ob technische Enum-Namen in lesbare Kategorien umgewandelt werden.
    @Test
    void berechnetKostenverteilungImZeitraum() {
        LocalDate startDatum = LocalDate.of(2026, 1, 1);
        LocalDate endDatum = LocalDate.of(2026, 1, 31);

        Immobilie immobilie = mock(Immobilie.class);
        when(immobilie.getId()).thenReturn(1L);

        Ausgabe reparatur = new Ausgabe();
        reparatur.setDatum(LocalDate.of(2026, 1, 5));
        reparatur.setKategorie(Ausgabenkategorie.REPARATUR);
        reparatur.setBetrag(BigDecimal.valueOf(200));
        reparatur.setImmobilie(immobilie);

        Ausgabe instandhaltung = new Ausgabe();
        instandhaltung.setDatum(LocalDate.of(2026, 1, 8));
        instandhaltung.setKategorie(Ausgabenkategorie.INSTANDHALTUNG);
        instandhaltung.setBetrag(BigDecimal.valueOf(300));
        instandhaltung.setImmobilie(immobilie);

        Ausgabe ausserhalb = new Ausgabe();
        ausserhalb.setDatum(LocalDate.of(2025, 12, 20));
        ausserhalb.setKategorie(Ausgabenkategorie.REPARATUR);
        ausserhalb.setBetrag(BigDecimal.valueOf(999));
        ausserhalb.setImmobilie(immobilie);

        when(ausgabeRepository.findAll())
                .thenReturn(List.of(
                        reparatur,
                        instandhaltung,
                        ausserhalb
                ));

        Map<String, BigDecimal> ergebnis =
                ausgabeService.berechneKostenverteilungImZeitraum(
                        startDatum,
                        endDatum,
                        1L
                );

        assertEquals(BigDecimal.valueOf(200), ergebnis.get("Reparatur"));
        assertEquals(BigDecimal.valueOf(300), ergebnis.get("Instandhaltung"));
        assertFalse(ergebnis.containsKey("INSTANDHALTUNG"));

        verify(ausgabeRepository).findAll();
    }

    // Testet, ob Ausgaben im Zeitraum mit Immobilienfilter korrekt zurückgegeben werden.
    // Ausgaben außerhalb des Zeitraums oder zu einer anderen Immobilie werden herausgefiltert.
    @Test
    void findetAusgabenImZeitraumMitImmobilienFilter() {
        LocalDate startDatum = LocalDate.of(2026, 1, 1);
        LocalDate endDatum = LocalDate.of(2026, 1, 31);

        Immobilie immobilie1 = mock(Immobilie.class);
        when(immobilie1.getId()).thenReturn(1L);

        Immobilie immobilie2 = mock(Immobilie.class);
        when(immobilie2.getId()).thenReturn(2L);

        Ausgabe passendeAusgabe = new Ausgabe();
        passendeAusgabe.setDatum(LocalDate.of(2026, 1, 15));
        passendeAusgabe.setImmobilie(immobilie1);

        Ausgabe falscheImmobilie = new Ausgabe();
        falscheImmobilie.setDatum(LocalDate.of(2026, 1, 15));
        falscheImmobilie.setImmobilie(immobilie2);

        Ausgabe ausserhalb = new Ausgabe();
        ausserhalb.setDatum(LocalDate.of(2026, 2, 1));
        ausserhalb.setImmobilie(immobilie1);

        when(ausgabeRepository.findAll())
                .thenReturn(List.of(
                        passendeAusgabe,
                        falscheImmobilie,
                        ausserhalb
                ));

        List<Ausgabe> ergebnis =
                ausgabeService.findeAusgabenImZeitraum(
                        startDatum,
                        endDatum,
                        1L
                );

        assertEquals(1, ergebnis.size());
        assertSame(passendeAusgabe, ergebnis.getFirst());

        verify(ausgabeRepository).findAll();
    }

    // Testet, ob eine Ausgabe gelöscht wird.
    // Dabei wird geprüft, ob deleteById im Repository mit der richtigen ID aufgerufen wird.
    @Test
    void loeschtAusgabe() {
        Long id = 1L;

        ausgabeService.loescheAusgabe(id);

        verify(ausgabeRepository).deleteById(id);
    }
}
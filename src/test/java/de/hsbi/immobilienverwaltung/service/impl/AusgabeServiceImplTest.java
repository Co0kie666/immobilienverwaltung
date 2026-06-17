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
}
package de.hsbi.immobilienverwaltung.ui.finanzen;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import de.hsbi.immobilienverwaltung.domain.Ausgabe;
import de.hsbi.immobilienverwaltung.domain.Immobilie;
import de.hsbi.immobilienverwaltung.domain.enums.Ausgabenkategorie;
import de.hsbi.immobilienverwaltung.service.interfaces.AusgabeService;
import de.hsbi.immobilienverwaltung.service.interfaces.MietvertragService;
import de.hsbi.immobilienverwaltung.service.interfaces.ZahlungsEingangService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BuchungListViewTest {

    @Mock
    private AusgabeService ausgabeService;

    @Mock
    private ZahlungsEingangService zahlungsEingangService;

    @Mock
    private MietvertragService mietvertragService;

    @Test
    void sollteKeineBuchungenAnzeigenWennKeineDatenVorhandenSind() {
        // Vorbereitung: Die Services liefern keine Buchungen zurück.
        // Dadurch wird ein leerer Datenbestand simuliert.
        when(ausgabeService.findeAlleAusgaben()).thenReturn(List.of());
        when(zahlungsEingangService.findeAlleZahlungseingaenge()).thenReturn(List.of());

        // Ausführung: Die View wird erstellt.
        // Dabei lädt sie automatisch die Buchungen aus den Services.
        BuchungListView view = new BuchungListView(
                ausgabeService,
                zahlungsEingangService,
                mietvertragService
        );

        // Die privaten UI-Elemente werden per Reflection ausgelesen,
        // damit ihr Zustand im Test überprüft werden kann.
        Span seitenInfo = getPrivateField(view, "seitenInfo", Span.class);
        Button vorherigeSeiteButton = getPrivateField(view, "vorherigeSeiteButton", Button.class);
        Button naechsteSeiteButton = getPrivateField(view, "naechsteSeiteButton", Button.class);

        assertEquals("Keine Buchungen gefunden", seitenInfo.getText());
        assertFalse(vorherigeSeiteButton.isEnabled());
        assertFalse(naechsteSeiteButton.isEnabled());
    }

    @Test
    void sollteBeiMehrAls20BuchungenNurErsteSeiteAnzeigenUndWeiterButtonAktivieren() {
        // Vorbereitung: Es werden 21 Test-Ausgaben erstellt.
        // Da pro Seite nur 20 Buchungen angezeigt werden sollen,
        // muss es eine zweite Seite geben.
        List<Ausgabe> ausgaben = List.of(
                erstelleAusgabe(1L, LocalDate.of(2026, 1, 1)),
                erstelleAusgabe(2L, LocalDate.of(2026, 1, 2)),
                erstelleAusgabe(3L, LocalDate.of(2026, 1, 3)),
                erstelleAusgabe(4L, LocalDate.of(2026, 1, 4)),
                erstelleAusgabe(5L, LocalDate.of(2026, 1, 5)),
                erstelleAusgabe(6L, LocalDate.of(2026, 1, 6)),
                erstelleAusgabe(7L, LocalDate.of(2026, 1, 7)),
                erstelleAusgabe(8L, LocalDate.of(2026, 1, 8)),
                erstelleAusgabe(9L, LocalDate.of(2026, 1, 9)),
                erstelleAusgabe(10L, LocalDate.of(2026, 1, 10)),
                erstelleAusgabe(11L, LocalDate.of(2026, 1, 11)),
                erstelleAusgabe(12L, LocalDate.of(2026, 1, 12)),
                erstelleAusgabe(13L, LocalDate.of(2026, 1, 13)),
                erstelleAusgabe(14L, LocalDate.of(2026, 1, 14)),
                erstelleAusgabe(15L, LocalDate.of(2026, 1, 15)),
                erstelleAusgabe(16L, LocalDate.of(2026, 1, 16)),
                erstelleAusgabe(17L, LocalDate.of(2026, 1, 17)),
                erstelleAusgabe(18L, LocalDate.of(2026, 1, 18)),
                erstelleAusgabe(19L, LocalDate.of(2026, 1, 19)),
                erstelleAusgabe(20L, LocalDate.of(2026, 1, 20)),
                erstelleAusgabe(21L, LocalDate.of(2026, 1, 21))
        );

        // Die Services werden so vorbereitet, dass nur diese 21 Ausgaben zurückgegeben werden.
        when(ausgabeService.findeAlleAusgaben()).thenReturn(ausgaben);
        when(zahlungsEingangService.findeAlleZahlungseingaenge()).thenReturn(List.of());

        // Ausführung: Die View wird erstellt und verarbeitet die Testdaten automatisch.
        BuchungListView view = new BuchungListView(
                ausgabeService,
                zahlungsEingangService,
                mietvertragService
        );

        Span seitenInfo = getPrivateField(view, "seitenInfo", Span.class);
        Button vorherigeSeiteButton = getPrivateField(view, "vorherigeSeiteButton", Button.class);
        Button naechsteSeiteButton = getPrivateField(view, "naechsteSeiteButton", Button.class);

        List<?> gefilterteBuchungen = getPrivateField(view, "gefilterteBuchungen", List.class);

        // Überprüfung: Insgesamt müssen 21 Buchungen vorhanden sein.
        assertEquals(21, gefilterteBuchungen.size());
        // Auf der ersten Seite sollen nur die Buchungen 1 bis 20 angezeigt werden.
        assertEquals("Zeige 1 - 20 von 21 Buchungen", seitenInfo.getText());
// Da wir uns auf der ersten Seite befinden, ist der Zurück-Button deaktiviert.
        assertFalse(vorherigeSeiteButton.isEnabled());
        // Da noch eine zweite Seite existiert, ist der Weiter-Button aktiviert.
        assertTrue(naechsteSeiteButton.isEnabled());
    }

    private Ausgabe erstelleAusgabe(Long id, LocalDate datum) {
        // Hilfsmethode zur Erstellung einer einfachen Test-Ausgabe.
        // Dadurch muss der gleiche Testdatensatz nicht mehrfach im Testcode geschrieben werden.
        Ausgabe ausgabe = new Ausgabe();
        ausgabe.setId(id);
        ausgabe.setDatum(datum);
        ausgabe.setBetrag(BigDecimal.valueOf(100));
        ausgabe.setBeschreibung("Testausgabe " + id);
        ausgabe.setKategorie(Ausgabenkategorie.SONSTIGES);
        ausgabe.setStatus("Bezahlt / Erledigt");

        Immobilie immobilie = new Immobilie();
        immobilie.setBezeichnung("Testimmobilie");

        ausgabe.setImmobilie(immobilie);

        return ausgabe;
    }

    private <T> T getPrivateField(Object object, String fieldName, Class<T> fieldType) {
        try {
            // Da die zu testenden Felder private sind,
            // werden sie hier per Reflection ausgelesen.
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return fieldType.cast(field.get(object));
        } catch (Exception e) {
            throw new RuntimeException("Feld konnte nicht gelesen werden: " + fieldName, e);
        }
    }
}

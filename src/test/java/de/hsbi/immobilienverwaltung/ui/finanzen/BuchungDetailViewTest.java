package de.hsbi.immobilienverwaltung.ui.finanzen;

import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.RouteParameters;
import de.hsbi.immobilienverwaltung.domain.Ausgabe;
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
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BuchungDetailViewTest {

    @Mock
    private AusgabeService ausgabeService;

    @Mock
    private ZahlungsEingangService zahlungsEingangService;

    @Mock
    private MietvertragService mietvertragService;

    @Mock
    private BeforeEnterEvent beforeEnterEvent;

    @Test
    void sollteBeiUngueltigerIdZurBuchungslisteWeiterleiten() {
        // Vorbereitung:
        // Die Route enthält eine ungültige ID.
        // "abc" kann nicht in Long umgewandelt werden.
        RouteParameters routeParameters = new RouteParameters(
                Map.of(
                        "typ", "ausgabe",
                        "id", "abc"
                )
        );

        when(beforeEnterEvent.getRouteParameters()).thenReturn(routeParameters);

        BuchungDetailView view = new BuchungDetailView(
                ausgabeService,
                zahlungsEingangService,
                mietvertragService
        );

        // Ausführung:
        // beforeEnter wird so ausgeführt, als würde der Nutzer die Detailseite öffnen.
        view.beforeEnter(beforeEnterEvent);

        // Überprüfung:
        // Bei einer ungültigen ID soll die View zurück zur Buchungsliste weiterleiten.
        verify(beforeEnterEvent).rerouteTo(BuchungListView.class);

        // Außerdem darf kein Service aufgerufen werden,
        // weil die ID gar nicht gültig ist.
        verifyNoInteractions(ausgabeService);
        verifyNoInteractions(zahlungsEingangService);
    }

    @Test
    void sollteVorhandeneAusgabeLadenWennRouteGueltigIst() {
        // Vorbereitung:
        // Die Route enthält den Typ "ausgabe" und die ID 1.
        RouteParameters routeParameters = new RouteParameters(
                Map.of(
                        "typ", "ausgabe",
                        "id", "1"
                )
        );

        when(beforeEnterEvent.getRouteParameters()).thenReturn(routeParameters);

        Ausgabe ausgabe = erstelleAusgabe();

        // Wenn die View die Ausgabe mit ID 1 sucht,
        // liefert der Mock-Service unsere Test-Ausgabe zurück.
        when(ausgabeService.findeAusgabeNachId(1L)).thenReturn(Optional.of(ausgabe));

        BuchungDetailView view = new BuchungDetailView(
                ausgabeService,
                zahlungsEingangService,
                mietvertragService
        );

        // Ausführung:
        view.beforeEnter(beforeEnterEvent);

        // Überprüfung:
        // Der AusgabeService muss mit der ID 1 aufgerufen worden sein.
        verify(ausgabeService).findeAusgabeNachId(1L);

        // Es darf keine Weiterleitung passieren,
        // weil die Ausgabe gefunden wurde.
        verify(beforeEnterEvent, never()).rerouteTo(BuchungListView.class);

        // Die internen Werte der View werden geprüft.
        assertEquals("ausgabe", getPrivateField(view, "typ", String.class));
        assertEquals(1L, getPrivateField(view, "id", Long.class));
        assertFalse(getPrivateField(view, "bearbeitenAktiv", Boolean.class));

        Ausgabe geladeneAusgabe = getPrivateField(view, "aktuelleAusgabe", Ausgabe.class);

        assertNotNull(geladeneAusgabe);
        assertEquals(1L, geladeneAusgabe.getId());
        assertEquals(BigDecimal.valueOf(250), geladeneAusgabe.getBetrag());
        assertEquals(LocalDate.of(2026, 1, 15), geladeneAusgabe.getDatum());
        assertEquals("Reparatur Heizung", geladeneAusgabe.getBeschreibung());
    }

    private Ausgabe erstelleAusgabe() {
        // Hilfsmethode zur Erstellung einer einfachen Test-Ausgabe.
        Ausgabe ausgabe = new Ausgabe();
        ausgabe.setId(1L);
        ausgabe.setBetrag(BigDecimal.valueOf(250));
        ausgabe.setDatum(LocalDate.of(2026, 1, 15));
        ausgabe.setFaelligkeitsdatum(LocalDate.of(2026, 1, 20));
        ausgabe.setKategorie(Ausgabenkategorie.SONSTIGES);
        ausgabe.setStatus("Offen / Ausstehend");
        ausgabe.setBeschreibung("Reparatur Heizung");

        return ausgabe;
    }

    private <T> T getPrivateField(Object object, String fieldName, Class<T> fieldType) {
        try {
            // Private Felder werden hier per Reflection ausgelesen,
            // damit der interne Zustand der View im Test geprüft werden kann.
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return fieldType.cast(field.get(object));
        } catch (Exception e) {
            throw new RuntimeException("Feld konnte nicht gelesen werden: " + fieldName, e);
        }
    }
}
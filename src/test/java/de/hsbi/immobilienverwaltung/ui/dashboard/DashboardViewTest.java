package de.hsbi.immobilienverwaltung.ui.dashboard;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import de.hsbi.immobilienverwaltung.service.interfaces.AusgabeService;
import de.hsbi.immobilienverwaltung.service.interfaces.GesamtAuswertungService;
import de.hsbi.immobilienverwaltung.service.interfaces.ZahlungsEingangService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DashboardViewTest {

    private GesamtAuswertungService gesamtAuswertungService;
    private ZahlungsEingangService zahlungsEingangService;
    private AusgabeService ausgabeService;

    @BeforeEach
    void setUp() {
        UI.setCurrent(new UI());

        gesamtAuswertungService = mock(GesamtAuswertungService.class);
        zahlungsEingangService = mock(ZahlungsEingangService.class);
        ausgabeService = mock(AusgabeService.class);

        when(gesamtAuswertungService.berechneAnzahlMieteinheiten())
                .thenReturn(10L);

        when(gesamtAuswertungService.berechneAnzahlLeerstehendeMieteinheiten())
                .thenReturn(2L);

        when(gesamtAuswertungService.berechneLeerstandsquote())
                .thenReturn(20.0);

        when(gesamtAuswertungService.berechneAnzahlAktiveVertraege())
                .thenReturn(8L);

        when(zahlungsEingangService.berechneGesamteBezahlteZahlungseingaenge())
                .thenReturn(BigDecimal.valueOf(12500));

        when(zahlungsEingangService.berechneBezahlteZahlungseingaengeImZeitraum(
                any(), any(), any(), any(), any()
        )).thenReturn(BigDecimal.ZERO);

        when(ausgabeService.berechneOffeneAusgaben())
                .thenReturn(BigDecimal.valueOf(750));

        when(ausgabeService.zaehleOffeneAusgaben())
                .thenReturn(3L);

        when(ausgabeService.berechneAusgabenImZeitraum(
                any(), any()
        )).thenReturn(BigDecimal.ZERO);
    }

    // Prüft ob der Seitentitel und der Untertitel der DashboardView korrekt zurückgegeben werden.
    @Test
    void zeigtKorrektenSeitentitelUndUntertitel() {
        DashboardView view = createView();

        assertThat(view.getPageTitle())
                .isEqualTo("Dashboard");

        assertThat(view.getPageSubtitle())
                .isEqualTo("Zentrale Übersicht und KPIs");
    }

    // Prüft ob die DashboardView die wichtigsten KPI-Bereiche rendert, wenn Backend-Daten vorhanden sind.
    @Test
    void zeigtDashboardKpiBereicheWennBackendDatenVorhandenSind() {
        DashboardView view = createView();

        assertThat(enthaeltText(view, "Gesamteinnahmen"))
                .isTrue();

        assertThat(enthaeltText(view, "Leerstandsquote"))
                .isTrue();

        assertThat(enthaeltText(view, "Offene Zahlungen"))
                .isTrue();

        assertThat(enthaeltText(view, "Aktive Verträge"))
                .isTrue();
    }

    private DashboardView createView() {
        return new DashboardView(
                gesamtAuswertungService,
                zahlungsEingangService,
                ausgabeService
        );
    }

    private boolean enthaeltText(Component component, String text) {
        String elementText = component.getElement().getText();

        if (elementText != null && elementText.contains(text)) {
            return true;
        }

        return component.getChildren()
                .anyMatch(child -> enthaeltText(child, text));
    }
}
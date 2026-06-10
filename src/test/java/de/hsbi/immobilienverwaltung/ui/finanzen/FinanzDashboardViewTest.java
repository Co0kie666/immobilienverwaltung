package de.hsbi.immobilienverwaltung.ui.finanzen;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import de.hsbi.immobilienverwaltung.service.interfaces.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FinanzDashboardViewTest {

    private ZahlungsEingangService zahlungsEingangService;
    private AusgabeService ausgabeService;
    private ImmobilieService immobilieService;
    private MieteinheitService mieteinheitService;
    private MieterService mieterService;
    private MietvertragService mietvertragService;

    @BeforeEach
    void setUp() {
        UI.setCurrent(new UI());

        zahlungsEingangService = mock(ZahlungsEingangService.class);
        ausgabeService = mock(AusgabeService.class);
        immobilieService = mock(ImmobilieService.class);
        mieteinheitService = mock(MieteinheitService.class);
        mieterService = mock(MieterService.class);

        when(immobilieService.findeAlleImmobilien()).thenReturn(List.of());
        when(mieteinheitService.findeAlleMieteinheiten()).thenReturn(List.of());
        when(mieterService.findeAlleMieter()).thenReturn(List.of());

        when(zahlungsEingangService.berechneBezahlteZahlungseingaengeImZeitraum(
                any(), any(), any(), any(), any()
        )).thenReturn(BigDecimal.ZERO);

        when(zahlungseingangServiceBerechneOffene()).thenReturn(BigDecimal.ZERO);

        when(ausgabeService.berechneBezahlteAusgabenImZeitraum(
                any(), any(), any()
        )).thenReturn(BigDecimal.ZERO);

        when(ausgabeService.berechneKostenverteilungImZeitraum(
                any(), any(), any()
        )).thenReturn(Map.of());
    }

    private BigDecimal zahlungseingangServiceBerechneOffene() {
        return zahlungsEingangService.berechneOffeneZahlungseingaengeImZeitraum(
                any(), any(), any(), any(), any()
        );
    }

    // Prüft ob der Seitentitel und der Untertitel der FinanzDashboardView korrekt zurückgegeben werden.
    @Test
    void zeigtKorrektenSeitentitelUndUntertitel() {
        FinanzDashboardView view = createView();

        assertThat(view.getPageTitle())
                .isEqualTo("Finanz-Dashboard");

        assertThat(view.getPageSubtitle())
                .isEqualTo("Übersicht über Einnahmen, Ausgaben und Cashflow");
    }

    // Prüft ob die Kostenverteilungs-Card angezeigt wird, wenn das Backend Kostenverteilungsdaten liefert.
    @Test
    void zeigtKostenverteilungWennBackendDatenVorhandenSind() {
        when(ausgabeService.berechneKostenverteilungImZeitraum(
                any(), any(), any()
        )).thenReturn(Map.of(
                "INSTANDHALTUNG", BigDecimal.valueOf(240),
                "REPARATUR", BigDecimal.valueOf(390)
        ));

        FinanzDashboardView view = createView();

        assertThat(enthaeltText(view, "Kostenverteilung"))
                .isTrue();
    }

    private FinanzDashboardView createView() {
        return new FinanzDashboardView(
                zahlungsEingangService,
                ausgabeService,
                immobilieService,
                mieteinheitService,
                mieterService,
                mietvertragService
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
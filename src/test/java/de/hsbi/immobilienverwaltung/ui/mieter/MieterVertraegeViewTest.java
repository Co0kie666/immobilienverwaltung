package de.hsbi.immobilienverwaltung.ui.mieter;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import de.hsbi.immobilienverwaltung.domain.Mieter;
import de.hsbi.immobilienverwaltung.domain.Mietvertrag;
import de.hsbi.immobilienverwaltung.service.interfaces.MieterService;
import de.hsbi.immobilienverwaltung.service.interfaces.MietvertragService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class MieterVertraegeViewTest {

    @Mock
    private MieterService mieterService;

    @Mock
    private MietvertragService mietvertragService;

    @BeforeEach
    void setUp() {
        UI.setCurrent(new UI());

        when(mieterService.findeAlleMieter())
                .thenReturn(List.of());

        when(mietvertragService.findeAlleMietvertraege())
                .thenReturn(List.of());
    }

    // Prüft, ob Seitentitel und Untertitel korrekt gesetzt sind.
    @Test
    void zeigtKorrektenSeitentitelUndUntertitel() {
        MieterVertraegeView view = new MieterVertraegeView(
                mieterService,
                mietvertragService
        );

        assertEquals("Mieter & Verträge", view.getPageTitle());
        assertEquals("Mieter, laufende Mietverhältnisse und Vertragsarchiv verwalten", view.getPageSubtitle());
    }

    // Prüft, ob beim Öffnen standardmäßig die Mieter-Tabelle sichtbar ist
    // und die Mietvertrag-Tabelle ausgeblendet bleibt.
    @Test
    void zeigtStandardmaessigMieterTabelleUndVerstecktVertragstabelle() {
        MieterVertraegeView view = new MieterVertraegeView(
                mieterService,
                mietvertragService
        );

        Grid<Mieter> mieterGrid =
                getPrivateField(view, "mieterGrid", Grid.class);

        Grid<Mietvertrag> mietvertragGrid =
                getPrivateField(view, "mietvertragGrid", Grid.class);

        assertTrue(mieterGrid.isVisible());
        assertFalse(mietvertragGrid.isVisible());
    }

    private <T> T getPrivateField(Object object, String fieldName, Class<T> fieldType) {
        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return fieldType.cast(field.get(object));
        } catch (Exception e) {
            throw new RuntimeException("Feld konnte nicht gelesen werden: " + fieldName, e);
        }
    }
}
package de.hsbi.immobilienverwaltung.ui.mieter;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import de.hsbi.immobilienverwaltung.domain.Mieter;
import de.hsbi.immobilienverwaltung.service.interfaces.MieterService;
import de.hsbi.immobilienverwaltung.service.interfaces.MietvertragService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MieterListViewTest {

    @Mock
    private MieterService mieterService;

    @Mock
    private MietvertragService mietvertragService;

    @BeforeEach
    void setUp() {
        UI.setCurrent(new UI());
    }

    // Prüft, ob eine Fehlermeldung angezeigt wird,
    // wenn für die übergebene ID kein Mieter gefunden wird.
    @Test
    void zeigtNotFoundCardWennMieterNichtGefundenWird() {
        when(mieterService.findeMieterNachId(99L))
                .thenReturn(Optional.empty());

        MieterListView view = new MieterListView(mieterService, mietvertragService);

        view.setParameter(null, "99");

        assertTrue(enthaeltText(view, "Mieter nicht gefunden"));
    }

    // Prüft, ob die Detailseite die Daten eines gefundenen Mieters anzeigt.
    @Test
    void zeigtMieterDatenWennMieterGefundenWird() {
        Mieter mieter = new Mieter();
        ReflectionTestUtils.setField(mieter, "id", 1L);
        mieter.setVorname("Max");
        mieter.setNachname("Mustermann");
        mieter.setEmail("max@test.de");

        when(mieterService.findeMieterNachId(1L))
                .thenReturn(Optional.of(mieter));

        when(mietvertragService.findeMietvertraegeNachMieter(1L))
                .thenReturn(List.of());

        MieterListView view = new MieterListView(mieterService, mietvertragService);

        view.setParameter(null, "1");

        assertTrue(enthaeltText(view, "Max Mustermann"));
        assertTrue(enthaeltText(view, "max@test.de"));
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
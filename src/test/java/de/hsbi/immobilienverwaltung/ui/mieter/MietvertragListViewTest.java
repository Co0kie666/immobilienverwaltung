package de.hsbi.immobilienverwaltung.ui.mieter;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import de.hsbi.immobilienverwaltung.domain.Immobilie;
import de.hsbi.immobilienverwaltung.domain.Mieteinheit;
import de.hsbi.immobilienverwaltung.domain.Mieter;
import de.hsbi.immobilienverwaltung.domain.Mietvertrag;
import de.hsbi.immobilienverwaltung.domain.enums.Vertragsstatus;
import de.hsbi.immobilienverwaltung.service.interfaces.MietvertragService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MietvertragListViewTest {

    @Mock
    private MietvertragService mietvertragService;

    @BeforeEach
    void setUp() {
        UI.setCurrent(new UI());
    }

    // Prüft, ob eine Fehlermeldung angezeigt wird,
    // wenn für die übergebene ID kein Mietvertrag gefunden wird.
    @Test
    void zeigtNotFoundCardWennMietvertragNichtGefundenWird() {
        when(mietvertragService.findeMietvertragNachId(99L))
                .thenReturn(Optional.empty());

        MietvertragListView view = new MietvertragListView(mietvertragService);

        view.setParameter(null, "99");

        assertTrue(enthaeltText(view, "Mietvertrag nicht gefunden"));
    }

    // Prüft, ob die Detailseite wichtige Vertragsdaten anzeigt,
    // wenn ein Mietvertrag gefunden wird.
    @Test
    void zeigtMietvertragsdatenWennMietvertragGefundenWird() {
        Mietvertrag mietvertrag = erstelleMietvertrag();

        when(mietvertragService.findeMietvertragNachId(5L))
                .thenReturn(Optional.of(mietvertrag));

        MietvertragListView view = new MietvertragListView(mietvertragService);

        view.setParameter(null, "5");

        assertTrue(enthaeltText(view, "MV-5"));
        assertTrue(enthaeltText(view, "Max Mustermann"));
        assertTrue(enthaeltText(view, "Wohnung 1"));
    }

    private Mietvertrag erstelleMietvertrag() {
        Mieter mieter = new Mieter();
        ReflectionTestUtils.setField(mieter, "id", 1L);
        mieter.setVorname("Max");
        mieter.setNachname("Mustermann");
        mieter.setEmail("max@test.de");

        Immobilie immobilie = new Immobilie();
        ReflectionTestUtils.setField(immobilie, "id", 2L);
        immobilie.setBezeichnung("Testimmobilie");

        Mieteinheit mieteinheit = new Mieteinheit();
        ReflectionTestUtils.setField(mieteinheit, "id", 3L);
        mieteinheit.setBezeichnung("Wohnung 1");
        mieteinheit.setImmobilie(immobilie);

        Mietvertrag mietvertrag = new Mietvertrag();
        ReflectionTestUtils.setField(mietvertrag, "id", 5L);
        mietvertrag.setMieter(mieter);
        mietvertrag.setMieteinheit(mieteinheit);
        mietvertrag.setStartdatum(LocalDate.of(2026, 1, 1));
        mietvertrag.setKaltmiete(700.0);
        mietvertrag.setNebenkosten(150.0);
        mietvertrag.setKaution(1400.0);
        mietvertrag.setStatus(Vertragsstatus.AKTIV);

        return mietvertrag;
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
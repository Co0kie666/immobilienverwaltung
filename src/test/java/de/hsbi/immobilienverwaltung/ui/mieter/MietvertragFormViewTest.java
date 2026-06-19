package de.hsbi.immobilienverwaltung.ui.mieter;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.NumberField;
import de.hsbi.immobilienverwaltung.service.interfaces.ImmobilieService;
import de.hsbi.immobilienverwaltung.service.interfaces.MieteinheitService;
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
class MietvertragFormViewTest {

    @Mock
    private MieterService mieterService;

    @Mock
    private ImmobilieService immobilieService;

    @Mock
    private MieteinheitService mieteinheitService;

    @Mock
    private MietvertragService mietvertragService;

    @BeforeEach
    void setUp() {
        UI.setCurrent(new UI());

        when(mieterService.findeAlleMieter())
                .thenReturn(List.of());

        when(immobilieService.findeAlleImmobilien())
                .thenReturn(List.of());
    }

    // Prüft, ob Seitentitel und Untertitel korrekt gesetzt sind.
    @Test
    void zeigtKorrektenSeitentitelUndUntertitel() {
        MietvertragFormView view = createView();

        assertEquals("Mietvertrag anlegen", view.getPageTitle());
        assertEquals("Neuen Mietvertrag erfassen", view.getPageSubtitle());
    }

    // Prüft, ob die Warmmiete in der Vorschau automatisch
    // aus Kaltmiete und Nebenkosten berechnet wird.
    @Test
    void berechnetWarmmieteInDerVorschau() {
        MietvertragFormView view = createView();

        NumberField kaltmieteField =
                getPrivateField(view, "kaltmieteField", NumberField.class);

        NumberField nebenkostenField =
                getPrivateField(view, "nebenkostenField", NumberField.class);

        Span previewWarmmiete =
                getPrivateField(view, "previewWarmmiete", Span.class);

        kaltmieteField.setValue(700.0);
        nebenkostenField.setValue(150.0);

        assertTrue(previewWarmmiete.getText().contains("850"));
    }

    private MietvertragFormView createView() {
        return new MietvertragFormView(
                mieterService,
                immobilieService,
                mieteinheitService,
                mietvertragService
        );
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
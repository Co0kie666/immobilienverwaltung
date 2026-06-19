package de.hsbi.immobilienverwaltung.ui.mieter;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.textfield.TextField;
import de.hsbi.immobilienverwaltung.service.interfaces.MieterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class MieterFormViewTest {

    @Mock
    private MieterService mieterService;

    @BeforeEach
    void setUp() {
        UI.setCurrent(new UI());
    }

    // Prüft, ob Seitentitel und Untertitel korrekt gesetzt sind.
    @Test
    void zeigtKorrektenSeitentitelUndUntertitel() {
        MieterFormView view = new MieterFormView(mieterService);

        assertEquals("Mieter anlegen", view.getPageTitle());
        assertEquals("Neuen Mieter erfassen", view.getPageSubtitle());
    }

    // Prüft, ob die Bankdatenfelder erst aktiviert werden,
    // wenn die Checkbox "Bankdaten erfassen" aktiv ist.
    @Test
    void aktiviertBankdatenfelderWennBankdatenErfasstWerden() {
        MieterFormView view = new MieterFormView(mieterService);

        Checkbox bankdatenAktiv =
                getPrivateField(view, "bankdatenAktiv", Checkbox.class);

        TextField kontoinhaberField =
                getPrivateField(view, "kontoinhaberField", TextField.class);

        TextField ibanField =
                getPrivateField(view, "ibanField", TextField.class);

        assertFalse(kontoinhaberField.isEnabled());
        assertFalse(ibanField.isEnabled());

        bankdatenAktiv.setValue(true);

        assertTrue(kontoinhaberField.isEnabled());
        assertTrue(ibanField.isEnabled());
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
package de.hsbi.immobilienverwaltung.ui.finanzen;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import de.hsbi.immobilienverwaltung.domain.enums.Ausgabenkategorie;
import de.hsbi.immobilienverwaltung.domain.enums.Zahlungseingangtyp;
import de.hsbi.immobilienverwaltung.service.interfaces.AusgabeService;
import de.hsbi.immobilienverwaltung.service.interfaces.ImmobilieService;
import de.hsbi.immobilienverwaltung.service.interfaces.MieteinheitService;
import de.hsbi.immobilienverwaltung.service.interfaces.MietvertragService;
import de.hsbi.immobilienverwaltung.service.interfaces.ZahlungsEingangService;
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
class BuchungFormViewTest {

    @Mock
    private AusgabeService ausgabeService;

    @Mock
    private ImmobilieService immobilieService;

    @Mock
    private MieteinheitService mieteinheitService;

    @Mock
    private ZahlungsEingangService zahlungsEingangService;

    @Mock
    private MietvertragService mietvertragService;

    @Test
    void sollteStandardmaessigEinnahmeAnzeigen() {
        // Vorbereitung:
        // Die View lädt beim Erstellen alle Immobilien.
        // Für diesen Test reicht eine leere Liste.
        when(immobilieService.findeAlleImmobilien()).thenReturn(List.of());

        // Ausführung:
        // Die BuchungFormView wird erstellt.
        BuchungFormView view = new BuchungFormView(
                ausgabeService,
                immobilieService,
                mieteinheitService,
                zahlungsEingangService,
                mietvertragService
        );

        RadioButtonGroup<String> buchungstypGroup =
                getPrivateField(view, "buchungstypGroup", RadioButtonGroup.class);

        ComboBox<Ausgabenkategorie> kategorieField =
                getPrivateField(view, "kategorieField", ComboBox.class);

        ComboBox<Zahlungseingangtyp> zahlungseingangTypField =
                getPrivateField(view, "zahlungseingangTypField", ComboBox.class);

        // Überprüfung:
        // Standardmäßig ist "Einnahme" ausgewählt.
        assertEquals("Einnahme", buchungstypGroup.getValue());

        // Bei Einnahmen wird das Zahlungseingangtyp-Feld angezeigt.
        assertTrue(zahlungseingangTypField.isVisible());

        // Das Ausgabenkategorie-Feld ist bei Einnahmen ausgeblendet.
        assertFalse(kategorieField.isVisible());
    }

    @Test
    void sollteBeiAusgabeKategorieAnzeigenUndZahlungstypAusblenden() {
        // Vorbereitung:
        when(immobilieService.findeAlleImmobilien()).thenReturn(List.of());

        BuchungFormView view = new BuchungFormView(
                ausgabeService,
                immobilieService,
                mieteinheitService,
                zahlungsEingangService,
                mietvertragService
        );

        RadioButtonGroup<String> buchungstypGroup =
                getPrivateField(view, "buchungstypGroup", RadioButtonGroup.class);

        ComboBox<Ausgabenkategorie> kategorieField =
                getPrivateField(view, "kategorieField", ComboBox.class);

        ComboBox<Zahlungseingangtyp> zahlungseingangTypField =
                getPrivateField(view, "zahlungseingangTypField", ComboBox.class);

        // Ausführung:
        // Der Buchungstyp wird im Test auf "Ausgabe" geändert.
        buchungstypGroup.setValue("Ausgabe");

        // Die private Methode aktualisiereKategorieFelder()
        // wird aufgerufen, damit die Sichtbarkeit der Felder aktualisiert wird.
        invokePrivateMethod(view, "aktualisiereKategorieFelder");

        // Überprüfung:
        // Bei Ausgabe soll die Ausgabenkategorie sichtbar sein.
        assertTrue(kategorieField.isVisible());

        // Der Zahlungseingangtyp soll bei Ausgabe ausgeblendet sein.
        assertFalse(zahlungseingangTypField.isVisible());
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

    private void invokePrivateMethod(Object object, String methodName) {
        try {
            var method = object.getClass().getDeclaredMethod(methodName);
            method.setAccessible(true);
            method.invoke(object);
        } catch (Exception e) {
            throw new RuntimeException("Methode konnte nicht aufgerufen werden: " + methodName, e);
        }
    }
}
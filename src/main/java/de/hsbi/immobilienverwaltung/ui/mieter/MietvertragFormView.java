package de.hsbi.immobilienverwaltung.ui.mieter;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import de.hsbi.immobilienverwaltung.security.LoginRequired;
import de.hsbi.immobilienverwaltung.ui.layout.HasPageHeader;
import de.hsbi.immobilienverwaltung.ui.layout.MainLayout;
import com.vaadin.flow.data.value.ValueChangeMode;

import java.text.NumberFormat;
import java.util.Locale;

@Route(value = "mietvertrag-anlegen", layout = MainLayout.class)
public class MietvertragFormView extends Div implements HasPageHeader, LoginRequired {

    public MietvertragFormView() {
        addClassName("page-content");

        Div pageWrapper = new Div();
        pageWrapper.addClassName("form-page-wrapper");

        pageWrapper.add(createFormContent());

        add(pageWrapper);
    }

    // Erstellt den Formularbereich
    private Component createFormContent() {
        Div content = new Div();
        content.setWidthFull();

        Div topCards = new Div();
        topCards.addClassName("form-grid-two-columns");

        topCards.add(
                createMietobjektMieterCard(),
                createVertragsdatenCard()
        );

        Div fullWidthArea = new Div();
        fullWidthArea.addClassName("form-grid-full-width");
        fullWidthArea.add(createFinanzielleDetailsCard());

        content.add(
                topCards,
                fullWidthArea,
                createBottomActions()
        );

        return content;
    }

    // Card für Mietobjekt und Mieter
    private Component createMietobjektMieterCard() {
        Select<String> mieter = new Select<>();
        mieter.setLabel("Mieter auswählen");
        mieter.setItems(
                "Max Mustermann",
                "Sarah Müller",
                "Julia Müller",
                "Lukas Becker",
                "Aylin Yildiz",
                "Jonas Weber"
        );
        mieter.setPlaceholder("Bitte wählen...");
        mieter.setRequiredIndicatorVisible(true);
        mieter.setWidthFull();

        Select<String> immobilie = new Select<>();
        immobilie.setLabel("Immobilie");
        immobilie.setItems(
                "Parkresidenz Süd",
                "Stadthaus Mitte",
                "Wohnpark Nord",
                "Campus Apartments",
                "Altbau Rosenstraße"
        );
        immobilie.setPlaceholder("Bitte wählen...");
        immobilie.setRequiredIndicatorVisible(true);
        immobilie.setWidthFull();

        Select<String> wohneinheit = new Select<>();
        wohneinheit.setLabel("Wohneinheit");
        wohneinheit.setItems(
                "WE-01",
                "WE-03",
                "WE-05",
                "WE-08",
                "WE-12",
                "WE-21"
        );
        wohneinheit.setPlaceholder("Zuerst Immobilie wählen...");
        wohneinheit.setRequiredIndicatorVisible(true);
        wohneinheit.setWidthFull();

        FormLayout form = createTwoColumnFormLayout();

        form.add(mieter, immobilie, wohneinheit);
        form.setColspan(mieter, 2);

        return createFormCard(
                "Mietobjekt & Mieter",
                "Zuweisung des Vertrags",
                form
        );
    }

    // Card für Vertragsdaten
    private Component createVertragsdatenCard() {
        DatePicker vertragsbeginn = createDatePicker("Vertragsbeginn", true);
        DatePicker vertragsende = createDatePicker("Vertragsende (Optional)", false);

        Select<String> kuendigungsfrist = new Select<>();
        kuendigungsfrist.setLabel("Kündigungsfrist");
        kuendigungsfrist.setItems(
                "Gesetzlich (3 Monate)",
                "1 Monat",
                "6 Monate",
                "Individuell"
        );
        kuendigungsfrist.setValue("Gesetzlich (3 Monate)");
        kuendigungsfrist.setWidthFull();

        Select<String> zahlungsintervall = new Select<>();
        zahlungsintervall.setLabel("Zahlungsintervall");
        zahlungsintervall.setItems(
                "Monatlich",
                "Vierteljährlich",
                "Jährlich"
        );
        zahlungsintervall.setValue("Monatlich");
        zahlungsintervall.setWidthFull();

        FormLayout form = createTwoColumnFormLayout();

        form.add(
                vertragsbeginn,
                vertragsende,
                kuendigungsfrist,
                zahlungsintervall
        );

        return createFormCard(
                "Vertragsdaten",
                "Laufzeit und Fristen",
                form
        );
    }

    // Card für finanzielle Details
    private Component createFinanzielleDetailsCard() {
        TextField kaltmiete = createMoneyField("Kaltmiete", "0.00", true);
        TextField nebenkosten = createMoneyField("Nebenkosten-Vorauszahlung", "0.00", true);

        TextField kaution = createMoneyField("Kaution", "0.00", false);

        Select<String> kautionsart = new Select<>();
        kautionsart.setLabel("Kautionsart");
        kautionsart.setItems(
                "Barkaution / Überweisung",
                "Bankbürgschaft",
                "Keine Kaution"
        );
        kautionsart.setValue("Barkaution / Überweisung");
        kautionsart.setWidthFull();

        Div warmmieteBox = new Div();
        warmmieteBox.addClassName("money-summary-box");

        Div warmmieteRow = new Div();
        warmmieteRow.addClassName("money-summary-row");

        Span warmmieteLabel = new Span("Warmmiete (Gesamt)");
        warmmieteLabel.addClassName("money-summary-label");

        Span warmmieteValue = new Span("0,00 €");
        warmmieteValue.addClassName("money-summary-value");

        warmmieteRow.add(warmmieteLabel, warmmieteValue);
        warmmieteBox.add(warmmieteRow);

        // Berechnet die Warmmiete direkt bei Eingabe
        configureWarmmieteCalculation(kaltmiete, nebenkosten, warmmieteValue);

        FormLayout form = createTwoColumnFormLayout();

        form.add(
                kaltmiete,
                nebenkosten,
                kaution,
                kautionsart,
                warmmieteBox
        );
        form.setColspan(warmmieteBox, 2);

        return createFormCard(
                "Finanzielle Details",
                "Miete, Nebenkosten und Kaution",
                form
        );
    }

    // Berechnet die Warmmiete automatisch aus Kaltmiete und Nebenkosten
    private void configureWarmmieteCalculation(
            TextField kaltmieteField,
            TextField nebenkostenField,
            Span warmmieteValue
    ) {
        kaltmieteField.setValueChangeMode(ValueChangeMode.EAGER);
        nebenkostenField.setValueChangeMode(ValueChangeMode.EAGER);

        kaltmieteField.addValueChangeListener(event ->
                updateWarmmiete(kaltmieteField, nebenkostenField, warmmieteValue)
        );

        nebenkostenField.addValueChangeListener(event ->
                updateWarmmiete(kaltmieteField, nebenkostenField, warmmieteValue)
        );

        updateWarmmiete(kaltmieteField, nebenkostenField, warmmieteValue);
    }

    // Aktualisiert den angezeigten Warmmiete-Wert
    private void updateWarmmiete(
            TextField kaltmieteField,
            TextField nebenkostenField,
            Span warmmieteValue
    ) {
        double kaltmiete = parseMoneyValue(kaltmieteField.getValue());
        double nebenkosten = parseMoneyValue(nebenkostenField.getValue());

        double warmmiete = kaltmiete + nebenkosten;

        warmmieteValue.setText(formatMoneyValue(warmmiete));
    }

    // Wandelt Texte wie 950,00 oder 950.00 in eine Zahl um
    private double parseMoneyValue(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }

        try {
            String normalizedValue = value
                    .replace("€", "")
                    .replace(" ", "")
                    .replace(".", "")
                    .replace(",", ".");

            return Double.parseDouble(normalizedValue);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // Formatiert die Zahl wieder als Euro-Betrag
    private String formatMoneyValue(double value) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.GERMANY);
        return formatter.format(value);
    }

    // Button-Leiste unten rechts
    private Component createBottomActions() {
        HorizontalLayout actions = new HorizontalLayout();
        actions.addClassName("form-bottom-actions");

        Button cancelButton = new Button("Abbrechen", VaadinIcon.CLOSE.create());
        cancelButton.addClassName("secondary-button");

        // Zurück zur Vertragsübersicht
        cancelButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate("mieter-vertraege?tab=vertraege"))
        );

        Button saveButton = new Button("Mietvertrag anlegen", VaadinIcon.PLUS.create());
        saveButton.addClassName("primary-button");

        actions.add(cancelButton, saveButton);

        return actions;
    }

    // Erstellt eine Form-Card mit Titel, Untertitel und Inhalt
    private Div createFormCard(String titleText, String subtitleText, Component content) {
        Div card = new Div();
        card.addClassName("form-card");
        card.setWidthFull();

        Div header = new Div();
        header.addClassName("form-card-header");

        Span title = new Span(titleText);
        title.addClassName("form-card-title");

        Span subtitle = new Span(subtitleText);
        subtitle.addClassName("form-card-subtitle");

        Div titleArea = new Div();
        titleArea.add(title, subtitle);

        header.add(titleArea);

        Div body = new Div();
        body.addClassName("form-card-content");
        body.add(content);

        card.add(header, body);

        return card;
    }

    // Erstellt ein zweispaltiges Formular
    private FormLayout createTwoColumnFormLayout() {
        FormLayout form = new FormLayout();
        form.addClassName("form-layout-two-columns");

        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2)
        );

        return form;
    }

    // Erstellt ein Datumsfeld
    private DatePicker createDatePicker(String label, boolean required) {
        DatePicker datePicker = new DatePicker(label);
        datePicker.setPlaceholder("tt.mm.jjjj");
        datePicker.setRequiredIndicatorVisible(required);
        datePicker.setWidthFull();

        return datePicker;
    }

    // Erstellt ein Euro-Textfeld
    private TextField createMoneyField(String label, String placeholder, boolean required) {
        TextField field = new TextField(label);
        field.setPlaceholder(placeholder);
        field.setRequiredIndicatorVisible(required);
        field.setSuffixComponent(new Span("€"));
        field.setWidthFull();

        return field;
    }

    @Override
    public String getPageTitle() {
        return "Mietvertrag anlegen";
    }

    @Override
    public String getPageSubtitle() {
        return "Neuen Mietvertrag erfassen";
    }
}
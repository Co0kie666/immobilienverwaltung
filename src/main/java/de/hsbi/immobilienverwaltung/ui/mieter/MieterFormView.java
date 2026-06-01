package de.hsbi.immobilienverwaltung.ui.mieter;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
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

import java.util.List;

@Route(value = "mieter-anlegen", layout = MainLayout.class)
public class MieterFormView extends Div implements HasPageHeader, LoginRequired {

    public MieterFormView() {
        addClassName("page-content");

        Div pageWrapper = new Div();
        pageWrapper.setWidthFull();
        pageWrapper.getStyle().set("max-width", "1180px");
        pageWrapper.getStyle().set("margin", "0 auto");

        pageWrapper.add(createFormContent());

        add(pageWrapper);
    }


    // Erstellt den Formularbereich
    private Component createFormContent() {
        Div content = new Div();
        content.setWidthFull();

        Div topCards = new Div();
        topCards.setWidthFull();
        topCards.getStyle().set("display", "grid");
        topCards.getStyle().set("grid-template-columns", "repeat(2, minmax(0, 1fr))");
        topCards.getStyle().set("gap", "28px");
        topCards.getStyle().set("margin-bottom", "28px");

        topCards.add(
                createPersonendatenCard(),
                createKontaktAdresseCard()
        );

        content.add(
                topCards,
                createBankverbindungCard(),
                createBottomActions()
        );

        return content;
    }

    // Button-Leiste unten rechts
    private Component createBottomActions() {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setWidthFull();
        actions.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        actions.setAlignItems(FlexComponent.Alignment.CENTER);
        actions.setSpacing(true);
        actions.getStyle().set("margin-top", "28px");

        Button cancelButton = new Button("Abbrechen", VaadinIcon.CLOSE.create());
        cancelButton.addClassName("secondary-button");

        // Zurück zur Mieter-Übersicht
        cancelButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate("mieter-vertraege?tab=mieter"))
        );

        Button saveButton = new Button("Mieter anlegen", VaadinIcon.PLUS.create());
        saveButton.addClassName("primary-button");

        actions.add(cancelButton, saveButton);

        return actions;
    }

    // Card für Personendaten
    private Component createPersonendatenCard() {
        Select<String> anrede = new Select<>();
        anrede.setLabel("Anrede");
        anrede.setItems("Herr", "Frau", "Divers");
        anrede.setPlaceholder("Bitte wählen...");
        anrede.setWidthFull();

        TextField titel = createTextField("Titel (Optional)", "z.B. Dr.", false);

        TextField vorname = createTextField("Vorname", "Max", true);
        TextField nachname = createTextField("Nachname", "Mustermann", true);

        DatePicker geburtsdatum = new DatePicker("Geburtsdatum");
        geburtsdatum.setPlaceholder("tt.mm.jjjj");
        geburtsdatum.setWidthFull();
        geburtsdatum.getStyle().set("min-width", "0");

        TextField beruf = createTextField("Beruf / Tätigkeit", "z.B. Softwareentwickler", false);

        FormLayout form = createTwoColumnFormLayout();

        form.add(
                anrede,
                titel,
                vorname,
                nachname,
                geburtsdatum,
                beruf
        );

        return createFormCard(
                "Personendaten",
                "Grundlegende Informationen zum Mieter",
                form
        );
    }

    // Card für Kontakt und Adresse
    private Component createKontaktAdresseCard() {
        TextField email = createTextField("E-Mail Adresse", "max@beispiel.de", true);
        TextField telefon = createTextField("Telefonnummer (Mobil)", "+49 151 1234567", false);

        TextField strasse = createTextField("Straße und Hausnummer", "Musterstraße 123", false);

        TextField plz = createTextField("PLZ", "10115", false);
        TextField ort = createTextField("Ort", "Berlin", false);

        FormLayout form = createTwoColumnFormLayout();

        form.add(email, telefon, strasse, plz, ort);
        form.setColspan(strasse, 2);

        return createFormCard(
                "Kontakt & Adresse",
                "Aktuelle Erreichbarkeit und Wohnanschrift",
                form
        );
    }

    // Optionale Card für Bankverbindung
    private Component createBankverbindungCard() {
        Checkbox bankdatenAktiv = new Checkbox("Bankdaten erfassen");

        TextField kontoinhaber = createTextField("Kontoinhaber", "Max Mustermann", false);
        TextField iban = createTextField("IBAN", "DE12 3456 7890 1234 5678 90", false);
        TextField bic = createTextField("BIC / Bankname", "Musterbank eG", false);

        List<TextField> bankFields = List.of(kontoinhaber, iban, bic);
        bankFields.forEach(field -> field.setEnabled(false));

        bankdatenAktiv.addValueChangeListener(event ->
                bankFields.forEach(field -> field.setEnabled(event.getValue()))
        );

        FormLayout form = createTwoColumnFormLayout();

        form.add(kontoinhaber, iban, bic);
        form.setColspan(kontoinhaber, 2);

        return createFormCard(
                "Bankverbindung",
                "Optional für SEPA-Lastschriftmandat",
                form,
                bankdatenAktiv
        );
    }

    // Erstellt eine normale Form-Card
    private Div createFormCard(String titleText, String subtitleText, Component content) {
        return createFormCard(titleText, subtitleText, content, null);
    }

    // Erstellt eine Form-Card mit optionalem Element rechts im Header
    private Div createFormCard(String titleText, String subtitleText, Component content, Component headerAction) {
        Div card = new Div();
        card.addClassName("form-card");
        card.setWidthFull();

        HorizontalLayout header = new HorizontalLayout();
        header.addClassName("form-card-header");
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        Div titleArea = new Div();

        Span title = new Span(titleText);
        title.addClassName("form-card-title");

        Span subtitle = new Span(subtitleText);
        subtitle.addClassName("form-card-subtitle");

        // Titel und Untertitel stehen untereinander
        titleArea.add(title, subtitle);

        if (headerAction == null) {
            header.add(titleArea);
        } else {
            header.add(titleArea, headerAction);
        }

        Div body = new Div();
        body.addClassName("form-card-content");
        body.add(content);

        card.add(header, body);

        return card;
    }

    // Erstellt ein zweispaltiges Formular
    private FormLayout createTwoColumnFormLayout() {
        FormLayout form = new FormLayout();
        form.setWidthFull();

        form.getStyle().set("--vaadin-form-layout-column-spacing", "24px");
        form.getStyle().set("--vaadin-form-layout-row-spacing", "18px");

        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2)
        );

        return form;
    }

    // Erstellt ein Textfeld
    private TextField createTextField(String label, String placeholder, boolean required) {
        TextField field = new TextField(label);
        field.setPlaceholder(placeholder);
        field.setRequiredIndicatorVisible(required);
        field.setWidthFull();
        field.getStyle().set("min-width", "0");

        return field;
    }

    @Override
    public String getPageTitle() {
        return "Mieter anlegen";
    }

    @Override
    public String getPageSubtitle() {
        return "Neuen Mieter erfassen";
    }
}
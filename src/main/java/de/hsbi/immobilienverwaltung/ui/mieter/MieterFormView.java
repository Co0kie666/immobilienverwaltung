package de.hsbi.immobilienverwaltung.ui.mieter;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import de.hsbi.immobilienverwaltung.domain.Adresse;
import de.hsbi.immobilienverwaltung.domain.Mieter;
import de.hsbi.immobilienverwaltung.service.interfaces.MieterService;
import de.hsbi.immobilienverwaltung.ui.layout.HasPageHeader;
import de.hsbi.immobilienverwaltung.ui.layout.MainLayout;
import jakarta.annotation.security.PermitAll;

import java.util.List;

@Route(value = "mieter-anlegen", layout = MainLayout.class)
@PermitAll
public class MieterFormView extends Div implements HasPageHeader {

    private final MieterService mieterService;

    private final Select<String> anredeSelect = new Select<>();
    private final TextField titelField = new TextField("Titel (Optional)");
    private final TextField vornameField = new TextField("Vorname");
    private final TextField nachnameField = new TextField("Nachname");
    private final DatePicker geburtsdatumPicker = new DatePicker("Geburtsdatum");
    private final TextField berufField = new TextField("Beruf / Tätigkeit");

    private final TextField emailField = new TextField("E-Mail Adresse");
    private final TextField telefonField = new TextField("Telefonnummer (Mobil)");
    private final TextField strasseField = new TextField("Straße und Hausnummer");
    private final TextField plzField = new TextField("PLZ");
    private final TextField ortField = new TextField("Ort");

    private final Checkbox bankdatenAktiv = new Checkbox("Bankdaten erfassen");
    private final TextField kontoinhaberField = new TextField("Kontoinhaber");
    private final TextField ibanField = new TextField("IBAN");
    private final TextField bicField = new TextField("BIC / Bankname");

    public MieterFormView(MieterService mieterService) {
        this.mieterService = mieterService;

        addClassName("page-content");

        Div pageWrapper = new Div();
        pageWrapper.setWidthFull();
        pageWrapper.getStyle().set("max-width", "1180px");
        pageWrapper.getStyle().set("margin", "0 auto");

        pageWrapper.add(createFormContent());

        add(pageWrapper);
    }

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

    private Component createBottomActions() {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setWidthFull();
        actions.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        actions.setAlignItems(FlexComponent.Alignment.CENTER);
        actions.setSpacing(true);
        actions.getStyle().set("margin-top", "28px");

        Button cancelButton = new Button("Abbrechen", VaadinIcon.CLOSE.create());
        cancelButton.addClassName("secondary-button");
        cancelButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate("mieter-vertraege?tab=mieter"))
        );

        Button saveButton = new Button("Mieter anlegen", VaadinIcon.PLUS.create());
        saveButton.addClassName("primary-button");
        saveButton.addClickListener(event -> speichereMieter());

        actions.add(cancelButton, saveButton);

        return actions;
    }

    private Component createPersonendatenCard() {
        anredeSelect.setLabel("Anrede");
        anredeSelect.setItems("Herr", "Frau", "Divers");
        anredeSelect.setPlaceholder("Bitte wählen...");
        anredeSelect.setWidthFull();

        titelField.setPlaceholder("z.B. Dr.");
        titelField.setWidthFull();

        vornameField.setPlaceholder("Max");
        vornameField.setRequiredIndicatorVisible(true);
        vornameField.setWidthFull();

        nachnameField.setPlaceholder("Mustermann");
        nachnameField.setRequiredIndicatorVisible(true);
        nachnameField.setWidthFull();

        geburtsdatumPicker.setPlaceholder("tt.mm.jjjj");
        geburtsdatumPicker.setWidthFull();

        berufField.setPlaceholder("z.B. Softwareentwickler");
        berufField.setWidthFull();

        FormLayout form = createTwoColumnFormLayout();

        form.add(
                anredeSelect,
                titelField,
                vornameField,
                nachnameField,
                geburtsdatumPicker,
                berufField
        );

        return createFormCard(
                "Personendaten",
                "Grundlegende Informationen zum Mieter",
                form
        );
    }

    private Component createKontaktAdresseCard() {
        emailField.setPlaceholder("max@beispiel.de");
        emailField.setRequiredIndicatorVisible(true);
        emailField.setWidthFull();

        telefonField.setPlaceholder("+49 151 1234567");
        telefonField.setWidthFull();

        strasseField.setPlaceholder("Musterstraße 123");
        strasseField.setWidthFull();

        plzField.setPlaceholder("10115");
        plzField.setWidthFull();

        ortField.setPlaceholder("Berlin");
        ortField.setWidthFull();

        FormLayout form = createTwoColumnFormLayout();

        form.add(emailField, telefonField, strasseField, plzField, ortField);
        form.setColspan(strasseField, 2);

        return createFormCard(
                "Kontakt & Adresse",
                "Aktuelle Erreichbarkeit und Wohnanschrift",
                form
        );
    }

    private Component createBankverbindungCard() {
        kontoinhaberField.setPlaceholder("Max Mustermann");
        ibanField.setPlaceholder("DE12 3456 7890 1234 5678 90");
        bicField.setPlaceholder("Musterbank eG");

        List<TextField> bankFields = List.of(kontoinhaberField, ibanField, bicField);
        bankFields.forEach(field -> field.setEnabled(false));

        bankdatenAktiv.addValueChangeListener(event ->
                bankFields.forEach(field -> field.setEnabled(event.getValue()))
        );

        FormLayout form = createTwoColumnFormLayout();

        form.add(kontoinhaberField, ibanField, bicField);
        form.setColspan(kontoinhaberField, 2);

        return createFormCard(
                "Bankverbindung",
                "Optional für SEPA-Lastschriftmandat",
                form,
                bankdatenAktiv
        );
    }

    private void speichereMieter() {
        try {
            pruefePflichtfelder();

            Adresse adresse = new Adresse(
                    strasseField.getValue(),
                    "",
                    plzField.getValue(),
                    ortField.getValue()
            );

            Mieter mieter = new Mieter(
                    anredeSelect.getValue(),
                    titelField.getValue(),
                    vornameField.getValue(),
                    nachnameField.getValue(),
                    geburtsdatumPicker.getValue(),
                    berufField.getValue(),
                    emailField.getValue(),
                    telefonField.getValue(),
                    adresse,
                    bankdatenAktiv.getValue(),
                    kontoinhaberField.getValue(),
                    ibanField.getValue(),
                    bicField.getValue()
            );

            mieterService.speichereMieter(mieter);

            Notification.show("Mieter wurde gespeichert: "
                    + vornameField.getValue() + " " + nachnameField.getValue());

            getUI().ifPresent(ui -> ui.navigate("mieter-vertraege?tab=mieter"));

        } catch (Exception ex) {
            Notification.show("Fehler beim Speichern: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
        }
    }

    private void pruefePflichtfelder() {
        boolean fehler = false;

        fehler |= markierePflichtfeld(vornameField, "Bitte Vorname eingeben");
        fehler |= markierePflichtfeld(nachnameField, "Bitte Nachname eingeben");
        fehler |= markierePflichtfeld(emailField, "Bitte E-Mail eingeben");

        if (!emailField.getValue().isBlank() && !emailField.getValue().contains("@")) {
            emailField.setInvalid(true);
            emailField.setErrorMessage("Bitte gültige E-Mail eingeben");
            fehler = true;
        }

        if (bankdatenAktiv.getValue()) {
            fehler |= markierePflichtfeld(kontoinhaberField, "Bitte Kontoinhaber eingeben");
            fehler |= markierePflichtfeld(ibanField, "Bitte IBAN eingeben");
        }

        if (fehler) {
            throw new IllegalArgumentException("Bitte alle Pflichtfelder korrekt ausfüllen.");
        }
    }

    private boolean markierePflichtfeld(TextField field, String errorMessage) {
        boolean leer = field.getValue() == null || field.getValue().isBlank();

        field.setInvalid(leer);
        field.setErrorMessage(errorMessage);

        return leer;
    }

    private Div createFormCard(String titleText, String subtitleText, Component content) {
        return createFormCard(titleText, subtitleText, content, null);
    }

    private Div createFormCard(String titleText, String subtitleText, Component content, Component headerAction) {
        Div card = new Div();
        card.addClassName("form-card");
        card.setWidthFull();

        HorizontalLayout header = new HorizontalLayout();
        header.addClassName("form-card-header");
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.START);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        Div titleArea = new Div();
        titleArea.getStyle().set("display", "flex");
        titleArea.getStyle().set("flex-direction", "column");
        titleArea.getStyle().set("gap", "4px");

        Span title = new Span(titleText);
        title.addClassName("form-card-title");

        Span subtitle = new Span(subtitleText);
        subtitle.addClassName("form-card-subtitle");

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

    @Override
    public String getPageTitle() {
        return "Mieter anlegen";
    }

    @Override
    public String getPageSubtitle() {
        return "Neuen Mieter erfassen";
    }
}
package de.hsbi.immobilienverwaltung.ui.mieter;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import de.hsbi.immobilienverwaltung.domain.Adresse;
import de.hsbi.immobilienverwaltung.domain.Mieter;
import de.hsbi.immobilienverwaltung.service.interfaces.MieterService;
import de.hsbi.immobilienverwaltung.ui.layout.HasPageHeader;
import de.hsbi.immobilienverwaltung.ui.layout.MainLayout;
import jakarta.annotation.security.PermitAll;

import java.util.List;
import java.util.Locale;

@Route(value = "mieter-vertraege/mieter-anlegen", layout = MainLayout.class)
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
    private final TextField strasseField = new TextField("Straße");
    private final TextField hausnummerField = new TextField("Hausnummer");
    private final TextField plzField = new TextField("PLZ");
    private final TextField ortField = new TextField("Ort");

    private final Checkbox bankdatenAktiv = new Checkbox("Bankdaten erfassen");
    private final TextField kontoinhaberField = new TextField("Kontoinhaber");
    private final TextField ibanField = new TextField("IBAN");
    private final TextField bicField = new TextField("BIC / Bankname");

    private final Span previewInitials = new Span("+");
    private final Span previewName = new Span("Neuer Mieter");
    private final Span previewMeta = new Span("Kontaktdaten & Adresse erfassen");

    public MieterFormView(MieterService mieterService) {
        this.mieterService = mieterService;

        addClassNames("page-content", "tenant-form-page");

        Div pageWrapper = new Div();
        pageWrapper.addClassName("tenant-form-wrapper");

        pageWrapper.add(createFormContent());

        add(pageWrapper);

        updatePreview();
    }

    private Component createFormContent() {
        Div content = new Div();
        content.addClassName("tenant-form-content");

        Div formGrid = new Div();
        formGrid.addClassName("tenant-form-grid");

        formGrid.add(
                createPersonendatenCard(),
                createKontaktAdresseCard()
        );

        content.add(
                createHero(),
                formGrid,
                createBankverbindungCard(),
                createBottomActions()
        );

        return content;
    }

    private Component createHero() {
        Div hero = new Div();
        hero.addClassName("tenant-form-hero");

        Div content = new Div();
        content.addClassName("tenant-form-hero-content");

        Span eyebrow = new Span("Mieterprofil");
        eyebrow.addClassName("tenant-form-eyebrow");

        H1 title = new H1("Neuen Mieter anlegen");
        title.addClassName("tenant-form-title");

        Paragraph subtitle = new Paragraph(
                "Erfasse persönliche Daten, Kontaktinformationen und optional die Bankverbindung. Die Vorschau rechts aktualisiert sich automatisch beim Ausfüllen."
        );
        subtitle.addClassName("tenant-form-subtitle");

        HorizontalLayout actions = new HorizontalLayout();
        actions.addClassName("tenant-form-hero-actions");
        actions.setAlignItems(FlexComponent.Alignment.CENTER);

        Button cancelButton = new Button("Zur Übersicht", VaadinIcon.ARROW_LEFT.create());
        cancelButton.addClassName("secondary-button");
        cancelButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate("mieter-vertraege?tab=mieter"))
        );

        Button saveButton = new Button("Mieter speichern", VaadinIcon.CHECK.create());
        saveButton.addClassName("primary-button");
        saveButton.addClickListener(event -> speichereMieter());

        actions.add(cancelButton, saveButton);

        content.add(eyebrow, title, subtitle, actions);

        Div preview = createProfilePreview();

        hero.add(content, preview);

        return hero;
    }

    private Div createProfilePreview() {
        Div preview = new Div();
        preview.addClassName("tenant-form-preview");

        Div avatar = new Div();
        avatar.addClassName("tenant-form-preview-avatar");
        previewInitials.addClassName("tenant-form-preview-initials");
        avatar.add(previewInitials);

        Span label = new Span("Live-Vorschau");
        label.addClassName("tenant-form-preview-label");

        previewName.addClassName("tenant-form-preview-name");
        previewMeta.addClassName("tenant-form-preview-meta");

        Div facts = new Div();
        facts.addClassName("tenant-form-preview-facts");
        facts.add(
                createPreviewFact(VaadinIcon.CHECK, "Pflichtfelder klar markiert"),
                createPreviewFact(VaadinIcon.ENVELOPE, "E-Mail wird geprüft"),
                createPreviewFact(VaadinIcon.CREDIT_CARD, "Bankdaten optional")
        );

        preview.add(label, avatar, previewName, previewMeta, facts);

        return preview;
    }

    private Component createPreviewFact(VaadinIcon icon, String text) {
        Div item = new Div();
        item.addClassName("tenant-form-preview-fact");

        Icon itemIcon = icon.create();
        itemIcon.addClassName("tenant-form-preview-fact-icon");

        Span itemText = new Span(text);

        item.add(itemIcon, itemText);

        return item;
    }

    private Component createBottomActions() {
        Div actionBar = new Div();
        actionBar.addClassName("tenant-form-actions");

        Div hint = new Div();
        hint.addClassName("tenant-form-action-hint");

        Span title = new Span("Bereit zum Speichern?");
        title.addClassName("tenant-form-action-title");

        Span subtitle = new Span("Vorname, Nachname und E-Mail sind Pflichtfelder. Bankdaten werden nur übernommen, wenn der Schalter aktiv ist.");
        subtitle.addClassName("tenant-form-action-subtitle");

        hint.add(title, subtitle);

        HorizontalLayout actions = new HorizontalLayout();
        actions.setAlignItems(FlexComponent.Alignment.CENTER);
        actions.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        actions.setSpacing(true);

        Button cancelButton = new Button("Abbrechen", VaadinIcon.CLOSE.create());
        cancelButton.addClassName("secondary-button");
        cancelButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate("mieter-vertraege?tab=mieter"))
        );

        Button saveButton = new Button("Mieter anlegen", VaadinIcon.PLUS.create());
        saveButton.addClassName("primary-button");
        saveButton.addClickListener(event -> speichereMieter());

        actions.add(cancelButton, saveButton);
        actionBar.add(hint, actions);

        return actionBar;
    }

    private Component createPersonendatenCard() {
        anredeSelect.setLabel("Anrede");
        anredeSelect.setItems("Herr", "Frau", "Divers");
        anredeSelect.setPlaceholder("Bitte wählen...");
        anredeSelect.setWidthFull();
        anredeSelect.addClassName("tenant-form-field");
        anredeSelect.addValueChangeListener(event -> updatePreview());

        configureTextField(titelField, "z.B. Dr.", false, null);
        configureTextField(vornameField, "Max", true, VaadinIcon.USER);
        configureTextField(nachnameField, "Mustermann", true, VaadinIcon.USER);
        configureTextField(berufField, "z.B. Softwareentwickler", false, null);

        vornameField.addValueChangeListener(event -> updatePreview());
        nachnameField.addValueChangeListener(event -> updatePreview());

        geburtsdatumPicker.setPlaceholder("tt.mm.jjjj");
        geburtsdatumPicker.setWidthFull();
        geburtsdatumPicker.setLocale(Locale.GERMANY);
        geburtsdatumPicker.addClassName("tenant-form-field");

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
                "Grunddaten für die eindeutige Zuordnung des Mieters.",
                VaadinIcon.USER,
                form,
                null
        );
    }

    private Component createKontaktAdresseCard() {
        configureTextField(emailField, "max@beispiel.de", true, VaadinIcon.ENVELOPE);
        configureTextField(telefonField, "+491511234567", false, VaadinIcon.PHONE);
        configureTextField(strasseField, "Musterstraße", false, VaadinIcon.HOME);
        configureTextField(hausnummerField, "123", false, VaadinIcon.HOME);
        configureTextField(plzField, "10115", false, VaadinIcon.MAP_MARKER);
        configureTextField(ortField, "Berlin", false, VaadinIcon.MAP_MARKER);

        telefonField.setAllowedCharPattern("[0-9+ ]");

        emailField.addValueChangeListener(event -> updatePreview());
        ortField.addValueChangeListener(event -> updatePreview());

        FormLayout form = createTwoColumnFormLayout();

        form.add(
                emailField,
                telefonField,
                strasseField,
                hausnummerField,
                plzField,
                ortField
        );

        return createFormCard(
                "Kontakt & Adresse",
                "So bleibt der Mieter schnell erreichbar und sauber dokumentiert.",
                VaadinIcon.ENVELOPE,
                form,
                null
        );
    }

    private Component createBankverbindungCard() {
        configureTextField(kontoinhaberField, "Max Mustermann", false, VaadinIcon.USER);
        configureTextField(ibanField, "DE12 3456 7890 1234 5678 90", false, VaadinIcon.CREDIT_CARD);
        configureTextField(bicField, "Musterbank eG", false, VaadinIcon.BUILDING);

        List<TextField> bankFields = List.of(kontoinhaberField, ibanField, bicField);
        bankFields.forEach(field -> field.setEnabled(false));

        bankdatenAktiv.addClassName("tenant-bank-toggle");

        FormLayout form = createTwoColumnFormLayout();

        form.add(kontoinhaberField, ibanField, bicField);
        form.setColspan(kontoinhaberField, 2);

        Div card = createFormCard(
                "Bankverbindung",
                "Optional für Lastschrift, Rückzahlungen oder interne Dokumentation.",
                VaadinIcon.CREDIT_CARD,
                form,
                bankdatenAktiv
        );

        bankdatenAktiv.addValueChangeListener(event -> {
            boolean aktiv = event.getValue();

            bankFields.forEach(field -> field.setEnabled(aktiv));

            if (aktiv) {
                card.addClassName("tenant-form-card-active");
            } else {
                card.removeClassName("tenant-form-card-active");
                bankFields.forEach(field -> field.setInvalid(false));
            }
        });

        return card;
    }

    private void configureTextField(
            TextField field,
            String placeholder,
            boolean required,
            VaadinIcon icon
    ) {
        field.setPlaceholder(placeholder);
        field.setRequiredIndicatorVisible(required);
        field.setWidthFull();
        field.setValueChangeMode(ValueChangeMode.EAGER);
        field.addClassName("tenant-form-field");

        if (icon != null) {
            field.setPrefixComponent(icon.create());
        }
    }

    private void speichereMieter() {
        try {
            pruefePflichtfelder();

            Adresse adresse = new Adresse(
                    strasseField.getValue(),
                    hausnummerField.getValue(),
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

            Notification.show(
                    "Mieter wurde gespeichert: "
                            + vornameField.getValue()
                            + " "
                            + nachnameField.getValue()
            );

            getUI().ifPresent(ui -> ui.navigate("mieter-vertraege?tab=mieter"));

        } catch (Exception ex) {
            Notification.show(
                    "Fehler beim Speichern: " + ex.getMessage(),
                    4000,
                    Notification.Position.MIDDLE
            );
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

        if (!telefonField.getValue().isBlank() && !telefonField.getValue().matches("\\+?[0-9 ]*")) {
            telefonField.setInvalid(true);
            telefonField.setErrorMessage("Telefonnummer darf nur Zahlen und optional ein + am Anfang enthalten");
            fehler = true;
        } else {
            telefonField.setInvalid(false);
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

    private Div createFormCard(
            String titleText,
            String subtitleText,
            VaadinIcon icon,
            Component content,
            Component headerAction
    ) {
        Div card = new Div();
        card.addClassName("tenant-form-card");

        HorizontalLayout header = new HorizontalLayout();
        header.addClassName("tenant-form-card-header");
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        Div titleArea = new Div();
        titleArea.addClassName("tenant-form-card-title-area");

        Div iconBox = new Div(icon.create());
        iconBox.addClassName("tenant-form-card-icon");

        Div titleTextWrapper = new Div();

        Span title = new Span(titleText);
        title.addClassName("tenant-form-card-title");

        Span subtitle = new Span(subtitleText);
        subtitle.addClassName("tenant-form-card-subtitle");

        titleTextWrapper.add(title, subtitle);
        titleArea.add(iconBox, titleTextWrapper);

        if (headerAction == null) {
            header.add(titleArea);
        } else {
            Div action = new Div(headerAction);
            action.addClassName("tenant-form-card-action");
            header.add(titleArea, action);
        }

        Div body = new Div();
        body.addClassName("tenant-form-card-content");
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
                new FormLayout.ResponsiveStep("650px", 2)
        );

        return form;
    }

    private void updatePreview() {
        String vorname = textOderLeer(vornameField.getValue()).trim();
        String nachname = textOderLeer(nachnameField.getValue()).trim();
        String email = textOderLeer(emailField.getValue()).trim();
        String ort = textOderLeer(ortField.getValue()).trim();

        String name = (vorname + " " + nachname).trim();

        if (name.isBlank()) {
            previewName.setText("Neuer Mieter");
            previewInitials.setText("+");
        } else {
            previewName.setText(name);
            previewInitials.setText(ermittleInitialen(vorname, nachname));
        }

        String meta = email.isBlank() ? "Noch keine E-Mail hinterlegt" : email;

        if (!ort.isBlank()) {
            meta += " · " + ort;
        }

        previewMeta.setText(meta);
    }

    private String ermittleInitialen(String vorname, String nachname) {
        StringBuilder initialen = new StringBuilder();

        if (!vorname.isBlank()) {
            initialen.append(vorname.charAt(0));
        }

        if (!nachname.isBlank()) {
            initialen.append(nachname.charAt(0));
        }

        return initialen.toString().toUpperCase(Locale.GERMANY);
    }

    private String textOderLeer(String text) {
        return text == null ? "" : text;
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

package de.hsbi.immobilienverwaltung.ui.mieter;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import de.hsbi.immobilienverwaltung.domain.Immobilie;
import de.hsbi.immobilienverwaltung.domain.Mieteinheit;
import de.hsbi.immobilienverwaltung.domain.Mieter;
import de.hsbi.immobilienverwaltung.domain.Mietvertrag;
import de.hsbi.immobilienverwaltung.domain.enums.Vertragsstatus;
import de.hsbi.immobilienverwaltung.service.interfaces.ImmobilieService;
import de.hsbi.immobilienverwaltung.service.interfaces.MieteinheitService;
import de.hsbi.immobilienverwaltung.service.interfaces.MieterService;
import de.hsbi.immobilienverwaltung.service.interfaces.MietvertragService;
import de.hsbi.immobilienverwaltung.ui.layout.HasPageHeader;
import de.hsbi.immobilienverwaltung.ui.layout.MainLayout;
import jakarta.annotation.security.PermitAll;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Route(value = "mietvertrag-anlegen", layout = MainLayout.class)
@PermitAll
public class MietvertragFormView extends Div implements HasPageHeader, BeforeEnterObserver {

    private final MieterService mieterService;
    private final ImmobilieService immobilieService;
    private final MieteinheitService mieteinheitService;
    private final MietvertragService mietvertragService;

    private final Select<Mieter> mieterSelect = new Select<>();
    private final Select<Immobilie> immobilieSelect = new Select<>();
    private final Select<Mieteinheit> mieteinheitSelect = new Select<>();

    private final DatePicker vertragsbeginnPicker = new DatePicker("Vertragsbeginn");
    private final DatePicker vertragsendePicker = new DatePicker("Vertragsende (Optional)");

    private final Select<String> kuendigungsfristSelect = new Select<>();
    private final Select<String> zahlungsintervallSelect = new Select<>();

    private final NumberField kaltmieteField = new NumberField("Kaltmiete");
    private final NumberField nebenkostenField = new NumberField("Nebenkosten-Vorauszahlung");

    private final Span previewMieter = new Span("Noch kein Mieter gewählt");
    private final Span previewObjekt = new Span("Noch kein Mietobjekt gewählt");
    private final Span previewZeitraum = new Span("Vertragsbeginn offen");
    private final Span previewWarmmiete = new Span("0,00 €");
    private final Span previewKuendigungsfrist = new Span("3 Monate");

    private Long immobilieId;
    private Long mieteinheitId;

    public MietvertragFormView(
            MieterService mieterService,
            ImmobilieService immobilieService,
            MieteinheitService mieteinheitService,
            MietvertragService mietvertragService
    ) {
        this.mieterService = mieterService;
        this.immobilieService = immobilieService;
        this.mieteinheitService = mieteinheitService;
        this.mietvertragService = mietvertragService;

        addClassNames("page-content", "contract-form-page");

        Div pageWrapper = new Div();
        pageWrapper.addClassName("contract-form-wrapper");
        pageWrapper.add(createFormContent());

        add(pageWrapper);
    }

    private Component createFormContent() {
        Div content = new Div();
        content.addClassName("contract-form-content");

        Div topCards = new Div();
        topCards.addClassName("contract-form-grid");
        topCards.add(
                createMietobjektMieterCard(),
                createVertragsdatenCard()
        );

        content.add(
                createHeroSection(),
                topCards,
                createFinanzielleDetailsCard(),
                createBottomActions()
        );

        return content;
    }

    private Component createHeroSection() {
        Div hero = new Div();
        hero.addClassName("contract-form-hero");

        Div left = new Div();
        left.addClassName("contract-form-hero-content");

        Span eyebrow = new Span("Neuer Mietvertrag");
        eyebrow.addClassName("contract-form-eyebrow");

        H2 title = new H2("Mieter, Einheit und Konditionen sauber zusammenführen.");
        title.addClassName("contract-form-title");

        Paragraph subtitle = new Paragraph("Wähle zuerst den Mieter und das Mietobjekt aus. Die Vertragszusammenfassung rechts aktualisiert sich automatisch und hilft beim Prüfen vor dem Speichern.");
        subtitle.addClassName("contract-form-subtitle");

        HorizontalLayout heroActions = new HorizontalLayout();
        heroActions.addClassName("contract-form-hero-actions");

        Button backButton = new Button("Zur Übersicht", VaadinIcon.ARROW_LEFT.create());
        backButton.addClassName("secondary-button");
        backButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate("mieter-vertraege?tab=vertraege"))
        );

        Button saveButton = new Button("Vertrag speichern", VaadinIcon.CHECK.create());
        saveButton.addClassName("primary-button");
        saveButton.addClickListener(event -> speichereMietvertrag());

        heroActions.add(backButton, saveButton);
        left.add(eyebrow, title, subtitle, heroActions);

        Div preview = new Div();
        preview.addClassName("contract-form-preview");

        Span previewLabel = new Span("Live-Vorschau");
        previewLabel.addClassName("contract-preview-label");

        Div previewIcon = new Div(VaadinIcon.FILE_TEXT.create());
        previewIcon.addClassName("contract-preview-icon");

        previewMieter.addClassName("contract-preview-title");
        previewObjekt.addClassName("contract-preview-subtitle");

        Div facts = new Div();
        facts.addClassName("contract-preview-facts");
        facts.add(
                createPreviewFact(VaadinIcon.CALENDAR, "Laufzeit", previewZeitraum),
                createPreviewFact(VaadinIcon.EURO, "Warmmiete", previewWarmmiete),
                createPreviewFact(VaadinIcon.CLOCK, "Kündigungsfrist", previewKuendigungsfrist)
        );

        preview.add(previewLabel, previewIcon, previewMieter, previewObjekt, facts);
        hero.add(left, preview);

        return hero;
    }

    private Component createPreviewFact(VaadinIcon icon, String labelText, Span valueSpan) {
        Div fact = new Div();
        fact.addClassName("contract-preview-fact");

        Div iconBox = new Div(icon.create());
        iconBox.addClassName("contract-preview-fact-icon");

        Div text = new Div();
        text.addClassName("contract-preview-fact-text");

        Span label = new Span(labelText);
        label.addClassName("contract-preview-fact-label");

        valueSpan.addClassName("contract-preview-fact-value");

        text.add(label, valueSpan);
        fact.add(iconBox, text);

        return fact;
    }

    private Component createMietobjektMieterCard() {
        mieterSelect.setLabel("Mieter auswählen");
        mieterSelect.setItems(mieterService.findeAlleMieter());
        mieterSelect.setItemLabelGenerator(this::formatMieter);
        mieterSelect.setPlaceholder("Bitte wählen...");
        mieterSelect.setRequiredIndicatorVisible(true);
        mieterSelect.setWidthFull();
        mieterSelect.addValueChangeListener(event -> updatePreview());

        immobilieSelect.setLabel("Immobilie");
        immobilieSelect.setItems(immobilieService.findeAlleImmobilien());
        immobilieSelect.setItemLabelGenerator(this::formatImmobilie);
        immobilieSelect.setPlaceholder("Bitte wählen...");
        immobilieSelect.setRequiredIndicatorVisible(true);
        immobilieSelect.setWidthFull();

        mieteinheitSelect.setLabel("Wohneinheit");
        mieteinheitSelect.setItemLabelGenerator(this::formatMieteinheit);
        mieteinheitSelect.setPlaceholder("Zuerst Immobilie wählen...");
        mieteinheitSelect.setRequiredIndicatorVisible(true);
        mieteinheitSelect.setWidthFull();
        mieteinheitSelect.setEnabled(false);
        mieteinheitSelect.addValueChangeListener(event -> updatePreview());

        immobilieSelect.addValueChangeListener(event -> {
            aktualisiereMieteinheiten(event.getValue());
            updatePreview();
        });

        FormLayout form = createTwoColumnFormLayout();
        form.add(mieterSelect, immobilieSelect, mieteinheitSelect);
        form.setColspan(mieterSelect, 2);

        return createFormCard(
                "Mietobjekt & Mieter",
                "Verknüpft den Vertrag mit einem bestehenden Mieter und einer freien bzw. passenden Einheit.",
                VaadinIcon.USERS,
                form
        );
    }

    private void aktualisiereMieteinheiten(Immobilie immobilie) {
        mieteinheitSelect.clear();

        if (immobilie == null || immobilie.getId() == null) {
            mieteinheitSelect.setEnabled(false);
            mieteinheitSelect.setPlaceholder("Zuerst Immobilie wählen...");
            return;
        }

        List<Mieteinheit> mieteinheiten =
                mieteinheitService.findeMieteinheitenNachImmobilie(immobilie.getId());

        mieteinheitSelect.setItems(mieteinheiten);
        mieteinheitSelect.setEnabled(true);
        mieteinheitSelect.setPlaceholder(
                mieteinheiten.isEmpty()
                        ? "Keine Einheiten vorhanden"
                        : "Bitte wählen..."
        );
    }

    private Component createVertragsdatenCard() {
        vertragsbeginnPicker.setPlaceholder("tt.mm.jjjj");
        vertragsbeginnPicker.setRequiredIndicatorVisible(true);
        vertragsbeginnPicker.setWidthFull();
        vertragsbeginnPicker.addValueChangeListener(event -> updatePreview());

        vertragsendePicker.setPlaceholder("tt.mm.jjjj");
        vertragsendePicker.setWidthFull();
        vertragsendePicker.addValueChangeListener(event -> updatePreview());

        kuendigungsfristSelect.setLabel("Kündigungsfrist");
        kuendigungsfristSelect.setItems(
                "Gesetzlich (3 Monate)",
                "1 Monat",
                "6 Monate",
                "Individuell"
        );
        kuendigungsfristSelect.setValue("Gesetzlich (3 Monate)");
        kuendigungsfristSelect.setWidthFull();
        kuendigungsfristSelect.addValueChangeListener(event -> updatePreview());

        zahlungsintervallSelect.setLabel("Zahlungsintervall");
        zahlungsintervallSelect.setItems(
                "Monatlich",
                "Vierteljährlich",
                "Jährlich"
        );
        zahlungsintervallSelect.setValue("Monatlich");
        zahlungsintervallSelect.setWidthFull();

        FormLayout form = createTwoColumnFormLayout();
        form.add(
                vertragsbeginnPicker,
                vertragsendePicker,
                kuendigungsfristSelect,
                zahlungsintervallSelect
        );

        return createFormCard(
                "Vertragsdaten",
                "Beginn, optionales Ende und Kündigungslogik des Mietverhältnisses.",
                VaadinIcon.CALENDAR_CLOCK,
                form
        );
    }

    private Component createFinanzielleDetailsCard() {
        kaltmieteField.setPlaceholder("0.00");
        kaltmieteField.setRequiredIndicatorVisible(true);
        kaltmieteField.setSuffixComponent(new Span("€"));
        kaltmieteField.setWidthFull();
        kaltmieteField.setMin(0);
        kaltmieteField.setStep(0.01);
        kaltmieteField.setClearButtonVisible(true);

        nebenkostenField.setPlaceholder("0.00");
        nebenkostenField.setRequiredIndicatorVisible(true);
        nebenkostenField.setSuffixComponent(new Span("€"));
        nebenkostenField.setWidthFull();
        nebenkostenField.setMin(0);
        nebenkostenField.setStep(0.01);
        nebenkostenField.setClearButtonVisible(true);

        Div warmmieteBox = new Div();
        warmmieteBox.addClassName("contract-money-summary");

        Div warmmieteIcon = new Div(VaadinIcon.WALLET.create());
        warmmieteIcon.addClassName("contract-money-summary-icon");

        Div warmmieteText = new Div();
        warmmieteText.addClassName("contract-money-summary-text");

        Span warmmieteLabel = new Span("Warmmiete pro Monat");
        warmmieteLabel.addClassName("contract-money-summary-label");

        Span warmmieteValue = new Span("0,00 €");
        warmmieteValue.addClassName("contract-money-summary-value");

        Span warmmieteHint = new Span("Kaltmiete + Nebenkosten-Vorauszahlung");
        warmmieteHint.addClassName("contract-money-summary-hint");

        warmmieteText.add(warmmieteLabel, warmmieteValue, warmmieteHint);
        warmmieteBox.add(warmmieteIcon, warmmieteText);

        configureWarmmieteCalculation(kaltmieteField, nebenkostenField, warmmieteValue);

        FormLayout form = createTwoColumnFormLayout();
        form.add(kaltmieteField, nebenkostenField, warmmieteBox);
        form.setColspan(warmmieteBox, 2);

        return createFormCard(
                "Finanzielle Details",
                "Monatliche Beträge werden direkt zur Warmmiete zusammengerechnet.",
                VaadinIcon.EURO,
                form
        );
    }

    private void speichereMietvertrag() {
        try {
            pruefePflichtfelder();

            Mietvertrag mietvertrag = new Mietvertrag();
            mietvertrag.setStartdatum(vertragsbeginnPicker.getValue());
            mietvertrag.setEnddatum(vertragsendePicker.getValue());
            mietvertrag.setKaltmiete(lesePflichtbetrag(kaltmieteField, "Bitte Kaltmiete eingeben"));
            mietvertrag.setNebenkosten(lesePflichtbetrag(nebenkostenField, "Bitte Nebenkosten eingeben"));
            mietvertrag.setKaution(null);
            mietvertrag.setKuendigungsfrist(berechneKuendigungsfrist());
            mietvertrag.setStatus(Vertragsstatus.AKTIV);

            mietvertragService.speichereMietvertrag(
                    mieterSelect.getValue().getId(),
                    mieteinheitSelect.getValue().getId(),
                    mietvertrag
            );

            Notification.show("Mietvertrag wurde gespeichert");

            getUI().ifPresent(ui -> ui.navigate("mieter-vertraege?tab=vertraege"));

        } catch (Exception ex) {
            Notification.show("Fehler beim Speichern: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
        }
    }

    private void pruefePflichtfelder() {
        boolean fehler = false;

        fehler |= markierePflichtSelect(mieterSelect, "Bitte Mieter auswählen");
        fehler |= markierePflichtSelect(immobilieSelect, "Bitte Immobilie auswählen");
        fehler |= markierePflichtSelect(mieteinheitSelect, "Bitte Wohneinheit auswählen");

        boolean startdatumFehlt = vertragsbeginnPicker.getValue() == null;
        vertragsbeginnPicker.setInvalid(startdatumFehlt);
        vertragsbeginnPicker.setErrorMessage("Bitte Vertragsbeginn auswählen");
        fehler |= startdatumFehlt;

        if (vertragsbeginnPicker.getValue() != null
                && vertragsendePicker.getValue() != null
                && vertragsendePicker.getValue().isBefore(vertragsbeginnPicker.getValue())) {
            vertragsendePicker.setInvalid(true);
            vertragsendePicker.setErrorMessage("Vertragsende darf nicht vor Vertragsbeginn liegen");
            fehler = true;
        } else {
            vertragsendePicker.setInvalid(false);
        }

        fehler |= markierePflichtfeld(kaltmieteField, "Bitte Kaltmiete eingeben");
        fehler |= markierePflichtfeld(nebenkostenField, "Bitte Nebenkosten eingeben");

        if (fehler) {
            throw new IllegalArgumentException("Bitte alle Pflichtfelder korrekt ausfüllen.");
        }
    }

    private <T> boolean markierePflichtSelect(Select<T> select, String errorMessage) {
        boolean leer = select.getValue() == null;

        select.setInvalid(leer);
        select.setErrorMessage(errorMessage);

        return leer;
    }

    private LocalDate berechneKuendigungsfrist() {
        if (vertragsbeginnPicker.getValue() == null) {
            return null;
        }

        String value = kuendigungsfristSelect.getValue();

        if ("1 Monat".equals(value)) {
            return vertragsbeginnPicker.getValue().plusMonths(1);
        }

        if ("6 Monate".equals(value)) {
            return vertragsbeginnPicker.getValue().plusMonths(6);
        }

        return vertragsbeginnPicker.getValue().plusMonths(3);
    }

    private boolean markierePflichtfeld(NumberField field, String errorMessage) {
        boolean leer = field.getValue() == null;
        boolean negativ = !leer && field.getValue() < 0;

        field.setInvalid(leer || negativ);

        if (leer) {
            field.setErrorMessage(errorMessage);
        } else if (negativ) {
            field.setErrorMessage("Betrag darf nicht negativ sein");
        }

        return leer || negativ;
    }

    private Double lesePflichtbetrag(NumberField field, String errorMessage) {
        if (field.getValue() == null) {
            field.setInvalid(true);
            field.setErrorMessage(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        if (field.getValue() < 0) {
            field.setInvalid(true);
            field.setErrorMessage("Betrag darf nicht negativ sein");
            throw new IllegalArgumentException("Betrag darf nicht negativ sein.");
        }

        field.setInvalid(false);
        return field.getValue();
    }

    private void configureWarmmieteCalculation(
            NumberField kaltmieteField,
            NumberField nebenkostenField,
            Span warmmieteValue
    ) {
        kaltmieteField.addValueChangeListener(event ->
                updateWarmmiete(kaltmieteField, nebenkostenField, warmmieteValue)
        );

        nebenkostenField.addValueChangeListener(event ->
                updateWarmmiete(kaltmieteField, nebenkostenField, warmmieteValue)
        );

        updateWarmmiete(kaltmieteField, nebenkostenField, warmmieteValue);
    }

    private void updateWarmmiete(
            NumberField kaltmieteField,
            NumberField nebenkostenField,
            Span warmmieteValue
    ) {
        double kaltmiete = zahlOderNull(kaltmieteField.getValue());
        double nebenkosten = zahlOderNull(nebenkostenField.getValue());
        double warmmiete = kaltmiete + nebenkosten;

        warmmieteValue.setText(formatMoneyValue(warmmiete));
        previewWarmmiete.setText(formatMoneyValue(warmmiete));
    }

    private double zahlOderNull(Double value) {
        return value == null ? 0 : value;
    }

    private String formatMoneyValue(double value) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.GERMANY);
        return formatter.format(value);
    }

    private Component createBottomActions() {
        Div actions = new Div();
        actions.addClassName("contract-form-actions");

        Div hint = new Div();
        hint.addClassName("contract-form-action-hint");

        Span title = new Span("Bereit zum Speichern?");
        title.addClassName("contract-form-action-title");

        Span subtitle = new Span("Pflichtfelder werden vor dem Anlegen automatisch geprüft.");
        subtitle.addClassName("contract-form-action-subtitle");

        hint.add(title, subtitle);

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setAlignItems(FlexComponent.Alignment.CENTER);
        buttons.setSpacing(true);

        Button cancelButton = new Button("Abbrechen", VaadinIcon.CLOSE.create());
        cancelButton.addClassName("secondary-button");
        cancelButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate("mieter-vertraege?tab=vertraege"))
        );

        Button saveButton = new Button("Mietvertrag anlegen", VaadinIcon.PLUS.create());
        saveButton.addClassName("primary-button");
        saveButton.addClickListener(event -> speichereMietvertrag());

        buttons.add(cancelButton, saveButton);
        actions.add(hint, buttons);

        return actions;
    }

    private Div createFormCard(String titleText, String subtitleText, VaadinIcon icon, Component content) {
        Div card = new Div();
        card.addClassName("contract-form-card");
        card.setWidthFull();

        HorizontalLayout header = new HorizontalLayout();
        header.addClassName("contract-form-card-header");
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        Div titleArea = new Div();
        titleArea.addClassName("contract-form-card-title-area");

        Div iconBox = new Div(icon.create());
        iconBox.addClassName("contract-form-card-icon");

        Div titleTexts = new Div();

        Span title = new Span(titleText);
        title.addClassName("contract-form-card-title");

        Span subtitle = new Span(subtitleText);
        subtitle.addClassName("contract-form-card-subtitle");

        titleTexts.add(title, subtitle);
        titleArea.add(iconBox, titleTexts);
        header.add(titleArea);

        Div body = new Div();
        body.addClassName("contract-form-card-content");
        body.add(content);

        card.add(header, body);
        return card;
    }

    private FormLayout createTwoColumnFormLayout() {
        FormLayout form = new FormLayout();
        form.addClassName("contract-form-layout");

        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2)
        );

        return form;
    }

    private void updatePreview() {
        Mieter mieter = mieterSelect.getValue();
        Immobilie immobilie = immobilieSelect.getValue();
        Mieteinheit mieteinheit = mieteinheitSelect.getValue();

        previewMieter.setText(mieter == null ? "Noch kein Mieter gewählt" : formatMieter(mieter));

        if (immobilie == null && mieteinheit == null) {
            previewObjekt.setText("Noch kein Mietobjekt gewählt");
        } else if (mieteinheit == null) {
            previewObjekt.setText(formatImmobilie(immobilie));
        } else if (immobilie == null) {
            previewObjekt.setText(formatMieteinheit(mieteinheit));
        } else {
            previewObjekt.setText(formatImmobilie(immobilie) + " / " + formatMieteinheit(mieteinheit));
        }

        LocalDate start = vertragsbeginnPicker.getValue();
        LocalDate ende = vertragsendePicker.getValue();

        if (start == null) {
            previewZeitraum.setText("Vertragsbeginn offen");
        } else if (ende == null) {
            previewZeitraum.setText("ab " + formatiereDatum(start) + " · unbefristet");
        } else {
            previewZeitraum.setText(formatiereDatum(start) + " – " + formatiereDatum(ende));
        }

        previewKuendigungsfrist.setText(formatiereKuendigungsfrist());
    }

    private String formatiereKuendigungsfrist() {
        String value = kuendigungsfristSelect.getValue();

        if (value == null || "Gesetzlich (3 Monate)".equals(value)) {
            return "3 Monate";
        }

        return value;
    }

    private String formatiereDatum(LocalDate datum) {
        return datum.format(DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.GERMANY));
    }

    private String formatMieter(Mieter mieter) {
        if (mieter == null) {
            return "";
        }

        String vorname = mieter.getVorname() == null ? "" : mieter.getVorname();
        String nachname = mieter.getNachname() == null ? "" : mieter.getNachname();

        return (vorname + " " + nachname).trim();
    }

    private String formatImmobilie(Immobilie immobilie) {
        if (immobilie == null) {
            return "";
        }

        return immobilie.getBezeichnung();
    }

    private String formatMieteinheit(Mieteinheit mieteinheit) {
        if (mieteinheit == null) {
            return "";
        }

        return mieteinheit.getBezeichnung();
    }

    @Override
    public String getPageTitle() {
        return "Mietvertrag anlegen";
    }

    @Override
    public String getPageSubtitle() {
        return "Neuen Mietvertrag erfassen";
    }

    // Falls Mietvertrag über MieteinheitDetailView angelegt wird -> übernehme Immobilie und Mieteinheit automatisch
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        this.immobilieId = leseLongQueryParameter(event, "immobilieId");
        this.mieteinheitId = leseLongQueryParameter(event, "mieteinheitId");

        if (immobilieId != null && mieteinheitId != null) {
            uebernehmeMietobjektAusUrl(immobilieId, mieteinheitId);
        }

        updatePreview();
    }

    private Long leseLongQueryParameter(BeforeEnterEvent event, String parameterName) {
        return event.getLocation()
                .getQueryParameters()
                .getSingleParameter(parameterName)
                .map(Long::valueOf)
                .orElse(null);
    }

    private void uebernehmeMietobjektAusUrl(Long immobilieId, Long mieteinheitId) {
        Immobilie immobilie = immobilieService.findeImmobilieNachId(immobilieId)
                .orElseThrow(() -> new IllegalArgumentException("Immobilie wurde nicht gefunden."));

        Mieteinheit mieteinheit = mieteinheitService.findeMieteinheitNachId(mieteinheitId)
                .orElseThrow(() -> new IllegalArgumentException("Mieteinheit wurde nicht gefunden."));

        immobilieSelect.setItems(immobilie);
        immobilieSelect.setValue(immobilie);

        mieteinheitSelect.setItems(mieteinheit);
        mieteinheitSelect.setEnabled(true);
        mieteinheitSelect.setValue(mieteinheit);

        immobilieSelect.setReadOnly(true);
        mieteinheitSelect.setReadOnly(true);
    }
}

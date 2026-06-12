package de.hsbi.immobilienverwaltung.ui.mieter;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
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
import com.vaadin.flow.component.textfield.NumberField;

import java.text.NumberFormat;
import java.time.LocalDate;
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

    private Long immobilieId;
    private Long mieteinheitId ;

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

        addClassName("page-content");

        Div pageWrapper = new Div();
        pageWrapper.addClassName("form-page-wrapper");

        pageWrapper.add(createFormContent());

        add(pageWrapper);
    }

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

    private Component createMietobjektMieterCard() {
        mieterSelect.setLabel("Mieter auswählen");
        mieterSelect.setItems(mieterService.findeAlleMieter());
        mieterSelect.setItemLabelGenerator(this::formatMieter);
        mieterSelect.setPlaceholder("Bitte wählen...");
        mieterSelect.setRequiredIndicatorVisible(true);
        mieterSelect.setWidthFull();

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

        immobilieSelect.addValueChangeListener(event ->
                aktualisiereMieteinheiten(event.getValue())
        );

        FormLayout form = createTwoColumnFormLayout();

        form.add(mieterSelect, immobilieSelect, mieteinheitSelect);
        form.setColspan(mieterSelect, 2);

        return createFormCard(
                "Mietobjekt & Mieter",
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
        mieteinheitSelect.setPlaceholder("Bitte wählen...");
    }

    private Component createVertragsdatenCard() {
        vertragsbeginnPicker.setPlaceholder("tt.mm.jjjj");
        vertragsbeginnPicker.setRequiredIndicatorVisible(true);
        vertragsbeginnPicker.setWidthFull();

        vertragsendePicker.setPlaceholder("tt.mm.jjjj");
        vertragsendePicker.setWidthFull();

        kuendigungsfristSelect.setLabel("Kündigungsfrist");
        kuendigungsfristSelect.setItems(
                "Gesetzlich (3 Monate)",
                "1 Monat",
                "6 Monate",
                "Individuell"
        );
        kuendigungsfristSelect.setValue("Gesetzlich (3 Monate)");
        kuendigungsfristSelect.setWidthFull();

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
        warmmieteBox.addClassName("money-summary-box");

        Div warmmieteRow = new Div();
        warmmieteRow.addClassName("money-summary-row");

        Span warmmieteLabel = new Span("Warmmiete (Gesamt)");
        warmmieteLabel.addClassName("money-summary-label");

        Span warmmieteValue = new Span("0,00 €");
        warmmieteValue.addClassName("money-summary-value");

        warmmieteRow.add(warmmieteLabel, warmmieteValue);
        warmmieteBox.add(warmmieteRow);

        configureWarmmieteCalculation(kaltmieteField, nebenkostenField, warmmieteValue);

        FormLayout form = createTwoColumnFormLayout();

        form.add(
                kaltmieteField,
                nebenkostenField,
                warmmieteBox
        );
        form.setColspan(warmmieteBox, 2);

        return createFormCard(
                "Finanzielle Details",
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
    }

    private double zahlOderNull(Double value) {
        return value == null ? 0 : value;
    }

    private String formatMoneyValue(double value) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.GERMANY);
        return formatter.format(value);
    }

    private Component createBottomActions() {
        HorizontalLayout actions = new HorizontalLayout();
        actions.addClassName("form-bottom-actions");

        Button cancelButton = new Button("Abbrechen", VaadinIcon.CLOSE.create());
        cancelButton.addClassName("secondary-button");
        cancelButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate("mieter-vertraege?tab=vertraege"))
        );

        Button saveButton = new Button("Mietvertrag anlegen", VaadinIcon.PLUS.create());
        saveButton.addClassName("primary-button");
        saveButton.addClickListener(event -> speichereMietvertrag());

        actions.add(cancelButton, saveButton);

        return actions;
    }

    private Div createFormCard(String titleText, Component content) {
        Div card = new Div();
        card.addClassName("form-card");
        card.setWidthFull();

        Div header = new Div();
        header.addClassName("form-card-header");

        Span title = new Span(titleText);
        title.addClassName("form-card-title");

        header.add(title);

        Div body = new Div();
        body.addClassName("form-card-content");
        body.add(content);

        card.add(header, body);

        return card;
    }

    private FormLayout createTwoColumnFormLayout() {
        FormLayout form = new FormLayout();
        form.addClassName("form-layout-two-columns");

        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("600px", 2)
        );

        return form;
    }

    private String formatMieter(Mieter mieter) {
        if (mieter == null) {
            return "";
        }

        return mieter.getVorname() + " " + mieter.getNachname();
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
        this.immobilieId  = leseLongQueryParameter(event, "immobilieId");
        this.mieteinheitId  = leseLongQueryParameter(event, "mieteinheitId");

        if (immobilieId  != null && mieteinheitId  != null) {
            uebernehmeMietobjektAusUrl(immobilieId, mieteinheitId);
        }
    }

    private Long leseLongQueryParameter(BeforeEnterEvent event, String parameterName) {
        return event.getLocation()
                .getQueryParameters() // Holt alle Query-Parameter aus der aktuellen URL
                .getSingleParameter(parameterName) // Holt den einzelnen Wert des gewünschten Parameters.
                .map(Long::valueOf) // umwandeln in Long
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
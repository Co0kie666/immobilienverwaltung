package de.hsbi.immobilienverwaltung.ui.mieter;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import de.hsbi.immobilienverwaltung.domain.Mietvertrag;
import de.hsbi.immobilienverwaltung.domain.enums.Vertragsstatus;
import de.hsbi.immobilienverwaltung.service.interfaces.MietvertragService;
import de.hsbi.immobilienverwaltung.ui.UiFormatUtils;
import de.hsbi.immobilienverwaltung.ui.layout.HasPageHeader;
import de.hsbi.immobilienverwaltung.ui.layout.MainLayout;
import jakarta.annotation.security.PermitAll;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Route(value = "mieter-vertraege/mietvertrag-details", layout = MainLayout.class)
@PermitAll
public class MietvertragListView extends Div implements HasPageHeader, HasUrlParameter<String> {

    private final MietvertragService mietvertragService;

    private Mietvertrag aktuellerMietvertrag;
    private boolean bearbeitenAktiv = false;

    private final DatePicker startdatumField = new DatePicker("Vertragsbeginn");
    private final DatePicker enddatumField = new DatePicker("Vertragsende");
    private final DatePicker kuendigungsfristField = new DatePicker("Kündigungsfrist bis");

    private final NumberField kaltmieteField = new NumberField("Kaltmiete");
    private final NumberField nebenkostenField = new NumberField("Nebenkosten");
    private final NumberField kautionField = new NumberField("Kaution");

    private Span editWarmmieteValue;

    public MietvertragListView(MietvertragService mietvertragService) {
        this.mietvertragService = mietvertragService;

        addClassNames("page-content", "contract-detail-page");
        bereiteBearbeitungsfelderVor();
    }

    @Override
    public void setParameter(BeforeEvent event, String mietvertragId) {
        try {
            Long id = Long.valueOf(mietvertragId);

            aktuellerMietvertrag = mietvertragService.findeMietvertragNachId(id)
                    .orElse(null);

            if (aktuellerMietvertrag == null) {
                removeAll();
                add(erstelleNichtGefundenKarte());
                return;
            }

            bearbeitenAktiv = false;
            zeigeMietvertrag();

        } catch (NumberFormatException ex) {
            removeAll();
            add(erstelleNichtGefundenKarte());
        }
    }

    // Baut die komplette Detailansicht neu auf.
    // Das wird auch nach Speichern, Abbrechen oder Kündigen genutzt.
    private void zeigeMietvertrag() {
        removeAll();

        add(
                erstelleKopfbereich(),
                erstelleKennzahlenBereich(),
                erstelleInhaltsbereich()
        );
    }

    // Diese Felder werden nur im Bearbeitungsmodus angezeigt.
    // Hier bekommen sie ihre Grundeinstellungen.
    private void bereiteBearbeitungsfelderVor() {
        startdatumField.setRequiredIndicatorVisible(true);
        startdatumField.setWidthFull();
        startdatumField.setPlaceholder("tt.mm.jjjj");

        enddatumField.setWidthFull();
        enddatumField.setPlaceholder("unbefristet");

        kuendigungsfristField.setWidthFull();
        kuendigungsfristField.setPlaceholder("tt.mm.jjjj");

        richteGeldfeldEin(kaltmieteField);
        richteGeldfeldEin(nebenkostenField);
        richteGeldfeldEin(kautionField);

        kaltmieteField.addValueChangeListener(event -> aktualisiereEditWarmmiete());
        nebenkostenField.addValueChangeListener(event -> aktualisiereEditWarmmiete());
    }

    // Gleiche Einstellungen für alle Geldfelder.
    private void richteGeldfeldEin(NumberField field) {
        field.setWidthFull();
        field.setMin(0);
        field.setStep(0.01);
        field.setClearButtonVisible(true);
        field.setSuffixComponent(new Span("€"));
    }

    // Oberer Bereich mit Zurück-Button, Titel, Status und den wichtigsten Aktionen.
    private Component erstelleKopfbereich() {
        Div hero = new Div();
        hero.addClassName("contract-detail-hero");

        Div left = new Div();
        left.addClassName("contract-detail-hero-left");

        Button backButton = new Button(VaadinIcon.ARROW_LEFT.create());
        backButton.addClassName("contract-detail-back-button");
        backButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate("mieter-vertraege?tab=vertraege"))
        );

        Div iconBox = new Div(VaadinIcon.FILE.create());
        iconBox.addClassName("contract-detail-icon");

        Div titleBox = new Div();
        titleBox.addClassName("contract-detail-title-box");

        Span eyebrow = new Span(bearbeitenAktiv ? "Bearbeitungsmodus" : "Vertragsakte");
        eyebrow.addClassName("contract-detail-eyebrow");

        H1 title = new H1("Mietvertrag MV-" + aktuellerMietvertrag.getId());
        title.addClassName("contract-detail-title");

        Div meta = new Div();
        meta.addClassName("contract-detail-meta");
        meta.add(
                erstelleInfoChip(VaadinIcon.USER, UiFormatUtils.formatiereMieterName(aktuellerMietvertrag.getMieter(), "-")),
                erstelleInfoChip(VaadinIcon.BUILDING, UiFormatUtils.formatiereImmobilienBezeichnung(aktuellerMietvertrag, "-")),
                erstelleInfoChip(VaadinIcon.HOME, UiFormatUtils.formatiereMieteinheitBezeichnung(aktuellerMietvertrag, "-"))
        );

        titleBox.add(eyebrow, title, meta);
        left.add(backButton, iconBox, titleBox);

        Div actions = new Div();
        actions.addClassName("contract-detail-hero-actions");

        Span statusBadge = new Span(UiFormatUtils.formatiereVertragsstatus(aktuellerMietvertrag));
        statusBadge.addClassNames("status-badge", ermittleStatusFarbe(), "contract-detail-status");
        actions.add(statusBadge);

        if (bearbeitenAktiv) {
            Button cancelButton = new Button("Abbrechen", VaadinIcon.CLOSE.create());
            cancelButton.addClassName("secondary-button");
            cancelButton.addClickListener(event -> {
                bearbeitenAktiv = false;
                zeigeMietvertrag();
            });

            Button kuendigenButton = new Button("Kündigen", VaadinIcon.TRASH.create());
            boolean kuendigenErlaubt = aktuellerMietvertrag.getStatus() == Vertragsstatus.AKTIV;
            kuendigenButton.addClassName(kuendigenErlaubt ? "danger-button" : "secondary-button");
            kuendigenButton.setEnabled(kuendigenErlaubt);
            kuendigenButton.addClickListener(event -> kuendigeMietvertrag());

            Button saveButton = new Button("Speichern", VaadinIcon.CHECK.create());
            saveButton.addClassName("primary-button");
            saveButton.addClickListener(event -> speichereAenderungen());

            actions.add(cancelButton, kuendigenButton, saveButton);
        } else {
            Button mieterButton = new Button("Zum Mieter", VaadinIcon.USER.create());
            mieterButton.addClassName("secondary-button");
            mieterButton.addClickListener(event -> navigiereZumMieter());

            Button einheitButton = new Button("Zur Einheit", VaadinIcon.HOME.create());
            einheitButton.addClassName("secondary-button");
            einheitButton.addClickListener(event -> navigiereZurMieteinheit());

            Button editButton = new Button("Bearbeiten", VaadinIcon.EDIT.create());
            editButton.addClassName("primary-button");
            editButton.setEnabled(aktuellerMietvertrag.getStatus() != Vertragsstatus.BEENDET);
            editButton.addClickListener(event -> {
                bearbeitenAktiv = true;
                zeigeMietvertrag();
            });

            actions.add(mieterButton, einheitButton, editButton);
        }

        hero.add(left, actions);
        return hero;
    }

    private Component erstelleInfoChip(VaadinIcon icon, String text) {
        Div pill = new Div();
        pill.addClassName("contract-detail-meta-pill");
        pill.add(icon.create(), new Span(UiFormatUtils.wertOderStrich(text)));
        return pill;
    }

    // Die vier Kennzahlen geben direkt oben einen schnellen Überblick.
    private Component erstelleKennzahlenBereich() {
        Div grid = new Div();
        grid.addClassName("contract-detail-stats-grid");

        grid.add(
                erstelleKennzahlenKarte(
                        "Warmmiete",
                        UiFormatUtils.formatiereWarmmiete(aktuellerMietvertrag),
                        "Kaltmiete + Nebenkosten",
                        VaadinIcon.EURO,
                        "success"
                ),
                erstelleKennzahlenKarte(
                        "Laufzeit",
                        formatiereLaufzeitKurz(),
                        formatiereRestlaufzeit(),
                        VaadinIcon.CALENDAR,
                        "primary"
                ),
                erstelleKennzahlenKarte(
                        "Kaution",
                        UiFormatUtils.formatiereEuroOderStrich(aktuellerMietvertrag.getKaution()),
                        "Hinterlegte Sicherheit",
                        VaadinIcon.LOCK,
                        "warning"
                ),
                erstelleKennzahlenKarte(
                        "Status",
                        UiFormatUtils.formatiereVertragsstatus(aktuellerMietvertrag),
                        "Aktueller Vertragsstand",
                        VaadinIcon.CLIPBOARD,
                        ermittleStatusFarbe()
                )
        );

        return grid;
    }

    private Component erstelleKennzahlenKarte(String label, String value, String subtitle, VaadinIcon icon, String color) {
        Div card = new Div();
        card.addClassNames("contract-detail-stat-card", color);

        Div text = new Div();
        text.addClassName("contract-detail-stat-text");

        Span labelSpan = new Span(label);
        labelSpan.addClassName("contract-detail-stat-label");

        H2 valueText = new H2(value);
        valueText.addClassName("contract-detail-stat-value");

        Span subtitleSpan = new Span(subtitle);
        subtitleSpan.addClassName("contract-detail-stat-subtitle");

        text.add(labelSpan, valueText, subtitleSpan);

        Div iconBox = new Div(icon.create());
        iconBox.addClassNames("contract-detail-stat-icon", color);

        card.add(text, iconBox);
        return card;
    }

    // Darunter wird die Seite in Hauptspalte und rechte Seitenspalte aufgeteilt.
    private Component erstelleInhaltsbereich() {
        Div layout = new Div();
        layout.addClassName("contract-detail-content-grid");

        Div mainColumn = new Div();
        mainColumn.addClassName("contract-detail-main-column");
        mainColumn.add(
                erstelleMieterUndEinheitKarte(),
                erstelleLaufzeitKarte()
        );

        Div sideColumn = new Div();
        sideColumn.addClassName("contract-detail-side-column");
        sideColumn.add(
                erstelleFinanzenKarte(),
                erstelleSchnellzugriffKarte()
        );

        layout.add(mainColumn, sideColumn);
        return layout;
    }

    // Zeigt, welcher Mieter zu welcher Immobilie und Mieteinheit gehört.
    private Component erstelleMieterUndEinheitKarte() {
        Div card = erstelleInfoKarte("Mieter & Einheit", "Wer mietet welches Objekt?");

        Div grid = new Div();
        grid.addClassName("contract-detail-info-grid");

        grid.add(
                erstelleInfoBlock("Hauptmieter", UiFormatUtils.formatiereMieterMitEmail(aktuellerMietvertrag.getMieter()), VaadinIcon.USER),
                erstelleInfoBlock("Immobilie", UiFormatUtils.formatiereImmobilienBezeichnung(aktuellerMietvertrag, "-"), VaadinIcon.BUILDING),
                erstelleInfoBlock("Mieteinheit", UiFormatUtils.formatiereMieteinheitDetails(aktuellerMietvertrag), VaadinIcon.HOME)
        );

        card.add(grid);
        return card;
    }

    // Im normalen Modus werden die Daten nur angezeigt.
    // Im Bearbeitungsmodus werden daraus DatePicker-Felder.
    private Component erstelleLaufzeitKarte() {
        Div card = erstelleInfoKarte("Vertragslaufzeit & Fristen", bearbeitenAktiv ? "Daten bearbeiten" : "Beginn, Ende und Kündigungsfrist");

        if (bearbeitenAktiv) {
            startdatumField.setValue(aktuellerMietvertrag.getStartdatum());
            enddatumField.setValue(aktuellerMietvertrag.getEnddatum());
            kuendigungsfristField.setValue(aktuellerMietvertrag.getKuendigungsfrist());

            Div formGrid = new Div();
            formGrid.addClassName("contract-detail-edit-grid");
            formGrid.add(startdatumField, enddatumField, kuendigungsfristField);

            card.add(formGrid);
        } else {
            Div timeline = new Div();
            timeline.addClassName("contract-detail-timeline");
            timeline.add(
                    erstelleZeitpunkt("Vertragsbeginn", UiFormatUtils.formatiereDatum(aktuellerMietvertrag.getStartdatum()), VaadinIcon.PLAY),
                    erstelleZeitpunkt("Vertragsende", formatiereEnddatum(), VaadinIcon.FLAG),
                    erstelleZeitpunkt("Kündigungsfrist", formatiereKuendigungsfrist(), VaadinIcon.CLOCK)
            );

            card.add(timeline);
        }

        return card;
    }

    // Zeigt die Beträge an oder macht sie im Bearbeitungsmodus bearbeitbar.
    private Component erstelleFinanzenKarte() {
        Div card = erstelleInfoKarte("Finanzen", bearbeitenAktiv ? "Monatliche Beträge bearbeiten" : "Miete, Nebenkosten und Kaution");
        card.addClassName("contract-detail-finance-card");

        if (bearbeitenAktiv) {
            kaltmieteField.setValue(aktuellerMietvertrag.getKaltmiete());
            nebenkostenField.setValue(aktuellerMietvertrag.getNebenkosten());
            kautionField.setValue(aktuellerMietvertrag.getKaution());

            Div formGrid = new Div();
            formGrid.addClassNames("contract-detail-edit-grid", "single");
            formGrid.add(kaltmieteField, nebenkostenField, kautionField);

            Div summary = new Div();
            summary.addClassName("contract-detail-money-summary");

            Span label = new Span("Warmmiete");
            label.addClassName("contract-detail-money-label");

            editWarmmieteValue = new Span();
            editWarmmieteValue.addClassName("contract-detail-money-value");
            aktualisiereEditWarmmiete();

            summary.add(label, editWarmmieteValue);
            card.add(formGrid, summary);
        } else {
            Div moneyGrid = new Div();
            moneyGrid.addClassName("contract-detail-money-grid");
            moneyGrid.add(
                    erstelleGeldBox("Kaltmiete", UiFormatUtils.formatiereEuroOderStrich(aktuellerMietvertrag.getKaltmiete())),
                    erstelleGeldBox("Nebenkosten", UiFormatUtils.formatiereEuroOderStrich(aktuellerMietvertrag.getNebenkosten())),
                    erstelleGeldBox("Warmmiete", UiFormatUtils.formatiereWarmmiete(aktuellerMietvertrag)),
                    erstelleGeldBox("Kaution", UiFormatUtils.formatiereEuroOderStrich(aktuellerMietvertrag.getKaution()))
            );

            card.add(moneyGrid);
        }

        return card;
    }

    // Kleine Abkürzungen zu den verbundenen Detailseiten.
    private Component erstelleSchnellzugriffKarte() {
        Div card = erstelleInfoKarte("Schnellzugriff", "Direkt zu verbundenen Datensätzen");
        card.addClassName("contract-detail-quick-card");

        Button mieterButton = new Button("Mieter anzeigen", VaadinIcon.USER.create());
        mieterButton.addClassName("primary-button");
        mieterButton.setWidthFull();
        mieterButton.addClickListener(event -> navigiereZumMieter());

        Button einheitButton = new Button("Mieteinheit öffnen", VaadinIcon.HOME.create());
        einheitButton.addClassName("secondary-button");
        einheitButton.setWidthFull();
        einheitButton.addClickListener(event -> navigiereZurMieteinheit());

        Div buttons = new Div();
        buttons.addClassName("contract-detail-quick-actions");
        buttons.add(mieterButton, einheitButton);

        card.add(buttons);
        return card;
    }

    // Einheitliche Karte mit Überschrift und Untertitel.
    private Div erstelleInfoKarte(String titleText, String subtitleText) {
        Div card = new Div();
        card.addClassName("contract-detail-card");

        Div header = new Div();
        header.addClassName("contract-detail-card-header");

        H3 title = new H3(titleText);
        title.addClassName("contract-detail-card-title");

        Paragraph subtitle = new Paragraph(subtitleText);
        subtitle.addClassName("contract-detail-card-subtitle");

        header.add(title, subtitle);
        card.add(header);

        return card;
    }

    private Component erstelleInfoBlock(String labelText, String valueText, VaadinIcon icon) {
        Div block = new Div();
        block.addClassName("contract-detail-highlight-block");

        Div iconBox = new Div(icon.create());
        iconBox.addClassName("contract-detail-highlight-icon");

        Div text = new Div();
        text.addClassName("contract-detail-highlight-text");

        Span label = new Span(labelText);
        label.addClassName("contract-detail-info-label");

        Span value = new Span(UiFormatUtils.wertOderStrich(valueText));
        value.addClassName("contract-detail-info-value");

        text.add(label, value);
        block.add(iconBox, text);

        return block;
    }

    private Component erstelleZeitpunkt(String labelText, String valueText, VaadinIcon icon) {
        Div item = new Div();
        item.addClassName("contract-detail-timeline-item");

        Div iconBox = new Div(icon.create());
        iconBox.addClassName("contract-detail-timeline-icon");

        Div text = new Div();
        text.addClassName("contract-detail-timeline-text");

        Span label = new Span(labelText);
        label.addClassName("contract-detail-info-label");

        Span value = new Span(UiFormatUtils.wertOderStrich(valueText));
        value.addClassName("contract-detail-info-value");

        text.add(label, value);
        item.add(iconBox, text);

        return item;
    }

    private Component erstelleGeldBox(String labelText, String valueText) {
        Div box = new Div();
        box.addClassName("contract-detail-money-box");

        Span label = new Span(labelText);
        label.addClassName("contract-detail-money-box-label");

        Span value = new Span(UiFormatUtils.wertOderStrich(valueText));
        value.addClassName("contract-detail-money-box-value");

        box.add(label, value);
        return box;
    }

    // Öffnet die Detailansicht des Mieters, falls ein Mieter vorhanden ist.
    private void navigiereZumMieter() {
        if (aktuellerMietvertrag.getMieter() == null || aktuellerMietvertrag.getMieter().getId() == null) {
            Notification.show("Mieter wurde nicht gefunden.", 3000, Notification.Position.MIDDLE);
            return;
        }

        getUI().ifPresent(ui -> ui.navigate(
                MieterListView.class,
                String.valueOf(aktuellerMietvertrag.getMieter().getId())
        ));
    }

    // Öffnet die Detailansicht der Mieteinheit.
    // Dafür werden die ID der Immobilie und die ID der Einheit gebraucht.
    private void navigiereZurMieteinheit() {
        if (aktuellerMietvertrag.getMieteinheit() == null
                || aktuellerMietvertrag.getMieteinheit().getId() == null
                || aktuellerMietvertrag.getMieteinheit().getImmobilie() == null
                || aktuellerMietvertrag.getMieteinheit().getImmobilie().getId() == null) {
            Notification.show("Mieteinheit wurde nicht gefunden.", 3000, Notification.Position.MIDDLE);
            return;
        }

        Long immobilieId = aktuellerMietvertrag.getMieteinheit().getImmobilie().getId();
        Long mieteinheitId = aktuellerMietvertrag.getMieteinheit().getId();

        getUI().ifPresent(ui -> ui.navigate(
                "immobilien/" + immobilieId + "/einheiten/" + mieteinheitId + "/details"
        ));
    }

    // Speichert die Änderungen aus dem Bearbeitungsmodus.
    // Vorher werden Startdatum, Enddatum und Geldbeträge grob geprüft.
    private void speichereAenderungen() {
        try {
            if (startdatumField.getValue() == null) {
                startdatumField.setInvalid(true);
                startdatumField.setErrorMessage("Bitte Vertragsbeginn auswählen");
                throw new IllegalArgumentException("Startdatum muss angegeben werden.");
            }

            startdatumField.setInvalid(false);

            if (enddatumField.getValue() != null && enddatumField.getValue().isBefore(startdatumField.getValue())) {
                enddatumField.setInvalid(true);
                enddatumField.setErrorMessage("Vertragsende darf nicht vor Vertragsbeginn liegen");
                throw new IllegalArgumentException("Vertragsende darf nicht vor Vertragsbeginn liegen.");
            }

            enddatumField.setInvalid(false);

            pruefeGeldfeld(kaltmieteField, "Kaltmiete");
            pruefeGeldfeld(nebenkostenField, "Nebenkosten");
            pruefeGeldfeld(kautionField, "Kaution");

            aktuellerMietvertrag.setStartdatum(startdatumField.getValue());
            aktuellerMietvertrag.setEnddatum(enddatumField.getValue());
            aktuellerMietvertrag.setKuendigungsfrist(kuendigungsfristField.getValue());
            aktuellerMietvertrag.setKaltmiete(kaltmieteField.getValue());
            aktuellerMietvertrag.setNebenkosten(nebenkostenField.getValue());
            aktuellerMietvertrag.setKaution(kautionField.getValue());

            mietvertragService.speichereMietvertrag(
                    aktuellerMietvertrag.getMieter().getId(),
                    aktuellerMietvertrag.getMieteinheit().getId(),
                    aktuellerMietvertrag
            );

            Notification.show("Mietvertrag wurde aktualisiert");

            bearbeitenAktiv = false;
            zeigeMietvertrag();

        } catch (Exception ex) {
            Notification.show("Fehler beim Speichern: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
        }
    }

    private void pruefeGeldfeld(NumberField field, String label) {
        if (field.getValue() != null && field.getValue() < 0) {
            field.setInvalid(true);
            field.setErrorMessage(label + " darf nicht negativ sein");
            throw new IllegalArgumentException(label + " darf nicht negativ sein.");
        }

        field.setInvalid(false);
    }

    // Kündigt den aktiven Vertrag und lädt ihn danach neu aus dem Service.
    private void kuendigeMietvertrag() {
        try {
            mietvertragService.kuendigeMietvertrag(aktuellerMietvertrag.getId());

            aktuellerMietvertrag = mietvertragService.findeMietvertragNachId(aktuellerMietvertrag.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Mietvertrag wurde nicht gefunden."));

            Notification.show("Mietvertrag wurde gekündigt");

            bearbeitenAktiv = false;
            zeigeMietvertrag();

        } catch (Exception ex) {
            Notification.show("Fehler beim Kündigen: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
        }
    }

    // Im Bearbeitungsmodus wird die Warmmiete direkt neu berechnet,
    // sobald Kaltmiete oder Nebenkosten geändert werden.
    private void aktualisiereEditWarmmiete() {
        if (editWarmmieteValue == null) {
            return;
        }

        double kaltmiete = kaltmieteField.getValue() == null ? 0 : kaltmieteField.getValue();
        double nebenkosten = nebenkostenField.getValue() == null ? 0 : nebenkostenField.getValue();

        editWarmmieteValue.setText(UiFormatUtils.formatiereEuro(kaltmiete + nebenkosten));
    }

    private Component erstelleNichtGefundenKarte() {
        Div card = new Div();
        card.addClassName("empty-state");

        Span title = new Span("Mietvertrag nicht gefunden");
        title.addClassName("empty-state-title");

        Span text = new Span("Für diese ID gibt es aktuell keine Daten.");
        text.addClassName("empty-state-text");

        card.add(title, text);

        return card;
    }

    private String formatiereEnddatum() {
        if (aktuellerMietvertrag.getEnddatum() == null) {
            return "unbefristet";
        }

        return UiFormatUtils.formatiereDatum(aktuellerMietvertrag.getEnddatum());
    }

    private String formatiereKuendigungsfrist() {
        if (aktuellerMietvertrag.getStartdatum() == null
                || aktuellerMietvertrag.getKuendigungsfrist() == null) {
            return "-";
        }

        long monate = ChronoUnit.MONTHS.between(
                aktuellerMietvertrag.getStartdatum(),
                aktuellerMietvertrag.getKuendigungsfrist()
        );

        if (monate <= 0) {
            return "-";
        }

        if (monate == 1) {
            return "1 Monat";
        }

        return monate + " Monate";
    }

    private String formatiereLaufzeitKurz() {
        if (aktuellerMietvertrag.getStartdatum() == null) {
            return "-";
        }

        if (aktuellerMietvertrag.getEnddatum() == null) {
            return "Unbefristet";
        }

        long monate = ChronoUnit.MONTHS.between(
                aktuellerMietvertrag.getStartdatum(),
                aktuellerMietvertrag.getEnddatum()
        );

        if (monate <= 0) {
            return "Befristet";
        }

        if (monate < 12) {
            return monate + " Monate";
        }

        long jahre = monate / 12;
        long restMonate = monate % 12;

        if (restMonate == 0) {
            return jahre == 1 ? "1 Jahr" : jahre + " Jahre";
        }

        return jahre + " J. " + restMonate + " Mon.";
    }

    private String formatiereRestlaufzeit() {
        if (aktuellerMietvertrag.getEnddatum() == null) {
            return "ohne festes Ende";
        }

        long tage = ChronoUnit.DAYS.between(LocalDate.now(), aktuellerMietvertrag.getEnddatum());

        if (tage < 0) {
            return "bereits beendet";
        }

        if (tage == 0) {
            return "endet heute";
        }

        if (tage == 1) {
            return "endet morgen";
        }

        return "noch " + tage + " Tage";
    }

    private String ermittleStatusFarbe() {
        if (aktuellerMietvertrag.getStatus() == null) {
            return "neutral";
        }

        return switch (aktuellerMietvertrag.getStatus()) {
            case AKTIV -> "success";
            case GEKUENDIGT, BEENDET -> "warning";
        };
    }

    @Override
    public String getPageTitle() {
        return "Mietvertrag";
    }

    @Override
    public String getPageSubtitle() {
        return "Mietvertragsdaten anzeigen und bearbeiten";
    }
}

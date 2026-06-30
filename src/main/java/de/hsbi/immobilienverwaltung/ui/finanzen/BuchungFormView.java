package de.hsbi.immobilienverwaltung.ui.finanzen;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Route;
import de.hsbi.immobilienverwaltung.domain.Ausgabe;
import de.hsbi.immobilienverwaltung.domain.Immobilie;
import de.hsbi.immobilienverwaltung.domain.Mieteinheit;
import de.hsbi.immobilienverwaltung.domain.Mietvertrag;
import de.hsbi.immobilienverwaltung.domain.Zahlungseingang;
import de.hsbi.immobilienverwaltung.domain.enums.Ausgabenkategorie;
import de.hsbi.immobilienverwaltung.domain.enums.Zahlungseingangtyp;
import de.hsbi.immobilienverwaltung.service.interfaces.AusgabeService;
import de.hsbi.immobilienverwaltung.service.interfaces.ImmobilieService;
import de.hsbi.immobilienverwaltung.service.interfaces.MieteinheitService;
import de.hsbi.immobilienverwaltung.service.interfaces.MietvertragService;
import de.hsbi.immobilienverwaltung.service.interfaces.ZahlungsEingangService;
import de.hsbi.immobilienverwaltung.ui.layout.HasPageHeader;
import de.hsbi.immobilienverwaltung.ui.layout.MainLayout;
import jakarta.annotation.security.PermitAll;
import de.hsbi.immobilienverwaltung.ui.UiFormatUtils;

@Route(value = "finanzen/buchung-neu", layout = MainLayout.class)
@PermitAll
public class BuchungFormView extends VerticalLayout implements HasPageHeader {

    // Services verbinden die View mit der Geschäftslogik und der Datenbank.
    // Je nach Buchungstyp wird später entweder eine Ausgabe oder ein Zahlungseingang gespeichert.
    private final AusgabeService ausgabeService;
    private final ImmobilieService immobilieService;
    private final MieteinheitService mieteinheitService;
    private final ZahlungsEingangService zahlungsEingangService;
    private final MietvertragService mietvertragService;

    private RadioButtonGroup<String> buchungstypGroup;
    private RadioButtonGroup<String> statusGroup;

    private BigDecimalField betragField;
    private DatePicker buchungsdatumField;
    private DatePicker faelligkeitsdatumField;

    // Es gibt zwei verschiedene Kategorie-Felder,
    // weil Ausgaben und Einnahmen unterschiedliche Enums verwenden.
    private ComboBox<Ausgabenkategorie> kategorieField;
    private ComboBox<Zahlungseingangtyp> zahlungseingangTypField;
    private TextArea beschreibungField;

    private ComboBox<Immobilie> immobilieField;
    private ComboBox<Mieteinheit> mieteinheitField;
    private ComboBox<Mietvertrag> mieterVertragField;

    private Span previewTyp;
    private Span previewBetrag;
    private Span previewStatus;
    private Span previewKategorie;
    private Span previewDatum;
    private Span previewZuordnung;

    public BuchungFormView(
            AusgabeService ausgabeService,
            ImmobilieService immobilieService,
            MieteinheitService mieteinheitService,
            ZahlungsEingangService zahlungsEingangService,
            MietvertragService mietvertragService
    ) {
        this.ausgabeService = ausgabeService;
        this.immobilieService = immobilieService;
        this.mieteinheitService = mieteinheitService;
        this.zahlungsEingangService = zahlungsEingangService;
        this.mietvertragService = mietvertragService;

        addClassNames("page-content", "buchung-form-view");
        setPadding(false);
        setSpacing(false);
        setWidthFull();

        createFields();
        aktualisiereKategorieFelder();
        aktualisiereMietvertraege();

        add(createContent());
        updatePreview();
    }

    // Erstellt alle Eingabefelder und verbindet wichtige Wertänderungen mit der Live-Vorschau.
    private void createFields() {
        buchungstypGroup = new RadioButtonGroup<>();
        buchungstypGroup.setItems("Einnahme", "Ausgabe");
        buchungstypGroup.setValue("Einnahme");

        statusGroup = new RadioButtonGroup<>();
        statusGroup.setItems("Bezahlt / Erledigt", "Offen / Ausstehend");
        statusGroup.setValue("Bezahlt / Erledigt");

        betragField = new BigDecimalField("Betrag");
        betragField.setPlaceholder("0,00");
        betragField.setPrefixComponent(new Span("€"));
        betragField.setWidthFull();
        betragField.setRequiredIndicatorVisible(true);

        buchungsdatumField = new DatePicker("Buchungsdatum");
        buchungsdatumField.setPlaceholder("tt.mm.jjjj");
        buchungsdatumField.setWidthFull();
        buchungsdatumField.setRequiredIndicatorVisible(true);

        faelligkeitsdatumField = new DatePicker("Fälligkeitsdatum / Leistungsmonat");
        faelligkeitsdatumField.setPlaceholder("tt.mm.jjjj");
        faelligkeitsdatumField.setWidthFull();

        kategorieField = new ComboBox<>("Kategorie");
        kategorieField.setItems(Ausgabenkategorie.values());
        kategorieField.setItemLabelGenerator(Ausgabenkategorie::getLabel);
        kategorieField.setValue(Ausgabenkategorie.SONSTIGES);
        kategorieField.setAllowCustomValue(false);
        kategorieField.setWidthFull();
        kategorieField.setRequiredIndicatorVisible(true);

        zahlungseingangTypField = new ComboBox<>("Zahlungstyp");
        zahlungseingangTypField.setItems(Zahlungseingangtyp.values());
        zahlungseingangTypField.setItemLabelGenerator(Zahlungseingangtyp::getLabel);
        zahlungseingangTypField.setValue(Zahlungseingangtyp.SONSTIGES);
        zahlungseingangTypField.setAllowCustomValue(false);
        zahlungseingangTypField.setWidthFull();
        zahlungseingangTypField.setRequiredIndicatorVisible(true);

        beschreibungField = new TextArea("Beschreibung / Notiz");
        beschreibungField.setPlaceholder("Details zur Buchung eingeben...");
        beschreibungField.setWidthFull();
        beschreibungField.setHeight("130px");

        immobilieField = new ComboBox<>("Immobilie");
        immobilieField.setItems(immobilieService.findeAlleImmobilien());
        immobilieField.setItemLabelGenerator(
                immobilie -> UiFormatUtils.formatiereImmobilienBezeichnung(immobilie, "")
        );
        immobilieField.setAllowCustomValue(false);
        immobilieField.setWidthFull();

        mieteinheitField = new ComboBox<>("Mieteinheit");
        mieteinheitField.setItemLabelGenerator(
                mieteinheit -> UiFormatUtils.formatiereMieteinheitBezeichnung(mieteinheit, "")
        );
        mieteinheitField.setAllowCustomValue(false);
        mieteinheitField.setWidthFull();

        mieterVertragField = new ComboBox<>("Mieter / Vertrag");
        mieterVertragField.setItemLabelGenerator(UiFormatUtils::formatiereMietvertragAuswahl);
        mieterVertragField.setAllowCustomValue(false);
        mieterVertragField.setWidthFull();

        // Wenn eine Immobilie gewählt wird, werden die passenden Mieteinheiten geladen.
        // Danach werden auch die möglichen Mietverträge aktualisiert.
        immobilieField.addValueChangeListener(event -> {
            Immobilie selectedImmobilie = event.getValue();

            mieteinheitField.clear();
            mieterVertragField.clear();

            if (selectedImmobilie != null) {
                mieteinheitField.setItems(
                        mieteinheitService.findeMieteinheitenNachImmobilie(
                                selectedImmobilie.getId()
                        )
                );
            }

            aktualisiereMietvertraege();
            updatePreview();
        });

        // Bei Auswahl einer Mieteinheit werden die Mietverträge weiter eingeschränkt.
        mieteinheitField.addValueChangeListener(event -> {
            mieterVertragField.clear();
            aktualisiereMietvertraege();
            updatePreview();
        });

        mieterVertragField.addValueChangeListener(event -> updatePreview());
        betragField.addValueChangeListener(event -> updatePreview());
        buchungsdatumField.addValueChangeListener(event -> updatePreview());
        faelligkeitsdatumField.addValueChangeListener(event -> updatePreview());
        kategorieField.addValueChangeListener(event -> updatePreview());
        zahlungseingangTypField.addValueChangeListener(event -> updatePreview());
        beschreibungField.addValueChangeListener(event -> updatePreview());
    }

    private Component createContent() {
        Div shell = new Div();
        shell.addClassName("buchung-form-shell");

        shell.add(
                createHero(),
                createMainLayout()
        );

        return shell;
    }

    private Component createHero() {
        Div hero = new Div();
        hero.addClassName("buchung-form-hero");

        Div left = new Div();

        Span eyebrow = new Span("Neue Buchung");
        eyebrow.addClassName("buchungen-eyebrow");

        H2 title = new H2("Finanzbuchung erfassen");
        title.addClassName("buchungen-hero-title");

        Paragraph subtitle = new Paragraph(
                "Erfasse Einnahmen und Ausgaben schnell, ordne sie Immobilien oder Verträgen zu und behalte den Status im Blick."
        );
        subtitle.addClassName("buchungen-hero-subtitle");

        left.add(eyebrow, title, subtitle);

        Div illustration = new Div();
        illustration.addClassName("buchung-form-illustration");
        illustration.add(new Icon(VaadinIcon.WALLET));

        hero.add(left, illustration);

        return hero;
    }

    private Component createMainLayout() {
        Div layout = new Div();
        layout.addClassName("buchung-form-layout");

        Div leftColumn = new Div();
        leftColumn.addClassName("buchung-form-left");

        leftColumn.add(
                createBuchungstypSection(),
                createKerndatenSection()
        );

        Div rightColumn = new Div();
        rightColumn.addClassName("buchung-form-right");

        rightColumn.add(
                createPreviewSection(),
                createStatusSection(),
                createZuordnungSection(),
                createActionSection()
        );

        layout.add(leftColumn, rightColumn);

        return layout;
    }

    private Component createBuchungstypSection() {
        Div card = createCard(
                "Buchungstyp",
                "Wähle aus, ob es sich um eine Einnahme oder Ausgabe handelt."
        );

        Div options = new Div();
        options.addClassName("booking-type-options");

        Div incomeCard = createBookingTypeCard(
                "Einnahme / Zahlungseingang",
                "Miete, Nebenkosten, Kaution oder sonstige Einnahmen",
                VaadinIcon.ARROW_DOWN,
                "income",
                true
        );

        Div expenseCard = createBookingTypeCard(
                "Ausgabe / Zahlungsausgang",
                "Instandhaltung, Verwaltung oder sonstige Kosten",
                VaadinIcon.ARROW_UP,
                "expense",
                false
        );

        incomeCard.addClickListener(event -> selectBuchungstyp("Einnahme", incomeCard, expenseCard));
        expenseCard.addClickListener(event -> selectBuchungstyp("Ausgabe", incomeCard, expenseCard));

        options.add(incomeCard, expenseCard);
        card.add(options);

        return card;
    }

    // Wechselt zwischen Einnahme und Ausgabe.
    // Dabei werden passende Felder ein- oder ausgeblendet und die Vorschau aktualisiert.
    private void selectBuchungstyp(String typ, Div incomeCard, Div expenseCard) {
        buchungstypGroup.setValue(typ);

        boolean istEinnahme = "Einnahme".equals(typ);

        incomeCard.setClassName("booking-type-card income");
        expenseCard.setClassName("booking-type-card expense");

        if (istEinnahme) {
            incomeCard.addClassName("selected");
            mieterVertragField.setEnabled(true);
        } else {
            expenseCard.addClassName("selected");
            mieterVertragField.clear();
            mieterVertragField.setEnabled(false);
        }

        aktualisiereKategorieFelder();
        updatePreview();
    }

    private Div createBookingTypeCard(
            String title,
            String subtitle,
            VaadinIcon icon,
            String colorClass,
            boolean selected
    ) {
        Div option = new Div();
        option.addClassNames("booking-type-card", colorClass);

        if (selected) {
            option.addClassName("selected");
        }

        Span radio = new Span();
        radio.addClassName("fake-radio");

        Div text = new Div();

        Span titleSpan = new Span(title);
        titleSpan.addClassName("booking-type-title");

        Span subtitleSpan = new Span(subtitle);
        subtitleSpan.addClassName("booking-type-subtitle");

        text.add(titleSpan, subtitleSpan);

        Icon typeIcon = icon.create();
        typeIcon.addClassNames("booking-type-icon", colorClass);

        option.add(radio, text, typeIcon);

        return option;
    }

    // Zeigt je nach Buchungstyp nur das passende Kategorie-Feld an:
    // Ausgaben nutzen Ausgabenkategorie, Einnahmen nutzen Zahlungseingangtyp.
    private void aktualisiereKategorieFelder() {
        boolean istAusgabe = "Ausgabe".equals(buchungstypGroup.getValue());

        kategorieField.setVisible(istAusgabe);
        zahlungseingangTypField.setVisible(!istAusgabe);
    }

    private Component createKerndatenSection() {
        Div card = createCard(
                "Kerndaten",
                "Betrag, Datum, Kategorie und Beschreibung der Buchung."
        );

        FormLayout form = createTwoColumnFormLayout();

        form.add(
                betragField,
                buchungsdatumField,
                kategorieField,
                zahlungseingangTypField,
                faelligkeitsdatumField
        );

        form.setColspan(faelligkeitsdatumField, 2);

        card.add(form, beschreibungField);

        return card;
    }

    private Component createPreviewSection() {
        Div card = new Div();
        card.addClassNames("form-card", "buchung-preview-card");

        Span label = new Span("Live-Vorschau");
        label.addClassName("buchung-preview-label");

        previewTyp = new Span("-");
        previewTyp.addClassName("buchung-preview-type");

        previewBetrag = new Span("0,00 €");
        previewBetrag.addClassName("buchung-preview-amount");

        Div details = new Div();
        details.addClassName("buchung-preview-details");

        previewStatus = new Span("-");
        previewKategorie = new Span("-");
        previewDatum = new Span("-");
        previewZuordnung = new Span("-");

        details.add(
                createPreviewRow("Status", previewStatus),
                createPreviewRow("Kategorie", previewKategorie),
                createPreviewRow("Datum", previewDatum),
                createPreviewRow("Zuordnung", previewZuordnung)
        );

        card.add(label, previewTyp, previewBetrag, details);

        return card;
    }

    private Component createPreviewRow(String label, Span value) {
        Div row = new Div();
        row.addClassName("buchung-preview-row");

        Span labelSpan = new Span(label);
        labelSpan.addClassName("buchung-preview-row-label");

        value.addClassName("buchung-preview-row-value");

        row.add(labelSpan, value);

        return row;
    }

    private Component createStatusSection() {
        Div card = createCard(
                "Status",
                "Markiere, ob die Buchung bereits erledigt oder noch offen ist."
        );

        Div paid = createStatusOption("Bezahlt / Erledigt", "green", true);
        Div open = createStatusOption("Offen / Ausstehend", "orange", false);

        paid.addClickListener(event -> selectStatus("Bezahlt / Erledigt", paid, open));
        open.addClickListener(event -> selectStatus("Offen / Ausstehend", paid, open));

        card.add(paid, open);

        return card;
    }

    // Setzt den ausgewählten Status und aktualisiert die Live-Vorschau.
    private void selectStatus(String status, Div paid, Div open) {
        statusGroup.setValue(status);

        paid.setClassName("status-option");
        open.setClassName("status-option");

        if ("Bezahlt / Erledigt".equals(status)) {
            paid.addClassName("selected");
        } else {
            open.addClassName("selected");
        }

        updatePreview();
    }

    private Div createStatusOption(String text, String color, boolean selected) {
        Div option = new Div();
        option.addClassName("status-option");

        if (selected) {
            option.addClassName("selected");
        }

        Span radio = new Span();
        radio.addClassName("fake-radio");

        Span dot = new Span();
        dot.addClassNames("status-dot", color);

        Span label = new Span(text);
        label.addClassName("status-label");

        option.add(radio, dot, label);

        return option;
    }

    private Component createZuordnungSection() {
        Div card = createCard(
                "Zuordnung",
                "Verknüpfe die Buchung optional mit Immobilie, Mieteinheit oder Mietvertrag."
        );

        card.add(
                immobilieField,
                mieteinheitField,
                mieterVertragField
        );

        return card;
    }

    private Component createActionSection() {
        Div card = new Div();
        card.addClassNames("form-card", "buchung-action-card");

        Button saveButton = new Button("Buchung speichern", VaadinIcon.CHECK.create());
        saveButton.addClassName("primary-button");
        saveButton.setWidthFull();
        saveButton.addClickListener(event -> speichereBuchung());

        Button cancelButton = new Button("Abbrechen");
        cancelButton.addClassName("secondary-button");
        cancelButton.setWidthFull();
        cancelButton.addClickListener(event ->
                UI.getCurrent().navigate(FinanzDashboardView.class)
        );

        card.add(saveButton, cancelButton);

        return card;
    }

    // Prüft die Eingaben und speichert je nach Buchungstyp
    // entweder eine Ausgabe oder einen Zahlungseingang in der Datenbank.
    private void speichereBuchung() {
        try {
            pruefePflichtfelder();

            if ("Ausgabe".equals(buchungstypGroup.getValue())) {
                Ausgabe ausgabe = new Ausgabe();

                ausgabe.setKategorie(kategorieField.getValue());
                ausgabe.setBetrag(betragField.getValue());
                ausgabe.setDatum(buchungsdatumField.getValue());
                ausgabe.setFaelligkeitsdatum(faelligkeitsdatumField.getValue());
                ausgabe.setBeschreibung(beschreibungField.getValue());
                ausgabe.setImmobilie(immobilieField.getValue());
                ausgabe.setStatus(statusGroup.getValue());

                ausgabeService.speichereAusgabe(ausgabe);

                Notification.show("Ausgabe gespeichert");
                UI.getCurrent().navigate(BuchungListView.class);

                return;
            }

            Zahlungseingang zahlungseingang = new Zahlungseingang();

            zahlungseingang.setTyp(zahlungseingangTypField.getValue());
            zahlungseingang.setBetrag(betragField.getValue());
            zahlungseingang.setZahlungsdatum(buchungsdatumField.getValue());
            zahlungseingang.setLeistungsmonat(faelligkeitsdatumField.getValue());
            zahlungseingang.setBeschreibung(beschreibungField.getValue());
            zahlungseingang.setMietvertrag(mieterVertragField.getValue());
            zahlungseingang.setStatus(statusGroup.getValue());

            zahlungsEingangService.speichereZahlungseingang(zahlungseingang);

            Notification.show("Zahlungseingang gespeichert");
            UI.getCurrent().navigate(BuchungListView.class);

        } catch (IllegalArgumentException e) {
            Notification.show(e.getMessage(), 4000, Notification.Position.MIDDLE);
        }
    }

    // Prüft die Pflichtfelder vor dem Speichern
    // und markiert fehlende Eingaben direkt im Formular.
    private void pruefePflichtfelder() {
        boolean fehler = false;

        boolean betragFehlt = betragField.getValue() == null;
        betragField.setInvalid(betragFehlt);
        betragField.setErrorMessage("Bitte Betrag eingeben");
        fehler |= betragFehlt;

        boolean datumFehlt = buchungsdatumField.getValue() == null;
        buchungsdatumField.setInvalid(datumFehlt);
        buchungsdatumField.setErrorMessage("Bitte Buchungsdatum auswählen");
        fehler |= datumFehlt;

        boolean istAusgabe = "Ausgabe".equals(buchungstypGroup.getValue());

        if (istAusgabe) {
            boolean kategorieFehlt = kategorieField.getValue() == null;
            kategorieField.setInvalid(kategorieFehlt);
            kategorieField.setErrorMessage("Bitte Kategorie auswählen");
            fehler |= kategorieFehlt;
        } else {
            boolean typFehlt = zahlungseingangTypField.getValue() == null;
            zahlungseingangTypField.setInvalid(typFehlt);
            zahlungseingangTypField.setErrorMessage("Bitte Zahlungstyp auswählen");
            fehler |= typFehlt;
        }

        if (fehler) {
            throw new IllegalArgumentException("Bitte alle Pflichtfelder korrekt ausfüllen.");
        }
    }

    // Aktualisiert die auswählbaren Mietverträge abhängig von Immobilie und Mieteinheit.
    // Wird keine Zuordnung gewählt, bleiben alle Mietverträge auswählbar.
    private void aktualisiereMietvertraege() {
        Immobilie immobilie = immobilieField.getValue();
        Mieteinheit mieteinheit = mieteinheitField.getValue();

        mieterVertragField.clear();

        mieterVertragField.setItems(
                mietvertragService.findeAlleMietvertraege()
                        .stream()
                        .filter(vertrag -> vertrag.getMieteinheit() != null)
                        .filter(vertrag -> {
                            Mieteinheit vertragMieteinheit =
                                    vertrag.getMieteinheit();

                            if (mieteinheit != null) {
                                return mieteinheit.getId() != null
                                        && mieteinheit.getId().equals(
                                        vertragMieteinheit.getId()
                                );
                            }

                            if (immobilie != null) {
                                return vertragMieteinheit.getImmobilie() != null
                                        && immobilie.getId() != null
                                        && immobilie.getId().equals(
                                        vertragMieteinheit
                                                .getImmobilie()
                                                .getId()
                                );
                            }

                            return true;
                        })
                        .toList()
        );
    }

    // Aktualisiert die Live-Vorschau auf der rechten Seite
    // anhand der aktuellen Eingaben im Formular.
    private void updatePreview() {
        if (previewTyp == null) {
            return;
        }

        boolean istAusgabe = "Ausgabe".equals(buchungstypGroup.getValue());

        previewTyp.setText(istAusgabe ? "Ausgabe" : "Einnahme");
        previewTyp.setClassName("buchung-preview-type");
        previewTyp.addClassName(istAusgabe ? "expense" : "income");

        previewBetrag.setText(UiFormatUtils.formatiereBetrag(betragField.getValue()));
        previewBetrag.setClassName("buchung-preview-amount");
        previewBetrag.addClassName(istAusgabe ? "expense" : "income");

        previewStatus.setText(statusGroup.getValue() == null ? "-" : statusGroup.getValue());

        if (istAusgabe) {
            previewKategorie.setText(
                    kategorieField.getValue() == null
                            ? "-"
                            : kategorieField.getValue().getLabel()
            );
        } else {
            previewKategorie.setText(
                    zahlungseingangTypField.getValue() == null
                            ? "-"
                            : zahlungseingangTypField.getValue().getLabel()
            );
        }

        previewDatum.setText(
                buchungsdatumField.getValue() == null
                        ? "-"
                        : UiFormatUtils.formatiereDatum(buchungsdatumField.getValue())
        );

        previewZuordnung.setText(ermittleZuordnungPreview());
    }

    // Ermittelt, welche Zuordnung in der Vorschau angezeigt wird.
    // Priorität: Mietvertrag vor Mieteinheit vor Immobilie.
    private String ermittleZuordnungPreview() {
        if (mieterVertragField.getValue() != null) {
            return UiFormatUtils.formatiereMietvertragAuswahl(mieterVertragField.getValue());
        }

        if (mieteinheitField.getValue() != null) {
            return UiFormatUtils.formatiereMieteinheitBezeichnung(
                    mieteinheitField.getValue(),
                    "-"
            );
        }

        if (immobilieField.getValue() != null) {
            return UiFormatUtils.formatiereImmobilienBezeichnung(
                    immobilieField.getValue(),
                    "-"
            );
        }

        return "-";
    }

    private Div createCard(String title, String subtitle) {
        Div card = new Div();
        card.addClassName("form-card");

        H3 heading = new H3(title);
        heading.addClassName("form-card-title");

        Paragraph description = new Paragraph(subtitle);
        description.addClassName("form-card-subtitle");

        card.add(heading, description);

        return card;
    }

    // Erstellt ein responsives Formularlayout,
    // das je nach Bildschirmbreite ein- oder zweispaltig angezeigt wird.
    private FormLayout createTwoColumnFormLayout() {
        FormLayout form = new FormLayout();
        form.addClassName("buchung-form-grid");

        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("640px", 2)
        );

        return form;
    }

    @Override
    public String getPageTitle() {
        return "Buchung anlegen";
    }

    @Override
    public String getPageSubtitle() {
        return "Finanzen › Übersicht › Neue Buchung";
    }
}
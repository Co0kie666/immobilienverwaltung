package de.hsbi.immobilienverwaltung.ui.finanzen;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import de.hsbi.immobilienverwaltung.domain.Ausgabe;
import de.hsbi.immobilienverwaltung.domain.Immobilie;
import de.hsbi.immobilienverwaltung.domain.Mieteinheit;
import de.hsbi.immobilienverwaltung.domain.Mieter;
import de.hsbi.immobilienverwaltung.domain.Mietvertrag;
import de.hsbi.immobilienverwaltung.domain.Zahlungseingang;
import de.hsbi.immobilienverwaltung.domain.enums.Ausgabenkategorie;
import de.hsbi.immobilienverwaltung.domain.enums.Vertragsstatus;
import de.hsbi.immobilienverwaltung.domain.enums.Zahlungseingangtyp;
import de.hsbi.immobilienverwaltung.service.interfaces.AusgabeService;
import de.hsbi.immobilienverwaltung.service.interfaces.MietvertragService;
import de.hsbi.immobilienverwaltung.service.interfaces.ZahlungsEingangService;
import de.hsbi.immobilienverwaltung.ui.components.ConfirmDeleteDialog;
import de.hsbi.immobilienverwaltung.ui.layout.HasPageHeader;
import de.hsbi.immobilienverwaltung.ui.layout.MainLayout;
import de.hsbi.immobilienverwaltung.ui.mieter.MieterListView;
import de.hsbi.immobilienverwaltung.ui.mieter.MietvertragListView;
import jakarta.annotation.security.PermitAll;
import de.hsbi.immobilienverwaltung.ui.UiFormatUtils;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Route(value = "finanzen/buchungen/:typ/:id", layout = MainLayout.class)
@PermitAll
public class BuchungDetailView extends VerticalLayout implements HasPageHeader, BeforeEnterObserver {

    private final AusgabeService ausgabeService;
    private final ZahlungsEingangService zahlungsEingangService;
    private final MietvertragService mietvertragService;

    private String typ;
    private Long id;
    private boolean bearbeitenAktiv = false;

    private Ausgabe aktuelleAusgabe;
    private Zahlungseingang aktuellerZahlungseingang;

    private BigDecimalField betragField;
    private DatePicker datumField;
    private DatePicker zweitesDatumField;
    private ComboBox<Ausgabenkategorie> ausgabeKategorieField;
    private ComboBox<Zahlungseingangtyp> zahlungseingangTypField;
    private ComboBox<String> statusField;
    private TextArea beschreibungField;

    public BuchungDetailView(AusgabeService ausgabeService,
                             ZahlungsEingangService zahlungsEingangService,
                             MietvertragService mietvertragService) {
        this.ausgabeService = ausgabeService;
        this.zahlungsEingangService = zahlungsEingangService;
        this.mietvertragService = mietvertragService;

        addClassNames("page-content", "buchung-detail-page");
        setWidthFull();
        setMinHeight("100%");
        setPadding(false);
        setSpacing(false);

        createFields();
    }

    // Wird aufgerufen, bevor die Detailseite geöffnet wird.
    // Liest den Buchungstyp und die ID aus der URL und lädt die passende Buchung.
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        typ = event.getRouteParameters().get("typ").orElse("").toLowerCase();
        String idText = event.getRouteParameters().get("id").orElse("");

        try {
            id = Long.parseLong(idText);
        } catch (NumberFormatException e) {
            Notification.show("Ungültige Buchungs-ID.");
            event.rerouteTo(BuchungListView.class);
            return;
        }

        if ("ausgabe".equals(typ)) {
            if (!ladeAusgabe(event)) {
                return;
            }
        } else if ("einnahme".equals(typ)) {
            if (!ladeZahlungseingang(event)) {
                return;
            }
        } else {
            Notification.show("Unbekannter Buchungstyp.");
            event.rerouteTo(BuchungListView.class);
            return;
        }

        bearbeitenAktiv = false;
        renderView();
    }

    // Lädt eine Ausgabe anhand der ID aus der Datenbank.
    // Gibt false zurück, wenn keine passende Ausgabe gefunden wurde.
    private boolean ladeAusgabe(BeforeEnterEvent event) {
        aktuelleAusgabe = ausgabeService.findeAusgabeNachId(id).orElse(null);

        if (aktuelleAusgabe == null) {
            Notification.show("Ausgabe wurde nicht gefunden.");
            event.rerouteTo(BuchungListView.class);
            return false;
        }

        aktuellerZahlungseingang = null;
        return true;
    }

    // Lädt einen Zahlungseingang anhand der ID aus der Datenbank.
    // Gibt false zurück, wenn kein passender Zahlungseingang gefunden wurde.
    private boolean ladeZahlungseingang(BeforeEnterEvent event) {
        aktuellerZahlungseingang = zahlungsEingangService.findeZahlungseingangNachId(id).orElse(null);

        if (aktuellerZahlungseingang == null) {
            Notification.show("Zahlungseingang wurde nicht gefunden.");
            event.rerouteTo(BuchungListView.class);
            return false;
        }

        aktuelleAusgabe = null;
        return true;
    }

    // Baut die Seite neu auf.
    // Das wird genutzt, um zwischen Ansicht und Bearbeitungsmodus zu wechseln.
    private void renderView() {
        removeAll();

        add(
                createHero(),
                createStatsGrid(),
                createContentLayout()
        );
    }

    // Erstellt die Eingabefelder für den Bearbeitungsmodus.
    // Die Felder werden später je nach Buchungstyp mit Daten befüllt.
    private void createFields() {
        betragField = new BigDecimalField("Betrag (€)");
        betragField.setPrefixComponent(new Span("€"));
        betragField.setWidthFull();

        datumField = new DatePicker("Datum");
        datumField.setWidthFull();

        zweitesDatumField = new DatePicker("Fälligkeitsdatum / Leistungsmonat");
        zweitesDatumField.setWidthFull();

        ausgabeKategorieField = new ComboBox<>("Kategorie");
        ausgabeKategorieField.setItems(Ausgabenkategorie.values());
        ausgabeKategorieField.setItemLabelGenerator(Ausgabenkategorie::getLabel);
        ausgabeKategorieField.setWidthFull();

        zahlungseingangTypField = new ComboBox<>("Zahlungstyp");
        zahlungseingangTypField.setItems(Zahlungseingangtyp.values());
        zahlungseingangTypField.setItemLabelGenerator(Zahlungseingangtyp::getLabel);
        zahlungseingangTypField.setWidthFull();

        statusField = new ComboBox<>("Status");
        statusField.setItems("Bezahlt / Erledigt", "Offen / Ausstehend");
        statusField.setWidthFull();

        beschreibungField = new TextArea("Beschreibung / Notiz");
        beschreibungField.setWidthFull();
        beschreibungField.setHeight("150px");
    }

    // Erstellt den oberen Bereich der Detailseite mit Titel, Status und Aktionsbuttons.
    private Component createHero() {
        Div hero = new Div();
        hero.addClassNames("buchung-detail-hero", istAusgabe() ? "expense" : "income");

        Div left = new Div();
        left.addClassName("buchung-detail-hero-left");

        Button backButton = new Button(VaadinIcon.ARROW_LEFT.create());
        backButton.addClassName("buchung-detail-back-button");
        backButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate(BuchungListView.class))
        );

        Div avatar = new Div(istAusgabe() ? VaadinIcon.ARROW_UP.create() : VaadinIcon.ARROW_DOWN.create());
        avatar.addClassNames("buchung-detail-avatar", istAusgabe() ? "expense" : "income");

        Div titleBox = new Div();
        titleBox.addClassName("buchung-detail-title-box");

        Span eyebrow = new Span(bearbeitenAktiv ? "Bearbeitungsmodus" : "Buchungsdetail");
        eyebrow.addClassName("buchung-detail-eyebrow");

        H3 title = new H3(getBuchungTypLabel() + " " + getBuchungNummer());
        title.addClassName("buchung-detail-title");

        Div meta = new Div();
        meta.addClassName("buchung-detail-meta");
        meta.add(
                createMetaPill(VaadinIcon.FILE_TEXT, formatKategorie()),
                createMetaPill(VaadinIcon.CALENDAR, UiFormatUtils.formatiereDatum(getDatum())),
                createMetaPill(VaadinIcon.BUILDING, UiFormatUtils.formatiereImmobilienBezeichnung(ermittleImmobilie(), "-"))
        );

        titleBox.add(eyebrow, title, meta);
        left.add(backButton, avatar, titleBox);

        Div actions = new Div();
        actions.addClassName("buchung-detail-hero-actions");

        Span statusBadge = new Span(formatStatusKurz());
        statusBadge.addClassNames("status-badge", getStatusStyle(), "buchung-detail-status");
        actions.add(statusBadge);

        if (bearbeitenAktiv) {
            Button ansichtButton = new Button("Ansicht");
            ansichtButton.addClassName("secondary-button");
            ansichtButton.addClickListener(event -> {
                bearbeitenAktiv = false;
                renderView();
            });

            Button speichernButton = new Button("Speichern", VaadinIcon.CHECK.create());
            speichernButton.addClassName("primary-button");
            speichernButton.addClickListener(event -> speichereBuchung());

            actions.add(ansichtButton, speichernButton, createLoeschenButton());
        } else {
            Button bearbeitenButton = new Button("Bearbeiten", VaadinIcon.EDIT.create());
            bearbeitenButton.addClassName("secondary-button");
            bearbeitenButton.addClickListener(event -> {
                bearbeitenAktiv = true;
                renderView();
            });

            actions.add(bearbeitenButton, createLoeschenButton());
        }

        hero.add(left, actions);
        return hero;
    }

    private Component createMetaPill(VaadinIcon icon, String value) {
        Div pill = new Div();
        pill.addClassName("buchung-detail-meta-pill");
        pill.add(icon.create(), new Span(UiFormatUtils.wertOderStrich(value)));
        return pill;
    }

    private Button createLoeschenButton() {
        Button loeschenButton = new Button("Löschen", VaadinIcon.TRASH.create());
        loeschenButton.addClassName("danger-button");
        loeschenButton.addClickListener(event -> oeffneLoeschDialog());
        return loeschenButton;
    }

    // Erstellt die Übersichtskarten mit den wichtigsten Informationen der Buchung.
    private Component createStatsGrid() {
        Div grid = new Div();
        grid.addClassName("buchung-detail-stats-grid");

        grid.add(
                createStatCard(
                        "Betrag",
                        UiFormatUtils.formatiereBetragOderStrich(getBetrag()),
                        istAusgabe() ? "Zahlungsausgang" : "Zahlungseingang",
                        istAusgabe() ? VaadinIcon.ARROW_UP : VaadinIcon.ARROW_DOWN,
                        istAusgabe() ? "danger" : "success"
                ),
                createStatCard(
                        "Status",
                        formatStatusKurz(),
                        UiFormatUtils.wertOderStrich(getStatus()),
                        VaadinIcon.CHECK,
                        getStatusStyle()
                ),
                createStatCard(
                        getDatumLabel(),
                        UiFormatUtils.formatiereDatum(getDatum()),
                        getZweitesDatumLabel() + ": " + UiFormatUtils.formatiereDatum(getZweitesDatum()),
                        VaadinIcon.CALENDAR,
                        "primary"
                ),
                createStatCard(
                        "Zuordnung",
                        UiFormatUtils.formatiereImmobilienBezeichnung(ermittleImmobilie(), "-"),
                        UiFormatUtils.formatiereMieteinheitBezeichnung(ermittleMieteinheit(), "-"),
                        VaadinIcon.BUILDING,
                        "primary"
                )
        );

        return grid;
    }

    private Component createStatCard(
            String label,
            String value,
            String subtitle,
            VaadinIcon icon,
            String color
    ) {
        Div card = new Div();
        card.addClassNames("buchung-detail-stat-card", color);

        Div text = new Div();
        text.addClassName("buchung-detail-stat-text");

        Span labelSpan = new Span(label);
        labelSpan.addClassName("buchung-detail-stat-label");

        Span valueSpan = new Span(UiFormatUtils.wertOderStrich(value));
        valueSpan.addClassName("buchung-detail-stat-value");

        Span subtitleSpan = new Span(UiFormatUtils.wertOderStrich(subtitle));
        subtitleSpan.addClassName("buchung-detail-stat-subtitle");

        text.add(labelSpan, valueSpan, subtitleSpan);

        Div iconBox = new Div(icon.create());
        iconBox.addClassNames("buchung-detail-stat-icon", color);

        card.add(text, iconBox);
        return card;
    }

    // Erstellt den Hauptbereich der Seite.
    // Je nach Modus wird entweder die Detailansicht oder das Bearbeitungsformular angezeigt.
    private Component createContentLayout() {
        Div contentGrid = new Div();
        contentGrid.addClassName("buchung-detail-content-grid");

        Div mainColumn = new Div();
        mainColumn.addClassName("buchung-detail-main-column");

        if (bearbeitenAktiv) {
            mainColumn.add(createEditCard());
        } else {
            mainColumn.add(
                    createKerndatenCard(),
                    createBeschreibungCard()
            );
        }

        Div sideColumn = new Div();
        sideColumn.addClassName("buchung-detail-side-column");
        sideColumn.add(
                createZuordnungCard(),
                createNavigationCard()
        );

        contentGrid.add(mainColumn, sideColumn);
        return contentGrid;
    }

    private Component createKerndatenCard() {
        Div card = createCard(
                "Buchungsdaten",
                "Kerninformationen dieser Buchung",
                VaadinIcon.FILE_TEXT
        );

        card.add(
                createReadonlyInfoBlock("Typ", getBuchungTypLabel(), VaadinIcon.FILE_TEXT),
                createReadonlyInfoBlock(getKategorieLabel(), formatKategorie(), VaadinIcon.TAG),
                createReadonlyInfoBlock("Betrag", UiFormatUtils.formatiereBetragOderStrich(getBetrag()), istAusgabe() ? VaadinIcon.ARROW_UP : VaadinIcon.ARROW_DOWN),
                createReadonlyInfoBlock(getDatumLabel(), UiFormatUtils.formatiereDatum(getDatum()), VaadinIcon.CALENDAR),
                createReadonlyInfoBlock(getZweitesDatumLabel(), UiFormatUtils.formatiereDatum(getZweitesDatum()), VaadinIcon.CALENDAR),
                createStatusInfoBlock()
        );

        if (istAusgabe()) {
            card.add(
                    createReadonlyInfoBlock("Titel", UiFormatUtils.wertOderStrich(aktuelleAusgabe.getTitel()), VaadinIcon.FILE_TEXT),
                    createReadonlyInfoBlock("Empfänger", UiFormatUtils.wertOderStrich(aktuelleAusgabe.getEmpfaenger()), VaadinIcon.USER)
            );
        }

        return card;
    }

    private Component createBeschreibungCard() {
        Div card = createCard(
                "Beschreibung",
                "Notiz und Zusatzinformationen",
                VaadinIcon.FILE_TEXT
        );

        Div note = new Div();
        note.addClassName("buchung-detail-note-box");
        note.add(new Span(UiFormatUtils.wertOderStrich(getBeschreibung())));

        card.add(note);
        return card;
    }
    // Erstellt das Formular zum Bearbeiten der Buchung.
    private Component createEditCard() {
        befuelleFelder();

        Div card = createCard(
                "Buchungsdaten bearbeiten",
                "Änderungen werden erst nach dem Speichern übernommen",
                VaadinIcon.EDIT
        );
        card.addClassName("buchung-detail-edit-card");

        Div row1 = new Div();
        row1.addClassName("buchung-detail-form-row");
        row1.add(betragField, datumField);

        Div row2 = new Div();
        row2.addClassName("buchung-detail-form-row");
        row2.add(ausgabeKategorieField, zahlungseingangTypField, statusField);

        Div row3 = new Div();
        row3.addClassName("buchung-detail-form-row");
        row3.add(zweitesDatumField);

        card.add(row1, row2, row3, beschreibungField);

        return card;
    }

    private Component createZuordnungCard() {
        Div card = createCard(
                "Zuordnung",
                "Verknüpfte Immobilie, Einheit und Person",
                VaadinIcon.BUILDING
        );

        card.add(
                createReadonlyInfoBlock("Immobilie", UiFormatUtils.formatiereImmobilienBezeichnung(ermittleImmobilie(), "-"), VaadinIcon.BUILDING),
                createReadonlyInfoBlock("Mieteinheit", UiFormatUtils.formatiereMieteinheitBezeichnung(ermittleMieteinheit(), "-"), VaadinIcon.FILE_TEXT),
                createReadonlyInfoBlock("Mietvertrag", UiFormatUtils.formatiereMietvertragMitPunkten(ermittleMietvertrag()), VaadinIcon.FILE_TEXT),
                createReadonlyInfoBlock("Mieter", UiFormatUtils.formatiereMieterName(ermittleMieter(), "-"), VaadinIcon.USER)
        );

        return card;
    }

    private Component createNavigationCard() {
        Div card = createCard(
                "Schnellzugriff",
                "Direkt zu verbundenen Datensätzen springen",
                VaadinIcon.ARROW_RIGHT
        );

        Div buttonRow = new Div();
        buttonRow.addClassName("buchung-detail-quick-actions");

        Button immobilieButton = new Button("Immobilie", VaadinIcon.BUILDING.create());
        immobilieButton.addClassName("secondary-button");
        immobilieButton.setWidthFull();
        immobilieButton.setEnabled(ermittleImmobilie() != null && ermittleImmobilie().getId() != null);
        immobilieButton.addClickListener(event -> navigiereZurImmobilie());

        Button mietvertragButton = new Button("Mietvertrag", VaadinIcon.FILE_TEXT.create());
        mietvertragButton.addClassName("secondary-button");
        mietvertragButton.setWidthFull();
        mietvertragButton.setEnabled(ermittleMietvertrag() != null && ermittleMietvertrag().getId() != null);
        mietvertragButton.addClickListener(event -> navigiereZumMietvertrag());

        Button mieterButton = new Button("Mieter", VaadinIcon.USER.create());
        mieterButton.addClassName("secondary-button");
        mieterButton.setWidthFull();
        mieterButton.setEnabled(ermittleMieter() != null && ermittleMieter().getId() != null);
        mieterButton.addClickListener(event -> navigiereZumMieter());

        buttonRow.add(immobilieButton, mietvertragButton, mieterButton);
        card.add(buttonRow);

        return card;
    }

    // Befüllt die Eingabefelder mit den aktuellen Werten der geladenen Buchung.
    private void befuelleFelder() {
        betragField.setValue(getBetrag());
        datumField.setValue(getDatum());
        zweitesDatumField.setValue(getZweitesDatum());
        statusField.setValue(getStatus());
        beschreibungField.setValue(getBeschreibung() != null ? getBeschreibung() : "");

        datumField.setLabel(getDatumLabel());
        zweitesDatumField.setLabel(getZweitesDatumLabel());

        if (istAusgabe()) {
            ausgabeKategorieField.setValue(aktuelleAusgabe.getKategorie());
            ausgabeKategorieField.setVisible(true);
            zahlungseingangTypField.setVisible(false);
        } else {
            zahlungseingangTypField.setValue(aktuellerZahlungseingang.getTyp());
            ausgabeKategorieField.setVisible(false);
            zahlungseingangTypField.setVisible(true);
        }
    }

    // Speichert die geänderten Werte in der Datenbank.
    // Je nach Buchungstyp wird entweder eine Ausgabe oder ein Zahlungseingang aktualisiert.
    private void speichereBuchung() {
        try {
            if (istAusgabe()) {
                aktuelleAusgabe.setBetrag(betragField.getValue());
                aktuelleAusgabe.setDatum(datumField.getValue());
                aktuelleAusgabe.setFaelligkeitsdatum(zweitesDatumField.getValue());
                aktuelleAusgabe.setKategorie(ausgabeKategorieField.getValue());
                aktuelleAusgabe.setStatus(statusField.getValue());
                aktuelleAusgabe.setBeschreibung(beschreibungField.getValue());

                ausgabeService.speichereAusgabe(aktuelleAusgabe);
                Notification.show("Ausgabe gespeichert");
            } else {
                aktuellerZahlungseingang.setBetrag(betragField.getValue());
                aktuellerZahlungseingang.setZahlungsdatum(datumField.getValue());
                aktuellerZahlungseingang.setLeistungsmonat(zweitesDatumField.getValue());
                aktuellerZahlungseingang.setTyp(zahlungseingangTypField.getValue());
                aktuellerZahlungseingang.setStatus(statusField.getValue());
                aktuellerZahlungseingang.setBeschreibung(beschreibungField.getValue());

                zahlungsEingangService.speichereZahlungseingang(aktuellerZahlungseingang);
                Notification.show("Zahlungseingang gespeichert");
            }

            bearbeitenAktiv = false;
            renderView();
        } catch (IllegalArgumentException e) {
            Notification.show(e.getMessage(), 4000, Notification.Position.MIDDLE);
        }
    }

    // Öffnet einen Bestätigungsdialog und löscht die Buchung erst nach Bestätigung.
    private void oeffneLoeschDialog() {
        ConfirmDeleteDialog dialog = new ConfirmDeleteDialog(
                getBuchungTypLabel() + " löschen?",
                "Möchtest du diese Buchung wirklich löschen?",
                () -> {
                    try {
                        if (istAusgabe()) {
                            ausgabeService.loescheAusgabe(id);
                            Notification.show("Ausgabe gelöscht");
                        } else {
                            zahlungsEingangService.loescheZahlungseingang(id);
                            Notification.show("Zahlungseingang gelöscht");
                        }

                        UI.getCurrent().navigate(BuchungListView.class);
                    } catch (Exception ex) {
                        Notification.show(ex.getMessage(), 4000, Notification.Position.MIDDLE);
                    }
                }
        );

        dialog.open();
    }

    // Navigiert zur verknüpften Immobilie, falls eine Zuordnung vorhanden ist.
    private void navigiereZurImmobilie() {
        Immobilie immobilie = ermittleImmobilie();

        if (immobilie == null || immobilie.getId() == null) {
            Notification.show("Immobilie wurde nicht gefunden.", 3000, Notification.Position.MIDDLE);
            return;
        }

        getUI().ifPresent(ui -> ui.navigate("immobilien/" + immobilie.getId()));
    }

    // Navigiert zum verknüpften Mietvertrag, falls eine Zuordnung vorhanden ist.
    private void navigiereZumMietvertrag() {
        Mietvertrag mietvertrag = ermittleMietvertrag();

        if (mietvertrag == null || mietvertrag.getId() == null) {
            Notification.show("Mietvertrag wurde nicht gefunden.", 3000, Notification.Position.MIDDLE);
            return;
        }

        getUI().ifPresent(ui -> ui.navigate(
                MietvertragListView.class,
                String.valueOf(mietvertrag.getId())
        ));
    }

    // Navigiert zum verknüpften Mieter, falls eine Zuordnung vorhanden ist.
    private void navigiereZumMieter() {
        Mieter mieter = ermittleMieter();

        if (mieter == null || mieter.getId() == null) {
            Notification.show("Mieter wurde nicht gefunden.", 3000, Notification.Position.MIDDLE);
            return;
        }

        getUI().ifPresent(ui -> ui.navigate(
                MieterListView.class,
                String.valueOf(mieter.getId())
        ));
    }

    private Div createCard(String title, String subtitle, VaadinIcon icon) {
        Div card = new Div();
        card.addClassName("buchung-detail-card");

        Div header = new Div();
        header.addClassName("buchung-detail-card-header");

        Div titleBox = new Div();
        titleBox.addClassName("buchung-detail-card-title-box");

        Div iconBox = new Div(icon.create());
        iconBox.addClassName("buchung-detail-card-icon");

        Div textBox = new Div();

        Span titleText = new Span(title);
        titleText.addClassName("buchung-detail-card-title");

        Span subtitleText = new Span(subtitle);
        subtitleText.addClassName("buchung-detail-card-subtitle");

        textBox.add(titleText, subtitleText);
        titleBox.add(iconBox, textBox);
        header.add(titleBox);
        card.add(header);

        return card;
    }

    private Component createReadonlyInfoBlock(String labelText, String valueText, VaadinIcon icon) {
        Div row = new Div();
        row.addClassName("buchung-detail-info-row");

        Div iconBox = new Div(icon.create());
        iconBox.addClassName("buchung-detail-info-icon");

        Div text = new Div();
        text.addClassName("buchung-detail-info-text");

        Span label = new Span(labelText);
        label.addClassName("buchung-detail-info-label");

        Span value = new Span(UiFormatUtils.wertOderStrich(valueText));
        value.addClassName("buchung-detail-info-value");

        text.add(label, value);
        row.add(iconBox, text);

        return row;
    }

    private Component createStatusInfoBlock() {
        Div row = new Div();
        row.addClassName("buchung-detail-info-row");

        Div iconBox = new Div(VaadinIcon.CHECK.create());
        iconBox.addClassName("buchung-detail-info-icon");

        Div text = new Div();
        text.addClassName("buchung-detail-info-text");

        Span label = new Span("Status");
        label.addClassName("buchung-detail-info-label");

        Span statusBadge = new Span(formatStatusKurz());
        statusBadge.addClassNames("status-badge", getStatusStyle());

        text.add(label, statusBadge);
        row.add(iconBox, text);

        return row;
    }

    private boolean istAusgabe() {
        return "ausgabe".equals(typ);
    }

    private String getBuchungTypLabel() {
        if ("ausgabe".equals(typ)) {
            return "Ausgabe";
        }

        if ("einnahme".equals(typ)) {
            return "Einnahme";
        }

        return "Buchung";
    }

    private String getBuchungNummer() {
        return "#" + id;
    }

    private String getKategorieLabel() {
        return istAusgabe() ? "Kategorie" : "Zahlungstyp";
    }

    // Gibt die passende Kategorie-Bezeichnung zurück.
    // Bei Ausgaben wird die Ausgabenkategorie verwendet, bei Einnahmen der Zahlungstyp.
    private String formatKategorie() {
        if (istAusgabe()) {
            return aktuelleAusgabe.getKategorie() == null ? "-" : aktuelleAusgabe.getKategorie().getLabel();
        }

        return aktuellerZahlungseingang.getTyp() == null ? "-" : aktuellerZahlungseingang.getTyp().getLabel();
    }

    private BigDecimal getBetrag() {
        return istAusgabe() ? aktuelleAusgabe.getBetrag() : aktuellerZahlungseingang.getBetrag();
    }

    private LocalDate getDatum() {
        return istAusgabe() ? aktuelleAusgabe.getDatum() : aktuellerZahlungseingang.getZahlungsdatum();
    }

    private LocalDate getZweitesDatum() {
        return istAusgabe() ? aktuelleAusgabe.getFaelligkeitsdatum() : aktuellerZahlungseingang.getLeistungsmonat();
    }

    private String getDatumLabel() {
        return istAusgabe() ? "Buchungsdatum" : "Zahlungsdatum";
    }

    private String getZweitesDatumLabel() {
        return istAusgabe() ? "Fälligkeitsdatum" : "Leistungsmonat";
    }

    private String getStatus() {
        return istAusgabe() ? aktuelleAusgabe.getStatus() : aktuellerZahlungseingang.getStatus();
    }

    private String getBeschreibung() {
        return istAusgabe() ? aktuelleAusgabe.getBeschreibung() : aktuellerZahlungseingang.getBeschreibung();
    }

    private String formatStatusKurz() {
        if ("Bezahlt / Erledigt".equals(getStatus())) {
            return "Bezahlt";
        }

        if ("Offen / Ausstehend".equals(getStatus())) {
            return "Offen";
        }

        return UiFormatUtils.wertOderStrich(getStatus());
    }

    private String getStatusStyle() {
        if ("Bezahlt / Erledigt".equals(getStatus())) {
            return "success";
        }

        if ("Offen / Ausstehend".equals(getStatus())) {
            return "warning";
        }

        return "neutral";
    }

    // Ermittelt die zugehörige Immobilie.
    // Bei Einnahmen wird sie über den Mietvertrag und die Mieteinheit gefunden.
    private Immobilie ermittleImmobilie() {
        if (istAusgabe()) {
            if (aktuelleAusgabe.getImmobilie() != null) {
                return aktuelleAusgabe.getImmobilie();
            }

            if (aktuelleAusgabe.getMieteinheit() != null) {
                return aktuelleAusgabe.getMieteinheit().getImmobilie();
            }

            return null;
        }

        Mietvertrag mietvertrag = aktuellerZahlungseingang.getMietvertrag();

        if (mietvertrag == null || mietvertrag.getMieteinheit() == null) {
            return null;
        }

        return mietvertrag.getMieteinheit().getImmobilie();
    }

    private Mieteinheit ermittleMieteinheit() {
        if (istAusgabe()) {
            return aktuelleAusgabe.getMieteinheit();
        }

        Mietvertrag mietvertrag = aktuellerZahlungseingang.getMietvertrag();
        return mietvertrag == null ? null : mietvertrag.getMieteinheit();
    }

    private Mietvertrag ermittleMietvertrag() {
        if (!istAusgabe()) {
            return aktuellerZahlungseingang.getMietvertrag();
        }

        return findeMietvertragFuerAusgabe();
    }

    // Sucht zu einer Ausgabe den passenden Mietvertrag über die Mieteinheit.
    // Bevorzugt wird ein aktiver Mietvertrag.
    private Mietvertrag findeMietvertragFuerAusgabe() {
        Mieteinheit mieteinheit = aktuelleAusgabe.getMieteinheit();

        if (mieteinheit == null || mieteinheit.getId() == null) {
            return null;
        }

        List<Mietvertrag> vertraege = mietvertragService.findeMietvertraegeNachMieteinheit(mieteinheit.getId());

        if (vertraege.isEmpty()) {
            return null;
        }

        for (Mietvertrag vertrag : vertraege) {
            if (vertrag.getStatus() == Vertragsstatus.AKTIV) {
                return vertrag;
            }
        }

        for (Mietvertrag vertrag : vertraege) {
            if (vertrag.getStatus() == Vertragsstatus.GEKUENDIGT) {
                return vertrag;
            }
        }

        return vertraege.getFirst();
    }

    private Mieter ermittleMieter() {
        Mietvertrag mietvertrag = ermittleMietvertrag();
        return mietvertrag == null ? null : mietvertrag.getMieter();
    }

    @Override
    public String getPageTitle() {
        return getBuchungTypLabel() + " Details";
    }

    @Override
    public String getPageSubtitle() {
        return "Finanzen › Buchungsdetails";
    }
}

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
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
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

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

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

        addClassName("page-content");
        addClassName("buchung-form-view");
        setWidthFull();
        setMinHeight("100%");
        setPadding(false);
        setSpacing(true);

        createFields();
    }

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

    private void renderView() {
        removeAll();

        add(
                createHeaderCard(),
                createContentLayout()
        );
    }

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
        beschreibungField.setHeight("140px");
    }

    private Component createHeaderCard() {
        Div headerCard = new Div();
        headerCard.addClassName("card");
        headerCard.addClassName("page-section");

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        Button backButton = new Button(VaadinIcon.ARROW_LEFT.create());
        backButton.addClassName("icon-button");
        backButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate(BuchungListView.class))
        );

        VerticalLayout titleArea = new VerticalLayout();
        titleArea.setPadding(false);
        titleArea.setSpacing(false);

        HorizontalLayout titleRow = new HorizontalLayout();
        titleRow.setAlignItems(FlexComponent.Alignment.CENTER);
        titleRow.setSpacing(true);

        Span title = new Span(getBuchungTypLabel() + " " + getBuchungNummer());
        title.addClassName("card-title");

        Span statusBadge = new Span(formatStatusKurz());
        statusBadge.addClassNames("status-badge", getStatusStyle());

        titleRow.add(title, statusBadge);

        Span subtitle = new Span(formatKategorie() + " • " + formatiereBetrag(getBetrag()));
        subtitle.addClassName("card-subtitle");

        titleArea.add(titleRow, subtitle);

        HorizontalLayout leftArea = new HorizontalLayout();
        leftArea.setAlignItems(FlexComponent.Alignment.CENTER);
        leftArea.setSpacing(true);
        leftArea.add(backButton, titleArea);

        HorizontalLayout rightArea = new HorizontalLayout();
        rightArea.setAlignItems(FlexComponent.Alignment.CENTER);
        rightArea.setSpacing(true);

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

            Button loeschenButton = createLoeschenButton();

            rightArea.add(ansichtButton, speichernButton, loeschenButton);
        } else {
            Button bearbeitenButton = new Button("Bearbeiten", VaadinIcon.EDIT.create());
            bearbeitenButton.addClassName("secondary-button");
            bearbeitenButton.addClickListener(event -> {
                bearbeitenAktiv = true;
                renderView();
            });

            rightArea.add(bearbeitenButton, createLoeschenButton());
        }

        header.add(leftArea, rightArea);
        headerCard.add(header);

        return headerCard;
    }

    private Button createLoeschenButton() {
        Button loeschenButton = new Button("Löschen", VaadinIcon.TRASH.create());
        loeschenButton.addClassName("danger-button");
        loeschenButton.addClickListener(event -> oeffneLoeschDialog());
        return loeschenButton;
    }

    private Component createContentLayout() {
        HorizontalLayout contentLayout = new HorizontalLayout();
        contentLayout.setWidthFull();
        contentLayout.setSpacing(true);
        contentLayout.setAlignItems(FlexComponent.Alignment.START);

        VerticalLayout leftColumn = new VerticalLayout();
        leftColumn.setPadding(false);
        leftColumn.setSpacing(true);
        leftColumn.setWidth("0");
        leftColumn.getStyle().set("min-width", "0");

        if (bearbeitenAktiv) {
            leftColumn.add(createEditCard());
        } else {
            leftColumn.add(
                    createKerndatenCard(),
                    createBeschreibungCard()
            );
        }

        VerticalLayout rightColumn = new VerticalLayout();
        rightColumn.setPadding(false);
        rightColumn.setSpacing(true);
        rightColumn.setWidth("380px");
        rightColumn.getStyle().set("min-width", "360px");

        rightColumn.add(
                createZuordnungCard(),
                createNavigationCard()
        );

        contentLayout.add(leftColumn, rightColumn);
        contentLayout.setFlexGrow(1, leftColumn);
        contentLayout.setFlexGrow(0, rightColumn);

        return contentLayout;
    }

    private Component createKerndatenCard() {
        Div card = createCard("Buchungsdaten");

        card.add(
                createReadonlyInfoBlock("Typ", getBuchungTypLabel()),
                createReadonlyInfoBlock(getKategorieLabel(), formatKategorie()),
                createReadonlyInfoBlock("Betrag", formatiereBetrag(getBetrag())),
                createReadonlyInfoBlock(getDatumLabel(), formatDatum(getDatum())),
                createReadonlyInfoBlock(getZweitesDatumLabel(), formatDatum(getZweitesDatum())),
                createReadonlyInfoBlock("Status", textOderStrich(getStatus()))
        );

        if (istAusgabe()) {
            card.add(
                    createReadonlyInfoBlock("Titel", textOderStrich(aktuelleAusgabe.getTitel())),
                    createReadonlyInfoBlock("Empfänger", textOderStrich(aktuelleAusgabe.getEmpfaenger()))
            );
        }

        return card;
    }

    private Component createBeschreibungCard() {
        Div card = createCard("Beschreibung");
        card.add(createReadonlyInfoBlock("Notiz", textOderStrich(getBeschreibung())));
        return card;
    }

    private Component createEditCard() {
        befuelleFelder();

        Div card = createCard("Buchungsdaten bearbeiten");

        HorizontalLayout row1 = new HorizontalLayout();
        row1.setWidthFull();
        row1.addClassName("form-row");
        row1.add(betragField, datumField);

        HorizontalLayout row2 = new HorizontalLayout();
        row2.setWidthFull();
        row2.addClassName("form-row");
        row2.add(ausgabeKategorieField, zahlungseingangTypField, statusField);

        HorizontalLayout row3 = new HorizontalLayout();
        row3.setWidthFull();
        row3.addClassName("form-row");
        row3.add(zweitesDatumField);

        card.add(row1, row2, row3, beschreibungField);

        return card;
    }

    private Component createZuordnungCard() {
        Div card = createCard("Zuordnung");

        card.add(
                createReadonlyInfoBlock("Immobilie", formatiereImmobilie(ermittleImmobilie())),
                createReadonlyInfoBlock("Mieteinheit", formatiereMieteinheit(ermittleMieteinheit())),
                createReadonlyInfoBlock("Mietvertrag", formatiereMietvertrag(ermittleMietvertrag())),
                createReadonlyInfoBlock("Mieter", formatiereMieter(ermittleMieter()))
        );

        return card;
    }

    private Component createNavigationCard() {
        Div card = createCard("Schnellzugriff");
        Div buttonRow = new Div();
        buttonRow.setWidthFull();
        buttonRow.getStyle().set("display", "grid");
        buttonRow.getStyle().set("grid-template-columns", "repeat(2, minmax(0, 1fr))");
        buttonRow.getStyle().set("gap", "12px");

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

    private void navigiereZurImmobilie() {
        Immobilie immobilie = ermittleImmobilie();

        if (immobilie == null || immobilie.getId() == null) {
            Notification.show("Immobilie wurde nicht gefunden.", 3000, Notification.Position.MIDDLE);
            return;
        }

        getUI().ifPresent(ui -> ui.navigate("immobilien/" + immobilie.getId()));
    }

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

    private Div createCard(String title) {
        Div card = new Div();
        card.addClassName("card");
        card.setWidthFull();

        H3 heading = new H3(title);
        heading.addClassName("card-title");

        card.add(heading);

        return card;
    }

    private Component createReadonlyInfoBlock(String labelText, String valueText) {
        VerticalLayout block = new VerticalLayout();
        block.setPadding(false);
        block.setSpacing(false);

        Span label = new Span(labelText);
        label.addClassName("card-subtitle");

        Span value = new Span(valueText);

        block.add(label, value);

        return block;
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

        return textOderStrich(getStatus());
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

        return vertraege.get(0);
    }

    private Mieter ermittleMieter() {
        Mietvertrag mietvertrag = ermittleMietvertrag();
        return mietvertrag == null ? null : mietvertrag.getMieter();
    }

    private String formatiereImmobilie(Immobilie immobilie) {
        if (immobilie == null || immobilie.getBezeichnung() == null || immobilie.getBezeichnung().isBlank()) {
            return "-";
        }

        return immobilie.getBezeichnung();
    }

    private String formatiereMieteinheit(Mieteinheit mieteinheit) {
        if (mieteinheit == null || mieteinheit.getBezeichnung() == null || mieteinheit.getBezeichnung().isBlank()) {
            return "-";
        }

        return mieteinheit.getBezeichnung();
    }

    private String formatiereMietvertrag(Mietvertrag mietvertrag) {
        if (mietvertrag == null) {
            return "-";
        }

        return "MV-" + mietvertrag.getId()
                + " • "
                + formatiereMieter(mietvertrag.getMieter())
                + " • "
                + formatiereMieteinheit(mietvertrag.getMieteinheit());
    }

    private String formatiereMieter(Mieter mieter) {
        if (mieter == null) {
            return "-";
        }

        String name = (wertOderLeer(mieter.getVorname()) + " " + wertOderLeer(mieter.getNachname())).trim();
        return name.isBlank() ? "-" : name;
    }

    private String formatiereBetrag(BigDecimal betrag) {
        if (betrag == null) {
            return "-";
        }

        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.GERMANY);
        return formatter.format(betrag);
    }

    private String formatDatum(LocalDate datum) {
        if (datum == null) {
            return "-";
        }

        return datum.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }

    private String textOderStrich(String text) {
        return text == null || text.isBlank() ? "-" : text;
    }

    private String wertOderLeer(String wert) {
        return wert == null ? "" : wert;
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

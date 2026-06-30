package de.hsbi.immobilienverwaltung.ui.finanzen;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import de.hsbi.immobilienverwaltung.domain.Ausgabe;
import de.hsbi.immobilienverwaltung.domain.Immobilie;
import de.hsbi.immobilienverwaltung.domain.Mietvertrag;
import de.hsbi.immobilienverwaltung.domain.Zahlungseingang;
import de.hsbi.immobilienverwaltung.domain.enums.Ausgabenkategorie;
import de.hsbi.immobilienverwaltung.domain.enums.Vertragsstatus;
import de.hsbi.immobilienverwaltung.domain.enums.Zahlungseingangtyp;
import de.hsbi.immobilienverwaltung.service.interfaces.AusgabeService;
import de.hsbi.immobilienverwaltung.service.interfaces.MietvertragService;
import de.hsbi.immobilienverwaltung.service.interfaces.ZahlungsEingangService;
import de.hsbi.immobilienverwaltung.ui.UiFormatUtils;
import de.hsbi.immobilienverwaltung.ui.layout.HasPageHeader;
import de.hsbi.immobilienverwaltung.ui.layout.MainLayout;
import jakarta.annotation.security.PermitAll;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Route(value = "finanzen/buchungen", layout = MainLayout.class)
@PermitAll
public class BuchungListView extends Div implements HasPageHeader {

    // Feste Werte für Filter und Pagination.
    // So werden gleiche Texte nicht mehrfach im Code verteilt.
    private static final String ALLE_KATEGORIEN = "Alle Kategorien";
    private static final String ALLE_IMMOBILIEN = "Alle Immobilien";
    private static final int BUCHUNGEN_PRO_SEITE = 20;

    private final Grid<BuchungRow> grid = new Grid<>(BuchungRow.class, false);

    // UI-Komponenten für Suche, Filter und Seitensteuerung der Buchungsliste.
    private final TextField searchField = new TextField("Suche");
    private final Select<String> buchungTypSelect = new Select<>();
    private final Select<String> statusSelect = new Select<>();
    private final Select<String> kategorieSelect = new Select<>();
    private final Select<String> immobilieSelect = new Select<>();
    private final Button vorherigeSeiteButton = new Button("Zurück");
    private final Button naechsteSeiteButton = new Button("Weiter");
    private final Span seitenInfo = new Span();

    // gefilterteBuchungen enthält nur die aktuell durch Suche und Filter passenden Einträge.
    // aktuelleSeite bestimmt, welche Seite dieser gefilterten Liste angezeigt wird.
    private List<BuchungRow> gefilterteBuchungen = new ArrayList<>();
    private int aktuelleSeite = 0;

    // Services liefern die Daten aus der Geschäftslogik.
    // Ausgaben und Zahlungseingänge werden später zu einer gemeinsamen Tabellenliste zusammengeführt.
    private final AusgabeService ausgabeService;
    private final ZahlungsEingangService zahlungsEingangService;
    private final MietvertragService mietvertragService;

    // alleBuchungen enthält alle geladenen Buchungen aus der Datenbank.
    private List<BuchungRow> alleBuchungen = new ArrayList<>();

    public BuchungListView(
            AusgabeService ausgabeService,
            ZahlungsEingangService zahlungsEingangService,
            MietvertragService mietvertragService
    ) {
        this.ausgabeService = ausgabeService;
        this.zahlungsEingangService = zahlungsEingangService;
        this.mietvertragService = mietvertragService;

        addClassNames("buchung-list-view", "buchung-list-modern", "page-content");

        ladeBuchungen();

        add(
                createHeroSection(),
                createKpiGrid(),
                createFilterCard(),
                createTableCard()
        );
    }

    @Override
    public String getPageTitle() {
        return "Buchungsübersicht";
    }

    @Override
    public String getPageSubtitle() {
        return "Finanzen › Alle Buchungen";
    }

    private Component createHeroSection() {
        Div hero = new Div();
        hero.addClassName("booking-list-hero");

        Div content = new Div();
        content.addClassName("booking-list-hero-content");

        Span eyebrow = new Span("Buchungsübersicht");
        eyebrow.addClassName("booking-list-eyebrow");

        H2 title = new H2("Finanzbuchungen im Griff");
        title.addClassName("booking-list-hero-title");

        Paragraph subtitle = new Paragraph(
                "Durchsuche Einnahmen und Ausgaben, filtere nach Immobilie, Vertrag, Kategorie oder Status und springe direkt in die Buchungsdetails."
        );
        subtitle.addClassName("booking-list-hero-subtitle");

        Div actions = new Div();
        actions.addClassName("booking-list-hero-actions");

        Button neueBuchungButton = new Button("Neue Buchung", VaadinIcon.EURO.create());
        neueBuchungButton.addClassName("primary-button");
        neueBuchungButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate(BuchungFormView.class))
        );

        Button dashboardButton = new Button("Zurück zum Dashboard", VaadinIcon.ARROW_LEFT.create());
        dashboardButton.addClassName("secondary-button");
        dashboardButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate(FinanzDashboardView.class))
        );

        actions.add(neueBuchungButton, dashboardButton);
        content.add(eyebrow, title, subtitle, actions);

        hero.add(content);
        return hero;
    }

    // Erstellt die Kennzahlen-Karten oberhalb der Tabelle,
    // z. B. Gesamtanzahl, Einnahmen, Ausgaben und offene Buchungen.
    private Component createKpiGrid() {
        Div grid = new Div();
        grid.addClassNames("booking-list-kpi-grid", "dashboard-kpi-grid");

        grid.add(
                createKpiCard(
                        "Alle Buchungen",
                        String.valueOf(alleBuchungen.size()),
                        "gesamt",
                        "Einnahmen und Ausgaben",
                        VaadinIcon.ARCHIVE,
                        "primary"
                ),
                createKpiCard(
                        "Einnahmen",
                        String.valueOf(zaehleTyp("Einnahme")),
                        "bezahlt",
                        "Zahlungseingänge",
                        VaadinIcon.ARROW_DOWN,
                        "success"
                ),
                createKpiCard(
                        "Ausgaben",
                        String.valueOf(zaehleTyp("Ausgabe")),
                        "erfasst",
                        "Zahlungsausgänge",
                        VaadinIcon.ARROW_UP,
                        "danger"
                ),
                createKpiCard(
                        "Offene Buchungen",
                        String.valueOf(zaehleOffeneBuchungen()),
                        "offen",
                        "Noch ausstehend",
                        VaadinIcon.CLOCK,
                        "warning"
                )
        );

        return grid;
    }

    private Component createKpiCard(
            String title,
            String value,
            String badge,
            String subtitle,
            VaadinIcon icon,
            String color
    ) {
        Div card = new Div();
        card.addClassNames("kpi-card", "dashboard-kpi-card", color);

        Div header = new Div();
        header.addClassName("kpi-card-header");

        Div iconBox = new Div(icon.create());
        iconBox.addClassNames("kpi-icon-box", color);

        Span badgeSpan = new Span(badge);

        if (!badge.isBlank()) {
            badgeSpan.addClassNames("status-badge", color);
        }

        header.add(iconBox, badgeSpan);

        Paragraph titleText = new Paragraph(title);
        titleText.addClassName("kpi-title");

        H2 valueText = new H2(value);
        valueText.addClassName("kpi-value");

        Div subtitleText = new Div();
        subtitleText.setText(subtitle);
        subtitleText.addClassName("kpi-subtitle");

        card.add(
                header,
                titleText,
                valueText,
                subtitleText
        );

        return card;
    }

    // Erstellt die Such- und Filterleiste.
    // Jede Änderung an einem Filter ruft filtereBuchungen() auf.
    private Div createFilterCard() {
        Div filterCard = new Div();
        filterCard.addClassName("filter-card");

        Div header = new Div();
        header.addClassName("booking-list-filter-header");

        Div titleBox = new Div();

        H3 title = new H3("Buchungen filtern");
        title.addClassName("card-title");

        Paragraph subtitle = new Paragraph("Suche gezielt nach Beschreibung, Mietvertrag, Status, Kategorie oder Immobilie.");
        subtitle.addClassName("card-subtitle");

        titleBox.add(title, subtitle);

        Span filterBadge = new Span("20 pro Seite");
        filterBadge.addClassNames("status-badge", "primary");

        header.add(titleBox, filterBadge);

        Div fields = new Div();
        fields.addClassName("booking-list-filter-fields");

        searchField.setPlaceholder("Beschreibung, Kategorie, Mietvertrag...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);

        buchungTypSelect.setLabel("Typ");
        buchungTypSelect.setItems("Alle", "Einnahme", "Ausgabe");
        buchungTypSelect.setValue("Alle");

        statusSelect.setLabel("Status");
        statusSelect.setItems("Alle", "Bezahlt", "Offen");
        statusSelect.setValue("Alle");

        kategorieSelect.setLabel("Kategorie");
        aktualisiereKategorieFilter();

        immobilieSelect.setLabel("Immobilie");
        aktualisiereImmobilienFilter();

        searchField.addValueChangeListener(event -> filtereBuchungen());

        buchungTypSelect.addValueChangeListener(event -> {
            aktualisiereKategorieFilter();
            filtereBuchungen();
        });

        statusSelect.addValueChangeListener(event -> filtereBuchungen());
        kategorieSelect.addValueChangeListener(event -> filtereBuchungen());
        immobilieSelect.addValueChangeListener(event -> filtereBuchungen());

        fields.add(
                searchField,
                buchungTypSelect,
                statusSelect,
                kategorieSelect,
                immobilieSelect
        );

        filterCard.add(header, fields);

        return filterCard;
    }

    private Div createTableCard() {
        Div tableCard = new Div();
        tableCard.addClassName("table-card");

        Div header = new Div();
        header.addClassName("booking-list-table-header");

        Div titleBox = new Div();

        H3 title = new H3("Buchungsliste");
        title.addClassName("card-title");

        Paragraph subtitle = new Paragraph("Gefilterte Übersicht mit Immobilie, Mietvertrag, Status und Betrag.");
        subtitle.addClassName("card-subtitle");

        titleBox.add(title, subtitle);

        Span badge = new Span(alleBuchungen.size() + " Buchungen");
        badge.addClassNames("status-badge", "neutral");

        header.add(titleBox, badge);

        configureGrid();

        tableCard.add(header, grid, createPaginationBar());

        return tableCard;
    }

    // Erstellt die Seitensteuerung unter der Tabelle.
    // Damit werden nur 20 Buchungen pro Seite angezeigt.
    private Div createPaginationBar() {
        Div paginationBar = new Div();
        paginationBar.addClassName("pagination-bar");

        vorherigeSeiteButton.addClassName("secondary-button");
        naechsteSeiteButton.addClassName("secondary-button");

        vorherigeSeiteButton.addClickListener(event -> {
            if (aktuelleSeite > 0) {
                aktuelleSeite--;
                aktualisiereAngezeigteSeite();
            }
        });

        naechsteSeiteButton.addClickListener(event -> {
            if (aktuelleSeite < ermittleLetzteSeite()) {
                aktuelleSeite++;
                aktualisiereAngezeigteSeite();
            }
        });

        Div buttons = new Div();
        buttons.addClassName("pagination-buttons");
        buttons.add(vorherigeSeiteButton, naechsteSeiteButton);

        paginationBar.add(seitenInfo, buttons);

        return paginationBar;
    }

    // Berechnet anhand der aktuellen Seite,
    // welche gefilterten Buchungen gerade in der Tabelle angezeigt werden.
    private void aktualisiereAngezeigteSeite() {
        int start = aktuelleSeite * BUCHUNGEN_PRO_SEITE;
        int ende = Math.min(start + BUCHUNGEN_PRO_SEITE, gefilterteBuchungen.size());

        if (start >= gefilterteBuchungen.size() && aktuelleSeite > 0) {
            aktuelleSeite = ermittleLetzteSeite();
            aktualisiereAngezeigteSeite();
            return;
        }

        List<BuchungRow> buchungenDerSeite;

        if (gefilterteBuchungen.isEmpty()) {
            buchungenDerSeite = new ArrayList<>();
        } else {
            buchungenDerSeite = gefilterteBuchungen.subList(start, ende);
        }

        grid.setItems(buchungenDerSeite);
        aktualisierePaginationAnzeige();
    }

    // Aktualisiert den Text und die Buttons der Pagination,
    // z. B. "Zeige 1 - 20 von 35 Buchungen".
    private void aktualisierePaginationAnzeige() {
        int gesamt = gefilterteBuchungen.size();

        if (gesamt == 0) {
            seitenInfo.setText("Keine Buchungen gefunden");
            vorherigeSeiteButton.setEnabled(false);
            naechsteSeiteButton.setEnabled(false);
            return;
        }

        int ersteBuchung = aktuelleSeite * BUCHUNGEN_PRO_SEITE + 1;
        int letzteBuchung = Math.min((aktuelleSeite + 1) * BUCHUNGEN_PRO_SEITE, gesamt);

        seitenInfo.setText(
                "Zeige " + ersteBuchung + " - " + letzteBuchung + " von " + gesamt + " Buchungen"
        );

        vorherigeSeiteButton.setEnabled(aktuelleSeite > 0);
        naechsteSeiteButton.setEnabled(aktuelleSeite < ermittleLetzteSeite());
    }

    private int ermittleLetzteSeite() {
        if (gefilterteBuchungen.isEmpty()) {
            return 0;
        }

        return (gefilterteBuchungen.size() - 1) / BUCHUNGEN_PRO_SEITE;
    }

    // Konfiguriert die Spalten der Buchungstabelle
    // und verbindet einen Klick auf eine Zeile mit der Detailansicht.
    private void configureGrid() {
        grid.addClassName("buchung-grid");
        grid.addClassName("clickable-booking-grid");
        grid.setAllRowsVisible(false);
        grid.setPageSize(BUCHUNGEN_PRO_SEITE);

        grid.addColumn(BuchungRow::datum)
                .setHeader("Datum")
                .setAutoWidth(true);

        grid.addComponentColumn(buchung -> createTypBadge(buchung.typ()))
                .setHeader("Typ")
                .setAutoWidth(true);

        grid.addColumn(BuchungRow::kategorie)
                .setHeader("Kategorie")
                .setAutoWidth(true)
                .setFlexGrow(1);

        grid.addColumn(BuchungRow::immobilie)
                .setHeader("Immobilie")
                .setAutoWidth(true)
                .setFlexGrow(1);

        grid.addColumn(BuchungRow::mietvertrag)
                .setHeader("Mietvertrag")
                .setAutoWidth(true)
                .setFlexGrow(1);

        grid.addColumn(BuchungRow::beschreibung)
                .setHeader("Beschreibung")
                .setAutoWidth(true)
                .setFlexGrow(2);

        grid.addComponentColumn(this::createAmountText)
                .setHeader("Betrag")
                .setAutoWidth(true);

        grid.addComponentColumn(buchung -> createStatusBadge(buchung.status()))
                .setHeader("Status")
                .setAutoWidth(true);

        grid.addItemClickListener(event -> {
            BuchungRow buchung = event.getItem();

            if ("Ausgabe".equals(buchung.typ())) {
                getUI().ifPresent(ui ->
                        ui.navigate("finanzen/buchungen/ausgabe/" + buchung.id())
                );
            } else if ("Einnahme".equals(buchung.typ())) {
                getUI().ifPresent(ui ->
                        ui.navigate("finanzen/buchungen/einnahme/" + buchung.id())
                );
            }
        });

        filtereBuchungen();
    }

    private Span createTypBadge(String typ) {
        Span badge = new Span(typ == null ? "-" : typ);
        badge.addClassName("booking-type-badge");

        if ("Einnahme".equals(typ)) {
            badge.addClassName("income");
        } else if ("Ausgabe".equals(typ)) {
            badge.addClassName("expense");
        } else {
            badge.addClassName("neutral");
        }

        return badge;
    }

    private Span createAmountText(BuchungRow buchung) {
        Span amount = new Span(buchung.betrag());
        amount.addClassName("booking-amount");

        if ("Einnahme".equals(buchung.typ())) {
            amount.addClassName("income");
        } else if ("Ausgabe".equals(buchung.typ())) {
            amount.addClassName("expense");
        }

        return amount;
    }

    private Span createStatusBadge(String status) {
        Span badge = new Span(status != null ? status : "-");
        badge.addClassName("buchung-status-badge");

        if ("Bezahlt / Erledigt".equals(status)) {
            badge.addClassName("status-bezahlt");
            badge.setText("Bezahlt");
        } else if ("Offen / Ausstehend".equals(status)) {
            badge.addClassName("status-offen");
            badge.setText("Offen");
        } else {
            badge.addClassName("status-unbekannt");
        }

        return badge;
    }

    // Lädt Ausgaben und Zahlungseingänge aus der Datenbank
    // und führt sie in einer gemeinsamen Liste von BuchungRow-Objekten zusammen.
    private void ladeBuchungen() {
        alleBuchungen = new ArrayList<>();

        for (Ausgabe ausgabe : ausgabeService.findeAlleAusgaben()) {
            Immobilie immobilie = ermittleImmobilie(ausgabe);
            Mietvertrag mietvertrag = findeMietvertragFuerAusgabe(ausgabe);

            alleBuchungen.add(new BuchungRow(
                    ausgabe.getId(),
                    ausgabe.getDatum() != null ? ausgabe.getDatum().toString() : "-",
                    "Ausgabe",
                    ausgabe.getKategorie() != null ? ausgabe.getKategorie().getLabel() : "-",
                    UiFormatUtils.formatiereImmobilienBezeichnung(immobilie, "-"),
                    UiFormatUtils.formatiereMietvertragKurz(mietvertrag),
                    UiFormatUtils.wertOderStrich(ausgabe.getBeschreibung()),
                    UiFormatUtils.formatiereBetragOderStrich(ausgabe.getBetrag()),
                    UiFormatUtils.wertOderStrich(ausgabe.getStatus())
            ));
        }

        for (Zahlungseingang zahlungseingang : zahlungsEingangService.findeAlleZahlungseingaenge()) {
            Mietvertrag mietvertrag = zahlungseingang.getMietvertrag();
            Immobilie immobilie = ermittleImmobilie(mietvertrag);

            alleBuchungen.add(new BuchungRow(
                    zahlungseingang.getId(),
                    zahlungseingang.getZahlungsdatum() != null ? zahlungseingang.getZahlungsdatum().toString() : "-",
                    "Einnahme",
                    zahlungseingang.getTyp() != null ? zahlungseingang.getTyp().getLabel() : "-",
                    UiFormatUtils.formatiereImmobilienBezeichnung(immobilie, "-"),
                    UiFormatUtils.formatiereMietvertragKurz(mietvertrag),
                    UiFormatUtils.wertOderStrich(zahlungseingang.getBeschreibung()),
                    UiFormatUtils.formatiereBetragOderStrich(zahlungseingang.getBetrag()),
                    UiFormatUtils.wertOderStrich(zahlungseingang.getStatus())
            ));
        }

        alleBuchungen.sort((erste, zweite) ->
                ermittleSortDatum(zweite).compareTo(ermittleSortDatum(erste))
        );
    }

    // Wandelt das Datum einer Buchung in ein LocalDate um,
    // damit die Liste nach Datum sortiert werden kann.
    private LocalDate ermittleSortDatum(BuchungRow buchung) {
        if (buchung == null || buchung.datum() == null || "-".equals(buchung.datum())) {
            return LocalDate.MIN;
        }

        try {
            return LocalDate.parse(buchung.datum());
        } catch (DateTimeParseException ex) {
            return LocalDate.MIN;
        }
    }

    // Aktualisiert die auswählbaren Kategorien abhängig vom Buchungstyp.
    // Bei Einnahmen werden Zahlungstypen angezeigt, bei Ausgaben Ausgabenkategorien.
    private void aktualisiereKategorieFilter() {
        String bisherigeKategorie = kategorieSelect.getValue();
        List<String> kategorien = new ArrayList<>();
        kategorien.add(ALLE_KATEGORIEN);

        if ("Einnahme".equals(buchungTypSelect.getValue())) {
            fuegeZahlungseingangtypenHinzu(kategorien);
        } else if ("Ausgabe".equals(buchungTypSelect.getValue())) {
            fuegeAusgabenkategorienHinzu(kategorien);
        } else {
            fuegeZahlungseingangtypenHinzu(kategorien);
            fuegeAusgabenkategorienHinzu(kategorien);
        }

        kategorieSelect.setItems(kategorien);

        if (bisherigeKategorie != null && kategorien.contains(bisherigeKategorie)) {
            kategorieSelect.setValue(bisherigeKategorie);
        } else {
            kategorieSelect.setValue(ALLE_KATEGORIEN);
        }
    }

    // Fügt die Labels der Zahlungseingangtypen zum Kategorie-Filter hinzu.
    private void fuegeZahlungseingangtypenHinzu(List<String> kategorien) {
        for (Zahlungseingangtyp typ : Zahlungseingangtyp.values()) {
            fuegeKategorieHinzu(kategorien, typ.getLabel());
        }
    }

    // Fügt die Labels der Ausgabenkategorien zum Kategorie-Filter hinzu.
    private void fuegeAusgabenkategorienHinzu(List<String> kategorien) {
        for (Ausgabenkategorie kategorie : Ausgabenkategorie.values()) {
            fuegeKategorieHinzu(kategorien, kategorie.getLabel());
        }
    }

    // Fügt eine Kategorie nur hinzu, wenn sie gültig ist
    // und noch nicht in der Filterliste existiert.
    private void fuegeKategorieHinzu(List<String> kategorien, String label) {
        if (label != null && !label.isBlank() && !kategorien.contains(label)) {
            kategorien.add(label);
        }
    }

    // Wendet Suche, Typ-, Status-, Kategorie- und Immobilienfilter an.
    // Das Ergebnis wird danach seitenweise in der Tabelle angezeigt.
    private void filtereBuchungen() {
        String suchtext = searchField.getValue() != null
                ? searchField.getValue().trim().toLowerCase()
                : "";

        String typ = buchungTypSelect.getValue();
        String status = statusSelect.getValue();
        String kategorie = kategorieSelect.getValue();
        String immobilie = immobilieSelect.getValue();

        this.gefilterteBuchungen = alleBuchungen.stream()
                .filter(buchung -> {
                    boolean passtZumSuchtext = suchtext.isEmpty()
                            || buchung.typ().toLowerCase().contains(suchtext)
                            || buchung.kategorie().toLowerCase().contains(suchtext)
                            || buchung.immobilie().toLowerCase().contains(suchtext)
                            || buchung.mietvertrag().toLowerCase().contains(suchtext)
                            || buchung.beschreibung().toLowerCase().contains(suchtext)
                            || buchung.betrag().toLowerCase().contains(suchtext)
                            || buchung.datum().toLowerCase().contains(suchtext)
                            || buchung.status().toLowerCase().contains(suchtext);

                    boolean passtZurImmobilie = immobilie == null
                            || ALLE_IMMOBILIEN.equals(immobilie)
                            || buchung.immobilie().equals(immobilie);

                    boolean passtZumTyp = typ == null
                            || "Alle".equals(typ)
                            || buchung.typ().equals(typ);

                    boolean passtZumStatus = status == null
                            || "Alle".equals(status)
                            || ("Bezahlt".equals(status) && "Bezahlt / Erledigt".equals(buchung.status()))
                            || ("Offen".equals(status) && "Offen / Ausstehend".equals(buchung.status()));

                    boolean passtZurKategorie = kategorie == null
                            || ALLE_KATEGORIEN.equals(kategorie)
                            || buchung.kategorie().equals(kategorie);

                    return passtZumSuchtext
                            && passtZurImmobilie
                            && passtZumTyp
                            && passtZumStatus
                            && passtZurKategorie;
                })
                .toList();

        aktuelleSeite = 0;
        aktualisiereAngezeigteSeite();
    }

    // Baut den Immobilienfilter aus den tatsächlich vorhandenen Buchungen auf.
    // Dadurch erscheinen nur Immobilien, die auch in der Liste vorkommen.
    private void aktualisiereImmobilienFilter() {
        String bisherigeImmobilie = immobilieSelect.getValue();

        List<String> immobilien = new ArrayList<>();
        immobilien.add(ALLE_IMMOBILIEN);

        for (BuchungRow buchung : alleBuchungen) {
            if (buchung.immobilie() != null
                    && !buchung.immobilie().isBlank()
                    && !"-".equals(buchung.immobilie())
                    && !immobilien.contains(buchung.immobilie())) {
                immobilien.add(buchung.immobilie());
            }
        }

        immobilieSelect.setItems(immobilien);

        if (bisherigeImmobilie != null && immobilien.contains(bisherigeImmobilie)) {
            immobilieSelect.setValue(bisherigeImmobilie);
        } else {
            immobilieSelect.setValue(ALLE_IMMOBILIEN);
        }
    }

    private int zaehleTyp(String typ) {
        return (int) alleBuchungen.stream()
                .filter(buchung -> typ.equals(buchung.typ()))
                .count();
    }

    private int zaehleOffeneBuchungen() {
        return (int) alleBuchungen.stream()
                .filter(buchung -> "Offen / Ausstehend".equals(buchung.status()))
                .count();
    }

    // Ermittelt die Immobilie einer Ausgabe.
    // Falls die Ausgabe keine direkte Immobilie hat, wird sie über die Mieteinheit gesucht.
    private Immobilie ermittleImmobilie(Ausgabe ausgabe) {
        if (ausgabe == null) {
            return null;
        }

        Immobilie immobilie = ausgabe.getImmobilie();

        if (immobilie == null && ausgabe.getMieteinheit() != null) {
            immobilie = ausgabe.getMieteinheit().getImmobilie();
        }

        return immobilie;
    }

    // Ermittelt die Immobilie eines Zahlungseingangs über Mietvertrag und Mieteinheit.
    private Immobilie ermittleImmobilie(Mietvertrag mietvertrag) {
        if (mietvertrag == null
                || mietvertrag.getMieteinheit() == null) {
            return null;
        }

        return mietvertrag.getMieteinheit().getImmobilie();
    }

    // Sucht zu einer Ausgabe den passenden Mietvertrag über die Mieteinheit.
    // Aktive Mietverträge werden bevorzugt angezeigt.
    private Mietvertrag findeMietvertragFuerAusgabe(Ausgabe ausgabe) {
        if (ausgabe == null
                || ausgabe.getMieteinheit() == null
                || ausgabe.getMieteinheit().getId() == null) {
            return null;
        }

        List<Mietvertrag> vertraege =
                mietvertragService.findeMietvertraegeNachMieteinheit(ausgabe.getMieteinheit().getId());

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

    // Gemeinsames Anzeigeobjekt für Ausgaben und Zahlungseingänge.
    // Dadurch kann die Tabelle beide Buchungstypen in einer Liste darstellen.
    private record BuchungRow(
            Long id,
            String datum,
            String typ,
            String kategorie,
            String immobilie,
            String mietvertrag,
            String beschreibung,
            String betrag,
            String status
    ) {
    }
}
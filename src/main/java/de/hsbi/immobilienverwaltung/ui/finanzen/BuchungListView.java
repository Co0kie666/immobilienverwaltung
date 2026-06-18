package de.hsbi.immobilienverwaltung.ui.finanzen;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import de.hsbi.immobilienverwaltung.domain.Ausgabe;
import de.hsbi.immobilienverwaltung.domain.Immobilie;
import de.hsbi.immobilienverwaltung.domain.Mieteinheit;
import de.hsbi.immobilienverwaltung.domain.Mietvertrag;
import de.hsbi.immobilienverwaltung.domain.Zahlungseingang;
import de.hsbi.immobilienverwaltung.domain.enums.Ausgabenkategorie;
import de.hsbi.immobilienverwaltung.domain.enums.Zahlungseingangtyp;
import de.hsbi.immobilienverwaltung.service.interfaces.AusgabeService;
import de.hsbi.immobilienverwaltung.service.interfaces.ZahlungsEingangService;
import de.hsbi.immobilienverwaltung.ui.layout.HasPageHeader;
import de.hsbi.immobilienverwaltung.ui.layout.MainLayout;
import jakarta.annotation.security.PermitAll;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Route(value = "finanzen/buchungen", layout = MainLayout.class)
@PermitAll
public class BuchungListView extends Div implements HasPageHeader {

    private static final String ALLE_KATEGORIEN = "Alle Kategorien";
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final Grid<BuchungRow> grid = new Grid<>(BuchungRow.class, false);
    private final TextField searchField = new TextField();
    private final Select<String> typSelect = new Select<>();
    private final Select<String> statusSelect = new Select<>();
    private final Select<String> kategorieSelect = new Select<>();

    private final AusgabeService ausgabeService;
    private final ZahlungsEingangService zahlungsEingangService;

    private List<BuchungRow> alleBuchungen = new ArrayList<>();

    private Span gesamtAnzahlValue;
    private Span einnahmenValue;
    private Span ausgabenValue;
    private Span offenePostenValue;
    private Span resultCount;

    public BuchungListView(
            AusgabeService ausgabeService,
            ZahlungsEingangService zahlungsEingangService
    ) {
        this.ausgabeService = ausgabeService;
        this.zahlungsEingangService = zahlungsEingangService;

        addClassNames("page-content", "buchung-list-view");

        add(
                createHeroSection(),
                createFilterCard(),
                createTableCard()
        );
    }

    private Component createHeroSection() {
        Div hero = new Div();
        hero.addClassName("buchungen-hero");

        Div left = new Div();
        left.addClassName("buchungen-hero-left");

        Span eyebrow = new Span("Finanzcenter");
        eyebrow.addClassName("buchungen-eyebrow");

        H2 title = new H2("Buchungen im Überblick");
        title.addClassName("buchungen-hero-title");

        Paragraph subtitle = new Paragraph(
                "Alle Einnahmen und Ausgaben gebündelt, filterbar und direkt bearbeitbar."
        );
        subtitle.addClassName("buchungen-hero-subtitle");

        Div actions = new Div();
        actions.addClassName("buchungen-hero-actions");

        Button backButton = new Button("Zurück zum Finanz-Dashboard", VaadinIcon.ARROW_LEFT.create());
        backButton.addClassName("secondary-button");
        backButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate(FinanzDashboardView.class))
        );

        Button newButton = new Button("Neue Buchung", VaadinIcon.PLUS.create());
        newButton.addClassName("primary-button");
        newButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate(BuchungFormView.class))
        );

        actions.add(backButton, newButton);
        left.add(eyebrow, title, subtitle, actions);

        Div stats = new Div();
        stats.addClassName("buchungen-hero-stats");

        gesamtAnzahlValue = new Span("0");
        einnahmenValue = new Span("0,00 €");
        ausgabenValue = new Span("0,00 €");
        offenePostenValue = new Span("0");

        stats.add(
                createHeroStat("Buchungen", gesamtAnzahlValue, VaadinIcon.ARCHIVE),
                createHeroStat("Einnahmen", einnahmenValue, VaadinIcon.TRENDING_UP),
                createHeroStat("Ausgaben", ausgabenValue, VaadinIcon.TRENDING_DOWN),
                createHeroStat("Offen", offenePostenValue, VaadinIcon.WARNING)
        );

        hero.add(left, stats);

        return hero;
    }

    private Component createHeroStat(String label, Span value, VaadinIcon icon) {
        Div stat = new Div();
        stat.addClassName("buchungen-hero-stat");

        Div iconBox = new Div(new Icon(icon));
        iconBox.addClassName("buchungen-hero-stat-icon");

        Span labelSpan = new Span(label);
        labelSpan.addClassName("buchungen-hero-stat-label");

        value.addClassName("buchungen-hero-stat-value");

        stat.add(iconBox, labelSpan, value);

        return stat;
    }

    private Div createFilterCard() {
        Div filterCard = new Div();
        filterCard.addClassName("buchungen-filter-card");

        searchField.setLabel("Suche");
        searchField.setPlaceholder("Beschreibung, Kategorie, Immobilie...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(event -> filtereBuchungen());

        typSelect.setLabel("Typ");
        typSelect.setItems("Alle", "Einnahme", "Ausgabe");
        typSelect.setValue("Alle");
        typSelect.addValueChangeListener(event -> {
            aktualisiereKategorieFilter();
            filtereBuchungen();
        });

        statusSelect.setLabel("Status");
        statusSelect.setItems("Alle", "Bezahlt", "Offen");
        statusSelect.setValue("Alle");
        statusSelect.addValueChangeListener(event -> filtereBuchungen());

        kategorieSelect.setLabel("Kategorie");
        aktualisiereKategorieFilter();
        kategorieSelect.addValueChangeListener(event -> filtereBuchungen());

        filterCard.add(searchField, typSelect, statusSelect, kategorieSelect);

        return filterCard;
    }

    private Div createTableCard() {
        Div tableCard = new Div();
        tableCard.addClassName("buchungen-table-card");

        Div header = new Div();
        header.addClassName("buchungen-table-header");

        Div titleBox = new Div();

        H3 title = new H3("Alle Buchungen");
        title.addClassName("card-title");

        Paragraph subtitle = new Paragraph("Klicke auf eine Zeile, um Einnahmen oder Ausgaben zu bearbeiten.");
        subtitle.addClassName("card-subtitle");

        titleBox.add(title, subtitle);

        resultCount = new Span("0 Einträge");
        resultCount.addClassName("buchungen-result-count");

        header.add(titleBox, resultCount);

        configureGrid();

        tableCard.add(header, grid);

        return tableCard;
    }

    private void configureGrid() {
        grid.addClassNames("buchung-grid", "buchungen-modern-grid");
        grid.setAllRowsVisible(true);

        grid.addColumn(BuchungRow::formatDatum)
                .setHeader("Datum")
                .setAutoWidth(true);

        grid.addComponentColumn(this::createTypBadge)
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

        grid.addColumn(BuchungRow::beschreibung)
                .setHeader("Beschreibung")
                .setAutoWidth(true)
                .setFlexGrow(2);

        grid.addComponentColumn(this::createAmount)
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

        aktualisiereGrid();
    }

    private Component createTypBadge(BuchungRow row) {
        Span badge = new Span(row.typ());
        badge.addClassNames(
                "buchung-type-badge",
                "Einnahme".equals(row.typ()) ? "income" : "expense"
        );
        return badge;
    }

    private Component createAmount(BuchungRow row) {
        Span amount = new Span(row.betrag());
        amount.addClassNames(
                "buchung-amount",
                "Einnahme".equals(row.typ()) ? "income" : "expense"
        );
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

    private void aktualisiereGrid() {
        alleBuchungen = new ArrayList<>();

        for (Ausgabe ausgabe : ausgabeService.findeAlleAusgaben()) {
            alleBuchungen.add(new BuchungRow(
                    ausgabe.getId(),
                    ausgabe.getDatum(),
                    "Ausgabe",
                    ausgabe.getKategorie() != null ? ausgabe.getKategorie().getLabel() : "-",
                    formatiereImmobilie(ausgabe),
                    ausgabe.getBeschreibung() != null && !ausgabe.getBeschreibung().isBlank()
                            ? ausgabe.getBeschreibung()
                            : "-",
                    formatiereBetrag(ausgabe.getBetrag()),
                    ausgabe.getStatus() != null ? ausgabe.getStatus() : "-",
                    ausgabe.getBetrag() == null ? BigDecimal.ZERO : ausgabe.getBetrag()
            ));
        }

        for (Zahlungseingang zahlungseingang : zahlungsEingangService.findeAlleZahlungseingaenge()) {
            alleBuchungen.add(new BuchungRow(
                    zahlungseingang.getId(),
                    zahlungseingang.getZahlungsdatum(),
                    "Einnahme",
                    zahlungseingang.getTyp() != null ? zahlungseingang.getTyp().getLabel() : "-",
                    formatiereImmobilie(zahlungseingang),
                    zahlungseingang.getBeschreibung() != null && !zahlungseingang.getBeschreibung().isBlank()
                            ? zahlungseingang.getBeschreibung()
                            : "-",
                    formatiereBetrag(zahlungseingang.getBetrag()),
                    zahlungseingang.getStatus() != null ? zahlungseingang.getStatus() : "-",
                    zahlungseingang.getBetrag() == null ? BigDecimal.ZERO : zahlungseingang.getBetrag()
            ));
        }

        alleBuchungen.sort(
                Comparator.comparing(BuchungRow::datumWert, Comparator.nullsLast(Comparator.reverseOrder()))
        );

        aktualisiereHeroKennzahlen();
        filtereBuchungen();
    }

    private void aktualisiereHeroKennzahlen() {
        BigDecimal einnahmen = BigDecimal.ZERO;
        BigDecimal ausgaben = BigDecimal.ZERO;
        long offenePosten = 0;

        for (BuchungRow row : alleBuchungen) {
            if ("Einnahme".equals(row.typ())) {
                einnahmen = einnahmen.add(row.betragWert());
            } else if ("Ausgabe".equals(row.typ())) {
                ausgaben = ausgaben.add(row.betragWert());
            }

            if ("Offen / Ausstehend".equals(row.status())) {
                offenePosten++;
            }
        }

        gesamtAnzahlValue.setText(String.valueOf(alleBuchungen.size()));
        einnahmenValue.setText(formatiereBetrag(einnahmen));
        ausgabenValue.setText(formatiereBetrag(ausgaben));
        offenePostenValue.setText(String.valueOf(offenePosten));
    }

    private void aktualisiereKategorieFilter() {
        String bisherigeKategorie = kategorieSelect.getValue();

        List<String> kategorien = new ArrayList<>();
        kategorien.add(ALLE_KATEGORIEN);

        if ("Einnahme".equals(typSelect.getValue())) {
            fuegeZahlungseingangtypenHinzu(kategorien);
        } else if ("Ausgabe".equals(typSelect.getValue())) {
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

    private void fuegeZahlungseingangtypenHinzu(List<String> kategorien) {
        for (Zahlungseingangtyp typ : Zahlungseingangtyp.values()) {
            fuegeKategorieHinzu(kategorien, typ.getLabel());
        }
    }

    private void fuegeAusgabenkategorienHinzu(List<String> kategorien) {
        for (Ausgabenkategorie kategorie : Ausgabenkategorie.values()) {
            fuegeKategorieHinzu(kategorien, kategorie.getLabel());
        }
    }

    private void fuegeKategorieHinzu(List<String> kategorien, String label) {
        if (label != null && !label.isBlank() && !kategorien.contains(label)) {
            kategorien.add(label);
        }
    }

    private void filtereBuchungen() {
        String suchtext = searchField.getValue() != null
                ? searchField.getValue().trim().toLowerCase()
                : "";

        String typ = typSelect.getValue();
        String status = statusSelect.getValue();
        String kategorie = kategorieSelect.getValue();

        List<BuchungRow> gefilterteBuchungen = alleBuchungen.stream()
                .filter(buchung -> {
                    boolean passtZumSuchtext = suchtext.isEmpty()
                            || buchung.typ().toLowerCase().contains(suchtext)
                            || buchung.kategorie().toLowerCase().contains(suchtext)
                            || buchung.immobilie().toLowerCase().contains(suchtext)
                            || buchung.beschreibung().toLowerCase().contains(suchtext)
                            || buchung.betrag().toLowerCase().contains(suchtext)
                            || buchung.formatDatum().toLowerCase().contains(suchtext)
                            || buchung.status().toLowerCase().contains(suchtext);

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
                            && passtZumTyp
                            && passtZumStatus
                            && passtZurKategorie;
                })
                .toList();

        grid.setItems(gefilterteBuchungen);
        resultCount.setText(gefilterteBuchungen.size() + " Einträge");
    }

    private String formatiereBetrag(BigDecimal betrag) {
        if (betrag == null) {
            return "-";
        }

        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.GERMANY);
        return formatter.format(betrag);
    }

    private String formatiereImmobilie(Ausgabe ausgabe) {
        if (ausgabe == null) {
            return "-";
        }

        Immobilie immobilie = ausgabe.getImmobilie();

        if (immobilie == null && ausgabe.getMieteinheit() != null) {
            immobilie = ausgabe.getMieteinheit().getImmobilie();
        }

        return formatiereImmobilie(immobilie);
    }

    private String formatiereImmobilie(Zahlungseingang zahlungseingang) {
        if (zahlungseingang == null) {
            return "-";
        }

        Mietvertrag mietvertrag = zahlungseingang.getMietvertrag();

        if (mietvertrag == null) {
            return "-";
        }

        Mieteinheit mieteinheit = mietvertrag.getMieteinheit();

        if (mieteinheit == null) {
            return "-";
        }

        return formatiereImmobilie(mieteinheit.getImmobilie());
    }

    private String formatiereImmobilie(Immobilie immobilie) {
        if (immobilie == null
                || immobilie.getBezeichnung() == null
                || immobilie.getBezeichnung().isBlank()) {
            return "-";
        }

        return immobilie.getBezeichnung();
    }

    @Override
    public String getPageTitle() {
        return "Buchungsübersicht";
    }

    @Override
    public String getPageSubtitle() {
        return "Finanzen › Alle Buchungen";
    }

    private record BuchungRow(
            Long id,
            LocalDate datumWert,
            String typ,
            String kategorie,
            String immobilie,
            String beschreibung,
            String betrag,
            String status,
            BigDecimal betragWert
    ) {
        String formatDatum() {
            return datumWert == null ? "-" : datumWert.format(DATE_FORMATTER);
        }
    }
}

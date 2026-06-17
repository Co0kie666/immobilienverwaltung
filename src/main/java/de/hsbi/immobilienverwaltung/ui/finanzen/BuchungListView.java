package de.hsbi.immobilienverwaltung.ui.finanzen;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import de.hsbi.immobilienverwaltung.domain.Ausgabe;
import de.hsbi.immobilienverwaltung.domain.Zahlungseingang;
import de.hsbi.immobilienverwaltung.domain.Immobilie;
import de.hsbi.immobilienverwaltung.domain.Mieteinheit;
import de.hsbi.immobilienverwaltung.domain.Mietvertrag;
import de.hsbi.immobilienverwaltung.domain.enums.Ausgabenkategorie;
import de.hsbi.immobilienverwaltung.domain.enums.Zahlungseingangtyp;
import de.hsbi.immobilienverwaltung.service.interfaces.AusgabeService;
import de.hsbi.immobilienverwaltung.service.interfaces.ZahlungsEingangService;
import de.hsbi.immobilienverwaltung.ui.layout.HasPageHeader;
import de.hsbi.immobilienverwaltung.ui.layout.MainLayout;
import com.vaadin.flow.component.html.Span;
import jakarta.annotation.security.PermitAll;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Route(value = "finanzen/buchungen", layout = MainLayout.class)
@PermitAll
public class BuchungListView extends Div implements HasPageHeader {

    private final Grid<BuchungRow> grid = new Grid<>(BuchungRow.class, false);

    private List<BuchungRow> alleBuchungen = new ArrayList<>();

    private final TextField searchField = new TextField("Suche");
    private final Select<String> typSelect = new Select<>();
    private final Select<String> statusSelect = new Select<>();
    private final Select<String> kategorieSelect = new Select<>();
    private final AusgabeService ausgabeService;
    private final ZahlungsEingangService zahlungsEingangService;
    private static final String ALLE_KATEGORIEN = "Alle Kategorien";

    public BuchungListView(AusgabeService ausgabeService,
                           ZahlungsEingangService zahlungsEingangService) {
        this.ausgabeService = ausgabeService;
        this.zahlungsEingangService = zahlungsEingangService;

        addClassName("buchung-list-view");
        addClassName("page-content");

        add(
                createActionBar(),
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

    private Div createActionBar() {
        Div actionBar = new Div();
        actionBar.addClassName("immobilien-action-bar");
        actionBar.getStyle()
                .set("display", "flex")
                .set("gap", "16px")
                .set("align-items", "center")
                .set("margin-bottom", "20px")
                .set("padding", "8px 0");

        Button zurueckButton = new Button("Zurück", VaadinIcon.ARROW_LEFT.create());
        zurueckButton.addClassName("secondary-button");
        zurueckButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate(FinanzDashboardView.class))
        );

        Button newButton = new Button("Neue Buchung", VaadinIcon.PLUS.create());
        newButton.addClassName("primary-button");

        newButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate(BuchungFormView.class))
        );

        actionBar.add(zurueckButton, newButton);

        return actionBar;
    }

    private Div createFilterCard() {
        Div filterCard = new Div();
        filterCard.addClassName("filter-card");


        searchField.setPlaceholder("Beschreibung, Kategorie...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
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
        tableCard.addClassName("table-card");

        configureGrid();

        tableCard.add(grid);

        return tableCard;
    }

    private void configureGrid() {
        grid.addClassName("buchung-grid");
        grid.addClassName("clickable-booking-grid");
        grid.setAllRowsVisible(true);

        grid.addColumn(BuchungRow::datum)
                .setHeader("Datum")
                .setAutoWidth(true);

        grid.addColumn(BuchungRow::typ)
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

        grid.addColumn(BuchungRow::betrag)
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
                    ausgabe.getDatum() != null ? ausgabe.getDatum().toString() : "-",
                    "Ausgabe",
                    ausgabe.getKategorie() != null ? ausgabe.getKategorie().getLabel() : "-",
                    formatiereImmobilie(ausgabe),
                    ausgabe.getBeschreibung() != null ? ausgabe.getBeschreibung() : "-",
                    formatiereBetrag(ausgabe.getBetrag()),
                    ausgabe.getStatus() != null ? ausgabe.getStatus() : "-"
            ));
        }

        for (Zahlungseingang zahlungseingang : zahlungsEingangService.findeAlleZahlungseingaenge()) {
            alleBuchungen.add(new BuchungRow(
                    zahlungseingang.getId(),
                    zahlungseingang.getZahlungsdatum() != null ? zahlungseingang.getZahlungsdatum().toString() : "-",
                    "Einnahme",
                    zahlungseingang.getTyp() != null ? zahlungseingang.getTyp().getLabel() : "-",
                    formatiereImmobilie(zahlungseingang),
                    zahlungseingang.getBeschreibung() != null ? zahlungseingang.getBeschreibung() : "-",
                    formatiereBetrag(zahlungseingang.getBetrag()),
                    zahlungseingang.getStatus() != null ? zahlungseingang.getStatus() : "-"
            ));
        }

        filtereBuchungen();
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
                            || buchung.datum().toLowerCase().contains(suchtext)
                            || buchung.status().toLowerCase().contains(suchtext);

                    boolean passtZumTyp = typ == null
                            || "Alle".equals(typ)
                            || buchung.typ().equals(typ);

                    boolean passtZumStatus = status == null
                            || "Alle".equals(status)
                            || ("Bezahlt".equals(status) && "Bezahlt / Erledigt".equals(buchung.status()))
                            || ("Offen".equals(status) && "Offen / Ausstehend".equals(buchung.status()));

                    boolean passtZurKategorie = kategorie == null
                            || "Alle Kategorien".equals(kategorie)
                            || buchung.kategorie().equals(kategorie);

                    return passtZumSuchtext
                            && passtZumTyp
                            && passtZumStatus
                            && passtZurKategorie;
                })
                .toList();

        grid.setItems(gefilterteBuchungen);
    }

    private String formatiereBetrag(java.math.BigDecimal betrag) {
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
        if (immobilie == null || immobilie.getBezeichnung() == null || immobilie.getBezeichnung().isBlank()) {
            return "-";
        }

        return immobilie.getBezeichnung();
    }

    private record BuchungRow(
            Long id,
            String datum,
            String typ,
            String kategorie,
            String immobilie,
            String beschreibung,
            String betrag,
            String status
    ) {
    }
}
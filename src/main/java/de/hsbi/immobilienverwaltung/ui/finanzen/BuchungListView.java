package de.hsbi.immobilienverwaltung.ui.finanzen;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import de.hsbi.immobilienverwaltung.domain.Ausgabe;
import de.hsbi.immobilienverwaltung.domain.Zahlungseingang;
import de.hsbi.immobilienverwaltung.service.interfaces.AusgabeService;
import de.hsbi.immobilienverwaltung.service.interfaces.ZahlungsEingangService;
import de.hsbi.immobilienverwaltung.ui.components.ConfirmDeleteDialog;
import de.hsbi.immobilienverwaltung.ui.layout.HasPageHeader;
import de.hsbi.immobilienverwaltung.ui.layout.MainLayout;
import jakarta.annotation.security.PermitAll;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Route(value = "finanzen/buchungen", layout = MainLayout.class)
@PermitAll
public class BuchungListView extends Div implements HasPageHeader {

    private final Grid<BuchungRow> grid = new Grid<>(BuchungRow.class, false);

    private final AusgabeService ausgabeService;
    private final ZahlungsEingangService zahlungsEingangService;

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

        Button newButton = new Button("Neue Buchung", VaadinIcon.PLUS.create());
        newButton.addClassName("primary-button");

        newButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate(BuchungFormView.class))
        );

        actionBar.add(newButton);

        return actionBar;
    }

    private Div createFilterCard() {
        Div filterCard = new Div();
        filterCard.addClassName("filter-card");

        TextField searchField = new TextField("Suche");
        searchField.setPlaceholder("Beschreibung, Kategorie...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());

        Select<String> typSelect = new Select<>();
        typSelect.setLabel("Typ");
        typSelect.setItems("Alle", "Einnahme", "Ausgabe");
        typSelect.setValue("Alle");

        Select<String> statusSelect = new Select<>();
        statusSelect.setLabel("Status");
        statusSelect.setItems("Alle", "Bezahlt", "Offen");
        statusSelect.setValue("Alle");

        Select<String> kategorieSelect = new Select<>();
        kategorieSelect.setLabel("Kategorie");
        kategorieSelect.setItems(
                "Alle Kategorien",
                "Kaution",
                "Instandhaltung",
                "Hausmeister",
                "Verwaltungskosten",
                "Sonstiges"
        );
        kategorieSelect.setValue("Alle Kategorien");

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

        grid.addColumn(BuchungRow::beschreibung)
                .setHeader("Beschreibung")
                .setAutoWidth(true)
                .setFlexGrow(2);

        grid.addColumn(BuchungRow::betrag)
                .setHeader("Betrag")
                .setAutoWidth(true);

        grid.addColumn(BuchungRow::status)
                .setHeader("Status")
                .setAutoWidth(true);

        grid.addColumn(new ComponentRenderer<>(this::createActionButtons))
                .setHeader("Aktionen")
                .setAutoWidth(true)
                .setFlexGrow(0);

        aktualisiereGrid();
    }

    private void aktualisiereGrid() {
        List<BuchungRow> rows = new ArrayList<>();

        for (Ausgabe ausgabe : ausgabeService.findeAlleAusgaben()) {
            rows.add(new BuchungRow(
                    ausgabe.getId(),
                    ausgabe.getDatum() != null ? ausgabe.getDatum().toString() : "-",
                    "Ausgabe",
                    ausgabe.getKategorie() != null ? ausgabe.getKategorie().name() : "-",
                    ausgabe.getBeschreibung() != null ? ausgabe.getBeschreibung() : "-",
                    formatiereBetrag(ausgabe.getBetrag()),
                    "Bezahlt / Erledigt"
            ));
        }

        for (Zahlungseingang zahlungseingang : zahlungsEingangService.findeAlleZahlungseingaenge()) {
            rows.add(new BuchungRow(
                    zahlungseingang.getId(),
                    zahlungseingang.getZahlungsdatum() != null ? zahlungseingang.getZahlungsdatum().toString() : "-",
                    "Einnahme",
                    zahlungseingang.getTyp() != null ? zahlungseingang.getTyp().name() : "-",
                    zahlungseingang.getBeschreibung() != null ? zahlungseingang.getBeschreibung() : "-",
                    formatiereBetrag(zahlungseingang.getBetrag()),
                    "Bezahlt / Erledigt"
            ));
        }

        grid.setItems(rows);
    }

    private String formatiereBetrag(java.math.BigDecimal betrag) {
        if (betrag == null) {
            return "-";
        }

        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.GERMANY);
        return formatter.format(betrag);
    }

    private Component createActionButtons(BuchungRow buchung) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);
        actions.setPadding(false);

        Button anzeigenButton = new Button("Anzeigen", VaadinIcon.EYE.create());
        Button bearbeitenButton = new Button("Bearbeiten", VaadinIcon.EDIT.create());
        Button loeschenButton = new Button("Löschen", VaadinIcon.TRASH.create());

        anzeigenButton.addClickListener(event ->
                Notification.show("Details sind noch nicht verbunden.")
        );

        bearbeitenButton.addClickListener(event ->
                Notification.show("Bearbeiten ist noch nicht verbunden.")
        );

        loeschenButton.addClickListener(event -> {
            ConfirmDeleteDialog dialog = new ConfirmDeleteDialog(
                    "Buchung löschen?",
                    "Möchtest du die Buchung \"" + buchung.beschreibung() + "\" wirklich löschen?",
                    () -> {
                        if ("Ausgabe".equals(buchung.typ())) {
                            ausgabeService.loescheAusgabe(buchung.id());
                            aktualisiereGrid();
                            Notification.show("Ausgabe gelöscht: " + buchung.beschreibung());
                        } else if ("Einnahme".equals(buchung.typ())) {
                            zahlungsEingangService.loescheZahlungseingang(buchung.id());
                            aktualisiereGrid();
                            Notification.show("Zahlungseingang gelöscht: " + buchung.beschreibung());
                        }
                    }
            );
            dialog.open();
        });

        actions.add(anzeigenButton, bearbeitenButton, loeschenButton);
        return actions;
    }

    private record BuchungRow(
            Long id,
            String datum,
            String typ,
            String kategorie,
            String beschreibung,
            String betrag,
            String status
    ) {
    }
}
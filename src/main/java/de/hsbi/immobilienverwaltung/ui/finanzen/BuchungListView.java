package de.hsbi.immobilienverwaltung.ui.finanzen;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
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
import de.hsbi.immobilienverwaltung.security.LoginRequired;
import de.hsbi.immobilienverwaltung.ui.components.ConfirmDeleteDialog;
import de.hsbi.immobilienverwaltung.ui.layout.HasPageHeader;
import de.hsbi.immobilienverwaltung.ui.layout.MainLayout;

import java.util.List;

@Route(value = "finanzen/buchungen", layout = MainLayout.class)
public class BuchungListView extends Div implements HasPageHeader, LoginRequired {

    private final Grid<BuchungRow> grid = new Grid<>(BuchungRow.class, false);

    public BuchungListView() {
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

    // Neue Buchung Button
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

    // Filter
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

    // Tabelle
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

        grid.setItems(List.of(
                new BuchungRow(1L, "01.05.2025", "Einnahme", "Mieteinnahmen", "Miete Mai - Wohnung 1A", "€850,00", "Bezahlt / Erledigt"),
                new BuchungRow(2L, "03.05.2025", "Ausgabe", "Handwerker", "Rohrreparatur Bad", "€320,00", "Bezahlt / Erledigt"),
                new BuchungRow(3L, "05.05.2025", "Einnahme", "Nebenkosten", "Nebenkostenabrechnung Q1", "€210,00", "Offen / Ausstehend"),
                new BuchungRow(4L, "10.05.2025", "Ausgabe", "Verwaltungskosten", "Hausverwaltung Mai", "€150,00", "Bezahlt / Erledigt"),
                new BuchungRow(5L, "15.05.2025", "Einnahme", "Kaution", "Kaution Neuvermietung 2B", "€1.700,00", "Bezahlt / Erledigt")
        ));
    }

    private Component createActionButtons(BuchungRow buchung) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.addClassName("table-actions");
        actions.setSpacing(false);
        actions.setPadding(false);

        Button anzeigenButton = new Button(VaadinIcon.EYE.create());
        anzeigenButton.addClassNames("table-action-button");

        Button bearbeitenButton = new Button(VaadinIcon.EDIT.create());
        bearbeitenButton.addClassNames("table-action-button");

        Button loeschenButton = new Button(VaadinIcon.TRASH.create());
        loeschenButton.addClassNames("table-action-button");

        anzeigenButton.addClickListener(event ->
                UI.getCurrent().navigate("finanzen/buchungen/" + buchung.id())
        );

        bearbeitenButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate("finanzen/buchungen/" + buchung.id() + "/bearbeiten"))
        );

        loeschenButton.addClickListener(event -> {
            ConfirmDeleteDialog dialog = new ConfirmDeleteDialog(
                    "Buchung löschen?",
                    "Möchtest du die Buchung \"" + buchung.beschreibung() + "\" wirklich löschen?",
                    () -> {
                        Notification.show("Buchung würde gelöscht werden: " + buchung.beschreibung());

                        // später:
                        // buchungService.loescheBuchung(buchung.id());
                        // grid.setItems(buchungService.findeBuchungen());
                    }
            );
            dialog.open();
        });
        actions.add(anzeigenButton, bearbeitenButton, loeschenButton);
        return actions;
    }

    // Dummy
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
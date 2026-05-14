package de.hsbi.immobilienverwaltung.ui.mieter;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import de.hsbi.immobilienverwaltung.ui.layout.HasPageHeader;
import de.hsbi.immobilienverwaltung.ui.layout.MainLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.renderer.ComponentRenderer;



@Route(value = "mieter-vertraege", layout = MainLayout.class)
public class MieterVertraegeView extends Div implements HasPageHeader {

    // Speichert, welcher Bereich gerade ausgewählt ist
    private TabellenModus aktiverModus = TabellenModus.MIETER;

    private final Grid<MieterDummyDaten.MieterRow> mieterGrid =
            new Grid<>(MieterDummyDaten.MieterRow.class, false);

    private final Grid<MieterDummyDaten.MietvertragRow> mietvertragGrid =
            new Grid<>(MieterDummyDaten.MietvertragRow.class, false);

    // Mögliche Bereiche in der Tabelle
    private enum TabellenModus {
        MIETER,
        VERTRAEGE
    }

    public MieterVertraegeView() {
        addClassName("page-content");

        add(createTableCard()); // Erstellt die große Tabelle
    }

    // Tabellen Körper
    private Component createTableCard() {
        Div tableCard = new Div();
        tableCard.addClassName("table-card");

        // Beide Tabellen vorbereiten
        configureMieterGrid();
        configureMietvertragGrid();

        // Am Anfang soll nur die Mieter-Tabelle sichtbar sein
        mietvertragGrid.setVisible(false);

        tableCard.add(
                createTableHeader(),
                mieterGrid,
                mietvertragGrid
        );

        return tableCard;
    }

    // Erstellt die Spalten und Daten für die Mieter-Tabelle
    private void configureMieterGrid() {
        mieterGrid.removeAllColumns();
        mieterGrid.setWidthFull();
        mieterGrid.setAllRowsVisible(true);

        mieterGrid.addColumn(MieterDummyDaten.MieterRow::name)
                .setHeader("Mieter")
                .setAutoWidth(true)
                .setFlexGrow(2);

        mieterGrid.addColumn(MieterDummyDaten.MieterRow::immobilieEinheit)
                .setHeader("Immobilie / Einheit")
                .setAutoWidth(true)
                .setFlexGrow(2);

        mieterGrid.addColumn(MieterDummyDaten.MieterRow::vertragsdauer)
                .setHeader("Vertragsdauer")
                .setAutoWidth(true)
                .setFlexGrow(2);

        mieterGrid.addColumn(MieterDummyDaten.MieterRow::miete)
                .setHeader("Miete mtl.")
                .setAutoWidth(true)
                .setFlexGrow(1);

        mieterGrid.addColumn(new ComponentRenderer<>(this::createStatusBadge))
                .setHeader("Status")
                .setAutoWidth(true)
                .setFlexGrow(1);

        mieterGrid.addColumn(new ComponentRenderer<>(this::createActionButtons))
                .setHeader("Aktionen")
                .setAutoWidth(true)
                .setFlexGrow(0);

        // Holt die Dummy-Daten aus der extra Dummy-Klasse
        mieterGrid.setItems(MieterDummyDaten.getMieter());
    }

    // Erstellt die Spalten und Daten für die Mietvertrag-Tabelle
    private void configureMietvertragGrid() {
        mietvertragGrid.removeAllColumns();
        mietvertragGrid.setWidthFull();
        mietvertragGrid.setAllRowsVisible(true);

        mietvertragGrid.addColumn(MieterDummyDaten.MietvertragRow::mieter)
                .setHeader("Mieter")
                .setAutoWidth(true)
                .setFlexGrow(2);

        mietvertragGrid.addColumn(MieterDummyDaten.MietvertragRow::immobilieEinheit)
                .setHeader("Immobilie / Einheit")
                .setAutoWidth(true)
                .setFlexGrow(2);

        mietvertragGrid.addColumn(MieterDummyDaten.MietvertragRow::laufzeit)
                .setHeader("Laufzeit")
                .setAutoWidth(true)
                .setFlexGrow(2);

        mietvertragGrid.addColumn(MieterDummyDaten.MietvertragRow::miete)
                .setHeader("Miete mtl.")
                .setAutoWidth(true)
                .setFlexGrow(1);

        mietvertragGrid.addColumn(new ComponentRenderer<>(this::createMietvertragStatusBadge))
                .setHeader("Status")
                .setAutoWidth(true)
                .setFlexGrow(1);

        mietvertragGrid.addColumn(new ComponentRenderer<>(this::createMietvertragActionButtons))
                .setHeader("Aktionen")
                .setAutoWidth(true)
                .setFlexGrow(0);

        // Holt die Dummy-Daten aus der extra Dummy-Klasse
        mietvertragGrid.setItems(MieterDummyDaten.getMietvertraege());
    }

    // Erstellt den Status-Badge für einen Mieter
    private Component createStatusBadge(MieterDummyDaten.MieterRow row) {
        Span badge = new Span(row.status());
        badge.addClassNames("status-badge", row.statusStyle());
        return badge;
    }

    // Erstellt die Aktionsbuttons für einen Mieter
    private Component createActionButtons(MieterDummyDaten.MieterRow row) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(false);

        Button detailsButton = new Button(VaadinIcon.EYE.create());
        detailsButton.addClassName("icon-button");

        // Öffnet die Detailansicht von diesem Mieter
        detailsButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate(MieterListView.class, row.id()))
        );

        Button editButton = new Button(VaadinIcon.EDIT.create());
        editButton.addClassName("icon-button");

        actions.add(detailsButton, editButton);

        return actions;
    }

    // Tabellen Kopf
    private Component createTableHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.addClassName("table-card-header");
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        // Mieter & Verträge switchen
        Tabs tabs = new Tabs(
                new Tab("Mieter"),
                new Tab("Verträge")
        );

        // Prüft, welcher Tab ausgewählt wurde
        tabs.addSelectedChangeListener(event -> {
            if (tabs.getSelectedIndex() == 0) {
                aktiverModus = TabellenModus.MIETER;
                mieterGrid.setVisible(true);
                mietvertragGrid.setVisible(false);
            } else {
                aktiverModus = TabellenModus.VERTRAEGE;
                mieterGrid.setVisible(false);
                mietvertragGrid.setVisible(true);
            }
        });

        // Suchfunktion
        TextField searchField = new TextField();
        searchField.setPlaceholder("Suchen...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.setWidth("320px");

        // Status aussuchen/filtern
        Select<String> statusFilter = new Select<>();
        statusFilter.setItems("Alle Status", "Aktiv", "Zahlungsverzug", "Gekündigt");
        statusFilter.setValue("Alle Status");
        statusFilter.setWidth("180px");

        // Anlegen Button für Mieter und Verträge
        Button addButton = new Button(VaadinIcon.PLUS.create());
        addButton.addClassName("primary-button");

        // Prüft, welcher Tab ausgewählt ist und öffnet dann die passende FormView
        addButton.addClickListener(event -> {
            if (aktiverModus == TabellenModus.MIETER) {
                getUI().ifPresent(ui -> ui.navigate(MieterFormView.class)); // Öffnet die Mieter-FormView
            } else {
                getUI().ifPresent(ui -> ui.navigate(MietvertragFormView.class)); // Öffnet die Mietvertrag-FormView
            }
        });

        // Rechter Bereich im Tabellenkopf
        HorizontalLayout rightArea = new HorizontalLayout();
        rightArea.setAlignItems(FlexComponent.Alignment.CENTER);
        rightArea.setSpacing(true);
        rightArea.add(searchField, statusFilter, addButton);

        header.add(tabs, rightArea);

        return header;
    }

    // Erstellt den Status-Badge für einen Mietvertrag
    private Component createMietvertragStatusBadge(MieterDummyDaten.MietvertragRow row) {
        Span badge = new Span(row.status());
        badge.addClassNames("status-badge", row.statusStyle());
        return badge;
    }

    // Erstellt die Aktionsbuttons für einen Mietvertrag
    private Component createMietvertragActionButtons(MieterDummyDaten.MietvertragRow row) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(false);

        Button detailsButton = new Button(VaadinIcon.EYE.create());
        detailsButton.addClassName("icon-button");

        Button editButton = new Button(VaadinIcon.EDIT.create());
        editButton.addClassName("icon-button");

        actions.add(detailsButton, editButton);

        return actions;
    }

    @Override
    public String getPageTitle() {
        return "Mieter & Verträge";
    }

    @Override
    public String getPageSubtitle() {
        return "Mieter und laufende Mietverhältnisse verwalten";
    }
}
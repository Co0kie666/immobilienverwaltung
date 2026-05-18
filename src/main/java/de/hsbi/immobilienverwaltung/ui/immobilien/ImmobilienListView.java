package de.hsbi.immobilienverwaltung.ui.immobilien;

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
import de.hsbi.immobilienverwaltung.ui.components.ConfirmDeleteDialog;
import de.hsbi.immobilienverwaltung.ui.layout.HasPageHeader;
import de.hsbi.immobilienverwaltung.ui.layout.MainLayout;

import java.util.List;

@Route(value = "immobilien", layout = MainLayout.class)
public class ImmobilienListView extends Div implements HasPageHeader {

    private final Grid<ImmobilienRow> grid = new Grid<>(ImmobilienRow.class, false);

    public ImmobilienListView() {
        addClassName("immobilien-list-view");
        addClassName("page-content");

        add(
                createProperty(),
                createFilterCard(),
                createTableCard()
        );
    }

    @Override
    public String getPageTitle() {
        return "Immobilienübersicht";
    }

    @Override
    public String getPageSubtitle() {
        return "Hier werden alle Immobilien angezeigt";
    }

    // Immobilie anlegen Button
    private Div createProperty() {
        Div createButton = new Div();
        createButton.addClassName("immobilien-action-bar");

        Button newButton = new Button("Neu anlegen", VaadinIcon.PLUS.create());
        newButton.addClassName("primary-button");

        newButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate(ImmobilieFormView.class))
        );

        createButton.add(newButton);

        return createButton;
    }

    // Filter mit Ort, Status, Einheiten und Leerstand
    private Div createFilterCard() {
        Div filterCard = new Div();
        filterCard.addClassName("filter-card");

        TextField locationField = new TextField("Ort / PLZ");
        locationField.setPlaceholder("Alle Orte");
        locationField.setPrefixComponent(VaadinIcon.MAP_MARKER.create());

        Select<String> typBox = new Select<>();
        typBox.setLabel("Immobilientyp");
        typBox.setItems("Alle Typen", "Wohngebäude", "Mehrfamilienhaus", "Gewerbeimmobilie");
        typBox.setValue("Alle Typen");

        Select<String> unitsSelect = new Select<>();
        unitsSelect.setLabel("Einheiten");
        unitsSelect.setItems("Alle Größen", "1-5 Einheiten", "6-20 Einheiten", "20+ Einheiten");
        unitsSelect.setValue("Alle Größen");

        Select<String> vacancySelect = new Select<>();
        vacancySelect.setLabel("Leerstand");
        vacancySelect.setItems("Alle anzeigen", "Mit Leerstand", "Ohne Leerstand");
        vacancySelect.setValue("Alle anzeigen");

        filterCard.add(locationField, typBox, unitsSelect, vacancySelect);

        return filterCard;
    }

    // Tabelle erstellen
    private Div createTableCard() {
        Div tableCard = new Div();
        tableCard.addClassName("table-card");

        configureGrid();

        tableCard.add(grid);

        return tableCard;
    }

    private void configureGrid() {
        grid.addClassName("immobilien-grid");
        grid.setAllRowsVisible(true);

        grid.addColumn(ImmobilienRow::name)
                .setHeader("Immobilie")
                .setAutoWidth(true)
                .setFlexGrow(2);

        grid.addColumn(ImmobilienRow::typ)
                .setHeader("Typ")
                .setAutoWidth(true);

        grid.addColumn(ImmobilienRow::adresse)
                .setHeader("Adresse")
                .setAutoWidth(true)
                .setFlexGrow(2);

        grid.addColumn(ImmobilienRow::einheiten)
                .setHeader("Einheiten")
                .setAutoWidth(true);

        grid.addColumn(ImmobilienRow::leerstand)
                .setHeader("Leerstand")
                .setAutoWidth(true);

        grid.addColumn(ImmobilienRow::offenePosten)
                .setHeader("Offene Posten")
                .setAutoWidth(true);

        grid.addColumn(new ComponentRenderer<>(this::createActionButtons))
                .setHeader("Aktionen")
                .setAutoWidth(true)
                .setFlexGrow(0);

        grid.setItems(List.of(
                new ImmobilienRow(
                        1L,
                        "Parkresidenz Süd",
                        "Mehrfamilienhaus",
                        "Parkstraße 12, 10115 Berlin",
                        "24 Mieteinheiten",
                        "0% (0)",
                        "€0.00"
                ),
                new ImmobilienRow(
                        2L,
                        "Altbau Ensemble Mitte",
                        "Wohngebäude",
                        "Hauptstraße 45, 80331 München",
                        "12 Mieteinheiten",
                        "16% (2)",
                        "€1,250.00"
                ),
                new ImmobilienRow(
                        3L,
                        "Seeblick Quartier",
                        "Gewerbeimmobilie",
                        "Seestraße 8-10, 20099 Hamburg",
                        "8 Mieteinheiten",
                        "0% (0)",
                        "€0.00"
                )
        ));
    }

    private Component createActionButtons(ImmobilienRow immobilie) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.addClassName("table-actions");
        actions.setSpacing(false);
        actions.setPadding(false);

        Button anzeigenButton = new Button(VaadinIcon.EYE.create());
        anzeigenButton.addClassNames("table-action-button", "view-action");

        Button bearbeitenButton = new Button(VaadinIcon.EDIT.create());
        bearbeitenButton.addClassNames("table-action-button", "edit-action");

        Button loeschenButton = new Button(VaadinIcon.TRASH.create());
        loeschenButton.addClassNames("table-action-button", "delete-action");

        anzeigenButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate("immobilien/" + immobilie.id()))
        );

        bearbeitenButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate("immobilien/" + immobilie.id() + "/bearbeiten"))
        );

        loeschenButton.addClickListener(event -> {
            ConfirmDeleteDialog dialog = new ConfirmDeleteDialog(
                    "Immobilie löschen?",
                    "Möchtest du die Immobilie \"" + immobilie.name() + "\" wirklich löschen?",
                    () -> {
                        Notification.show("Immobilie würde gelöscht werden: " + immobilie.name());

                        // später:
                        // immobilieService.loescheImmobilie(immobilie.id());
                        // grid.setItems(immobilieService.findeImmobilienUebersicht());
                    }
            );

            dialog.open();
        });
        actions.add(anzeigenButton, bearbeitenButton, loeschenButton);

        return actions;
    }

    // Dummy
    private record ImmobilienRow(
            Long id,
            String name,
            String typ,
            String adresse,
            String einheiten,
            String leerstand,
            String offenePosten
    ) {
    }
}
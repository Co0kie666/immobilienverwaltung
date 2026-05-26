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
import de.hsbi.immobilienverwaltung.domain.Adresse;
import de.hsbi.immobilienverwaltung.domain.Immobilie;
import de.hsbi.immobilienverwaltung.domain.enums.Immobilientyp;
import de.hsbi.immobilienverwaltung.security.LoginRequired;
import de.hsbi.immobilienverwaltung.service.interfaces.ImmobilieService;
import de.hsbi.immobilienverwaltung.ui.components.ConfirmDeleteDialog;
import de.hsbi.immobilienverwaltung.ui.layout.HasPageHeader;
import de.hsbi.immobilienverwaltung.ui.layout.MainLayout;

import java.util.List;

@Route(value = "immobilien", layout = MainLayout.class)
public class ImmobilienListView extends Div implements HasPageHeader, LoginRequired {

    private final Grid<Immobilie> grid = new Grid<>(Immobilie.class, false);
    private final ImmobilieService immobilieService;

    public ImmobilienListView(ImmobilieService immobilieService) {
        this.immobilieService = immobilieService;
        addClassName("immobilien-list-view");
        addClassName("page-content");

        add(
                createProperty(),
                createFilterCard(),
                createTableCard()
        );

        ladeImmobilien();
    }

    private void ladeImmobilien() {
        grid.setItems(immobilieService.findeAlleImmobilien());
    }

    private String formatAdresse(Immobilie immobilie) {
        Adresse adresse = immobilie.getAdresse();

        if (adresse == null) {
            return "-";
        }

        return adresse.getStrasse() + " "
                + adresse.getHausnummer() + ", "
                + adresse.getPlz() + " "
                + adresse.getStadt();
    }

    private String formatTyp(Immobilientyp typ) {
        if (typ == null) {
            return "-";
        }

        return switch (typ) {
            case WOHNGEBAEUDE -> "Wohngebäude";
            case MEHRFAMILIENHAUS -> "Mehrfamilienhaus";
            case GEWERBEIMMOBILIE -> "Gewerbeimmobilie";
        };
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

        grid.addColumn(Immobilie::getBezeichnung)
                .setHeader("Immobilie")
                .setAutoWidth(true)
                .setFlexGrow(2);

        grid.addColumn(immobilie -> formatTyp(immobilie.getTyp()))
                .setHeader("Typ")
                .setAutoWidth(true);

        grid.addColumn(this::formatAdresse)
                .setHeader("Adresse")
                .setAutoWidth(true)
                .setFlexGrow(2);

        grid.addColumn(immobilie -> "-")
                .setHeader("Einheiten")
                .setAutoWidth(true);

        grid.addColumn(immobilie -> "-")
                .setHeader("Leerstand")
                .setAutoWidth(true);

        grid.addColumn(immobilie -> "-")
                .setHeader("Offene Posten")
                .setAutoWidth(true);

        grid.addColumn(new ComponentRenderer<>(this::createActionButtons))
                .setHeader("Aktionen")
                .setAutoWidth(true)
                .setFlexGrow(0);
    }

    private Component createActionButtons(Immobilie immobilie) {
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
                getUI().ifPresent(ui -> ui.navigate("immobilien/" + immobilie.getId()))
        );

        bearbeitenButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate("immobilien/" + immobilie.getId() + "/bearbeiten"))
        );

        loeschenButton.addClickListener(event -> {
            ConfirmDeleteDialog dialog = new ConfirmDeleteDialog(
                    "Immobilie löschen?",
                    "Möchtest du die Immobilie \"" + immobilie.getBezeichnung() + "\" wirklich löschen?",
                    () -> {
                        Notification.show("Immobilie würde gelöscht werden: " + immobilie.getBezeichnung());

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
}
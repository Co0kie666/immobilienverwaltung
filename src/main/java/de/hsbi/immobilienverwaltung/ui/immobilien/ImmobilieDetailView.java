package de.hsbi.immobilienverwaltung.ui.immobilien;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import de.hsbi.immobilienverwaltung.ui.components.ConfirmDeleteDialog;
import de.hsbi.immobilienverwaltung.ui.components.StatusBadge;
import de.hsbi.immobilienverwaltung.ui.layout.HasPageHeader;
import de.hsbi.immobilienverwaltung.ui.layout.MainLayout;

import java.util.List;

@Route(value = "immobilien/:immobilieId", layout = MainLayout.class)
public class ImmobilieDetailView extends Div implements HasPageHeader, BeforeEnterObserver {

    private Long immobilieId;
    private final Grid<MieteinheitRow> mieteinheitenGrid = new Grid<>(MieteinheitRow.class, false);

    public ImmobilieDetailView() {
        addClassName("page-content");
        addClassName("immobilie-detail-view");

        add(
                createTopActions(),
                createKpiSection(),
                createMieteinheitenCard(),
                createBelegungCard()
        );
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        this.immobilieId = event.getRouteParameters()
                .get("immobilieId")
                .map(Long::valueOf)
                .orElse(null);

        // Später: Immobilie anhand der ID aus dem Service laden
    }

    @Override
    public String getPageTitle() {
        return "Parkresidenz Süd";
    }

    @Override
    public String getPageSubtitle() {
        return "Immobilien > Parkresidenz Süd";
    }

    private Component createTopActions() {
        HorizontalLayout actionRow = new HorizontalLayout();
        actionRow.addClassName("detail-action-row");

        Button backButton = new Button("Zurück", VaadinIcon.ARROW_LEFT.create());
        backButton.addClassName("secondary-button");
        backButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate(ImmobilienListView.class))
        );

        Button editButton = new Button("Bearbeiten", VaadinIcon.EDIT.create());
        editButton.addClassName("secondary-button");
        editButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate("immobilien/" + immobilieId + "/bearbeiten"))
        );

        Button addUnitButton = new Button("Einheit hinzufügen", VaadinIcon.PLUS.create());
        addUnitButton.addClassName("primary-button");
        addUnitButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate("immobilien/" + immobilieId + "/einheiten/neu"))
        );

        actionRow.add(backButton, editButton, addUnitButton);

        return actionRow;
    }

    private Component createKpiSection() {
        Div kpiGrid = new Div();
        kpiGrid.addClassName("detail-kpi-grid");

        kpiGrid.add(
                createKpiCard("Einheiten Gesamt", "24", "18 Wohnungen, 6 Gewerbe", "primary"),
                createKpiCard("Leerstand", "2", "8.3% Leerstandsquote", "warning"),
                createKpiCard("Offene Zahlungen", "€ 1.250", "2 Mieter im Verzug", "danger")
        );

        return kpiGrid;
    }

    private Component createKpiCard(String title, String value, String subtitle, String type) {
        Div card = new Div();
        card.addClassNames("card", "detail-kpi-card");

        Paragraph titleText = new Paragraph(title);
        titleText.addClassName("kpi-title");

        Span valueText = new Span(value);
        valueText.addClassName("kpi-value");

        Paragraph subtitleText = new Paragraph(subtitle);
        subtitleText.addClassName("kpi-subtitle");

        Div iconBox = new Div();
        iconBox.addClassNames("kpi-icon-box", type);
        iconBox.add(VaadinIcon.BUILDING.create());

        card.add(titleText, valueText, subtitleText, iconBox);

        return card;
    }

    private Component createMieteinheitenCard() {
        Div card = new Div();
        card.addClassName("table-card");

        HorizontalLayout header = new HorizontalLayout();
        header.addClassName("detail-table-header");

        H3 title = new H3("Mieteinheiten");
        title.addClassName("card-title");

        TextField searchField = new TextField();
        searchField.addClassName("detail-table-search");
        searchField.setPlaceholder("Suchen...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);

        header.add(title, searchField);

        configureMieteinheitenGrid();

        card.add(header, mieteinheitenGrid);

        return card;
    }

    private void configureMieteinheitenGrid() {
        mieteinheitenGrid.addClassName("mieteinheiten-grid");
        mieteinheitenGrid.setAllRowsVisible(true);

        mieteinheitenGrid.addColumn(MieteinheitRow::nummer)
                .setHeader("Einheit-Nr.")
                .setAutoWidth(true);

        mieteinheitenGrid.addColumn(MieteinheitRow::typ)
                .setHeader("Typ")
                .setAutoWidth(true);

        mieteinheitenGrid.addColumn(MieteinheitRow::groesse)
                .setHeader("Größe")
                .setAutoWidth(true);

        mieteinheitenGrid.addColumn(MieteinheitRow::miete)
                .setHeader("Miete (kalt)")
                .setAutoWidth(true);

        mieteinheitenGrid.addColumn(new ComponentRenderer<>(row -> {
                    if (row.status().equals("Vermietet")) {
                        return StatusBadge.success("Vermietet");
                    }

                    if (row.status().equals("Frei")) {
                        return StatusBadge.neutral("Frei");
                    }

                    if (row.status().equals("In Renovierung")) {
                        return StatusBadge.warning("In Renovierung");
                    }

                    return StatusBadge.neutral(row.status());
                }))
                .setHeader("Status")
                .setAutoWidth(true);

        mieteinheitenGrid.addColumn(new ComponentRenderer<>(this::createMieteinheitenActionButtons))
                .setHeader("Aktionen")
                .setAutoWidth(true)
                .setFlexGrow(0);

        mieteinheitenGrid.setItems(List.of(
                new MieteinheitRow(1L, "WE-01", "Wohnung (EG)", "85 m²", "€ 1.150,00", "Vermietet"),
                new MieteinheitRow(2L, "WE-02", "Wohnung (1. OG)", "92 m²", "€ 1.250,00", "Vermietet"),
                new MieteinheitRow(3L, "WE-03", "Wohnung (2. OG)", "75 m²", "€ 950,00", "Frei")
        ));
    }

    private Component createBelegungCard() {
        Div card = new Div();
        card.addClassNames("card", "belegung-card");

        H3 title = new H3("Belegungsstatus");
        title.addClassName("card-title");

        Div chartPlaceholder = new Div();
        chartPlaceholder.addClassName("donut-placeholder");
        chartPlaceholder.setText("91.6%");

        Div legend = new Div();
        legend.addClassName("belegung-legend");
        legend.add(new Span("■ Vermietet"), new Span("■ Leerstand"));

        card.add(title, chartPlaceholder, legend);

        return card;
    }

    private Component createMieteinheitenActionButtons(MieteinheitRow mieteinheit) {
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
                getUI().ifPresent(ui -> ui.navigate(
                        "immobilien/" + immobilieId + "/einheiten/" + mieteinheit.id() + "/details"
                ))
        );

        bearbeitenButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate(
                        "immobilien/" + immobilieId + "/einheiten/" + mieteinheit.id() + "/bearbeiten"
                ))
        );

        loeschenButton.addClickListener(event -> {
            ConfirmDeleteDialog dialog = new ConfirmDeleteDialog(
                    "Mieteinheit löschen?",
                    "Möchtest du die Mieteinheit \"" + mieteinheit.nummer() + "\" wirklich löschen?",
                    () -> {
                        Notification.show("Mieteinheit würde gelöscht werden: " + mieteinheit.nummer());

                        // später:
                        // mieteinheitService.loescheMieteinheit(mieteinheit.id());
                        // reloadMieteinheitenGrid();
                    }
            );

            dialog.open();
        });

        actions.add(anzeigenButton, bearbeitenButton, loeschenButton);

        return actions;
    }

    private record MieteinheitRow(
            Long id,
            String nummer,
            String typ,
            String groesse,
            String miete,
            String status
    ) {
    }
}
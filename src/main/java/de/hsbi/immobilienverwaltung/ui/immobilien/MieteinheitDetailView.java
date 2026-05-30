package de.hsbi.immobilienverwaltung.ui.immobilien;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import de.hsbi.immobilienverwaltung.domain.Mieteinheit;
import de.hsbi.immobilienverwaltung.domain.enums.Immobilientyp;
import de.hsbi.immobilienverwaltung.domain.enums.MieteinheitTyp;
import de.hsbi.immobilienverwaltung.domain.enums.Mieteinheitstatus;
import de.hsbi.immobilienverwaltung.security.LoginRequired;
import de.hsbi.immobilienverwaltung.service.interfaces.MieteinheitService;
import de.hsbi.immobilienverwaltung.ui.components.ConfirmDeleteDialog;
import de.hsbi.immobilienverwaltung.ui.components.StatusBadge;
import de.hsbi.immobilienverwaltung.ui.layout.HasPageHeader;
import de.hsbi.immobilienverwaltung.ui.layout.MainLayout;

@Route(value = "immobilien/:immobilieId/einheiten/:mieteinheitId/details", layout = MainLayout.class)
public class MieteinheitDetailView extends Div implements HasPageHeader, BeforeEnterObserver, LoginRequired {

    private final MieteinheitService mieteinheitService;
    private Long immobilieId;
    private Long mieteinheitId;

    private Mieteinheit mieteinheit;

    public MieteinheitDetailView(MieteinheitService mieteinheitService) {
        this.mieteinheitService = mieteinheitService;
        addClassName("page-content");
        addClassName("mieteinheit-detail-view");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        this.immobilieId = event.getRouteParameters()
                .get("immobilieId")
                .map(Long::valueOf)
                .orElseThrow(() -> new IllegalArgumentException("Immobilie-ID fehlt."));

        this.mieteinheitId = event.getRouteParameters()
                .get("mieteinheitId")
                .map(Long::valueOf)
                .orElseThrow(() -> new IllegalArgumentException("Mieteinheit-ID fehlt."));

        this.mieteinheit = mieteinheitService.findeMieteinheitNachId(mieteinheitId)
                .orElseThrow(() -> new IllegalArgumentException("Mieteinheit wurde nicht gefunden."));

        removeAll();

        add(
                createTopActions(),
                createInfoGrid(),
                createMietvertragHistorieCard()
        );
    }

    private Component createTopActions() {
        HorizontalLayout actionRow = new HorizontalLayout();
        actionRow.addClassName("detail-action-row");

        Button backButton = new Button("Zurück", VaadinIcon.ARROW_LEFT.create());
        backButton.addClassName("secondary-button");
        backButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate("immobilien/" + immobilieId))
        );

        Button editButton = new Button("Bearbeiten", VaadinIcon.EDIT.create());
        editButton.addClassName("primary-button");
        editButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate(
                        "immobilien/" + immobilieId + "/einheiten/" + mieteinheitId + "/bearbeiten"
                ))
        );

        Button deleteButton = new Button("Löschen", VaadinIcon.TRASH.create());
        deleteButton.addClassName("danger-button");
        deleteButton.addClickListener(event -> {
            ConfirmDeleteDialog dialog = new ConfirmDeleteDialog(
                    "Mieteinheit löschen?",
                    "Möchtest du die Mieteinheit \"" + mieteinheit.getBezeichnung() + "\" wirklich löschen?",
                    () -> {
                        mieteinheitService.loescheMieteinheit(mieteinheitId);
                        Notification.show("Mieteinheit wurde gelöscht: " + mieteinheit.getBezeichnung());
                        getUI().ifPresent(ui -> ui.navigate("immobilien/" + immobilieId));
                    }
            );

            dialog.open();
        });

        actionRow.add(backButton, editButton, deleteButton);

        return actionRow;
    }

    private Component createInfoGrid() {
        Div grid = new Div();
        grid.addClassName("mieteinheit-info-grid");

        Div stammdatenCard = new Div();
        stammdatenCard.addClassNames("card", "mieteinheit-info-card");

        H3 stammdatenTitle = new H3("Stammdaten");
        stammdatenTitle.addClassName("card-title");

        stammdatenCard.add(
                stammdatenTitle,
                createInfoItem("Einheit-Nr.", mieteinheit.getBezeichnung()),
                createInfoItem("Typ", formatMieteinheitTyp(mieteinheit.getTyp())),
                createInfoItem("Größe", valueOrDash(mieteinheit.getGroesse()) + " m²"),
                createInfoItem("Stockwerk", valueOrDash(mieteinheit.getStockwerk())),
                createInfoItem("Zimmeranzahl", valueOrDash(mieteinheit.getZimmerzahl())),
                createStatusItem("Status", StatusBadge.neutral(formatStatus(mieteinheit.getStatus())))
        );

        grid.add(stammdatenCard, createAktuellerMietvertragCard());

        return grid;
    }

    private String valueOrDash(Object value) {
        return value == null ? "-" : value.toString();
    }

    private String formatStatus(Mieteinheitstatus status) {
        if (status == null) return null;

        return switch (status) {
            case FREI -> "Frei";
            case VERMIETET -> "Vermietet";
            case IN_RENOVIERUNG -> "In Renovierung";
        };
    }

    private String formatMieteinheitTyp(MieteinheitTyp typ) {
        if (typ == null) {
            return "-";
        }

        return switch (typ) {
            case WOHNUNG -> "Wohnung";
            case BUERO -> "Büro";
            case LAGERHALLE ->  "Lagerhalle";
            case GEWERBEFLAECHE -> "Gewerbeflaeche";
            case GESAMTOBJEKT ->  "Gesamtobjekt";
        };
    }

    private Component createAktuellerMietvertragCard() {
        if (hatAktivenMietvertrag()) {
            return createAktiverMietvertragCard();
        }

        return createKeinMietvertragCard();
    }

    private boolean hatAktivenMietvertrag() {
        // TODO Später: über MietvertragService prüfen
        return false;
    }

    private Component createKeinMietvertragCard() {
        Div card = new Div();
        card.addClassNames("card", "mieteinheit-info-card");

        H3 title = new H3("Aktueller Mietvertrag");
        title.addClassName("card-title");

        Paragraph text = new Paragraph(
                "Für diese Mieteinheit ist aktuell kein aktiver Mietvertrag vorhanden. Daher gibt es noch keine verbindlichen Mietdaten."
        );
        text.addClassName("card-subtitle");

        Button createContractButton = new Button("Mietvertrag anlegen", VaadinIcon.PLUS.create());
        createContractButton.addClassName("primary-button");

        // TODO Festlegen dass beim anlegen direkt die jeweilige Immobilie + Mieteinheit ausgewählt ist
        createContractButton.addClickListener(event ->
                 getUI().ifPresent(ui -> ui.navigate("mietvertrag-anlegen"))
        );

        card.add(title, text, createContractButton);

        return card;
    }

    private Component createAktiverMietvertragCard() {
        Div card = new Div();
        card.addClassNames("card", "mieteinheit-info-card");

        H3 title = new H3("Aktueller Mietvertrag");
        title.addClassName("card-title");

        card.add(
                title,
                createInfoItem("Mieter", "Max Mustermann"),
                createStatusItem("Status", StatusBadge.success("Aktiv")),
                createInfoItem("Vertragsbeginn", "01.10.2021"),
                createInfoItem("Kaltmiete", "€ 950,00"),
                createInfoItem("Nebenkosten", "€ 180,00"),
                createInfoItem("Warmmiete", "€ 1.130,00")
        );

        Button showContractButton = new Button("Mietvertrag anzeigen", VaadinIcon.EYE.create());
        showContractButton.addClassName("secondary-button");

        card.add(showContractButton);

        return card;
    }

    private Component createMietvertragHistorieCard() {
        Div card = new Div();
        card.addClassNames("card", "mieteinheit-contract-card");

        H3 title = new H3("Mietvertragshistorie");
        title.addClassName("card-title");

        Paragraph text = new Paragraph("Bisher sind keine früheren Mietverträge für diese Mieteinheit vorhanden.");
        text.addClassName("card-subtitle");

        card.add(title, text);

        return card;
    }

    private Component createInfoItem(String label, String value) {
        Div item = new Div();
        item.addClassName("info-item");

        Span labelText = new Span(label);
        labelText.addClassName("info-label");

        Span valueText = new Span(value);
        valueText.addClassName("info-value");

        item.add(labelText, valueText);

        return item;
    }

    private Component createStatusItem(String label, Component statusBadge) {
        Div item = new Div();
        item.addClassName("info-item");

        Span labelText = new Span(label);
        labelText.addClassName("info-label");

        item.add(labelText, statusBadge);

        return item;
    }

    @Override
    public String getPageTitle() {
        return mieteinheit != null
                ? "Mieteinheit " + mieteinheit.getBezeichnung()
                : "Mieteinheit";
    }

    @Override
    public String getPageSubtitle() {
        return mieteinheit != null
                ? "Immobilien > Mieteinheit " + mieteinheit.getBezeichnung()
                : "Immobilien > Mieteinheit";
    }

}
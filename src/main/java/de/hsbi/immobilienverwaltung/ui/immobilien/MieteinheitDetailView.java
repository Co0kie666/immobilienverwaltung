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
import de.hsbi.immobilienverwaltung.domain.Mietvertrag;
import de.hsbi.immobilienverwaltung.domain.enums.Vertragsstatus;
import de.hsbi.immobilienverwaltung.service.interfaces.MieteinheitService;
import de.hsbi.immobilienverwaltung.service.interfaces.MietvertragService;
import de.hsbi.immobilienverwaltung.ui.components.ConfirmDeleteDialog;
import de.hsbi.immobilienverwaltung.ui.components.StatusBadge;
import de.hsbi.immobilienverwaltung.ui.layout.HasPageHeader;
import de.hsbi.immobilienverwaltung.ui.layout.MainLayout;
import de.hsbi.immobilienverwaltung.ui.mieter.MietvertragListView;
import jakarta.annotation.security.PermitAll;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Route(value = "immobilien/:immobilieId/einheiten/:mieteinheitId/details", layout = MainLayout.class)
@PermitAll
public class MieteinheitDetailView extends Div implements HasPageHeader, BeforeEnterObserver {

    private final MieteinheitService mieteinheitService;
    private final MietvertragService mietvertragService;

    private Long immobilieId;
    private Long mieteinheitId;

    private Mieteinheit mieteinheit;
    private List<Mietvertrag> mietvertraege = List.of();

    public MieteinheitDetailView(MieteinheitService mieteinheitService, MietvertragService mietvertragService) {
        this.mieteinheitService = mieteinheitService;
        this.mietvertragService = mietvertragService;

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

        this.mietvertraege = mietvertragService.findeMietvertraegeNachMieteinheit(mieteinheitId);

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
                createInfoItem("Einheit-Nr.", valueOrDash(mieteinheit.getBezeichnung())),
                createInfoItem("Typ", mieteinheit.getTyp() == null ? "-" : mieteinheit.getTyp().getLabel()),
                createInfoItem("Größe", mieteinheit.getGroesse() == null ? "-" : mieteinheit.getGroesse() + " m²"),
                createInfoItem("Stockwerk", valueOrDash(mieteinheit.getStockwerk())),
                createInfoItem("Zimmeranzahl", valueOrDash(mieteinheit.getZimmerzahl())),
                createStatusItem("Status", StatusBadge.neutral(mieteinheit.getStatus().getLabel()))
        );

        grid.add(stammdatenCard, createAktuellerMietvertragCard());

        return grid;
    }

    private Component createAktuellerMietvertragCard() {
        Mietvertrag aktuellerMietvertrag = findeAktuellenMietvertrag();

        if (aktuellerMietvertrag == null) {
            return createKeinMietvertragCard();
        }

        return createAktiverMietvertragCard(aktuellerMietvertrag);
    }

    private Mietvertrag findeAktuellenMietvertrag() {
        return mietvertraege.stream()
                .filter(this::istLaufenderVertrag)
                .findFirst()
                .orElse(null);
    }

    private Component createKeinMietvertragCard() {
        Div card = new Div();
        card.addClassNames("card", "mieteinheit-info-card");

        H3 title = new H3("Aktueller Mietvertrag");
        title.addClassName("card-title");

        Paragraph text = new Paragraph(
                "Für diese Mieteinheit ist aktuell kein laufender Mietvertrag vorhanden."
        );
        text.addClassName("card-subtitle");

        Button createContractButton = new Button("Mietvertrag anlegen", VaadinIcon.PLUS.create());
        createContractButton.addClassName("primary-button");

        createContractButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate("mietvertrag-anlegen"))
        );

        card.add(title, text, createContractButton);

        return card;
    }

    private Component createAktiverMietvertragCard(Mietvertrag mietvertrag) {
        Div card = new Div();
        card.addClassNames("card", "mieteinheit-info-card");

        H3 title = new H3("Aktueller Mietvertrag");
        title.addClassName("card-title");

        card.add(
                title,
                createInfoItem("Mieter", formatMieterName(mietvertrag)),
                createStatusItem("Status", createVertragsStatusBadge(mietvertrag)),
                createInfoItem("Vertragsbeginn", formatDatum(mietvertrag.getStartdatum())),
                createInfoItem("Vertragsende", mietvertrag.getEnddatum() == null ? "unbefristet" : formatDatum(mietvertrag.getEnddatum())),
                createInfoItem("Kaltmiete", formatEuro(mietvertrag.getKaltmiete())),
                createInfoItem("Nebenkosten", formatEuro(mietvertrag.getNebenkosten())),
                createInfoItem("Warmmiete", formatWarmmiete(mietvertrag))
        );

        Button showContractButton = new Button("Mietvertrag anzeigen", VaadinIcon.EYE.create());
        showContractButton.addClassName("secondary-button");
        showContractButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate(MietvertragListView.class, String.valueOf(mietvertrag.getId())))
        );

        card.add(showContractButton);

        return card;
    }

    private Component createMietvertragHistorieCard() {
        Div card = new Div();
        card.addClassNames("card", "mieteinheit-contract-card");

        H3 title = new H3("Mietvertragshistorie");
        title.addClassName("card-title");

        List<Mietvertrag> historischeVertraege = mietvertraege.stream()
                .filter(this::istHistorischerVertrag)
                .sorted(this::vergleicheNachEnddatumAbsteigend)
                .toList();

        card.add(title);

        if (historischeVertraege.isEmpty()) {
            Paragraph text = new Paragraph("Bisher sind keine beendeten Mietverträge für diese Mieteinheit vorhanden.");
            text.addClassName("card-subtitle");
            card.add(text);
            return card;
        }

        historischeVertraege.forEach(mietvertrag -> card.add(createHistorieEintrag(mietvertrag)));

        return card;
    }

    private Component createHistorieEintrag(Mietvertrag mietvertrag) {
        Div eintrag = new Div();
        eintrag.addClassName("contract-history-row");

        Button showButton = new Button("Anzeigen", VaadinIcon.EYE.create());
        showButton.addClassName("secondary-button");
        showButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate(MietvertragListView.class, String.valueOf(mietvertrag.getId())))
        );

        eintrag.add(
                createInfoItem("Mieter", formatMieterName(mietvertrag)),
                createInfoItem("Zeitraum", formatZeitraum(mietvertrag)),
                createInfoItem("Warmmiete", formatWarmmiete(mietvertrag)),
                createStatusItem("Status", createVertragsStatusBadge(mietvertrag)),
                showButton
        );

        return eintrag;
    }

    private boolean istLaufenderVertrag(Mietvertrag mietvertrag) {
        if (mietvertrag == null || mietvertrag.getStatus() == null) {
            return false;
        }

        if (mietvertrag.getStatus() == Vertragsstatus.AKTIV) {
            return true;
        }

        return mietvertrag.getStatus() == Vertragsstatus.GEKUENDIGT
                && (
                mietvertrag.getEnddatum() == null
                        || !mietvertrag.getEnddatum().isBefore(LocalDate.now())
        );
    }

    private boolean istHistorischerVertrag(Mietvertrag mietvertrag) {
        if (mietvertrag == null || mietvertrag.getStatus() == null) {
            return false;
        }

        return mietvertrag.getStatus() == Vertragsstatus.BEENDET
                || (
                mietvertrag.getStatus() == Vertragsstatus.GEKUENDIGT
                        && mietvertrag.getEnddatum() != null
                        && mietvertrag.getEnddatum().isBefore(LocalDate.now())
        );
    }

    private int vergleicheNachEnddatumAbsteigend(Mietvertrag a, Mietvertrag b) {
        LocalDate aEnde = a.getEnddatum();
        LocalDate bEnde = b.getEnddatum();

        if (aEnde == null && bEnde == null) {
            return 0;
        }

        if (aEnde == null) {
            return 1;
        }

        if (bEnde == null) {
            return -1;
        }

        return bEnde.compareTo(aEnde);
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

    private Component createVertragsStatusBadge(Mietvertrag mietvertrag) {
        String status = formatVertragsstatus(mietvertrag);

        Span badge = new Span(status);
        badge.addClassNames("status-badge", getVertragsStatusStyle(mietvertrag));

        return badge;
    }

    private String formatVertragsstatus(Mietvertrag mietvertrag) {
        if (mietvertrag == null || mietvertrag.getStatus() == null) {
            return "-";
        }

        if (mietvertrag.getStatus() == Vertragsstatus.AKTIV) {
            return "Aktiv";
        }

        if (mietvertrag.getStatus() == Vertragsstatus.GEKUENDIGT && istLaufenderVertrag(mietvertrag)) {
            return "Läuft aus";
        }

        return "Beendet";
    }

    private String getVertragsStatusStyle(Mietvertrag mietvertrag) {
        if (mietvertrag == null || mietvertrag.getStatus() == null) {
            return "neutral";
        }

        if (mietvertrag.getStatus() == Vertragsstatus.AKTIV) {
            return "success";
        }

        if (mietvertrag.getStatus() == Vertragsstatus.GEKUENDIGT && istLaufenderVertrag(mietvertrag)) {
            return "warning";
        }

        return "neutral";
    }

    private String formatMieterName(Mietvertrag mietvertrag) {
        if (mietvertrag == null || mietvertrag.getMieter() == null) {
            return "-";
        }

        String vorname = mietvertrag.getMieter().getVorname() == null ? "" : mietvertrag.getMieter().getVorname();
        String nachname = mietvertrag.getMieter().getNachname() == null ? "" : mietvertrag.getMieter().getNachname();

        return (vorname + " " + nachname).trim();
    }

    private String formatZeitraum(Mietvertrag mietvertrag) {
        return formatDatum(mietvertrag.getStartdatum()) + " - " + formatDatum(mietvertrag.getEnddatum());
    }

    private String formatDatum(LocalDate datum) {
        if (datum == null) {
            return "-";
        }

        return datum.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }

    private String formatWarmmiete(Mietvertrag mietvertrag) {
        double kaltmiete = mietvertrag.getKaltmiete() == null ? 0 : mietvertrag.getKaltmiete();
        double nebenkosten = mietvertrag.getNebenkosten() == null ? 0 : mietvertrag.getNebenkosten();

        return formatEuro(kaltmiete + nebenkosten);
    }

    private String formatEuro(Double betrag) {
        double wert = betrag == null ? 0 : betrag;
        return NumberFormat.getCurrencyInstance(Locale.GERMANY).format(wert);
    }

    private String valueOrDash(Object value) {
        return value == null ? "-" : value.toString();
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
package de.hsbi.immobilienverwaltung.ui.immobilien;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
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
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import de.hsbi.immobilienverwaltung.service.interfaces.ZahlungsEingangService;
import de.hsbi.immobilienverwaltung.ui.components.ConfirmDeleteDialog;
import de.hsbi.immobilienverwaltung.ui.layout.HasPageHeader;
import de.hsbi.immobilienverwaltung.ui.layout.MainLayout;
import de.hsbi.immobilienverwaltung.domain.Mieteinheit;
import de.hsbi.immobilienverwaltung.service.interfaces.MieteinheitService;
import de.hsbi.immobilienverwaltung.domain.Adresse;
import de.hsbi.immobilienverwaltung.domain.Immobilie;
import de.hsbi.immobilienverwaltung.service.interfaces.ImmobilieService;
import com.vaadin.flow.data.value.ValueChangeMode;
import jakarta.annotation.security.PermitAll;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

@Route(value = "immobilien/:immobilieId", layout = MainLayout.class)
@PermitAll
public class ImmobilieDetailView extends Div implements HasPageHeader, BeforeEnterObserver {

    private Long immobilieId;
    private final MieteinheitService mieteinheitService;
    private final Grid<Mieteinheit> mieteinheitenGrid = new Grid<>(Mieteinheit.class, false);
    private Immobilie immobilie;
    private final ImmobilieService immobilieService;

    private final TextField mieteinheitenSearchField = new TextField();
    private final ZahlungsEingangService zahlungsEingangService;

    public ImmobilieDetailView(MieteinheitService mieteinheitService, ImmobilieService immobilieService, ZahlungsEingangService zahlungsEingangService) {
        this.mieteinheitService = mieteinheitService;
        this.immobilieService = immobilieService;
        this.zahlungsEingangService = zahlungsEingangService;

        addClassName("page-content");
        addClassName("immobilie-detail-view");

        konfiguriereMieteinheitenSuchfeld();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        this.immobilieId = event.getRouteParameters()
                .get("immobilieId")
                .map(Long::valueOf)
                .orElse(null);

        ladeImmobilie();

        // Vaadin Methode -> verhindert dass Komponenten doppelt angezeigt werden
        removeAll();

        add(
                createTopActions(),
                createKpiSection(),
                createOverviewSection(),
                createMieteinheitenCard()
        );

        ladeMieteinheiten();
    }

    private void ladeImmobilie() {
        this.immobilie = immobilieService.findeImmobilieNachId(immobilieId)
                .orElseThrow(() -> new IllegalArgumentException("Immobilie wurde nicht gefunden."));
    }

    private void wendeMieteinheitenSucheAn() {
        if (immobilieId == null) {
            return;
        }

        mieteinheitenGrid.setItems(mieteinheitService.sucheMieteinheitenDerImmobilie(
                        immobilieId,
                        mieteinheitenSearchField.getValue()
                )
        );
    }

    private void ladeMieteinheiten() {
        wendeMieteinheitenSucheAn();
    }

    // ImmobilienCard und BelegungsCard nebeneinander
    private Component createOverviewSection() {
        Div overview = new Div();
        overview.addClassName("detail-overview-grid");

        overview.add(
                createImmobilieInfoCard(),
                createBelegungCard()
        );

        return overview;
    }

    private Component createImmobilieInfoCard() {
        Div card = new Div();
        card.addClassNames("card", "immobilie-info-card");

        H3 title = new H3("Stammdaten");
        title.addClassName("card-title");

        card.add(
                title,
                createInfoItem("Bezeichnung", valueOrDash(immobilie.getBezeichnung())),
                createInfoItem("Typ",valueOrDash(immobilie.getTyp().getLabel())),
                createInfoItem("Baujahr", valueOrDash(immobilie.getBaujahr())),
                createInfoItem("Fläche", formatFlaeche(immobilie.getFlaeche())),
                createInfoItem("Adresse", formatAdresse(immobilie))
        );

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

    private String formatFlaeche(Integer flaeche) {
        if (flaeche == null) {
            return "-";
        }

        return flaeche + " m²";
    }

    private String valueOrDash(Object value) {
        return value == null ? "-" : value.toString();
    }

    @Override
    public String getPageTitle() {
        return immobilie != null ? immobilie.getBezeichnung() : "Immobilie";
    }

    @Override
    public String getPageSubtitle() {
        return immobilie != null
                ? "Immobilien > " + immobilie.getBezeichnung()
                : "Immobilien > Detailansicht";
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

        Button deleteButton = new Button("Löschen", VaadinIcon.TRASH.create());
        deleteButton.addClassName("danger-button");
        deleteButton.addClickListener(event -> {
            ConfirmDeleteDialog dialog = new ConfirmDeleteDialog(
                    "Immobilie löschen?",
                    "Möchtest du die Immobilie \"" + immobilie.getBezeichnung() + "\" wirklich löschen?",
                    () -> {
                        try {
                            immobilieService.loescheImmobilie(immobilieId);
                            Notification.show("Immobilie wurde gelöscht.");
                            getUI().ifPresent(ui -> ui.navigate("immobilien"));
                        } catch (Exception ex) {
                            Notification.show(ex.getMessage(), 4000, Notification.Position.MIDDLE);
                        }
                    }
            );

            dialog.open();
        });

        Button addUnitButton = new Button("Einheit hinzufügen", VaadinIcon.PLUS.create());
        addUnitButton.addClassName("primary-button");
        addUnitButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate("immobilien/" + immobilieId + "/einheiten/neu"))
        );

        actionRow.add(backButton, editButton, deleteButton, addUnitButton);

        return actionRow;
    }

    private Component createKpiSection() {
        Div kpiGrid = new Div();
        kpiGrid.addClassName("detail-kpi-grid");

        long einheitenGesamt = mieteinheitService.zaehleMieteinheiten(immobilieId);
        long frei = mieteinheitService.zaehleFreieMieteinheiten(immobilieId);
        long inRenovierung = mieteinheitService.zaehleMieteinheitenInRenovierung(immobilieId);
        long leerstand = frei + inRenovierung;
        long vermietet = mieteinheitService.zaehleVermieteteMieteinheiten(immobilieId);

        double leerstandsquote = mieteinheitService.berechneLeerstandsquote(immobilieId);

        BigDecimal offeneZahlungenSumme = zahlungsEingangService.berechneOffeneZahlungenFuerImmobilie(immobilieId);

        long offeneZahlungenAnzahl = zahlungsEingangService.zaehleOffeneZahlungenFuerImmobilie(immobilieId);

        kpiGrid.add(
                createKpiCard(
                        "Einheiten Gesamt",
                        String.valueOf(einheitenGesamt),
                        vermietet + " vermietet, " + inRenovierung + " in Renovierung",
                        "primary",
                        VaadinIcon.BUILDING
                ),
                createKpiCard(
                        "Leerstand",
                        String.valueOf(leerstand),
                        String.format("%.1f%% Leerstandsquote", leerstandsquote),
                        "warning",
                        VaadinIcon.HOME
                ),
                createKpiCard(
                        "Offene Zahlungen",
                        formatiereBetrag(offeneZahlungenSumme),
                        offeneZahlungenAnzahl + " offene Buchung(en)",
                        "danger",
                        VaadinIcon.WARNING
                )
        );

        return kpiGrid;
    }

    private Component createKpiCard(String title, String value, String subtitle, String type, VaadinIcon icon) {
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
        iconBox.add(icon.create());

        card.add(titleText, valueText, subtitleText, iconBox);

        return card;
    }

    private void konfiguriereMieteinheitenSuchfeld() {
        mieteinheitenSearchField.addClassName("detail-table-search");
        mieteinheitenSearchField.setPlaceholder("Suchen...");
        mieteinheitenSearchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        mieteinheitenSearchField.setClearButtonVisible(true);
        mieteinheitenSearchField.setValueChangeMode(ValueChangeMode.LAZY);

        mieteinheitenSearchField.addValueChangeListener(event -> wendeMieteinheitenSucheAn());
    }

    private Component createMieteinheitenCard() {
        Div card = new Div();
        card.addClassName("table-card");

        HorizontalLayout header = new HorizontalLayout();
        header.addClassName("detail-table-header");

        H3 title = new H3("Mieteinheiten");
        title.addClassName("card-title");

        header.add(title, mieteinheitenSearchField);

        configureMieteinheitenGrid();

        card.add(header, mieteinheitenGrid);

        return card;
    }

    private void configureMieteinheitenGrid() {
        mieteinheitenGrid.removeAllColumns();

        mieteinheitenGrid.addClassName("mieteinheiten-grid");
        mieteinheitenGrid.setAllRowsVisible(true);

        mieteinheitenGrid.addColumn(Mieteinheit::getBezeichnung)
                .setHeader("Bezeichnung")
                .setAutoWidth(true);

        mieteinheitenGrid.addColumn(mieteinheit -> mieteinheit.getTyp().getLabel())
                .setHeader("Typ")
                .setAutoWidth(true);

        mieteinheitenGrid.addColumn(mieteinheit -> valueOrDash(mieteinheit.getGroesse()) + " m²")
                .setHeader("Größe")
                .setAutoWidth(true);

        mieteinheitenGrid.addColumn(mieteinheit -> valueOrDash(mieteinheit.getStockwerk()))
                .setHeader("Stockwerk")
                .setAutoWidth(true);

        mieteinheitenGrid.addColumn(mieteinheit -> valueOrDash(mieteinheit.getZimmerzahl()))
                .setHeader("Zimmer")
                .setAutoWidth(true);

        mieteinheitenGrid.addColumn(mieteinheit -> mieteinheit.getStatus().getLabel())
                .setHeader("Status")
                .setAutoWidth(true);

        mieteinheitenGrid.addItemClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate(
                        "immobilien/" + immobilieId + "/einheiten/" + event.getItem().getId() + "/details"
                ))
        );
    }

    private Component createBelegungCard() {
        Div card = new Div();
        card.addClassNames("card", "belegung-card");

        H3 title = new H3("Leerstandsquote");
        title.addClassName("card-title");

        long vermietet = mieteinheitService.zaehleVermieteteMieteinheiten(immobilieId);
        long frei = mieteinheitService.zaehleFreieMieteinheiten(immobilie.getId());
        long inRenovierung = mieteinheitService.zaehleMieteinheitenInRenovierung(immobilie.getId());
        long leerstand = frei + inRenovierung;
        double leerstandsquote = mieteinheitService.berechneLeerstandsquote(immobilieId);

        String centerText = String.format("%.1f%%", leerstandsquote);

        Html canvas = new Html("""
        <div style="width:100%; max-width:280px; margin:auto;">
            <canvas id="belegungChart"></canvas>
        </div>
    """);

        card.add(title, canvas);

        card.getElement().executeJs("""
        const vermietet = Number($0);
        const leerstand = Number($1);
        const centerText = $2;

        function renderBelegungChart() {
            const ctx = document.getElementById('belegungChart');

            if (window.belegungChartInstance) {
                window.belegungChartInstance.destroy();
            }

            window.belegungChartInstance = new Chart(ctx, {
                type: 'doughnut',
                data: {
                    labels: ['Vermietet', 'Leerstand'],
                    datasets: [{
                        data: [vermietet, leerstand],
                        borderWidth: 0
                    }]
                },
                options: {
                    cutout: '70%',
                    responsive: true,
                    plugins: {
                        legend: {
                            position: 'bottom'
                        },
                        tooltip: {
                            callbacks: {
                                label: function(context) {
                                    const total = context.dataset.data.reduce((a, b) => a + b, 0);
    
                                    if (total === 0) {
                                        return context.label + ': 0%';
                                    }

                                    const value = context.raw;
                                    const percent = ((value / total) * 100).toFixed(1);

                                    return context.label + ': ' + value + ' Einheit(en), ' + percent + '%';
                                }
                            }
                        }
                    }
                },
                plugins: [{
                    id: 'centerText',
                    beforeDraw(chart) {
                        const { width, height, ctx } = chart;
                        ctx.restore();

                        const fontSize = (height / 120).toFixed(2);
                        ctx.font = `bold ${fontSize}em sans-serif`;
                        ctx.textBaseline = 'middle';

                        const text = centerText;
   
                        ctx.textAlign = 'center';
                        ctx.textBaseline = 'middle';
    
                        const centerX = (chart.chartArea.left + chart.chartArea.right) / 2;
                        const centerY = (chart.chartArea.top + chart.chartArea.bottom) / 2;
    
                        ctx.fillText(text, centerX, centerY);
                        ctx.save();
                    }
                }]
            });
        }

        if (!window.Chart) {
            const script = document.createElement('script');
            script.src = 'https://cdn.jsdelivr.net/npm/chart.js';
            script.onload = () => renderBelegungChart();
            document.head.appendChild(script);
        } else {
            renderBelegungChart();
        }
    """, vermietet, leerstand, centerText);

        return card;
    }

    private String formatiereBetrag(BigDecimal betrag) {
        if (betrag == null) {
            return "0,00 €";
        }

        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.GERMANY);
        return formatter.format(betrag);
    }
}
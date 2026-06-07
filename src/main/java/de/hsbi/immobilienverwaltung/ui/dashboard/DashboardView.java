package de.hsbi.immobilienverwaltung.ui.dashboard;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.Route;
import de.hsbi.immobilienverwaltung.domain.Zahlungseingang;
import jakarta.annotation.security.PermitAll;

import de.hsbi.immobilienverwaltung.service.interfaces.GesamtAuswertungService;
import de.hsbi.immobilienverwaltung.service.interfaces.ZahlungsEingangService;
import de.hsbi.immobilienverwaltung.service.interfaces.AusgabeService;
import de.hsbi.immobilienverwaltung.ui.layout.HasPageHeader;
import de.hsbi.immobilienverwaltung.ui.layout.MainLayout;

import java.math.BigDecimal;
import java.util.List;

@Route(value = "dashboard", layout = MainLayout.class)
@PermitAll
public class DashboardView extends Div implements HasPageHeader {


    private final long gesamtMieteinheiten;
    private final long leerstehendeMieteinheiten;
    private final long vermieteteMieteinheiten;
    private final double leerstandsquote;
    private final long aktiveVertraege;
    private final BigDecimal gesamteinnahmen;
    private final BigDecimal offeneAusgaben;
    private final long anzahlOffeneAusgaben;
    private final List<Zahlungseingang> offeneZahlungseingaenge;

    private final double[] chartEinnahmen;
    private final double[] chartAusgaben;
    private final String[] chartMonate;

    public DashboardView(
            GesamtAuswertungService gesamtAuswertungService,
            ZahlungsEingangService zahlungsEingangService,
            AusgabeService ausgabeService
    ) {
        this.gesamtMieteinheiten =
                gesamtAuswertungService.berechneAnzahlMieteinheiten();

        this.leerstehendeMieteinheiten =
                gesamtAuswertungService.berechneAnzahlLeerstehendeMieteinheiten();

        this.vermieteteMieteinheiten =
                gesamtMieteinheiten - leerstehendeMieteinheiten;

        this.leerstandsquote =
                gesamtAuswertungService.berechneLeerstandsquote();

        this.aktiveVertraege =
                gesamtAuswertungService.berechneAnzahlAktiveVertraege();

        this.gesamteinnahmen =
                zahlungsEingangService.berechneGesamteBezahlteZahlungseingaenge();

        this.offeneAusgaben =
                ausgabeService.berechneOffeneAusgaben();

        this.anzahlOffeneAusgaben =
                ausgabeService.zaehleOffeneAusgaben();

        this.chartEinnahmen =
                berechneEinnahmenChartDaten(zahlungsEingangService);

        this.chartAusgaben =
                berechneAusgabenChartDaten(ausgabeService);

        this.chartMonate =
                berechneChartMonate();

        this.offeneZahlungseingaenge =
                zahlungsEingangService.findeOffeneZahlungseingaenge();

        UI.getCurrent().getPage().addJavaScript(
                "https://cdn.jsdelivr.net/npm/chart.js"
        );

        addClassName("page-content");

        add(createActionBar());
        add(createKpiSection());
        add(createChartSection());
    }

    private Component createActionBar() {

        HorizontalLayout layout = new HorizontalLayout();

        layout.setWidthFull();

        layout.setJustifyContentMode(
                FlexComponent.JustifyContentMode.BETWEEN
        );

        layout.setAlignItems(FlexComponent.Alignment.CENTER);

        layout.addClassName("page-section");

        Button neueImmobilie = primaryButton("Neue Immobilie", VaadinIcon.PLUS);
        neueImmobilie.addClickListener(e ->
                UI.getCurrent().navigate("immobilien/neu")
        );

        Button neuerMieter = secondaryButton("Neuer Mieter", VaadinIcon.USER);
        neuerMieter.addClickListener(e ->
                UI.getCurrent().navigate("mieter-anlegen")
        );

        Button neueZahlung = secondaryButton("Neue Zahlung", VaadinIcon.EURO);
        neueZahlung.addClickListener(e ->
                UI.getCurrent().navigate("finanzen/buchung-neu")
        );

        HorizontalLayout actions = new HorizontalLayout(
                neueImmobilie,
                neuerMieter,
                neueZahlung
        );

        layout.add(actions);

        return layout;
    }

    private String formatEuro(BigDecimal betrag) {
        if (betrag == null) {
            betrag = BigDecimal.ZERO;
        }

        return java.text.NumberFormat
                .getCurrencyInstance(java.util.Locale.GERMANY)
                .format(betrag);
    }

    private String formatAnzahlOffeneAusgaben(long anzahl) {
        if (anzahl == 1) {
            return "1 ausstehende Ausgabe";
        }

        return anzahl + " ausstehende Ausgaben";
    }

    private Component createKpiSection() {

        HorizontalLayout layout = new HorizontalLayout();

        layout.setWidthFull();

        layout.addClassName("page-section");

        layout.add(
                kpiCard(
                        "Gesamteinnahmen",
                        formatEuro(gesamteinnahmen),
                        "",
                        "Summe aller Zahlungseingänge",
                        VaadinIcon.LINE_CHART,
                        "primary"
                ),


                kpiCard(
                        "Leerstandsquote",
                        String.format("%.2f %%", leerstandsquote),
                        "",
                        leerstehendeMieteinheiten
                                + " von "
                                + gesamtMieteinheiten
                                + " Mieteinheiten frei oder in Renovierung",
                        VaadinIcon.HOME,
                        "danger"
                ),

                kpiCard(
                        "Offene Zahlungen",
                        formatEuro(offeneAusgaben),
                        "",
                        formatAnzahlOffeneAusgaben(anzahlOffeneAusgaben),
                        VaadinIcon.WARNING,
                        "warning"
                ),

                kpiCard(
                        "Aktive Verträge",
                        String.valueOf(aktiveVertraege),
                        "",
                        "Laufende Mietverträge",
                        VaadinIcon.USERS,
                        "success"
                )//
        );

        layout.getChildren().forEach(component ->
                component.getElement().getStyle().set("flex", "1")
        );

        return layout;
    }

    private double[] berechneEinnahmenChartDaten(
            ZahlungsEingangService zahlungsEingangService
    ) {
        double[] daten = new double[12];

        int jahr = java.time.Year.now().getValue();

        for (int i = 0; i < 12; i++) {
            java.time.YearMonth monat = java.time.YearMonth.of(jahr, i + 1);

            BigDecimal summe =
                    zahlungsEingangService.berechneBezahlteZahlungseingaengeImZeitraum(
                            monat.atDay(1),
                            monat.atEndOfMonth(),
                            null,null,null
                    );

            daten[i] = summe.doubleValue();
        }

        return daten;
    }

    private double[] berechneAusgabenChartDaten(
            AusgabeService ausgabeService
    ) {
        double[] daten = new double[12];

        int jahr = java.time.Year.now().getValue();

        for (int i = 0; i < 12; i++) {
            java.time.YearMonth monat = java.time.YearMonth.of(jahr, i + 1);

            BigDecimal summe =
                    ausgabeService.berechneOffeneAusgabenImZeitraum(
                            monat.atDay(1),
                            monat.atEndOfMonth()
                    );

            daten[i] = summe.doubleValue();
        }

        return daten;
    }

    private String[] berechneChartMonate() {
        String[] monate = new String[12];

        java.time.format.DateTimeFormatter formatter =
                java.time.format.DateTimeFormatter.ofPattern(
                        "MMM",
                        java.util.Locale.GERMANY
                );

        int jahr = java.time.Year.now().getValue();

        for (int i = 0; i < 12; i++) {
            java.time.YearMonth monat = java.time.YearMonth.of(jahr, i + 1);
            monate[i] = monat.format(formatter);
        }

        return monate;
    }

    private Component createChartSection() {

        Div section = new Div();

        section.setWidthFull();
        section.addClassName("page-section");

        Div barChart = chartCard(
                "Einnahmen vs. Ausgaben",
                createBarChart()
        );

        barChart.setWidthFull();

        HorizontalLayout lowerCharts = new HorizontalLayout();
        lowerCharts.setWidthFull();
        lowerCharts.setSpacing(true);
        lowerCharts.setAlignItems(FlexComponent.Alignment.STRETCH);

        Div pieChart = chartCard(
                "Vermietet vs Leerstand",
                createPieChart()
        );

        Div openItemsCard = createOpenItemsCard();

        pieChart.getStyle().set("flex", "2");
        openItemsCard.getStyle().set("flex", "1");

        pieChart.setWidthFull();
        openItemsCard.setWidthFull();

        lowerCharts.add(pieChart, openItemsCard);

        section.add(barChart, lowerCharts);

        return section;
    }

    private String ermittleMieterName(Zahlungseingang zahlung) {
        if (zahlung == null ||
                zahlung.getMietvertrag() == null ||
                zahlung.getMietvertrag().getMieter() == null) {
            return "Unbekannter Mieter";
        }

        String vorname = zahlung.getMietvertrag().getMieter().getVorname();
        String nachname = zahlung.getMietvertrag().getMieter().getNachname();

        if (vorname == null) {
            vorname = "";
        }

        if (nachname == null) {
            nachname = "";
        }

        String name = (vorname + " " + nachname).trim();

        if (name.isBlank()) {
            return "Unbekannter Mieter";
        }

        return name;
    }

    private Div createOpenItemsCard() {

        Div card = new Div();

        card.addClassName("card");
        card.setWidthFull();

        H3 title = new H3("Offene Posten");
        title.addClassName("card-title");

        card.add(title);

        if (offeneZahlungseingaenge.isEmpty()) {
            Div emptyState = new Div();
            emptyState.setText("Keine offenen Posten vorhanden.");
            emptyState.getStyle().set("font-size", "14px");
            emptyState.getStyle().set("color", "var(--lumo-secondary-text-color)");

            card.add(emptyState);
            return card;
        }

        offeneZahlungseingaenge.forEach(zahlung -> {
            String name = ermittleMieterName(zahlung);
            String beschreibung = zahlung.getBeschreibung();
            String betrag = formatEuro(zahlung.getBetrag());
            String status = "Ausstehend";

            card.add(openItem(
                    name,
                    beschreibung,
                    betrag,
                    status
            ));
        });

        return card;
    }

    private Div kpiCard(
            String title,
            String value,
            String badge,
            String subtitle,
            VaadinIcon icon,
            String color
    ) {

        Div card = new Div();

        card.addClassName("kpi-card");

        Div header = new Div();

        header.addClassName("kpi-card-header");

        Div iconBox = new Div(new Icon(icon));

        iconBox.addClassNames("kpi-icon-box", color);

        Span badgeSpan = new Span(badge);

        if (!badge.isBlank()) {

            badgeSpan.addClassNames(
                    "status-badge",
                    badge.startsWith("-")
                            ? "danger"
                            : "success"
            );
        }

        header.add(iconBox, badgeSpan);

        Paragraph titleText = new Paragraph(title);

        titleText.addClassName("kpi-title");

        H2 valueText = new H2(value);

        valueText.addClassName("kpi-value");

        Div subtitleText = new Div();

        subtitleText.setText(subtitle);

        subtitleText.addClassName("kpi-subtitle");

        card.add(
                header,
                titleText,
                valueText,
                subtitleText
        );

        return card;
    }

    private Div chartCard(String title, Component chart) {

        Div card = new Div();

        card.addClassName("card");

        Div header = new Div();

        header.addClassName("card-header");

        H3 titleText = new H3(title);

        titleText.addClassName("card-title");

        header.add(titleText);

        card.add(header, chart);

        return card;
    }

    // Bar chart
    private Component createBarChart() {

        Div wrapper = new Div();

        wrapper.setWidthFull();
        wrapper.getStyle().set("height", "420");

        Element canvas = new Element("canvas");
        canvas.setAttribute("id", "incomeExpenseChart");
        canvas.getStyle().set("width", "100%");
        canvas.getStyle().set("height", "420px");

        wrapper.getElement().appendChild(canvas);

        UI.getCurrent().getPage().executeJs("""
        setTimeout(() => {

            const ctx = document.getElementById('incomeExpenseChart');

            if (!ctx) return;

            new Chart(ctx, {
                type: 'bar',

                data: {
                    labels: $0,

                    datasets: [
                        {
                            label: 'Einnahmen',
                            data: $1,
                            borderRadius: 8
                        },
                        {
                            label: 'Ausgaben',
                            data: $2,
                            borderRadius: 8
                        }
                    ]
                },

                options: {
                    responsive: true,
                    maintainAspectRatio: false,

                    plugins: {
                        legend: {
                            position: 'bottom'
                        }
                    },

                    scales: {
                        y: {
                            beginAtZero: true
                        }
                    }
                }
            });

        }, 300);
    """, chartMonate, chartEinnahmen, chartAusgaben);

        return wrapper;
    }

    // Pie chart
    private Component createPieChart() {

        Div wrapper = new Div();

        wrapper.setWidthFull();

        wrapper.getStyle().set("height", "300px");

        Element canvas = new Element("canvas");

        canvas.setAttribute("id", "vacancyPieChart");

        canvas.getStyle().set("width", "100%");
        canvas.getStyle().set("height", "300px");

        wrapper.getElement().appendChild(canvas);

        UI.getCurrent().getPage().executeJs("""
            setTimeout(() => {

                const ctx =
                    document.getElementById(
                        'vacancyPieChart'
                    );

                if (!ctx) return;

                new Chart(ctx, {

                    type: 'doughnut',

                    data: {

                        labels: [
                            'Vermietet',
                            'Leerstand'
                        ],

                        datasets: [{
                            data: [$0, $1],
                            borderWidth: 0
                        }]
                    },

                    options: {

                        responsive: true,

                        maintainAspectRatio: false,

                        plugins: {

                            legend: {
                                position: 'bottom'
                            },

                            tooltip: {
                                callbacks: {
                                    label: function(context) {
                                        const label =
                                            context.label || '';

                                        const value =
                                            context.raw || 0;

                                        const total =
                                            context.dataset.data.reduce(
                                                (sum, current) => sum + current,
                                                0
                                            );

                                        const percentage =
                                            total === 0
                                                    ? 0
                                                    : (value / total * 100)
                                                        .toFixed(2);

                                        return label
                                                + ': '
                                                + value
                                                + ' Mieteinheiten ('
                                                + percentage
                                                + ' %)';
                                    }
                                }
                            }
                        },

                        cutout: '70%'
                    }
                });

            }, 300);
        """, vermieteteMieteinheiten, leerstehendeMieteinheiten);

        return wrapper;
    }

    private Component openItem(
            String name,
            String description,
            String amount,
            String overdue
    ) {

        HorizontalLayout row = new HorizontalLayout();

        row.setWidthFull();

        row.setJustifyContentMode(
                FlexComponent.JustifyContentMode.BETWEEN
        );

        row.setAlignItems(FlexComponent.Alignment.CENTER);

        row.getStyle().set("padding", "16px 0");

        Div left = new Div();

        left.add(
                new Span(name),
                new Div(description)
        );

        left.getElement()
                .getStyle()
                .set("font-size", "14px");

        Div right = new Div();

        right.add(
                new Span(amount),
                new Div(overdue)
        );

        right.getStyle().set("text-align", "right");

        right.getStyle().set(
                "color",
                "var(--color-danger)"
        );

        right.getElement()
                .getStyle()
                .set("font-size", "14px");

        row.add(left, right);

        return row;
    }

    private Button primaryButton(
            String text,
            VaadinIcon icon
    ) {

        Button button = new Button(
                text,
                new Icon(icon)
        );

        button.addClassName("primary-button");

        return button;
    }

    private Button secondaryButton(
            String text,
            VaadinIcon icon
    ) {

        Button button = new Button(
                text,
                new Icon(icon)
        );

        button.addClassName("secondary-button");

        return button;
    }

    @Override
    public String getPageTitle() {
        return "Dashboard";
    }

    @Override
    public String getPageSubtitle() {
        return "Zentrale Übersicht und KPIs";
    }
}
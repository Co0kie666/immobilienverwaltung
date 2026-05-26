package de.hsbi.immobilienverwaltung.ui.finanzen;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.Route;
import de.hsbi.immobilienverwaltung.ui.layout.HasPageHeader;
import de.hsbi.immobilienverwaltung.ui.layout.MainLayout;

import java.util.List;

@Route(value = "finanzen", layout = MainLayout.class)
public class FinanzDashboardView extends Div implements HasPageHeader {

    public FinanzDashboardView() {
        UI.getCurrent().getPage().addJavaScript(
                "https://cdn.jsdelivr.net/npm/chart.js"
        );

        addClassName("finance-page");

        add(createFilterBar());
        add(createKpiGrid());
        add(createDashboardGrid());
        add(createTableGrid());
    }

    private Component createFilterBar() {
        Div filterBar = new Div();
        filterBar.addClassName("finance-filter-bar");

        Div left = new Div();
        left.addClassName("finance-filter-left");

        Button oneMonth = new Button("1M");
        Button threeMonths = new Button("3M");
        Button sixMonths = new Button("6M");
        Button ytd = new Button("YTD");

        List<Button> filterButtons = List.of(
                oneMonth,
                threeMonths,
                sixMonths,
                ytd
        );

        filterButtons.forEach(btn ->
                btn.addClassName("secondary-button")
        );

        oneMonth.addClassName("finance-filter-active");

        filterButtons.forEach(button -> {
            button.addClickListener(e -> {
                filterButtons.forEach(btn ->
                        btn.removeClassName("finance-filter-active")
                );

                button.addClassName("finance-filter-active");
            });
        });

        Button allProperties = new Button(
                "Alle Immobilien",
                new Icon(VaadinIcon.CHEVRON_DOWN)
        );
        allProperties.addClassName("secondary-button");

        Button allUnits = new Button(
                "Alle Einheiten",
                new Icon(VaadinIcon.CHEVRON_DOWN)
        );
        allUnits.addClassName("secondary-button");

        Button allTenants = new Button(
                "Alle Mieter",
                new Icon(VaadinIcon.CHEVRON_DOWN)
        );
        allTenants.addClassName("secondary-button");

        left.add(
                oneMonth,
                threeMonths,
                sixMonths,
                ytd,
                allProperties,
                allUnits,
                allTenants
        );

        Button addBooking = new Button("Buchung anlegen", new Icon(VaadinIcon.PLUS));
        Button showBookings = new Button("Alle Buchungen anzeigen", new Icon(VaadinIcon.PLUS));

        addBooking.addClickListener(e ->
                UI.getCurrent().navigate("finanzen/buchung-neu")
        );
        showBookings.addClickListener(e ->
                UI.getCurrent().navigate("finanzen/buchungen")
        );

        addBooking.addClassName("primary-button");
        showBookings.addClassName("primary-button");

        HorizontalLayout bookingButtons = new HorizontalLayout(addBooking, showBookings);
        bookingButtons.setSpacing(true);

        filterBar.setWidthFull();

        filterBar.add(left, bookingButtons);

        return filterBar;
    }

    private Component createKpiGrid() {
        Div grid = new Div();
        grid.addClassName("finance-kpi-grid");

        grid.add(
                kpiCard("Summe Einnahmen", "€ 124.500,00", "↑ 12.5%  vs. Vormonat", VaadinIcon.TRENDING_UP, "success"),
                kpiCard("Summe Ausgaben", "€ 42.850,00", "↑ 4.2%  vs. Vormonat", VaadinIcon.TRENDING_DOWN, "danger"),
                kpiCard("Rückstände", "€ 5.240,00", "↓ 2.1%  vs. Vormonat", VaadinIcon.REFRESH, "warning"),
                kpiCard("Cashflow", "€ 81.650,00", "↑ 18.4%  vs. Vormonat", VaadinIcon.WALLET, "primary")
        );

        return grid;
    }

    private Component kpiCard(String title, String value, String trend, VaadinIcon icon, String color) {
        Div card = new Div();
        card.addClassNames("finance-kpi-card", color);

        Div top = new Div();
        top.addClassName("finance-kpi-top");

        Div text = new Div();

        Span titleSpan = new Span(title);
        titleSpan.addClassName("finance-kpi-title");

        H2 valueText = new H2(value);
        valueText.addClassName("finance-kpi-value");

        Span trendText = new Span(trend);
        trendText.addClassNames("finance-kpi-trend", color);

        text.add(titleSpan, valueText, trendText);

        Div iconBox = new Div(new Icon(icon));
        iconBox.addClassNames("finance-kpi-icon", color);

        top.add(text, iconBox);
        card.add(top);

        return card;
    }

    private Component createDashboardGrid() {
        Div grid = new Div();
        grid.addClassName("finance-dashboard-grid");

        Div chartCard = new Div();
        chartCard.addClassName("card");

        H3 chartTitle = new H3("Einnahmen vs. Ausgaben");
        chartTitle.addClassName("card-title");

        Paragraph subtitle = new Paragraph("Monatlicher Verlauf");
        subtitle.addClassName("card-subtitle");

        chartCard.add(chartTitle, subtitle, createLineChart());

        Div side = new Div();
        side.addClassName("finance-side-column");

        side.add(createPaymentStatusCard(), createCostDistributionCard());

        grid.add(chartCard, side);
        return grid;
    }

    private Component createLineChart() {
        Div wrapper = new Div();
        wrapper.setWidthFull();
        wrapper.getStyle().set("height", "320px");
        wrapper.getStyle().set("position", "relative");

        Element canvas = new Element("canvas");
        canvas.setAttribute("id", "financeLineChart");

        canvas.getStyle().set("width", "100%");
        canvas.getStyle().set("height", "100%");

        wrapper.getElement().appendChild(canvas);

        UI.getCurrent().getPage().executeJs("""
            setTimeout(() => {
                const ctx = document.getElementById('financeLineChart');

                if (!ctx) return;

                new Chart(ctx, {
                    type: 'line',
                    data: {
                        labels: [
                            'Jan', 'Feb', 'Mär', 'Apr', 'Mai', 'Jun',
                            'Jul', 'Aug', 'Sep', 'Okt', 'Nov', 'Dez'
                        ],
                        datasets: [
                            {
                                label: 'Einnahmen',
                                data: [
                                    102000, 108000, 112000, 118000,
                                    121000, 124000, 127000, 129000,
                                    132000, 135000, 138000, 142000
                                ],
                                tension: 0.4,
                                fill: false
                            },
                            {
                                label: 'Ausgaben',
                                data: [
                                    42000, 44000, 43000, 47000,
                                    46000, 45000, 49000, 52000,
                                    51000, 53000, 54000, 56000
                                ],
                                tension: 0.4,
                                fill: false
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
        """);

        return wrapper;
    }

    private Component createPaymentStatusCard() {
        Div card = new Div();
        card.addClassName("card");

        Div header = new Div();
        header.addClassName("finance-card-header");

        H3 title = new H3("Zahlungsstatus");
        title.addClassName("card-title");

        header.add(title);

        Div chartWrapper = new Div();
        chartWrapper.getStyle().set("height", "220px");

        Element canvas = new Element("canvas");
        canvas.setAttribute("id", "paymentStatusChart");
        canvas.getStyle().set("width", "100%");
        canvas.getStyle().set("height", "220px");

        chartWrapper.getElement().appendChild(canvas);

        UI.getCurrent().getPage().executeJs("""
            setTimeout(() => {
                const ctx = document.getElementById('paymentStatusChart');

                if (!ctx) return;

                new Chart(ctx, {
                    type: 'pie',
                    data: {
                        labels: [
                            'Bezahlt',
                            'Offen'
                        ],
                        datasets: [{
                            data: [75, 25],
                            borderWidth: 0
                        }]
                    },
                    options: {
                        responsive: true,
                        maintainAspectRatio: false,
                        plugins: {
                            legend: {
                                position: 'bottom'
                            }
                        }
                    }
                });
            }, 300);
        """);

        Div stats = new Div();
        stats.addClassName("finance-mini-grid");

        stats.add(miniBox("Bezahlt", "75 %"));
        stats.add(miniBox("Offen", "25 %"));

        card.add(header, chartWrapper, stats);
        return card;
    }

    private Component createCostDistributionCard() {
        Div card = new Div();
        card.addClassName("card");

        H3 title = new H3("Kostenverteilung");
        title.addClassName("card-title");

        Div wrapper = new Div();
        wrapper.getStyle().set("height", "260px");

        Element canvas = new Element("canvas");
        canvas.setAttribute("id", "costDistributionChart");
        canvas.getStyle().set("width", "100%");
        canvas.getStyle().set("height", "260px");

        wrapper.getElement().appendChild(canvas);

        UI.getCurrent().getPage().executeJs("""
            setTimeout(() => {
                const ctx = document.getElementById('costDistributionChart');

                if (!ctx) return;

                new Chart(ctx, {
                    type: 'doughnut',
                    data: {
                        labels: [
                            'Instandhaltung',
                            'Nebenkosten',
                            'Verwaltung'
                        ],
                        datasets: [{
                            data: [43, 30, 27],
                            borderWidth: 0
                        }]
                    },
                    options: {
                        responsive: true,
                        maintainAspectRatio: false,
                        plugins: {
                            legend: {
                                position: 'bottom'
                            }
                        },
                        cutout: '68%'
                    }
                });
            }, 300);
        """);

        card.add(title, wrapper);
        return card;
    }

    private Component miniBox(String label, String value) {
        Div box = new Div();
        box.addClassName("finance-mini-box");

        Span labelSpan = new Span(label);
        labelSpan.addClassName("finance-mini-label");

        Span valueStrong = new Span(value);

        box.add(labelSpan, valueStrong);
        return box;
    }

    private Component createTableGrid() {
        Div grid = new Div();
        grid.addClassName("finance-table-grid");

        grid.add(
                transactionTable("Letzte Einnahmen", "Mieten & Nebenkosten",
                        new String[][]{
                                {"01.10.2023", "Max Mustermann", "Miete", "Bezahlt", "+ € 1.250,00"},
                                {"01.10.2023", "Anna Schmidt", "Miete", "Offen", "+ € 980,00"},
                                {"28.09.2023", "Thomas Weber", "Nebenkosten", "Bezahlt", "+ € 250,00"}
                        }),
                transactionTable("Letzte Ausgaben", "Instandhaltung & Verwaltung",
                        new String[][]{
                                {"02.10.2023", "Handwerker GmbH", "Reparatur", "Bezahlt", "- € 450,00"},
                                {"01.10.2023", "Stadtwerke", "Strom/Wasser", "Offen", "- € 1.200,00"},
                                {"25.09.2023", "Hausverwaltung Meyer", "Verwaltung", "Bezahlt", "- € 850,00"}
                        })
        );

        return grid;
    }

    private Component transactionTable(String title, String subtitle, String[][] rows) {
        Div card = new Div();
        card.addClassName("table-card");

        Div titleBox = new Div();

        H3 titleText = new H3(title);
        titleText.addClassName("card-title");

        Paragraph subtitleText = new Paragraph(subtitle);
        subtitleText.addClassName("card-subtitle");

        titleBox.add(titleText, subtitleText);

        Div table = new Div();
        table.addClassName("finance-table");

        table.add(tableHeader());

        for (String[] row : rows) {
            table.add(tableRow(row));
        }

        card.add(titleBox, table);
        return card;
    }

    private Component tableHeader() {
        Div row = new Div();
        row.addClassNames("finance-table-row", "finance-table-head");

        row.add(
                new Span("Datum"),
                new Span("Mieter / Objekt"),
                new Span("Kategorie"),
                new Span("Status"),
                new Span("Betrag")
        );

        return row;
    }

    private Component tableRow(String[] data) {
        Div row = new Div();
        row.addClassName("finance-table-row");

        Span status = new Span(data[3]);
        status.addClassNames(
                "status-badge",
                data[3].equals("Bezahlt") ? "success" : "warning"
        );

        row.add(
                new Span(data[0]),
                new Span(data[1]),
                new Span(data[2]),
                status,
                new Span(data[4])
        );

        return row;
    }

    @Override
    public String getPageTitle() {
        return "Finanz-Dashboard";
    }

    @Override
    public String getPageSubtitle() {
        return "Übersicht über Einnahmen, Ausgaben und Cashflow";
    }
}
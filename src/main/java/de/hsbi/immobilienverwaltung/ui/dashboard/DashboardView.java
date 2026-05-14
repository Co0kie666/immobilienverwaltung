package de.hsbi.immobilienverwaltung.ui.dashboard;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.router.Route;
import de.hsbi.immobilienverwaltung.ui.layout.HasPageHeader;
import de.hsbi.immobilienverwaltung.ui.layout.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;

@Route(value = "dashboard", layout = MainLayout.class)
public class DashboardView extends Div implements HasPageHeader {

    public DashboardView() {
        addClassName("page-content");

        add(createActionBar());
        add(createKpiSection());
        add(createChartSection());
        add(createOpenItemsCard());
    }

    private Component createActionBar() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.addClassName("page-section");

        HorizontalLayout filters = new HorizontalLayout(
                secondaryButton("Dieser Monat", VaadinIcon.CALENDAR),
                secondaryButton("Alle Objekte", VaadinIcon.BUILDING)
        );

        HorizontalLayout actions = new HorizontalLayout(
                primaryButton("Neue Immobilie", VaadinIcon.PLUS),
                secondaryButton("Neuer Mieter", VaadinIcon.USER),
                secondaryButton("Neue Zahlung", VaadinIcon.EURO)
        );

        layout.add(filters, actions);
        return layout;
    }

    private Component createKpiSection() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.addClassName("page-section");

        layout.add(
                kpiCard("Gesamteinnahmen", "€124,500", "+12%", "Einnahmen diesen Monat", VaadinIcon.LINE_CHART, "primary"),
                kpiCard("Leerstandsquote", "4.2%", "-2.4%", "3 Einheiten leerstehend", VaadinIcon.HOME, "danger"),
                kpiCard("Offene Zahlungen", "€4,250", "", "5 ausstehende Mieten", VaadinIcon.WARNING, "warning"),
                kpiCard("Aktive Verträge", "142", "+4", "Laufende Mietverträge", VaadinIcon.USERS, "success")
        );

        layout.getChildren().forEach(component -> component.getElement().getStyle().set("flex", "1"));
        return layout;
    }

    private Component createChartSection() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.addClassName("page-section");

        Div barChart = chartCard("Einnahmen vs. Ausgaben", createBarChart());
        Div pieChart = chartCard("Leerstand Übersicht", createPieChart());

        barChart.getStyle().set("flex", "1");
        pieChart.getStyle().set("flex", "1");

        layout.add(barChart, pieChart);
        return layout;
    }

    private Div createOpenItemsCard() {
        Div card = new Div();
        card.addClassName("card");
        card.setWidth("420px");

        H3 title = new H3("Offene Posten");
        title.addClassName("card-title");

        card.add(title);
        card.add(openItem("Max Mustermann", "Miete Mai 2024", "€850.00", "5 Tage überfällig"));
        card.add(openItem("Julia Schmidt", "Nebenkosten 2023", "€120.50", "12 Tage überfällig"));

        return card;
    }

    private Div kpiCard(String title, String value, String badge, String subtitle,
                        VaadinIcon icon, String color) {
        Div card = new Div();
        card.addClassName("kpi-card");

        Div header = new Div();
        header.addClassName("kpi-card-header");

        Div iconBox = new Div(new Icon(icon));
        iconBox.addClassNames("kpi-icon-box", color);

        Span badgeSpan = new Span(badge);
        if (!badge.isBlank()) {
            badgeSpan.addClassNames("status-badge", badge.startsWith("-") ? "danger" : "success");
        }

        header.add(iconBox, badgeSpan);

        Paragraph titleText = new Paragraph(title);
        titleText.addClassName("kpi-title");

        H2 valueText = new H2(value);
        valueText.addClassName("kpi-value");

        Div subtitleText = new Div();
        subtitleText.setText(subtitle);
        subtitleText.addClassName("kpi-subtitle");

        card.add(header, titleText, valueText, subtitleText);
        return card;
    }

    private Div chartCard(String title, Component chart) {
        Div card = new Div();
        card.addClassName("card");

        Div header = new Div();
        header.addClassName("card-header");

        H3 titleText = new H3(title);
        titleText.addClassName("card-title");

        Button menuButton = new Button(new Icon(VaadinIcon.ELLIPSIS_DOTS_H));
        menuButton.addClassName("icon-button");

        header.add(titleText, menuButton);

        card.add(header, chart);
        return card;
    }

    private Component createBarChart() {
        Div chart = new Div();
        chart.addClassName("dashboard-bar-chart");

        String[] months = {"Jan", "Feb", "Mär", "Apr", "Mai", "Jun"};
        int[] income = {110, 115, 112, 118, 125, 120};
        int[] expenses = {45, 48, 42, 50, 47, 46};

        for (int i = 0; i < months.length; i++) {
            Div group = new Div();
            group.addClassName("bar-group");

            Div incomeBar = new Div();
            incomeBar.addClassNames("bar", "income");
            incomeBar.getStyle().set("height", income[i] + "px");

            Div expenseBar = new Div();
            expenseBar.addClassNames("bar", "expense");
            expenseBar.getStyle().set("height", expenses[i] + "px");

            Span label = new Span(months[i]);

            group.add(incomeBar, expenseBar, label);
            chart.add(group);
        }

        return chart;
    }

    private Component createPieChart() {
        Div wrapper = new Div();
        wrapper.addClassName("dashboard-pie-wrapper");

        Div pie = new Div();
        pie.addClassName("dashboard-pie-chart");

        Div legend = new Div();
        legend.addClassName("dashboard-pie-legend");

        legend.add(legendItem("Vermietet", "95.8%"));
        legend.add(legendItem("Leerstand", "4.2%"));

        wrapper.add(pie, legend);
        return wrapper;
    }

    private Component legendItem(String label, String value) {
        Div item = new Div();
        item.addClassName("legend-item");

        Span text = new Span(label);
        Span number = new Span(value);
        number.getStyle().set("font-weight", "800");

        item.add(text, number);
        return item;
    }

    private Component openItem(String name, String description, String amount, String overdue) {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.getStyle().set("padding", "16px 0");

        Div left = new Div();
        left.add(new Span(name), new Div(description));
        left.getElement().getStyle().set("font-size", "14px");

        Div right = new Div();
        right.add(new Span(amount), new Div(overdue));
        right.getStyle().set("text-align", "right");
        right.getStyle().set("color", "var(--color-danger)");
        right.getElement().getStyle().set("font-size", "14px");

        row.add(left, right);
        return row;
    }

    private Button primaryButton(String text, VaadinIcon icon) {
        Button button = new Button(text, new Icon(icon));
        button.addClassName("primary-button");
        return button;
    }

    private Button secondaryButton(String text, VaadinIcon icon) {
        Button button = new Button(text, new Icon(icon));
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
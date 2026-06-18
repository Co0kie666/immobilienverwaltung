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
import de.hsbi.immobilienverwaltung.service.interfaces.AusgabeService;
import de.hsbi.immobilienverwaltung.service.interfaces.GesamtAuswertungService;
import de.hsbi.immobilienverwaltung.service.interfaces.ZahlungsEingangService;
import de.hsbi.immobilienverwaltung.ui.layout.HasPageHeader;
import de.hsbi.immobilienverwaltung.ui.layout.MainLayout;
import jakarta.annotation.security.PermitAll;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

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

    private final double[] chartEinnahmen;
    private final double[] chartAusgaben;
    private final String[] chartMonate;

    private final List<Zahlungseingang> offeneZahlungseingaenge;

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

        this.offeneZahlungseingaenge =
                zahlungsEingangService.findeOffeneZahlungseingaenge();

        this.chartEinnahmen =
                berechneEinnahmenChartDaten(zahlungsEingangService);

        this.chartAusgaben =
                berechneAusgabenChartDaten(ausgabeService);

        this.chartMonate =
                berechneChartMonate();

        addJavaScriptIfUiAvailable(
                "https://cdn.jsdelivr.net/npm/chart.js"
        );

        addClassNames("page-content", "dashboard-page");

        add(createHeroSection());
        add(createKpiSection());
        add(createDashboardMainGrid());
    }

    private void addJavaScriptIfUiAvailable(String url) {
        UI ui = UI.getCurrent();

        if (ui == null || ui.getSession() == null) {
            return;
        }

        ui.getPage().addJavaScript(url);
    }

    private void executeJsIfUiAvailable(String script, Object... arguments) {
        UI ui = UI.getCurrent();

        if (ui == null || ui.getSession() == null) {
            return;
        }

        ui.getPage().executeJs(script, arguments);
    }

    private Component createHeroSection() {
        Div hero = new Div();
        hero.addClassName("dashboard-hero");

        Div content = new Div();
        content.addClassName("dashboard-hero-content");

        Span eyebrow = new Span("Portfolio Control Center");
        eyebrow.addClassName("dashboard-eyebrow");

        H2 title = new H2("Alles Wichtige auf einen Blick");
        title.addClassName("dashboard-hero-title");

        Paragraph subtitle = new Paragraph(
                "Verfolge Einnahmen, Leerstand und offene Posten in einem modernen Überblick."
        );
        subtitle.addClassName("dashboard-hero-subtitle");

        Div actions = new Div();
        actions.addClassName("dashboard-hero-actions");

        Button neueImmobilie = primaryButton("Neue Immobilie", VaadinIcon.PLUS);
        neueImmobilie.addClickListener(e ->
                getUI().ifPresent(ui -> ui.navigate("immobilien/neu"))
        );

        Button neuerMieter = secondaryButton("Neuer Mieter", VaadinIcon.USER);
        neuerMieter.addClickListener(e ->
                getUI().ifPresent(ui -> ui.navigate("mieter-anlegen"))
        );

        Button neueZahlung = secondaryButton("Neue Zahlung", VaadinIcon.EURO);
        neueZahlung.addClickListener(e ->
                getUI().ifPresent(ui -> ui.navigate("finanzen/buchung-neu"))
        );

        actions.add(neueImmobilie, neuerMieter, neueZahlung);
        content.add(eyebrow, title, subtitle, actions);

        Div visual = new Div();
        visual.addClassName("dashboard-hero-visual");

        Div illustration = new Div();
        illustration.addClassName("dashboard-building-illustration");

        Div stats = new Div();
        stats.addClassName("dashboard-hero-stats");
        stats.add(
                heroStat("Vermietet", String.valueOf(vermieteteMieteinheiten)),
                heroStat("Leerstand", String.valueOf(leerstehendeMieteinheiten)),
                heroStat("Verträge", String.valueOf(aktiveVertraege))
        );

        visual.add(illustration, stats);
        hero.add(content, visual);

        return hero;
    }

    private Component heroStat(String label, String value) {
        Div stat = new Div();
        stat.addClassName("dashboard-hero-stat");

        Span valueText = new Span(value);
        valueText.addClassName("dashboard-hero-stat-value");

        Span labelText = new Span(label);
        labelText.addClassName("dashboard-hero-stat-label");

        stat.add(valueText, labelText);
        return stat;
    }

    private Component createKpiSection() {
        Div grid = new Div();
        grid.addClassName("dashboard-kpi-grid");

        grid.add(
                kpiCard(
                        "Gesamteinnahmen",
                        formatEuro(gesamteinnahmen),
                        "Portfolio",
                        "Summe aller Zahlungseingänge",
                        VaadinIcon.LINE_CHART,
                        "primary"
                ),
                kpiCard(
                        "Leerstandsquote",
                        String.format(Locale.GERMANY, "%.2f %%", leerstandsquote),
                        leerstehendeMieteinheiten + " frei",
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
                        anzahlOffeneAusgaben + " offen",
                        formatAnzahlOffeneAusgaben(anzahlOffeneAusgaben),
                        VaadinIcon.WARNING,
                        "warning"
                ),
                kpiCard(
                        "Aktive Verträge",
                        String.valueOf(aktiveVertraege),
                        "laufend",
                        "Laufende Mietverträge",
                        VaadinIcon.USERS,
                        "success"
                )
        );

        return grid;
    }

    private Component createDashboardMainGrid() {
        Div grid = new Div();
        grid.addClassName("dashboard-main-grid");

        Div chartColumn = new Div();
        chartColumn.addClassName("dashboard-chart-column");

        chartColumn.add(
                chartCard(
                        "Einnahmen vs. Ausgaben",
                        "Entwicklung der letzten sechs Monate",
                        createBarChart(),
                        "wide"
                ),
                chartCard(
                        "Vermietet vs. Leerstand",
                        "Aktuelle Verteilung der Mieteinheiten",
                        createPieChart(),
                        "compact"
                )
        );

        grid.add(chartColumn, createOpenItemsCard());

        return grid;
    }

    private Div createOpenItemsCard() {
        Div card = new Div();
        card.addClassNames("card", "dashboard-open-card");

        Div header = new Div();
        header.addClassName("dashboard-card-header");

        Div titleBox = new Div();

        H3 title = new H3("Offene Posten");
        title.addClassName("card-title");

        Paragraph subtitle = new Paragraph("Priorisierte Zahlungen und Fälligkeiten");
        subtitle.addClassName("card-subtitle");

        titleBox.add(title, subtitle);

        Span counter = new Span(String.valueOf(offeneZahlungseingaenge == null ? 0 : offeneZahlungseingaenge.size()));
        counter.addClassNames("status-badge", anzahlOffeneAusgaben == 0 ? "success" : "warning");

        header.add(titleBox, counter);
        card.add(header);

        if (offeneZahlungseingaenge == null || offeneZahlungseingaenge.isEmpty()) {
            Div empty = new Div();
            empty.addClassName("dashboard-empty-state");

            Div icon = new Div(new Icon(VaadinIcon.CHECK_CIRCLE));
            icon.addClassName("dashboard-empty-icon");

            H3 emptyTitle = new H3("Alles erledigt");
            Paragraph emptyText = new Paragraph("Aktuell sind keine offenen Posten vorhanden.");

            empty.add(icon, emptyTitle, emptyText);
            card.add(empty);

            return card;
        }

        Div list = new Div();
        list.addClassName("dashboard-open-list");

        offeneZahlungseingaenge.stream()
                .filter(zahlung -> zahlung.getZahlungsdatum() != null)
                .sorted((z1, z2) -> z1.getZahlungsdatum().compareTo(z2.getZahlungsdatum()))
                .limit(5)
                .forEach(zahlung -> list.add(openItem(
                        ermittleMieterName(zahlung),
                        ermittleBeschreibung(zahlung),
                        formatEuro(zahlung.getBetrag()),
                        ermittleUeberfaelligkeit(zahlung)
                )));

        card.add(list);

        return card;
    }

    private String ermittleMieterName(Zahlungseingang zahlung) {
        if (zahlung.getMietvertrag() == null ||
                zahlung.getMietvertrag().getMieter() == null) {
            return "-";
        }

        String vorname = zahlung.getMietvertrag()
                .getMieter()
                .getVorname();

        String nachname = zahlung.getMietvertrag()
                .getMieter()
                .getNachname();

        if (vorname == null) {
            vorname = "";
        }

        if (nachname == null) {
            nachname = "";
        }

        String name = (vorname + " " + nachname).trim();

        return name.isBlank() ? "-" : name;
    }

    private String ermittleBeschreibung(Zahlungseingang zahlung) {
        if (zahlung.getBeschreibung() != null &&
                !zahlung.getBeschreibung().isBlank()) {
            return zahlung.getBeschreibung();
        }

        if (zahlung.getTyp() != null) {
            return formatiereZahlungseingangTyp(
                    zahlung.getTyp().toString()
            );
        }

        return "Offene Zahlung";
    }

    private String ermittleUeberfaelligkeit(Zahlungseingang zahlung) {
        if (zahlung.getZahlungsdatum() == null) {
            return "-";
        }

        long tage = ChronoUnit.DAYS.between(
                zahlung.getZahlungsdatum(),
                LocalDate.now()
        );

        if (tage < 0) {
            return "Fällig in " + Math.abs(tage) + " Tagen";
        }

        if (tage == 0) {
            return "Heute fällig";
        }

        if (tage == 1) {
            return "1 Tag überfällig";
        }

        return tage + " Tage überfällig";
    }

    private String formatiereZahlungseingangTyp(String typ) {
        return switch (typ) {
            case "KALTMIETE" -> "Kaltmiete";
            case "NEBENKOSTEN" -> "Nebenkosten";
            case "KAUTION" -> "Kaution";
            default -> typ;
        };
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
        card.addClassNames("kpi-card", "dashboard-kpi-card", color);

        Div header = new Div();
        header.addClassName("kpi-card-header");

        Div iconBox = new Div(new Icon(icon));
        iconBox.addClassNames("kpi-icon-box", color);

        Span badgeSpan = new Span(badge);

        if (!badge.isBlank()) {
            badgeSpan.addClassNames("status-badge", color);
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

    private Div chartCard(String title, String subtitle, Component chart, String size) {
        Div card = new Div();
        card.addClassNames("card", "dashboard-chart-card", size);

        Div header = new Div();
        header.addClassName("dashboard-card-header");

        Div titleBox = new Div();

        H3 titleText = new H3(title);
        titleText.addClassName("card-title");

        Paragraph subtitleText = new Paragraph(subtitle);
        subtitleText.addClassName("card-subtitle");

        titleBox.add(titleText, subtitleText);
        header.add(titleBox);

        card.add(header, chart);

        return card;
    }

    private Component createBarChart() {
        Div wrapper = new Div();
        wrapper.addClassName("dashboard-chart-wrapper");

        Element canvas = new Element("canvas");
        canvas.setAttribute("id", "incomeExpenseChart");

        wrapper.getElement().appendChild(canvas);

        executeJsIfUiAvailable("""
            setTimeout(() => {
                const ctx = document.getElementById('incomeExpenseChart');

                if (!ctx || !window.Chart) return;

                const styles = getComputedStyle(document.documentElement);
                const primary = styles.getPropertyValue('--color-primary').trim() || '#2563eb';
                const danger = styles.getPropertyValue('--color-danger').trim() || '#ef4444';
                const grid = styles.getPropertyValue('--color-border').trim() || '#e5e7eb';

                if (window.incomeExpenseChartInstance) {
                    window.incomeExpenseChartInstance.destroy();
                }

                window.incomeExpenseChartInstance = new Chart(ctx, {
                    type: 'bar',
                    data: {
                        labels: $0,
                        datasets: [
                            {
                                label: 'Einnahmen',
                                data: $1,
                                backgroundColor: primary,
                                borderRadius: 12,
                                maxBarThickness: 34
                            },
                            {
                                label: 'Ausgaben',
                                data: $2,
                                backgroundColor: danger,
                                borderRadius: 12,
                                maxBarThickness: 34
                            }
                        ]
                    },
                    options: {
                        responsive: true,
                        maintainAspectRatio: false,
                        plugins: {
                            legend: {
                                position: 'bottom',
                                labels: {
                                    usePointStyle: true,
                                    boxWidth: 8,
                                    boxHeight: 8
                                }
                            }
                        },
                        scales: {
                            x: {
                                grid: {
                                    display: false
                                }
                            },
                            y: {
                                beginAtZero: true,
                                grid: {
                                    color: grid
                                }
                            }
                        }
                    }
                });
            }, 300);
        """, chartMonate, chartEinnahmen, chartAusgaben);

        return wrapper;
    }

    private Component createPieChart() {
        Div wrapper = new Div();
        wrapper.addClassName("dashboard-chart-wrapper");

        Element canvas = new Element("canvas");
        canvas.setAttribute("id", "vacancyPieChart");

        wrapper.getElement().appendChild(canvas);

        executeJsIfUiAvailable("""
            setTimeout(() => {
                const ctx = document.getElementById('vacancyPieChart');

                if (!ctx || !window.Chart) return;

                const styles = getComputedStyle(document.documentElement);
                const success = styles.getPropertyValue('--color-success').trim() || '#10b981';
                const warning = styles.getPropertyValue('--color-warning').trim() || '#f59e0b';

                if (window.vacancyPieChartInstance) {
                    window.vacancyPieChartInstance.destroy();
                }

                window.vacancyPieChartInstance = new Chart(ctx, {
                    type: 'doughnut',
                    data: {
                        labels: [
                            'Vermietet',
                            'Leerstand'
                        ],
                        datasets: [{
                            data: [$0, $1],
                            backgroundColor: [success, warning],
                            borderWidth: 0,
                            hoverOffset: 8
                        }]
                    },
                    options: {
                        responsive: true,
                        maintainAspectRatio: false,
                        plugins: {
                            legend: {
                                position: 'bottom',
                                labels: {
                                    usePointStyle: true,
                                    boxWidth: 8,
                                    boxHeight: 8
                                }
                            },
                            tooltip: {
                                callbacks: {
                                    label: function(context) {
                                        const label = context.label || '';
                                        const value = context.raw || 0;

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
                        cutout: '72%'
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
        Div row = new Div();
        row.addClassName("dashboard-open-item");

        Div iconBox = new Div(new Icon(VaadinIcon.CLOCK));
        iconBox.addClassName("dashboard-open-item-icon");

        Div left = new Div();
        left.addClassName("dashboard-open-item-content");

        Span nameText = new Span(name);
        nameText.addClassName("dashboard-open-item-title");

        Span descriptionText = new Span(description);
        descriptionText.addClassName("dashboard-open-item-subtitle");

        left.add(nameText, descriptionText);

        Div right = new Div();
        right.addClassName("dashboard-open-item-amount");

        Span amountText = new Span(amount);
        amountText.addClassName("dashboard-open-item-value");

        Span overdueText = new Span(overdue);
        overdueText.addClassName("dashboard-open-item-meta");

        right.add(amountText, overdueText);

        row.add(iconBox, left, right);

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

    private double[] berechneEinnahmenChartDaten(
            ZahlungsEingangService zahlungsEingangService
    ) {
        double[] daten = new double[6];

        YearMonth aktuellerMonat = YearMonth.now();

        for (int i = 0; i < 6; i++) {
            YearMonth monat = aktuellerMonat.minusMonths(5 - i);

            BigDecimal summe =
                    zahlungsEingangService.berechneBezahlteZahlungseingaengeImZeitraum(
                            monat.atDay(1),
                            monat.atEndOfMonth(),
                            null,
                            null,
                            null
                    );

            if (summe == null) {
                summe = BigDecimal.ZERO;
            }

            daten[i] = summe.doubleValue();
        }

        return daten;
    }

    private double[] berechneAusgabenChartDaten(
            AusgabeService ausgabeService
    ) {
        double[] daten = new double[6];

        YearMonth aktuellerMonat = YearMonth.now();

        for (int i = 0; i < 6; i++) {
            YearMonth monat = aktuellerMonat.minusMonths(5 - i);

            BigDecimal summe =
                    ausgabeService.berechneAusgabenImZeitraum(
                            monat.atDay(1),
                            monat.atEndOfMonth()
                    );

            if (summe == null) {
                summe = BigDecimal.ZERO;
            }

            daten[i] = summe.doubleValue();
        }

        return daten;
    }

    private String[] berechneChartMonate() {
        String[] monate = new String[6];

        YearMonth aktuellerMonat = YearMonth.now();

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern(
                        "MMM",
                        Locale.GERMANY
                );

        for (int i = 0; i < 6; i++) {
            YearMonth monat = aktuellerMonat.minusMonths(5 - i);
            monate[i] = monat.format(formatter);
        }

        return monate;
    }

    private String formatEuro(BigDecimal betrag) {
        if (betrag == null) {
            betrag = BigDecimal.ZERO;
        }

        return NumberFormat
                .getCurrencyInstance(Locale.GERMANY)
                .format(betrag);
    }

    private String formatAnzahlOffeneAusgaben(long anzahl) {
        if (anzahl == 1) {
            return "1 ausstehende Ausgabe";
        }

        return anzahl + " ausstehende Ausgaben";
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
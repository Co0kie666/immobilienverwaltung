package de.hsbi.immobilienverwaltung.ui.dashboard;

// Vaadin-Komponenten für UI-Aufbau
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.Route;

// Domänenobjekt für Zahlungseingänge
import de.hsbi.immobilienverwaltung.domain.Zahlungseingang;

// Services für Auswertungen, Ausgaben und Zahlungseingänge
import de.hsbi.immobilienverwaltung.service.interfaces.AusgabeService;
import de.hsbi.immobilienverwaltung.service.interfaces.GesamtAuswertungService;
import de.hsbi.immobilienverwaltung.service.interfaces.ZahlungsEingangService;

// Layout- und UI-Hilfsklassen
import de.hsbi.immobilienverwaltung.ui.layout.HasPageHeader;
import de.hsbi.immobilienverwaltung.ui.layout.MainLayout;
import de.hsbi.immobilienverwaltung.ui.UiFormatUtils;

// Zugriffsschutz
import jakarta.annotation.security.PermitAll;

// Java-Standardklassen für Geldbeträge, Datum, Monate und Formatierung
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

/**
 * DashboardView stellt die zentrale Übersichtsseite der Immobilienverwaltung dar.
 *
 * Die View zeigt:
 * - wichtige Kennzahlen, z. B. Einnahmen, Leerstand und aktive Verträge
 * - Diagramme für Einnahmen/Ausgaben und Vermietungsstatus
 * - eine Liste offener Zahlungsposten
 * - Schnellaktionen zum Anlegen neuer Immobilien, Mieter, Verträge und Buchungen
 */
@Route(value = "dashboard", layout = MainLayout.class)
@PermitAll
public class DashboardView extends Div implements HasPageHeader {

    // Gesamtanzahl aller Mieteinheiten im Portfolio
    private final long gesamtMieteinheiten;

    // Anzahl leerstehender Mieteinheiten
    private final long leerstehendeMieteinheiten;

    // Anzahl vermieteter Mieteinheiten
    private final long vermieteteMieteinheiten;

    // Prozentuale Leerstandsquote
    private final double leerstandsquote;

    // Anzahl aktuell aktiver Mietverträge
    private final long aktiveVertraege;

    // Summe aller bezahlten Zahlungseingänge
    private final BigDecimal gesamteinnahmen;

    // Summe der offenen Ausgaben
    private final BigDecimal offeneAusgaben;

    // Anzahl offener Ausgaben
    private final long anzahlOffeneAusgaben;

    // Einnahmenwerte für das Balkendiagramm
    private final double[] chartEinnahmen;

    // Ausgabenwerte für das Balkendiagramm
    private final double[] chartAusgaben;

    // Monatsbeschriftungen für das Balkendiagramm
    private final String[] chartMonate;

    // Liste offener Zahlungseingänge für die Card "Offene Posten"
    private final List<Zahlungseingang> offeneZahlungseingaenge;

    /**
     * Konstruktor der DashboardView.
     *
     * Beim Erstellen der View werden alle relevanten Kennzahlen direkt
     * über die Services geladen und anschließend die UI-Komponenten aufgebaut.
     */
    public DashboardView(
            GesamtAuswertungService gesamtAuswertungService,
            ZahlungsEingangService zahlungsEingangService,
            AusgabeService ausgabeService
    ) {
        // Lädt die Gesamtanzahl aller Mieteinheiten.
        this.gesamtMieteinheiten =
                gesamtAuswertungService.berechneAnzahlMieteinheiten();

        // Lädt die Anzahl leerstehender Mieteinheiten.
        this.leerstehendeMieteinheiten =
                gesamtAuswertungService.berechneAnzahlLeerstehendeMieteinheiten();

        // Berechnet die vermieteten Mieteinheiten aus Gesamtanzahl minus Leerstand.
        this.vermieteteMieteinheiten =
                gesamtMieteinheiten - leerstehendeMieteinheiten;

        // Lädt die Leerstandsquote aus dem Auswertungsservice.
        this.leerstandsquote =
                gesamtAuswertungService.berechneLeerstandsquote();

        // Lädt die Anzahl aktiver Mietverträge.
        this.aktiveVertraege =
                gesamtAuswertungService.berechneAnzahlAktiveVertraege();

        // Berechnet die Gesamteinnahmen aus bereits bezahlten Zahlungseingängen.
        this.gesamteinnahmen =
                zahlungsEingangService.berechneGesamteBezahlteZahlungseingaenge();

        // Berechnet die Summe offener Ausgaben.
        this.offeneAusgaben =
                ausgabeService.berechneOffeneAusgaben();

        // Zählt offene Ausgaben.
        this.anzahlOffeneAusgaben =
                ausgabeService.zaehleOffeneAusgaben();

        // Lädt offene Zahlungseingänge für die Anzeige der offenen Posten.
        this.offeneZahlungseingaenge =
                zahlungsEingangService.findeOffeneZahlungseingaenge();

        // Bereitet die Einnahmendaten der letzten sechs Monate für das Diagramm vor.
        this.chartEinnahmen =
                berechneEinnahmenChartDaten(zahlungsEingangService);

        // Bereitet die Ausgabendaten der letzten sechs Monate für das Diagramm vor.
        this.chartAusgaben =
                berechneAusgabenChartDaten(ausgabeService);

        // Erzeugt die Monatslabels passend zu den Diagrammdaten.
        this.chartMonate =
                berechneChartMonate();

        // Bindet Chart.js ein, damit Diagramme im Browser gerendert werden können.
        addJavaScriptIfUiAvailable(
                "https://cdn.jsdelivr.net/npm/chart.js"
        );

        // Fügt CSS-Klassen für das Styling der Dashboard-Seite hinzu.
        addClassNames("page-content", "dashboard-page");

        // Baut die einzelnen Bereiche der Seite zusammen.
        add(createHeroSection());
        add(createKpiSection());
        add(createDashboardMainGrid());
    }

    /**
     * Lädt eine externe JavaScript-Datei, wenn eine Vaadin-UI vorhanden ist.
     *
     * Diese Prüfung ist wichtig, weil in Unit-Tests häufig keine vollständige
     * Vaadin-Session existiert. Ohne Prüfung könnte es zu NullPointerExceptions kommen.
     */
    private void addJavaScriptIfUiAvailable(String url) {
        UI ui = UI.getCurrent();

        // Wenn keine UI oder keine Session vorhanden ist, wird nichts geladen.
        if (ui == null || ui.getSession() == null) {
            return;
        }

        // Fügt das JavaScript zur aktuellen Seite hinzu.
        ui.getPage().addJavaScript(url);
    }

    /**
     * Führt JavaScript im Browser aus, sofern eine Vaadin-UI verfügbar ist.
     *
     * Wird vor allem für die Chart.js-Diagramme verwendet.
     */
    private void executeJsIfUiAvailable(String script, Object... arguments) {
        UI ui = UI.getCurrent();

        // Verhindert Fehler in Tests oder Umgebungen ohne aktive UI-Session.
        if (ui == null || ui.getSession() == null) {
            return;
        }

        // Führt das JavaScript mit optionalen Parametern aus.
        ui.getPage().executeJs(script, arguments);
    }

    /**
     * Erstellt den oberen Hero-Bereich des Dashboards.
     *
     * Dieser Bereich enthält:
     * - einen Eyebrow-Text
     * - eine Überschrift
     * - eine Kurzbeschreibung
     * - Schnellaktionsbuttons
     */
    private Component createHeroSection() {
        Div hero = new Div();
        hero.addClassName("dashboard-hero");

        Div content = new Div();
        content.addClassName("dashboard-hero-content");

        // Kleiner Label-Text über der Hauptüberschrift.
        Span eyebrow = new Span("Portfolio Control Center");
        eyebrow.addClassName("dashboard-eyebrow");

        // Hauptüberschrift des Dashboards.
        H2 title = new H2("Alles Wichtige auf einen Blick");
        title.addClassName("dashboard-hero-title");

        // Kurze Beschreibung des Dashboards.
        Paragraph subtitle = new Paragraph(
                "Verfolge Einnahmen, Leerstand und offene Posten in einem modernen Überblick."
        );
        subtitle.addClassName("dashboard-hero-subtitle");

        // Container für die Schnellaktionsbuttons.
        Div actions = new Div();
        actions.addClassName("dashboard-hero-actions");

        // Button zum Anlegen einer neuen Immobilie.
        Button neueImmobilie = primaryButton("Neue Immobilie", VaadinIcon.BUILDING);
        neueImmobilie.addClickListener(e ->
                getUI().ifPresent(ui -> ui.navigate("immobilien/neu"))
        );

        // Button zum Anlegen eines neuen Mieters.
        Button neuerMieter = secondaryButton("Neuer Mieter", VaadinIcon.USER);
        neuerMieter.addClickListener(e ->
                getUI().ifPresent(ui -> ui.navigate("mieter-vertraege/mieter-anlegen"))
        );

        // Button zum Anlegen eines neuen Mietvertrags.
        Button neuerVertrag = secondaryButton("Neuer Vertrag", VaadinIcon.FILE_TEXT);
        neuerVertrag.addClickListener(e ->
                getUI().ifPresent(ui -> ui.navigate("mieter-vertraege/mietvertrag-anlegen"))
        );

        // Button zum Anlegen einer neuen Buchung.
        Button neueBuchung = secondaryButton("Neue Buchung", VaadinIcon.EURO);
        neueBuchung.addClickListener(e ->
                getUI().ifPresent(ui -> ui.navigate("finanzen/buchung-neu"))
        );

        // Fügt alle Buttons dem Aktionsbereich hinzu.
        actions.add(neueImmobilie, neuerMieter, neuerVertrag, neueBuchung);

        // Fügt Texte und Aktionen in den Content-Bereich ein.
        content.add(eyebrow, title, subtitle, actions);

        // Visueller Bereich auf der rechten Seite, z. B. für dekoratives Styling.
        Div visual = new Div();
        visual.addClassName("dashboard-hero-visual");

        // Fügt Inhalt und Visual zum Hero-Bereich hinzu.
        hero.add(content, visual);

        return hero;
    }

    /**
     * Erstellt den KPI-Bereich.
     *
     * Hier werden vier zentrale Kennzahlen als Cards angezeigt:
     * - Gesamteinnahmen
     * - Leerstandsquote
     * - offene Zahlungen
     * - aktive Verträge
     */
    private Component createKpiSection() {
        Div grid = new Div();
        grid.addClassName("dashboard-kpi-grid");

        grid.add(
                // Card für die Gesamteinnahmen.
                kpiCard(
                        "Gesamteinnahmen",
                        UiFormatUtils.formatiereBetrag(gesamteinnahmen),
                        "Portfolio",
                        "Summe aller Zahlungseingänge",
                        VaadinIcon.LINE_CHART,
                        "primary"
                ),

                // Card für die Leerstandsquote.
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

                // Card für offene Ausgaben bzw. offene Zahlungen.
                kpiCard(
                        "Offene Zahlungen",
                        UiFormatUtils.formatiereBetrag(offeneAusgaben),
                        anzahlOffeneAusgaben + " offen",
                        formatAnzahlOffeneAusgaben(anzahlOffeneAusgaben),
                        VaadinIcon.WARNING,
                        "warning"
                ),

                // Card für aktive Mietverträge.
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

    /**
     * Erstellt den Hauptbereich des Dashboards.
     *
     * Der Bereich besteht aus:
     * - einer linken Spalte mit Diagrammen
     * - einer rechten Card mit offenen Posten
     */
    private Component createDashboardMainGrid() {
        Div grid = new Div();
        grid.addClassName("dashboard-main-grid");

        // Linke Spalte für die Diagramme.
        Div chartColumn = new Div();
        chartColumn.addClassName("dashboard-chart-column");

        chartColumn.add(
                // Balkendiagramm: Einnahmen vs. Ausgaben.
                chartCard(
                        "Einnahmen vs. Ausgaben",
                        "Entwicklung der letzten sechs Monate",
                        createBarChart(),
                        "wide"
                ),

                // Kreisdiagramm: Vermietete Einheiten vs. Leerstand.
                chartCard(
                        "Vermietet vs. Leerstand",
                        "Aktuelle Verteilung der Mieteinheiten",
                        createPieChart(),
                        "compact"
                )
        );

        // Fügt Diagrammspalte und offene Posten zur Hauptgrid hinzu.
        grid.add(chartColumn, createOpenItemsCard());

        return grid;
    }

    /**
     * Erstellt die Card für offene Zahlungsposten.
     *
     * Wenn keine offenen Zahlungen vorhanden sind, wird ein Empty-State angezeigt.
     * Wenn offene Zahlungen existieren, werden maximal fünf Einträge angezeigt.
     */
    private Div createOpenItemsCard() {
        Div card = new Div();
        card.addClassNames("card", "dashboard-open-card");

        // Header-Bereich der Card.
        Div header = new Div();
        header.addClassName("dashboard-card-header");

        Div titleBox = new Div();

        H3 title = new H3("Offene Posten");
        title.addClassName("card-title");

        Paragraph subtitle = new Paragraph("Priorisierte Zahlungen und Fälligkeiten");
        subtitle.addClassName("card-subtitle");

        titleBox.add(title, subtitle);

        // Badge mit Anzahl offener Zahlungseingänge.
        Span counter = new Span(String.valueOf(offeneZahlungseingaenge == null ? 0 : offeneZahlungseingaenge.size()));

        // Farbe des Badges abhängig davon, ob offene Ausgaben vorhanden sind.
        counter.addClassNames("status-badge", anzahlOffeneAusgaben == 0 ? "success" : "warning");

        header.add(titleBox, counter);
        card.add(header);

        // Empty-State, wenn keine offenen Zahlungseingänge vorhanden sind.
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

        // Liste für offene Zahlungseinträge.
        Div list = new Div();
        list.addClassName("dashboard-open-list");

        offeneZahlungseingaenge.stream()
                // Nur Zahlungen mit Zahlungsdatum werden angezeigt.
                .filter(zahlung -> zahlung.getZahlungsdatum() != null)

                // Älteste offene Zahlungen stehen oben, weil sie am dringendsten sind.
                .sorted((z1, z2) -> z1.getZahlungsdatum().compareTo(z2.getZahlungsdatum()))

                // Das Dashboard zeigt nur eine kompakte Vorschau der wichtigsten offenen Posten.
                .limit(5)

                // Für jede Zahlung wird ein UI-Listeneintrag erzeugt.
                .forEach(zahlung -> list.add(openItem(
                        UiFormatUtils.formatiereMieterName(zahlung.getMietvertrag()),
                        ermittleBeschreibung(zahlung),
                        UiFormatUtils.formatiereBetrag(zahlung.getBetrag()),
                        ermittleUeberfaelligkeit(zahlung)
                )));

        card.add(list);

        return card;
    }

    /**
     * Ermittelt die Beschreibung eines Zahlungseingangs.
     *
     * Reihenfolge:
     * 1. vorhandene Beschreibung verwenden
     * 2. Zahlungstyp formatieren
     * 3. Standardtext "Offene Zahlung" verwenden
     */
    private String ermittleBeschreibung(Zahlungseingang zahlung) {
        // Falls eine Beschreibung vorhanden ist, wird diese verwendet.
        if (zahlung.getBeschreibung() != null &&
                !zahlung.getBeschreibung().isBlank()) {
            return zahlung.getBeschreibung();
        }

        // Falls ein Zahlungstyp vorhanden ist, wird dieser lesbar formatiert.
        if (zahlung.getTyp() != null) {
            return formatiereZahlungseingangTyp(
                    zahlung.getTyp().toString()
            );
        }

        // Fallback, wenn weder Beschreibung noch Typ vorhanden sind.
        return "Offene Zahlung";
    }

    /**
     * Berechnet den Fälligkeitsstatus einer Zahlung.
     *
     * Beispiele:
     * - "Fällig in 3 Tagen"
     * - "Heute fällig"
     * - "1 Tag überfällig"
     * - "5 Tage überfällig"
     */
    private String ermittleUeberfaelligkeit(Zahlungseingang zahlung) {
        // Ohne Zahlungsdatum kann keine Fälligkeit berechnet werden.
        if (zahlung.getZahlungsdatum() == null) {
            return "-";
        }

        // Differenz zwischen Zahlungsdatum und aktuellem Datum berechnen.
        long tage = ChronoUnit.DAYS.between(
                zahlung.getZahlungsdatum(),
                LocalDate.now()
        );

        // Zahlung liegt in der Zukunft.
        if (tage < 0) {
            return "Fällig in " + Math.abs(tage) + " Tagen";
        }

        // Zahlung ist heute fällig.
        if (tage == 0) {
            return "Heute fällig";
        }

        // Zahlung ist genau einen Tag überfällig.
        if (tage == 1) {
            return "1 Tag überfällig";
        }

        // Zahlung ist mehrere Tage überfällig.
        return tage + " Tage überfällig";
    }

    /**
     * Formatiert technische Zahlungstypen in benutzerfreundliche Bezeichnungen.
     */
    private String formatiereZahlungseingangTyp(String typ) {
        return switch (typ) {
            case "KALTMIETE" -> "Kaltmiete";
            case "NEBENKOSTEN" -> "Nebenkosten";
            case "KAUTION" -> "Kaution";
            default -> typ;
        };
    }

    /**
     * Erstellt eine KPI-Card.
     *
     * Eine KPI-Card besteht aus:
     * - Icon
     * - Badge
     * - Titel
     * - Wert
     * - Untertitel
     */
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

        // Header mit Icon und Badge.
        Div header = new Div();
        header.addClassName("kpi-card-header");

        // Icon-Box mit farblicher Kennzeichnung.
        Div iconBox = new Div(new Icon(icon));
        iconBox.addClassNames("kpi-icon-box", color);

        // Badge-Text, z. B. "Portfolio", "laufend" oder "offen".
        Span badgeSpan = new Span(badge);

        // Wenn ein Badge-Text vorhanden ist, werden Badge-Klassen gesetzt.
        if (!badge.isBlank()) {
            badgeSpan.addClassNames("status-badge", color);
        }

        header.add(iconBox, badgeSpan);

        // Titel der KPI.
        Paragraph titleText = new Paragraph(title);
        titleText.addClassName("kpi-title");

        // Hauptwert der KPI.
        H2 valueText = new H2(value);
        valueText.addClassName("kpi-value");

        // Beschreibung unterhalb des KPI-Werts.
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

    /**
     * Erstellt eine Card für ein Diagramm.
     *
     * Die Card enthält:
     * - Titel
     * - Untertitel
     * - Diagramm-Komponente
     */
    private Div chartCard(String title, String subtitle, Component chart, String size) {
        Div card = new Div();
        card.addClassNames("card", "dashboard-chart-card", size);

        // Header der Diagramm-Card.
        Div header = new Div();
        header.addClassName("dashboard-card-header");

        Div titleBox = new Div();

        H3 titleText = new H3(title);
        titleText.addClassName("card-title");

        Paragraph subtitleText = new Paragraph(subtitle);
        subtitleText.addClassName("card-subtitle");

        titleBox.add(titleText, subtitleText);
        header.add(titleBox);

        // Fügt Header und Diagramm in die Card ein.
        card.add(header, chart);

        return card;
    }

    /**
     * Erstellt das Balkendiagramm "Einnahmen vs. Ausgaben".
     *
     * Das Diagramm wird über Chart.js im Browser gerendert.
     * Die Daten werden aus Java als Parameter an JavaScript übergeben.
     */
    private Component createBarChart() {
        Div wrapper = new Div();
        wrapper.addClassName("dashboard-chart-wrapper");

        // Canvas-Element, in das Chart.js das Diagramm rendert.
        Element canvas = new Element("canvas");
        canvas.setAttribute("id", "incomeExpenseChart");

        wrapper.getElement().appendChild(canvas);

        // Chart.js rendert das Balkendiagramm im Browser.
        // Die Java-Daten werden als Parameter in das JavaScript übergeben.
        executeJsIfUiAvailable("""
            setTimeout(() => {
                const ctx = document.getElementById('incomeExpenseChart');

                // Wenn Canvas oder Chart.js nicht verfügbar sind, wird abgebrochen.
                if (!ctx || !window.Chart) return;

                // Farben werden aus den CSS-Variablen gelesen.
                const styles = getComputedStyle(document.documentElement);
                const primary = styles.getPropertyValue('--color-primary').trim() || '#2563eb';
                const danger = styles.getPropertyValue('--color-danger').trim() || '#ef4444';
                const grid = styles.getPropertyValue('--color-border').trim() || '#e5e7eb';

                // Existierendes Diagramm zerstören, um doppelte Instanzen zu vermeiden.
                if (window.incomeExpenseChartInstance) {
                    window.incomeExpenseChartInstance.destroy();
                }

                // Neues Balkendiagramm erstellen.
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

    /**
     * Erstellt das Doughnut-Diagramm "Vermietet vs. Leerstand".
     *
     * Das Diagramm zeigt das Verhältnis zwischen vermieteten und leerstehenden
     * Mieteinheiten.
     */
    private Component createPieChart() {
        Div wrapper = new Div();
        wrapper.addClassName("dashboard-chart-wrapper");

        // Canvas-Element für Chart.js.
        Element canvas = new Element("canvas");
        canvas.setAttribute("id", "vacancyPieChart");

        wrapper.getElement().appendChild(canvas);

        // Das Diagramm zeigt das Verhältnis von vermieteten und leerstehenden Mieteinheiten.
        executeJsIfUiAvailable("""
            setTimeout(() => {
                const ctx = document.getElementById('vacancyPieChart');

                // Wenn Canvas oder Chart.js fehlen, wird nicht gerendert.
                if (!ctx || !window.Chart) return;

                // Farben aus CSS-Variablen auslesen.
                const styles = getComputedStyle(document.documentElement);
                const success = styles.getPropertyValue('--color-success').trim() || '#10b981';
                const warning = styles.getPropertyValue('--color-warning').trim() || '#f59e0b';

                // Vorherige Chart-Instanz zerstören, damit keine doppelten Charts entstehen.
                if (window.vacancyPieChartInstance) {
                    window.vacancyPieChartInstance.destroy();
                }

                // Doughnut-Chart erstellen.
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

                                        // Gesamtanzahl berechnen, um Prozentwerte anzeigen zu können.
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

                                        // Tooltip-Text für jedes Segment.
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

                        // Sorgt für den typischen Doughnut-Look.
                        cutout: '72%'
                    }
                });
            }, 300);
        """, vermieteteMieteinheiten, leerstehendeMieteinheiten);

        return wrapper;
    }

    /**
     * Erstellt einen einzelnen Listeneintrag für offene Zahlungsposten.
     *
     * Angezeigt werden:
     * - Icon
     * - Name des Mieters
     * - Beschreibung
     * - Betrag
     * - Fälligkeitsstatus
     */
    private Component openItem(
            String name,
            String description,
            String amount,
            String overdue
    ) {
        Div row = new Div();
        row.addClassName("dashboard-open-item");

        // Icon links im Listeneintrag.
        Div iconBox = new Div(new Icon(VaadinIcon.CLOCK));
        iconBox.addClassName("dashboard-open-item-icon");

        // Linker Textbereich mit Name und Beschreibung.
        Div left = new Div();
        left.addClassName("dashboard-open-item-content");

        Span nameText = new Span(name);
        nameText.addClassName("dashboard-open-item-title");

        Span descriptionText = new Span(description);
        descriptionText.addClassName("dashboard-open-item-subtitle");

        left.add(nameText, descriptionText);

        // Rechter Bereich mit Betrag und Fälligkeitsstatus.
        Div right = new Div();
        right.addClassName("dashboard-open-item-amount");

        Span amountText = new Span(amount);
        amountText.addClassName("dashboard-open-item-value");

        Span overdueText = new Span(overdue);
        overdueText.addClassName("dashboard-open-item-meta");

        right.add(amountText, overdueText);

        // Fügt alle Bereiche in eine Zeile zusammen.
        row.add(iconBox, left, right);

        return row;
    }

    /**
     * Erstellt einen primären Button.
     *
     * Primäre Buttons werden für besonders wichtige Aktionen verwendet,
     * z. B. "Neue Immobilie".
     */
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

    /**
     * Erstellt einen sekundären Button.
     *
     * Sekundäre Buttons werden für weitere Aktionen verwendet.
     */
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

    /**
     * Berechnet die Einnahmen für die letzten sechs Monate.
     *
     * Für jeden Monat wird die Summe der bezahlten Zahlungseingänge
     * im jeweiligen Zeitraum abgefragt.
     */
    private double[] berechneEinnahmenChartDaten(
            ZahlungsEingangService zahlungsEingangService
    ) {
        // Array mit sechs Monatswerten.
        double[] daten = new double[6];

        // Aktueller Monat dient als Ausgangspunkt.
        YearMonth aktuellerMonat = YearMonth.now();

        // Erstellt die Einnahmenwerte für die letzten sechs Monate.
        for (int i = 0; i < 6; i++) {
            // Berechnet den jeweiligen Monat von vor fünf Monaten bis heute.
            YearMonth monat = aktuellerMonat.minusMonths(5 - i);

            // Fragt bezahlte Zahlungseingänge innerhalb des Monats ab.
            BigDecimal summe =
                    zahlungsEingangService.berechneBezahlteZahlungseingaengeImZeitraum(
                            monat.atDay(1),
                            monat.atEndOfMonth(),
                            null,
                            null,
                            null
                    );

            // Null-Schutz: Wenn der Service null liefert, wird 0 verwendet.
            if (summe == null) {
                summe = BigDecimal.ZERO;
            }

            // BigDecimal wird für Chart.js in double umgewandelt.
            daten[i] = summe.doubleValue();
        }

        return daten;
    }

    /**
     * Berechnet die Ausgaben für die letzten sechs Monate.
     *
     * Für jeden Monat wird die Summe der Ausgaben im jeweiligen Zeitraum
     * aus dem AusgabeService abgefragt.
     */
    private double[] berechneAusgabenChartDaten(
            AusgabeService ausgabeService
    ) {
        // Array mit sechs Monatswerten.
        double[] daten = new double[6];

        // Aktueller Monat als Referenz.
        YearMonth aktuellerMonat = YearMonth.now();

        // Erstellt die Ausgabenwerte für die letzten sechs Monate.
        for (int i = 0; i < 6; i++) {
            // Monat im Zeitraum der letzten sechs Monate berechnen.
            YearMonth monat = aktuellerMonat.minusMonths(5 - i);

            // Summe der Ausgaben im Monatszeitraum berechnen.
            BigDecimal summe =
                    ausgabeService.berechneAusgabenImZeitraum(
                            monat.atDay(1),
                            monat.atEndOfMonth()
                    );

            // Null-Schutz.
            if (summe == null) {
                summe = BigDecimal.ZERO;
            }

            // Wert für Chart.js speichern.
            daten[i] = summe.doubleValue();
        }

        return daten;
    }

    /**
     * Erstellt die Monatsbeschriftungen für das Balkendiagramm.
     *
     * Die Labels beziehen sich auf dieselben sechs Monate wie die Einnahmen-
     * und Ausgabendaten.
     */
    private String[] berechneChartMonate() {
        // Array für sechs Monatsnamen.
        String[] monate = new String[6];

        // Aktueller Monat als Ausgangspunkt.
        YearMonth aktuellerMonat = YearMonth.now();

        // Deutsches Monatsformat, z. B. "Jan.", "Feb.", "März".
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern(
                        "MMM",
                        Locale.GERMANY
                );

        // Erstellt die Monatsbeschriftungen passend zu den Chart-Daten.
        for (int i = 0; i < 6; i++) {
            YearMonth monat = aktuellerMonat.minusMonths(5 - i);
            monate[i] = monat.format(formatter);
        }

        return monate;
    }

    /**
     * Formatiert die Anzahl offener Ausgaben grammatikalisch korrekt.
     *
     * Bei genau einer Ausgabe wird Singular verwendet,
     * ansonsten Plural.
     */
    private String formatAnzahlOffeneAusgaben(long anzahl) {
        if (anzahl == 1) {
            return "1 ausstehende Ausgabe";
        }

        return anzahl + " ausstehende Ausgaben";
    }

    /**
     * Gibt den Seitentitel für den Header zurück.
     */
    @Override
    public String getPageTitle() {
        return "Dashboard";
    }

    /**
     * Gibt den Untertitel für den Header zurück.
     */
    @Override
    public String getPageSubtitle() {
        return "Zentrale Übersicht und KPIs";
    }
}
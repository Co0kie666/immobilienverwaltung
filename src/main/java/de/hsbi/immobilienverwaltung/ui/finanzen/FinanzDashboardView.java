package de.hsbi.immobilienverwaltung.ui.finanzen;

// Vaadin-Komponenten für UI-Aufbau
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.Route;

// Domain-Klassen wie Immobilie, Mieteinheit, Mieter, Mietvertrag, Ausgabe, Zahlungseingang
import de.hsbi.immobilienverwaltung.domain.*;

// Service-Interfaces für Datenzugriff und Berechnungen
import de.hsbi.immobilienverwaltung.service.interfaces.*;

// Layout- und Header-Interfaces
import de.hsbi.immobilienverwaltung.ui.layout.HasPageHeader;
import de.hsbi.immobilienverwaltung.ui.layout.MainLayout;

// Zugriffsschutz
import jakarta.annotation.security.PermitAll;

// Hilfsklasse zur Formatierung von Datum und Geldbeträgen
import de.hsbi.immobilienverwaltung.ui.UiFormatUtils;

// Java-Standardklassen für Beträge, Datum, Monate, Formatierung und Collections
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * FinanzDashboardView stellt das Finanz-Dashboard der Immobilienverwaltung dar.
 *
 * Die View zeigt:
 * - Einnahmen
 * - Ausgaben
 * - Rückstände
 * - Cashflow
 * - Diagramme für Einnahmen, Ausgaben, Zahlungsstatus und Kostenverteilung
 * - Tabellen mit den letzten Einnahmen und Ausgaben
 * - Filter nach Zeitraum, Immobilie, Mieteinheit und Mieter
 */
@Route(value = "finanzen", layout = MainLayout.class)
@PermitAll
public class FinanzDashboardView extends Div implements HasPageHeader {

    /**
     * Zeitraumfilter für das Dashboard.
     *
     * Je nach Auswahl werden die Finanzdaten für unterschiedliche Zeiträume geladen.
     */
    private enum ZeitraumFilter {
        EIN_MONAT,
        DREI_MONATE,
        SECHS_MONATE,
        YTD
    }

    /**
     * Typ einer Buchung.
     *
     * Wird genutzt, um zwischen Einnahmen und Ausgaben zu unterscheiden.
     * Der routeValue wird später für die Navigation zur Detailseite verwendet.
     */
    private enum BuchungTyp {
        EINNAHME("einnahme"),
        AUSGABE("ausgabe");

        private final String routeValue;

        BuchungTyp(String routeValue) {
            this.routeValue = routeValue;
        }

        public String getRouteValue() {
            return routeValue;
        }
    }

    /**
     * Generische Filteroption für ComboBoxen.
     *
     * Wird für Immobilien, Mieteinheiten und Mieter verwendet.
     *
     * id:
     * - null bedeutet: "Alle"
     *
     * label:
     * - Text, der in der ComboBox angezeigt wird
     *
     * value:
     * - das eigentliche Domain-Objekt
     */
    private record FilterOption<T>(
            Long id,
            String label,
            T value
    ) {
        /**
         * Prüft, ob diese Option die "Alle"-Option ist.
         */
        boolean isAll() {
            return id == null;
        }
    }

    /**
     * Datenstruktur für eine Tabellenzeile in den Buchungstabellen.
     *
     * Wird sowohl für Einnahmen als auch für Ausgaben verwendet.
     */
    private record BuchungTabellenZeile(
            Long id,
            BuchungTyp typ,
            String datum,
            String objekt,
            String kategorie,
            String status,
            String betrag
    ) {
    }

    // Service für Zahlungseingänge, z. B. Mieteinnahmen
    private final ZahlungsEingangService zahlungsEingangService;

    // Service für Ausgaben
    private final AusgabeService ausgabeService;

    // Standardmäßig wird der aktuelle Monat angezeigt
    private ZeitraumFilter aktuellerFilter = ZeitraumFilter.EIN_MONAT;

    // Summe bezahlter Einnahmen im aktuellen Filterzeitraum
    private BigDecimal summeEinnahmen;

    // Summe bezahlter Ausgaben im aktuellen Filterzeitraum
    private BigDecimal summeAusgaben;

    // Summe offener Zahlungseingänge im aktuellen Filterzeitraum
    private BigDecimal rueckstaende;

    // Cashflow = Einnahmen minus Ausgaben
    private BigDecimal cashflow;

    // Einnahmendaten für das Diagramm
    private double[] chartEinnahmen;

    // Ausgabendaten für das Diagramm
    private double[] chartAusgaben;

    // Monatsbeschriftungen für die Diagramme
    private String[] chartMonate;

    // Labels für die Kostenverteilung, z. B. Kategorien
    private String[] kostenverteilungLabels;

    // Werte für die Kostenverteilung
    private double[] kostenverteilungDaten;

    // Prozentualer Anteil bezahlter Zahlungen
    private double zahlungsstatusBezahlt;

    // Prozentualer Anteil offener Zahlungen
    private double zahlungsstatusOffen;

    // Tabellenzeilen für die letzten Einnahmen
    private List<BuchungTabellenZeile> letzteEinnahmenRows;

    // Tabellenzeilen für die letzten Ausgaben
    private List<BuchungTabellenZeile> letzteAusgabenRows;

    // Weitere Services für Filterdaten
    private final ImmobilieService immobilieService;
    private final MieteinheitService mieteinheitService;
    private final MieterService mieterService;
    private final MietvertragService mietvertragService;

    // ComboBox-Filter für Immobilien
    private ComboBox<FilterOption<Immobilie>> immobilieFilter;

    // ComboBox-Filter für Mieteinheiten
    private ComboBox<FilterOption<Mieteinheit>> einheitFilter;

    // ComboBox-Filter für Mieter
    private ComboBox<FilterOption<Mieter>> mieterFilter;

    // Aktuell ausgewählte Immobilie
    private Long ausgewaehlteImmobilieId;

    // Aktuell ausgewählte Mieteinheit
    private Long ausgewaehlteMieteinheitId;

    // Aktuell ausgewählter Mieter
    private Long ausgewaehlterMieterId;

    /**
     * Konstruktor der FinanzDashboardView.
     *
     * Die benötigten Services werden per Dependency Injection übergeben.
     * Danach werden Chart.js geladen, CSS-Klassen gesetzt, Finanzdaten geladen
     * und die Seite aufgebaut.
     */
    public FinanzDashboardView(
            ZahlungsEingangService zahlungsEingangService,
            AusgabeService ausgabeService,
            ImmobilieService immobilieService,
            MieteinheitService mieteinheitService,
            MieterService mieterService,
            MietvertragService mietvertragService
    ) {
        // Services speichern
        this.immobilieService = immobilieService;
        this.mieteinheitService = mieteinheitService;
        this.mieterService = mieterService;
        this.zahlungsEingangService = zahlungsEingangService;
        this.ausgabeService = ausgabeService;
        this.mietvertragService = mietvertragService;

        // Chart.js wird für die Diagramme eingebunden.
        addJavaScriptIfUiAvailable(
                "https://cdn.jsdelivr.net/npm/chart.js"
        );

        // CSS-Klassen für Layout und Styling der Seite.
        addClassNames("page-content", "dashboard-page", "finance-page", "finance-page-modern");

        // Initial werden Daten ohne Objekt-, Einheiten- oder Mieterfilter geladen.
        ladeFinanzdaten(null, null, null);

        // Danach wird die komplette Seite aufgebaut.
        baueSeiteNeu();
    }

    /**
     * Lädt eine externe JavaScript-Datei, wenn eine Vaadin-UI vorhanden ist.
     *
     * Die Prüfung verhindert Fehler in Tests oder Situationen ohne aktive Session.
     */
    private void addJavaScriptIfUiAvailable(String url) {
        UI ui = UI.getCurrent();

        if (ui == null || ui.getSession() == null) {
            return;
        }

        ui.getPage().addJavaScript(url);
    }

    /**
     * Führt JavaScript im Browser aus, wenn eine Vaadin-UI vorhanden ist.
     *
     * Wird für das Rendern der Chart.js-Diagramme verwendet.
     */
    private void executeJsIfUiAvailable(String script, Object... arguments) {
        UI ui = UI.getCurrent();

        if (ui == null || ui.getSession() == null) {
            return;
        }

        ui.getPage().executeJs(script, arguments);
    }

    /**
     * Lädt die Daten neu und baut die Ansicht mit den aktuellen Filtern auf.
     *
     * Wird aufgerufen, wenn sich ein Zeitraum-, Immobilien-, Einheiten-
     * oder Mieterfilter ändert.
     */
    private void baueSeiteNeu() {
        // Entfernt alle vorhandenen UI-Komponenten.
        removeAll();

        // Lädt Daten basierend auf den aktuell ausgewählten Filtern.
        ladeFinanzdaten(
                ausgewaehlteImmobilieId,
                ausgewaehlteMieteinheitId,
                ausgewaehlterMieterId
        );

        // Baut die UI-Bereiche neu auf.
        add(createHeroSection());
        add(createFilterBar());
        add(createKpiGrid());
        add(createDashboardGrid());
        add(createTableGrid());
    }

    /**
     * Berechnet alle Finanzdaten für den ausgewählten Zeitraum und die gesetzten Filter.
     *
     * Dazu gehören:
     * - Summe Einnahmen
     * - Summe Ausgaben
     * - Rückstände
     * - Cashflow
     * - Diagrammdaten
     * - Tabellenzeilen
     * - Kostenverteilung
     * - Zahlungsstatus
     */
    private void ladeFinanzdaten(
            Long immobilieId,
            Long mieteinheitId,
            Long mieterId
    ) {
        // Startdatum ergibt sich aus dem aktuellen Zeitraumfilter.
        LocalDate startDatum = ermittleStartDatum();

        // Enddatum ist immer heute.
        LocalDate endDatum = LocalDate.now();

        // Bezahlte Zahlungseingänge im Zeitraum berechnen.
        this.summeEinnahmen =
                zahlungsEingangService.berechneBezahlteZahlungseingaengeImZeitraum(
                        startDatum,
                        endDatum,
                        immobilieId,
                        mieteinheitId,
                        mieterId
                );

        // Bezahlte Ausgaben im Zeitraum berechnen.
        this.summeAusgaben =
                ausgabeService.berechneBezahlteAusgabenImZeitraum(
                        startDatum,
                        endDatum,
                        immobilieId
                );

        // Offene Zahlungseingänge im Zeitraum berechnen.
        this.rueckstaende =
                zahlungsEingangService.berechneOffeneZahlungseingaengeImZeitraum(
                        startDatum,
                        endDatum,
                        immobilieId,
                        mieteinheitId,
                        mieterId
                );

        // Cashflow berechnen: Einnahmen minus Ausgaben.
        this.cashflow =
                this.summeEinnahmen.subtract(this.summeAusgaben);

        // Einnahmendaten für das Diagramm berechnen.
        this.chartEinnahmen =
                berechneEinnahmenChartDaten(
                        startDatum,
                        endDatum,
                        immobilieId,
                        mieteinheitId,
                        mieterId
                );

        // Ausgabendaten für das Diagramm berechnen.
        this.chartAusgaben =
                berechneAusgabenChartDaten(
                        startDatum,
                        endDatum,
                        immobilieId
                );

        // Monatslabels für die Diagramme berechnen.
        this.chartMonate =
                berechneChartMonate(startDatum, endDatum);

        // Tabellenzeilen für die letzten Einnahmen berechnen.
        this.letzteEinnahmenRows =
                berechneLetzteEinnahmenTabellenZeilen(
                        startDatum,
                        endDatum,
                        immobilieId,
                        mieteinheitId,
                        mieterId
                );

        // Tabellenzeilen für die letzten Ausgaben berechnen.
        this.letzteAusgabenRows =
                berechneLetzteAusgabenTabellenZeilen(
                        startDatum,
                        endDatum,
                        immobilieId
                );

        // Kostenverteilung nach Kategorien berechnen.
        berechneKostenverteilung(
                startDatum,
                endDatum,
                immobilieId
        );

        // Verhältnis bezahlt/offen berechnen.
        berechneZahlungsstatus();
    }

    /**
     * Berechnet die Tabellenzeilen für die letzten Einnahmen.
     *
     * Es werden maximal fünf Zahlungseingänge angezeigt,
     * sortiert nach Zahlungsdatum absteigend.
     */
    private List<BuchungTabellenZeile> berechneLetzteEinnahmenTabellenZeilen(
            LocalDate startDatum,
            LocalDate endDatum,
            Long immobilieId,
            Long mieteinheitId,
            Long mieterId
    ) {
        // Zahlungseingänge aus dem Service laden.
        List<Zahlungseingang> zahlungseingaenge =
                zahlungsEingangService.findeZahlungseingaengeImZeitraum(
                        startDatum,
                        endDatum,
                        immobilieId,
                        mieteinheitId,
                        mieterId
                );

        return zahlungseingaenge.stream()
                // Nur Einträge mit Zahlungsdatum anzeigen.
                .filter(z -> z.getZahlungsdatum() != null)

                // Neueste Zahlungen zuerst.
                .sorted((z1, z2) -> z2.getZahlungsdatum().compareTo(z1.getZahlungsdatum()))

                // Nur die letzten fünf Einträge.
                .limit(5)

                // Domain-Objekte in Tabellenzeilen umwandeln.
                .map(zahlung -> new BuchungTabellenZeile(
                        zahlung.getId(),
                        BuchungTyp.EINNAHME,
                        UiFormatUtils.formatiereDatum(zahlung.getZahlungsdatum()),
                        ermittleZahlungObjektText(zahlung),
                        zahlung.getTyp() == null
                                ? "Einnahme"
                                : zahlung.getTyp().getLabel(),
                        zahlung.getStatus() == null
                                ? "-"
                                : zahlung.getStatus(),
                        UiFormatUtils.formatiereBetrag(zahlung.getBetrag())
                ))
                .toList();
    }

    /**
     * Ermittelt den Text für die Spalte "Mieter / Objekt" bei Zahlungseingängen.
     *
     * Priorität:
     * 1. Mietername
     * 2. Bezeichnung der Mieteinheit
     * 3. "-"
     */
    private String ermittleZahlungObjektText(Zahlungseingang zahlung) {
        if (zahlung.getMietvertrag() == null) {
            return "-";
        }

        // Wenn ein Mieter vorhanden ist, wird dessen vollständiger Name angezeigt.
        if (zahlung.getMietvertrag().getMieter() != null) {
            return zahlung.getMietvertrag()
                    .getMieter()
                    .getVorname()
                    + " "
                    + zahlung.getMietvertrag()
                    .getMieter()
                    .getNachname();
        }

        // Falls kein Mieter vorhanden ist, aber eine Mieteinheit, wird diese angezeigt.
        if (zahlung.getMietvertrag().getMieteinheit() != null) {
            return zahlung.getMietvertrag()
                    .getMieteinheit()
                    .getBezeichnung();
        }

        return "-";
    }

    /**
     * Berechnet die Tabellenzeilen für die letzten Ausgaben.
     *
     * Es werden maximal fünf Ausgaben angezeigt,
     * sortiert nach Datum absteigend.
     */
    private List<BuchungTabellenZeile> berechneLetzteAusgabenTabellenZeilen(
            LocalDate startDatum,
            LocalDate endDatum,
            Long immobilieId
    ) {
        // Ausgaben im Zeitraum laden.
        List<Ausgabe> ausgaben =
                ausgabeService.findeAusgabenImZeitraum(
                        startDatum,
                        endDatum,
                        immobilieId
                );

        return ausgaben.stream()
                // Nur Ausgaben mit Datum anzeigen.
                .filter(a -> a.getDatum() != null)

                // Neueste Ausgaben zuerst.
                .sorted((a1, a2) -> a2.getDatum().compareTo(a1.getDatum()))

                // Maximal fünf Einträge anzeigen.
                .limit(5)

                // Ausgabe in Tabellenzeile umwandeln.
                .map(ausgabe -> new BuchungTabellenZeile(
                        ausgabe.getId(),
                        BuchungTyp.AUSGABE,
                        UiFormatUtils.formatiereDatum(ausgabe.getDatum()),
                        ermittleAusgabeObjektText(ausgabe),
                        ausgabe.getKategorie() == null
                                ? "-"
                                : ausgabe.getKategorie().getLabel(),
                        ausgabe.getStatus() == null
                                ? "-"
                                : ausgabe.getStatus(),
                        "- " + UiFormatUtils.formatiereBetrag(ausgabe.getBetrag())
                ))
                .toList();
    }

    /**
     * Ermittelt den Objekttext für eine Ausgabe.
     *
     * Priorität:
     * 1. Mieteinheit
     * 2. Immobilie
     * 3. "-"
     */
    private String ermittleAusgabeObjektText(Ausgabe ausgabe) {
        if (ausgabe.getMieteinheit() != null) {
            return ausgabe.getMieteinheit().getBezeichnung();
        }

        if (ausgabe.getImmobilie() != null) {
            return ausgabe.getImmobilie().getBezeichnung();
        }

        return "-";
    }

    /**
     * Ermittelt das Startdatum anhand des ausgewählten Zeitraumfilters.
     */
    private LocalDate ermittleStartDatum() {
        LocalDate heute = LocalDate.now();

        return switch (aktuellerFilter) {
            // Start des aktuellen Monats
            case EIN_MONAT -> heute.withDayOfMonth(1);

            // Start des Monats vor zwei Monaten
            case DREI_MONATE -> heute.minusMonths(2).withDayOfMonth(1);

            // Start des Monats vor fünf Monaten
            case SECHS_MONATE -> heute.minusMonths(5).withDayOfMonth(1);

            // Jahresbeginn
            case YTD -> heute.withDayOfYear(1);
        };
    }

    /**
     * Berechnet den Zahlungsstatus in Prozent.
     *
     * Zahlungsstatus besteht aus:
     * - bezahlt
     * - offen
     */
    private void berechneZahlungsstatus() {
        // Null-Schutz für Einnahmen und Rückstände.
        BigDecimal bezahlt = summeEinnahmen == null ? BigDecimal.ZERO : summeEinnahmen;
        BigDecimal offen = rueckstaende == null ? BigDecimal.ZERO : rueckstaende;

        // Gesamtvolumen aus bezahlten und offenen Zahlungen.
        BigDecimal gesamt = bezahlt.add(offen);

        // Wenn es keine Zahlungen gibt, sind beide Werte 0 %.
        if (gesamt.compareTo(BigDecimal.ZERO) == 0) {
            this.zahlungsstatusBezahlt = 0;
            this.zahlungsstatusOffen = 0;
            return;
        }

        // Prozentanteil bezahlter Zahlungen.
        this.zahlungsstatusBezahlt =
                bezahlt.multiply(BigDecimal.valueOf(100))
                        .divide(gesamt, 2, RoundingMode.HALF_UP)
                        .doubleValue();

        // Prozentanteil offener Zahlungen.
        this.zahlungsstatusOffen =
                offen.multiply(BigDecimal.valueOf(100))
                        .divide(gesamt, 2, RoundingMode.HALF_UP)
                        .doubleValue();
    }

    /**
     * Berechnet die Kostenverteilung nach Ausgabenkategorien.
     *
     * Die Daten werden später im Doughnut-Diagramm angezeigt.
     */
    private void berechneKostenverteilung(
            LocalDate startDatum,
            LocalDate endDatum,
            Long immobilieId
    ) {
        Map<String, BigDecimal> kostenverteilung =
                ausgabeService.berechneKostenverteilungImZeitraum(
                        startDatum,
                        endDatum,
                        immobilieId
                );

        // Kategorienamen als Labels speichern.
        this.kostenverteilungLabels =
                kostenverteilung.keySet().toArray(new String[0]);

        // Beträge in double-Werte für Chart.js umwandeln.
        this.kostenverteilungDaten =
                kostenverteilung.values()
                        .stream()
                        .mapToDouble(BigDecimal::doubleValue)
                        .toArray();
    }

    /**
     * Prüft, ob Kostenverteilungsdaten vorhanden sind.
     *
     * Die Kostenverteilung wird nur angezeigt, wenn mindestens ein Wert größer als 0 ist.
     */
    private boolean hatKostenverteilungDaten() {
        if (kostenverteilungDaten == null || kostenverteilungDaten.length == 0) {
            return false;
        }

        double summe = 0;

        for (double wert : kostenverteilungDaten) {
            summe += wert;
        }

        return summe > 0;
    }

    /**
     * Erstellt den Hero-Bereich des Finanz-Dashboards.
     *
     * Enthält:
     * - Eyebrow-Text
     * - Titel
     * - Untertitel
     * - Buttons für neue Buchung und Buchungsübersicht
     */
    private Component createHeroSection() {
        Div hero = new Div();
        hero.addClassName("finance-hero");

        Div content = new Div();
        content.addClassName("finance-hero-content");

        Span eyebrow = new Span("Finance Control Center");
        eyebrow.addClassName("finance-eyebrow");

        H2 title = new H2("Finanzen klar im Blick");
        title.addClassName("finance-hero-title");

        Paragraph subtitle = new Paragraph(
                "Analysiere Einnahmen, Ausgaben, Rückstände und Cashflow für dein Portfolio."
        );
        subtitle.addClassName("finance-hero-subtitle");

        Div actions = new Div();
        actions.addClassName("finance-hero-actions");

        // Navigiert zur Seite zum Erstellen einer neuen Buchung.
        Button addBooking = primaryButton("Neue Buchung", VaadinIcon.EURO);
        addBooking.addClickListener(e ->
                getUI().ifPresent(ui -> ui.navigate("finanzen/buchung-neu"))
        );

        // Navigiert zur Übersicht aller Buchungen.
        Button showBookings = secondaryButton("Alle Buchungen anzeigen", VaadinIcon.LIST);
        showBookings.addClickListener(e ->
                getUI().ifPresent(ui -> ui.navigate("finanzen/buchungen"))
        );

        actions.add(addBooking, showBookings);
        content.add(eyebrow, title, subtitle, actions);

        hero.add(content);

        return hero;
    }

    /**
     * Erstellt die Filterleiste.
     *
     * Enthaltene Filter:
     * - Zeitraum: 1M, 3M, 6M, YTD
     * - Immobilie
     * - Mieteinheit
     * - Mieter
     */
    private Component createFilterBar() {
        Div filterBar = new Div();
        filterBar.addClassName("finance-filter-bar");

        Div left = new Div();
        left.addClassName("finance-filter-left");

        // Zeitraum-Buttons
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

        // Einheitliches Styling für Filterbuttons.
        filterButtons.forEach(btn ->
                btn.addClassName("secondary-button")
        );

        // Markiert den aktuell aktiven Zeitraumfilter.
        markiereAktivenFilter(oneMonth, threeMonths, sixMonths, ytd);

        // Klicklistener für Zeitraumwechsel.
        oneMonth.addClickListener(e -> wechselZeitraum(ZeitraumFilter.EIN_MONAT));
        threeMonths.addClickListener(e -> wechselZeitraum(ZeitraumFilter.DREI_MONATE));
        sixMonths.addClickListener(e -> wechselZeitraum(ZeitraumFilter.SECHS_MONATE));
        ytd.addClickListener(e -> wechselZeitraum(ZeitraumFilter.YTD));

        // Optionen für Immobilienfilter vorbereiten.
        List<FilterOption<Immobilie>> immobilienOptionen = new ArrayList<>();

        // Erste Option: alle Immobilien.
        immobilienOptionen.add(
                new FilterOption<>(null, "Alle Immobilien", null)
        );

        // Alle Immobilien aus dem Service laden und als Filteroption hinzufügen.
        immobilieService.findeAlleImmobilien().forEach(immobilie ->
                immobilienOptionen.add(
                        new FilterOption<>(
                                immobilie.getId(),
                                immobilie.getBezeichnung(),
                                immobilie
                        )
                )
        );

        // Optionen für Mieteinheiten vorbereiten.
        List<FilterOption<Mieteinheit>> einheitenOptionen = new ArrayList<>();

        // Erste Option: alle Einheiten.
        einheitenOptionen.add(
                new FilterOption<>(null, "Alle Einheiten", null)
        );

        // Mieteinheiten laden.
        // Wenn eine Immobilie ausgewählt ist, werden nur deren Einheiten angezeigt.
        mieteinheitService.findeAlleMieteinheiten()
                .stream()
                .filter(mieteinheit ->
                        ausgewaehlteImmobilieId == null
                                || (
                                mieteinheit.getImmobilie() != null
                                        && ausgewaehlteImmobilieId.equals(
                                        mieteinheit.getImmobilie().getId()
                                )
                        )
                )
                .forEach(mieteinheit ->
                        einheitenOptionen.add(
                                new FilterOption<>(
                                        mieteinheit.getId(),
                                        mieteinheit.getBezeichnung(),
                                        mieteinheit
                                )
                        )
                );

        // Optionen für Mieter vorbereiten.
        List<FilterOption<Mieter>> mieterOptionen = new ArrayList<>();

        // Erste Option: alle Mieter.
        mieterOptionen.add(
                new FilterOption<>(null, "Alle Mieter", null)
        );

        // Mieter werden aus den Mietverträgen ermittelt.
        mietvertragService.findeAlleMietvertraege()
                .stream()

                // Nur Verträge mit Mieter verwenden.
                .filter(mietvertrag -> mietvertrag.getMieter() != null)

                // Nur Verträge mit Mieteinheit verwenden.
                .filter(mietvertrag -> mietvertrag.getMieteinheit() != null)

                // Filterlogik abhängig von ausgewählter Immobilie oder Mieteinheit.
                .filter(mietvertrag -> {
                    Mieteinheit mieteinheit = mietvertrag.getMieteinheit();

                    // Wenn eine Mieteinheit ausgewählt ist, nur deren Mieter anzeigen.
                    if (ausgewaehlteMieteinheitId != null) {
                        return ausgewaehlteMieteinheitId.equals(
                                mieteinheit.getId()
                        );
                    }

                    // Wenn eine Immobilie ausgewählt ist, nur Mieter dieser Immobilie anzeigen.
                    if (ausgewaehlteImmobilieId != null) {
                        return mieteinheit.getImmobilie() != null
                                && ausgewaehlteImmobilieId.equals(
                                mieteinheit.getImmobilie().getId()
                        );
                    }

                    // Ohne Filter werden alle Mieter angezeigt.
                    return true;
                })

                // Aus Mietverträgen die Mieter extrahieren.
                .map(Mietvertrag::getMieter)
                .filter(Objects::nonNull)

                // Doppelte Mieter vermeiden.
                .distinct()

                // Mieter als Filteroption hinzufügen.
                .forEach(mieter ->
                        mieterOptionen.add(
                                new FilterOption<>(
                                        mieter.getId(),
                                        mieter.getVorname()
                                                + " "
                                                + mieter.getNachname(),
                                        mieter
                                )
                        )
                );

        // ComboBox für Immobilien erstellen.
        immobilieFilter = new ComboBox<>();
        immobilieFilter.setItems(immobilienOptionen);
        immobilieFilter.setItemLabelGenerator(FilterOption::label);
        immobilieFilter.addClassName("dashboard-filter-combo");

        // Aktuell ausgewählte Immobilie setzen.
        immobilieFilter.setValue(
                findeOptionNachId(
                        immobilienOptionen,
                        ausgewaehlteImmobilieId
                )
        );

        // ComboBox für Mieteinheiten erstellen.
        einheitFilter = new ComboBox<>();
        einheitFilter.setItems(einheitenOptionen);
        einheitFilter.setItemLabelGenerator(FilterOption::label);
        einheitFilter.addClassName("dashboard-filter-combo");

        // Aktuell ausgewählte Mieteinheit setzen.
        einheitFilter.setValue(
                findeOptionNachId(
                        einheitenOptionen,
                        ausgewaehlteMieteinheitId
                )
        );

        // ComboBox für Mieter erstellen.
        mieterFilter = new ComboBox<>();
        mieterFilter.setItems(mieterOptionen);
        mieterFilter.setItemLabelGenerator(FilterOption::label);
        mieterFilter.addClassName("dashboard-filter-combo");

        // Aktuell ausgewählten Mieter setzen.
        mieterFilter.setValue(
                findeOptionNachId(
                        mieterOptionen,
                        ausgewaehlterMieterId
                )
        );

        // Reaktion auf Änderung des Immobilienfilters.
        immobilieFilter.addValueChangeListener(event -> {
            FilterOption<Immobilie> option = event.getValue();

            // Ausgewählte Immobilie speichern oder zurücksetzen.
            ausgewaehlteImmobilieId =
                    option == null || option.isAll()
                            ? null
                            : option.id();

            // Bei Änderung der Immobilie werden Einheit und Mieter zurückgesetzt.
            ausgewaehlteMieteinheitId = null;
            ausgewaehlterMieterId = null;

            // Seite mit neuen Filtern neu aufbauen.
            baueSeiteNeu();
        });

        // Reaktion auf Änderung des Mieteinheitenfilters.
        einheitFilter.addValueChangeListener(event -> {
            FilterOption<Mieteinheit> option = event.getValue();

            // Wenn "Alle Einheiten" gewählt ist, Einheit und Mieter zurücksetzen.
            if (option == null || option.isAll()) {
                ausgewaehlteMieteinheitId = null;
                ausgewaehlterMieterId = null;
            } else {
                Mieteinheit mieteinheit = option.value();

                // Ausgewählte Mieteinheit speichern.
                ausgewaehlteMieteinheitId = mieteinheit.getId();

                // Falls die Mieteinheit zu einer Immobilie gehört,
                // wird diese Immobilie automatisch ebenfalls gesetzt.
                if (mieteinheit.getImmobilie() != null) {
                    ausgewaehlteImmobilieId =
                            mieteinheit.getImmobilie().getId();
                }

                // Mieter wird zurückgesetzt, da die Einheit geändert wurde.
                ausgewaehlterMieterId = null;
            }

            // Seite neu laden.
            baueSeiteNeu();
        });

        // Reaktion auf Änderung des Mieterfilters.
        mieterFilter.addValueChangeListener(event -> {
            FilterOption<Mieter> option = event.getValue();

            // Ausgewählten Mieter setzen oder zurücksetzen.
            ausgewaehlterMieterId =
                    option == null || option.isAll()
                            ? null
                            : option.id();

            // Seite neu laden.
            baueSeiteNeu();
        });

        // Alle Filterelemente in die linke Filterleiste einfügen.
        left.add(
                oneMonth,
                threeMonths,
                sixMonths,
                ytd,
                immobilieFilter,
                einheitFilter,
                mieterFilter
        );

        filterBar.setWidthFull();
        filterBar.add(left);

        return filterBar;
    }

    /**
     * Erstellt einen primären Button.
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
     * Sucht in einer Liste von Filteroptionen die Option mit der passenden ID.
     *
     * Wird verwendet, um nach dem Neuaufbau der Seite die aktuell gewählte
     * ComboBox-Auswahl wiederherzustellen.
     */
    private <T> FilterOption<T> findeOptionNachId(
            List<FilterOption<T>> optionen,
            Long id
    ) {
        return optionen.stream()
                .filter(option -> id == null
                        ? option.id() == null
                        : id.equals(option.id()))
                .findFirst()

                // Falls keine passende Option gefunden wird,
                // wird die erste Option genutzt, meistens "Alle".
                .orElse(optionen.getFirst());
    }

    /**
     * Wechselt den Zeitraumfilter und baut die Seite neu auf.
     */
    private void wechselZeitraum(ZeitraumFilter filter) {
        this.aktuellerFilter = filter;
        baueSeiteNeu();
    }

    /**
     * Markiert den aktuell aktiven Zeitraumbutton optisch.
     */
    private void markiereAktivenFilter(
            Button oneMonth,
            Button threeMonths,
            Button sixMonths,
            Button ytd
    ) {
        switch (aktuellerFilter) {
            case EIN_MONAT -> oneMonth.addClassName("finance-filter-active");
            case DREI_MONATE -> threeMonths.addClassName("finance-filter-active");
            case SECHS_MONATE -> sixMonths.addClassName("finance-filter-active");
            case YTD -> ytd.addClassName("finance-filter-active");
        }
    }

    /**
     * Erstellt die KPI-Karten mit den wichtigsten Finanzkennzahlen.
     *
     * Angezeigt werden:
     * - Summe Einnahmen
     * - Summe Ausgaben
     * - Rückstände
     * - Cashflow
     */
    private Component createKpiGrid() {
        Div grid = new Div();
        grid.addClassName("dashboard-kpi-grid");

        grid.add(
                // KPI für bezahlte Einnahmen.
                kpiCard(
                        "Summe Einnahmen",
                        UiFormatUtils.formatiereBetrag(summeEinnahmen),
                        "bezahlt",
                        getZeitraumText(),
                        VaadinIcon.TRENDING_UP,
                        "success"
                ),

                // KPI für bezahlte Ausgaben.
                kpiCard(
                        "Summe Ausgaben",
                        UiFormatUtils.formatiereBetrag(summeAusgaben),
                        "bezahlt",
                        getZeitraumText(),
                        VaadinIcon.TRENDING_DOWN,
                        "danger"
                ),

                // KPI für offene Zahlungseingänge.
                kpiCard(
                        "Rückstände",
                        UiFormatUtils.formatiereBetrag(rueckstaende),
                        "offen",
                        "Offene Zahlungseingänge",
                        VaadinIcon.REFRESH,
                        "warning"
                ),

                // KPI für Cashflow.
                kpiCard(
                        "Cashflow",
                        UiFormatUtils.formatiereBetrag(cashflow),
                        cashflow.signum() >= 0 ? "positiv" : "negativ",
                        "Einnahmen minus Ausgaben",
                        VaadinIcon.WALLET,
                        cashflow.signum() >= 0 ? "primary" : "danger"
                )
        );

        return grid;
    }

    /**
     * Gibt den passenden Text für den aktuell ausgewählten Zeitraum zurück.
     */
    private String getZeitraumText() {
        return switch (aktuellerFilter) {
            case EIN_MONAT -> "Aktueller Monat";
            case DREI_MONATE -> "Letzte 3 Monate";
            case SECHS_MONATE -> "Letzte 6 Monate";
            case YTD -> "Seit Jahresbeginn";
        };
    }

    /**
     * Erstellt eine einzelne KPI-Card.
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

        Div header = new Div();
        header.addClassName("kpi-card-header");

        // Icon-Box der KPI.
        Div iconBox = new Div(new Icon(icon));
        iconBox.addClassNames("kpi-icon-box", color);

        // Badge, z. B. "bezahlt", "offen", "positiv".
        Span badgeSpan = new Span(badge);

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

        // Untertitel oder Zeitraum.
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
     * Baut den Diagrammbereich auf.
     *
     * Links:
     * - Einnahmen- und Ausgabendiagramme
     *
     * Rechts:
     * - Zahlungsstatus
     * - optional Kostenverteilung
     */
    private Component createDashboardGrid() {
        Div grid = new Div();
        grid.addClassName("finance-dashboard-grid");

        // Hauptkarte für Einnahmen- und Ausgabendiagramme.
        Div chartCard = new Div();
        chartCard.addClassNames("card", "finance-split-main-chart-card");

        Div header = new Div();
        header.addClassName("finance-card-header");

        Div titleBox = new Div();

        H3 chartTitle = new H3("Einnahmen und Ausgaben");
        chartTitle.addClassName("card-title");

        Paragraph subtitle = new Paragraph(getZeitraumText());
        subtitle.addClassName("card-subtitle");

        titleBox.add(chartTitle, subtitle);
        header.add(titleBox);

        // Fügt die beiden Liniencharts hinzu.
        chartCard.add(header, createSplitChartStack());

        // Rechte Spalte für kleinere Diagramme.
        Div side = new Div();
        side.addClassName("finance-side-column");

        // Zahlungsstatus immer anzeigen.
        side.add(createPaymentStatusCard());

        // Kostenverteilung nur anzeigen, wenn Daten vorhanden sind.
        Component costDistributionCard = createCostDistributionCard();

        if (costDistributionCard != null) {
            side.add(costDistributionCard);
        }

        grid.add(chartCard, side);

        return grid;
    }

    /**
     * Erstellt einen Stack mit zwei Diagrammblöcken:
     * - Einnahmen
     * - Ausgaben
     */
    private Component createSplitChartStack() {
        Div stack = new Div();
        stack.addClassName("finance-split-chart-stack");

        stack.add(
                createSplitChartBlock(
                        "financeIncomeChart",
                        "Einnahmen",
                        "Bezahlte Zahlungseingänge im Zeitraum",
                        UiFormatUtils.formatiereBetrag(summeEinnahmen),
                        chartEinnahmen,
                        "success",
                        "Einnahmen"
                ),
                createSplitChartBlock(
                        "financeExpenseChart",
                        "Ausgaben",
                        "Bezahlte Ausgaben im Zeitraum",
                        UiFormatUtils.formatiereBetrag(summeAusgaben),
                        chartAusgaben,
                        "danger",
                        "Ausgaben"
                )
        );

        return stack;
    }

    /**
     * Erstellt einen einzelnen Linienchart-Block für Einnahmen oder Ausgaben.
     *
     * Das Diagramm wird mit Chart.js im Browser gerendert.
     */
    private Component createSplitChartBlock(
            String chartId,
            String title,
            String subtitle,
            String value,
            double[] data,
            String colorClass,
            String datasetLabel
    ) {
        Div block = new Div();
        block.addClassNames("finance-split-chart-block", colorClass);

        // Header des Chart-Blocks.
        Div header = new Div();
        header.addClassName("finance-split-chart-header");

        Div textBox = new Div();

        Span titleText = new Span(title);
        titleText.addClassName("finance-split-chart-title");

        Span subtitleText = new Span(subtitle);
        subtitleText.addClassName("finance-split-chart-subtitle");

        textBox.add(titleText, subtitleText);

        // Gesamtwert neben dem Titel anzeigen.
        Span valueText = new Span(value);
        valueText.addClassName("finance-split-chart-value");

        header.add(textBox, valueText);

        // Canvas-Bereich für Chart.js.
        Div canvasWrapper = new Div();
        canvasWrapper.addClassName("finance-split-chart-canvas");

        Element canvas = new Element("canvas");
        canvas.setAttribute("id", chartId);

        canvasWrapper.getElement().appendChild(canvas);

        block.add(header, canvasWrapper);

        // JavaScript zum Rendern des Liniencharts.
        executeJsIfUiAvailable("""
            setTimeout(() => {
                const ctx = document.getElementById($0);

                // Wenn Canvas oder Chart.js nicht verfügbar sind, abbrechen.
                if (!ctx || !window.Chart) return;

                const styles = getComputedStyle(document.documentElement);

                // Farbe abhängig von success/danger setzen.
                const chartColor = styles
                    .getPropertyValue($1 === 'success'
                        ? '--color-success'
                        : '--color-danger')
                    .trim();

                // Füllfarbe abhängig von success/danger setzen.
                const fillColor = styles
                    .getPropertyValue($1 === 'success'
                        ? '--color-success-light'
                        : '--color-danger-light')
                    .trim();

                const gridColor =
                    styles.getPropertyValue('--color-border').trim() || '#e5e7eb';

                // Dynamischer Name für die Chart-Instanz.
                const instanceName = $0 + 'Instance';

                // Vorhandene Chart-Instanz entfernen, damit keine Doppelcharts entstehen.
                if (window[instanceName]) {
                    window[instanceName].destroy();
                }

                // Neues Linienchart erstellen.
                window[instanceName] = new Chart(ctx, {
                    type: 'line',
                    data: {
                        labels: $2,
                        datasets: [
                            {
                                label: $3,
                                data: $4,
                                borderColor: chartColor,
                                backgroundColor: fillColor,
                                pointBackgroundColor: chartColor,
                                pointBorderColor: chartColor,
                                pointRadius: 4,
                                pointHoverRadius: 6,
                                borderWidth: 3,
                                tension: 0.4,
                                fill: true
                            }
                        ]
                    },
                    options: {
                        responsive: true,
                        maintainAspectRatio: false,
                        plugins: {
                            legend: {
                                display: false
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
                                    color: gridColor
                                }
                            }
                        }
                    }
                });
            }, 300);
        """, chartId, colorClass, chartMonate, datasetLabel, data);

        return block;
    }

    /**
     * Erstellt die Card für den Zahlungsstatus.
     *
     * Das Diagramm zeigt das Verhältnis zwischen:
     * - bezahlt
     * - offen
     */
    private Component createPaymentStatusCard() {
        Div card = new Div();
        card.addClassName("card");

        Div header = new Div();
        header.addClassName("finance-card-header");

        H3 title = new H3("Zahlungsstatus");
        title.addClassName("card-title");

        header.add(title);

        // Wrapper für das Kreisdiagramm.
        Div chartWrapper = new Div();
        chartWrapper.getStyle().set("height", "220px");

        Element canvas = new Element("canvas");
        canvas.setAttribute("id", "paymentStatusChart");
        canvas.getStyle().set("width", "100%");
        canvas.getStyle().set("height", "220px");

        chartWrapper.getElement().appendChild(canvas);

        // Chart.js-Kreisdiagramm für bezahlt/offen.
        executeJsIfUiAvailable("""
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
                            }
                        }
                    }
                });
            }, 300);
        """, zahlungsstatusBezahlt, zahlungsstatusOffen);

        // Zusätzliche Prozentanzeige unterhalb des Diagramms.
        Div stats = new Div();
        stats.addClassName("finance-mini-grid");

        stats.add(miniBox("Bezahlt", String.format(Locale.GERMANY, "%.1f %%", zahlungsstatusBezahlt)));
        stats.add(miniBox("Offen", String.format(Locale.GERMANY, "%.1f %%", zahlungsstatusOffen)));

        card.add(header, chartWrapper, stats);

        return card;
    }

    /**
     * Erstellt die Card für die Kostenverteilung.
     *
     * Wird nur angezeigt, wenn Kostenverteilungsdaten vorhanden sind.
     */
    private Component createCostDistributionCard() {
        // Wenn keine Daten vorhanden sind, wird keine Card erzeugt.
        if (!hatKostenverteilungDaten()) {
            return null;
        }

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

        // Doughnut-Diagramm für Ausgabenkategorien.
        executeJsIfUiAvailable("""
            setTimeout(() => {
                const ctx = document.getElementById('costDistributionChart');

                if (!ctx) return;

                new Chart(ctx, {
                    type: 'doughnut',
                    data: {
                        labels: $0,
                        datasets: [{
                            data: $1,
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
        """, kostenverteilungLabels, kostenverteilungDaten);

        card.add(title, wrapper);

        return card;
    }

    /**
     * Erstellt eine kleine Box für Kennzahlen unter Diagrammen.
     *
     * Beispiel:
     * - Bezahlt: 80 %
     * - Offen: 20 %
     */
    private Component miniBox(String label, String value) {
        Div box = new Div();
        box.addClassName("finance-mini-box");

        Span labelSpan = new Span(label);
        labelSpan.addClassName("finance-mini-label");

        Span valueStrong = new Span(value);

        box.add(labelSpan, valueStrong);

        return box;
    }

    /**
     * Erstellt den Tabellenbereich.
     *
     * Enthält:
     * - Tabelle für letzte Einnahmen
     * - Tabelle für letzte Ausgaben
     */
    private Component createTableGrid() {
        Div grid = new Div();
        grid.addClassName("finance-table-grid");

        grid.add(
                transactionTable(
                        "Letzte Einnahmen",
                        "Mieten & Nebenkosten",
                        letzteEinnahmenRows
                ),
                transactionTable(
                        "Letzte Ausgaben",
                        "Instandhaltung & Verwaltung",
                        letzteAusgabenRows
                )
        );

        return grid;
    }

    /**
     * Erstellt eine Buchungstabelle.
     *
     * Wird sowohl für Einnahmen als auch für Ausgaben genutzt.
     */
    private Component transactionTable(
            String title,
            String subtitle,
            List<BuchungTabellenZeile> rows
    ) {
        Div card = new Div();
        card.addClassNames("card", "finance-transaction-card");

        Div titleBox = new Div();
        titleBox.addClassName("finance-table-title-box");

        H3 titleText = new H3(title);
        titleText.addClassName("card-title");

        Paragraph subtitleText = new Paragraph(subtitle);
        subtitleText.addClassName("card-subtitle");

        titleBox.add(titleText, subtitleText);

        Div table = new Div();
        table.addClassName("finance-table");

        // Tabellenkopf hinzufügen.
        table.add(tableHeader());

        // Wenn keine Daten vorhanden sind, wird eine leere Tabellenzeile angezeigt.
        if (rows == null || rows.isEmpty()) {
            table.add(emptyTableRow());
        } else {
            // Ansonsten werden alle Tabellenzeilen gerendert.
            for (BuchungTabellenZeile row : rows) {
                table.add(tableRow(row));
            }
        }

        card.add(titleBox, table);
        return card;
    }

    /**
     * Erstellt den Tabellenkopf.
     */
    private Component tableHeader() {
        Div row = new Div();
        row.addClassNames("finance-table-row", "finance-table-head");

        row.add(
                new Span("Zeitraum"),
                new Span("Mieter / Objekt"),
                new Span("Kategorie"),
                new Span("Status"),
                new Span("Betrag")
        );

        return row;
    }

    /**
     * Erstellt eine Platzhalterzeile, wenn keine Tabellenwerte vorhanden sind.
     */
    private Component emptyTableRow() {
        Div row = new Div();
        row.addClassName("finance-table-row");

        Span status = new Span("-");
        status.addClassNames("status-badge", "warning");

        row.add(
                new Span(getZeitraumText()),
                new Span("-"),
                new Span("Keine Daten"),
                status,
                new Span(UiFormatUtils.formatiereBetrag(BigDecimal.ZERO))
        );

        return row;
    }

    /**
     * Erstellt eine klickbare Tabellenzeile.
     *
     * Beim Klick wird zur passenden Buchungsdetailseite navigiert:
     * - Einnahme: /finanzen/buchungen/einnahme/{id}
     * - Ausgabe: /finanzen/buchungen/ausgabe/{id}
     */
    private Component tableRow(BuchungTabellenZeile data) {
        Div row = new Div();
        row.addClassNames("finance-table-row", "clickable-table-row");

        // Status-Badge farblich markieren.
        Span status = new Span(data.status());
        status.addClassNames(
                "status-badge",
                data.status().equals("Bezahlt / Erledigt")
                        ? "success"
                        : "warning"
        );

        // Betrag abhängig vom Typ positiv oder negativ stylen.
        Span betrag = new Span(data.betrag());
        betrag.addClassName(
                data.typ() == BuchungTyp.EINNAHME
                        ? "amount-positive"
                        : "amount-negative"
        );

        row.add(
                new Span(data.datum()),
                new Span(data.objekt()),
                new Span(data.kategorie()),
                status,
                betrag
        );

        // Klick auf Zeile öffnet Detailseite der Buchung.
        row.addClickListener(event ->
                getUI().ifPresent(ui ->
                        ui.navigate(
                                "finanzen/buchungen/"
                                        + data.typ().getRouteValue()
                                        + "/"
                                        + data.id()
                        )
                )
        );

        return row;
    }

    /**
     * Berechnet die Einnahmendaten für das Diagramm.
     *
     * Für jeden Monat im ausgewählten Zeitraum wird die Summe der bezahlten
     * Zahlungseingänge berechnet.
     */
    private double[] berechneEinnahmenChartDaten(
            LocalDate startDatum,
            LocalDate endDatum,
            Long immobilieId,
            Long mieteinheitId,
            Long mieterId
    ) {
        // Alle Monate im gewählten Zeitraum ermitteln.
        List<YearMonth> monate = ermittleMonateImZeitraum(startDatum, endDatum);

        // Array für Chartwerte vorbereiten.
        double[] daten = new double[monate.size()];

        for (int i = 0; i < monate.size(); i++) {
            YearMonth monat = monate.get(i);

            // Summe der bezahlten Zahlungseingänge für den jeweiligen Monat.
            BigDecimal summe =
                    zahlungsEingangService.berechneBezahlteZahlungseingaengeImZeitraum(
                            monat.atDay(1),
                            monat.atEndOfMonth(),
                            immobilieId,
                            mieteinheitId,
                            mieterId
                    );

            // Wert für Chart.js speichern.
            daten[i] = summe.doubleValue();
        }

        return daten;
    }

    /**
     * Berechnet die Ausgabendaten für das Diagramm.
     *
     * Für jeden Monat im ausgewählten Zeitraum wird die Summe der bezahlten
     * Ausgaben berechnet.
     */
    private double[] berechneAusgabenChartDaten(
            LocalDate startDatum,
            LocalDate endDatum,
            Long immobilieId
    ) {
        // Alle Monate im Zeitraum ermitteln.
        List<YearMonth> monate = ermittleMonateImZeitraum(startDatum, endDatum);

        // Array für Chartwerte vorbereiten.
        double[] daten = new double[monate.size()];

        for (int i = 0; i < monate.size(); i++) {
            YearMonth monat = monate.get(i);

            // Summe der bezahlten Ausgaben für den jeweiligen Monat.
            BigDecimal summe =
                    ausgabeService.berechneBezahlteAusgabenImZeitraum(
                            monat.atDay(1),
                            monat.atEndOfMonth(),
                            immobilieId
                    );

            // Wert für Chart.js speichern.
            daten[i] = summe.doubleValue();
        }

        return daten;
    }

    /**
     * Erstellt die Monatslabels für die Diagramme.
     */
    private String[] berechneChartMonate(
            LocalDate startDatum,
            LocalDate endDatum
    ) {
        // Monate im Zeitraum bestimmen.
        List<YearMonth> monate = ermittleMonateImZeitraum(startDatum, endDatum);

        // Deutsches Monatsformat verwenden.
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("MMM", Locale.GERMANY);

        String[] labels = new String[monate.size()];

        // Monatsnamen formatieren.
        for (int i = 0; i < monate.size(); i++) {
            labels[i] = monate.get(i).format(formatter);
        }

        return labels;
    }

    /**
     * Ermittelt alle Monate, die im ausgewählten Zeitraum angezeigt werden sollen.
     *
     * Beispiel:
     * Start: Januar
     * Ende: März
     * Ergebnis: Januar, Februar, März
     */
    private List<YearMonth> ermittleMonateImZeitraum(
            LocalDate startDatum,
            LocalDate endDatum
    ) {
        List<YearMonth> monate = new ArrayList<>();

        YearMonth start = YearMonth.from(startDatum);
        YearMonth ende = YearMonth.from(endDatum);

        YearMonth aktuell = start;

        // Solange der aktuelle Monat nicht nach dem Endmonat liegt,
        // wird er zur Liste hinzugefügt.
        while (!aktuell.isAfter(ende)) {
            monate.add(aktuell);
            aktuell = aktuell.plusMonths(1);
        }

        return monate;
    }

    /**
     * Seitentitel für den Header.
     */
    @Override
    public String getPageTitle() {
        return "Finanz-Dashboard";
    }

    /**
     * Seitenuntertitel für den Header.
     */
    @Override
    public String getPageSubtitle() {
        return "Übersicht über Einnahmen, Ausgaben und Cashflow";
    }
}
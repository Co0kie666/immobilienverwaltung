package de.hsbi.immobilienverwaltung.ui.finanzen;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.Route;
import de.hsbi.immobilienverwaltung.domain.*;
import de.hsbi.immobilienverwaltung.service.interfaces.*;
import de.hsbi.immobilienverwaltung.ui.layout.HasPageHeader;
import de.hsbi.immobilienverwaltung.ui.layout.MainLayout;
import jakarta.annotation.security.PermitAll;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Route(value = "finanzen", layout = MainLayout.class)
@PermitAll
public class FinanzDashboardView extends Div implements HasPageHeader {

    private enum ZeitraumFilter {
        EIN_MONAT,
        DREI_MONATE,
        SECHS_MONATE,
        YTD
    }

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

    private record FilterOption<T>(
            Long id,
            String label,
            T value
    ) {
        boolean isAll() {
            return id == null;
        }
    }

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

    private final ZahlungsEingangService zahlungsEingangService;
    private final AusgabeService ausgabeService;

    private ZeitraumFilter aktuellerFilter = ZeitraumFilter.EIN_MONAT;

    private BigDecimal summeEinnahmen;
    private BigDecimal summeAusgaben;
    private BigDecimal rueckstaende;
    private BigDecimal cashflow;

    private double[] chartEinnahmen;
    private double[] chartAusgaben;
    private String[] chartMonate;

    private String[] kostenverteilungLabels;
    private double[] kostenverteilungDaten;

    private double zahlungsstatusBezahlt;
    private double zahlungsstatusOffen;

    private List<BuchungTabellenZeile> letzteEinnahmenRows;
    private List<BuchungTabellenZeile> letzteAusgabenRows;

    private final ImmobilieService immobilieService;
    private final MieteinheitService mieteinheitService;
    private final MieterService mieterService;
    private final MietvertragService mietvertragService;

    private ComboBox<FilterOption<Immobilie>> immobilieFilter;
    private ComboBox<FilterOption<Mieteinheit>> einheitFilter;
    private ComboBox<FilterOption<Mieter>> mieterFilter;

    private Long ausgewaehlteImmobilieId;
    private Long ausgewaehlteMieteinheitId;
    private Long ausgewaehlterMieterId;

    public FinanzDashboardView(
            ZahlungsEingangService zahlungsEingangService,
            AusgabeService ausgabeService,
            ImmobilieService immobilieService,
            MieteinheitService mieteinheitService,
            MieterService mieterService,
            MietvertragService mietvertragService
    ) {
        this.immobilieService = immobilieService;
        this.mieteinheitService = mieteinheitService;
        this.mieterService = mieterService;
        this.zahlungsEingangService = zahlungsEingangService;
        this.ausgabeService = ausgabeService;
        this.mietvertragService = mietvertragService;

        addJavaScriptIfUiAvailable(
                "https://cdn.jsdelivr.net/npm/chart.js"
        );

        addClassNames("finance-page", "finance-page-modern");

        ladeFinanzdaten(null, null, null);
        baueSeiteNeu();
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

    private void baueSeiteNeu() {
        removeAll();

        ladeFinanzdaten(
                ausgewaehlteImmobilieId,
                ausgewaehlteMieteinheitId,
                ausgewaehlterMieterId
        );

        add(createFinanceHero());
        add(createFilterBar());
        add(createKpiGrid());
        add(createDashboardGrid());
        add(createTableGrid());
    }

    private void ladeFinanzdaten(
            Long immobilieId,
            Long mieteinheitId,
            Long mieterId
    ) {
        LocalDate startDatum = ermittleStartDatum();
        LocalDate endDatum = LocalDate.now();

        this.summeEinnahmen =
                zahlungsEingangService.berechneBezahlteZahlungseingaengeImZeitraum(
                        startDatum,
                        endDatum,
                        immobilieId,
                        mieteinheitId,
                        mieterId
                );

        this.summeAusgaben =
                ausgabeService.berechneBezahlteAusgabenImZeitraum(
                        startDatum,
                        endDatum,
                        immobilieId
                );

        this.rueckstaende =
                zahlungsEingangService.berechneOffeneZahlungseingaengeImZeitraum(
                        startDatum,
                        endDatum,
                        immobilieId,
                        mieteinheitId,
                        mieterId
                );

        this.cashflow =
                this.summeEinnahmen.subtract(this.summeAusgaben);

        this.chartEinnahmen =
                berechneEinnahmenChartDaten(
                        startDatum,
                        endDatum,
                        immobilieId,
                        mieteinheitId,
                        mieterId
                );

        this.chartAusgaben =
                berechneAusgabenChartDaten(
                        startDatum,
                        endDatum,
                        immobilieId
                );

        this.chartMonate =
                berechneChartMonate(startDatum, endDatum);

        this.letzteEinnahmenRows =
                berechneLetzteEinnahmenTabellenZeilen(
                        startDatum,
                        endDatum,
                        immobilieId,
                        mieteinheitId,
                        mieterId
                );

        this.letzteAusgabenRows =
                berechneLetzteAusgabenTabellenZeilen(
                        startDatum,
                        endDatum,
                        immobilieId
                );

        berechneKostenverteilung(
                startDatum,
                endDatum,
                immobilieId
        );

        berechneZahlungsstatus();
    }

    private List<BuchungTabellenZeile> berechneLetzteEinnahmenTabellenZeilen(
            LocalDate startDatum,
            LocalDate endDatum,
            Long immobilieId,
            Long mieteinheitId,
            Long mieterId
    ) {
        List<Zahlungseingang> zahlungseingaenge =
                zahlungsEingangService.findeZahlungseingaengeImZeitraum(
                        startDatum,
                        endDatum,
                        immobilieId,
                        mieteinheitId,
                        mieterId
                );

        return zahlungseingaenge.stream()
                .filter(z -> z.getZahlungsdatum() != null)
                .sorted((z1, z2) -> z2.getZahlungsdatum().compareTo(z1.getZahlungsdatum()))
                .limit(5)
                .map(zahlung -> new BuchungTabellenZeile(
                        zahlung.getId(),
                        BuchungTyp.EINNAHME,
                        formatiereDatum(zahlung.getZahlungsdatum()),
                        ermittleZahlungObjektText(zahlung),
                        zahlung.getTyp() == null
                                ? "Einnahme"
                                : zahlung.getTyp().getLabel(),
                        zahlung.getStatus() == null
                                ? "-"
                                : zahlung.getStatus(),
                        formatEuro(zahlung.getBetrag())
                ))
                .toList();
    }

    private String ermittleZahlungObjektText(Zahlungseingang zahlung) {
        if (zahlung.getMietvertrag() == null) {
            return "-";
        }

        if (zahlung.getMietvertrag().getMieter() != null) {
            return zahlung.getMietvertrag()
                    .getMieter()
                    .getVorname()
                    + " "
                    + zahlung.getMietvertrag()
                    .getMieter()
                    .getNachname();
        }

        if (zahlung.getMietvertrag().getMieteinheit() != null) {
            return zahlung.getMietvertrag()
                    .getMieteinheit()
                    .getBezeichnung();
        }

        return "-";
    }

    private List<BuchungTabellenZeile> berechneLetzteAusgabenTabellenZeilen(
            LocalDate startDatum,
            LocalDate endDatum,
            Long immobilieId
    ) {
        List<Ausgabe> ausgaben =
                ausgabeService.findeAusgabenImZeitraum(
                        startDatum,
                        endDatum,
                        immobilieId
                );

        return ausgaben.stream()
                .filter(a -> a.getDatum() != null)
                .sorted((a1, a2) -> a2.getDatum().compareTo(a1.getDatum()))
                .limit(5)
                .map(ausgabe -> new BuchungTabellenZeile(
                        ausgabe.getId(),
                        BuchungTyp.AUSGABE,
                        formatiereDatum(ausgabe.getDatum()),
                        ermittleAusgabeObjektText(ausgabe),
                        ausgabe.getKategorie() == null
                                ? "-"
                                : ausgabe.getKategorie().getLabel(),
                        ausgabe.getStatus() == null
                                ? "-"
                                : ausgabe.getStatus(),
                        "- " + formatEuro(ausgabe.getBetrag())
                ))
                .toList();
    }

    private String ermittleAusgabeObjektText(Ausgabe ausgabe) {
        if (ausgabe.getMieteinheit() != null) {
            return ausgabe.getMieteinheit().getBezeichnung();
        }

        if (ausgabe.getImmobilie() != null) {
            return ausgabe.getImmobilie().getBezeichnung();
        }

        return "-";
    }

    private String formatiereDatum(LocalDate datum) {
        if (datum == null) {
            return "-";
        }

        return datum.format(
                DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.GERMANY)
        );
    }

    private LocalDate ermittleStartDatum() {
        LocalDate heute = LocalDate.now();

        return switch (aktuellerFilter) {
            case EIN_MONAT -> heute.withDayOfMonth(1);
            case DREI_MONATE -> heute.minusMonths(2).withDayOfMonth(1);
            case SECHS_MONATE -> heute.minusMonths(5).withDayOfMonth(1);
            case YTD -> heute.withDayOfYear(1);
        };
    }

    private void berechneZahlungsstatus() {
        BigDecimal bezahlt = summeEinnahmen == null ? BigDecimal.ZERO : summeEinnahmen;
        BigDecimal offen = rueckstaende == null ? BigDecimal.ZERO : rueckstaende;
        BigDecimal gesamt = bezahlt.add(offen);

        if (gesamt.compareTo(BigDecimal.ZERO) == 0) {
            this.zahlungsstatusBezahlt = 0;
            this.zahlungsstatusOffen = 0;
            return;
        }

        this.zahlungsstatusBezahlt =
                bezahlt.multiply(BigDecimal.valueOf(100))
                        .divide(gesamt, 2, RoundingMode.HALF_UP)
                        .doubleValue();

        this.zahlungsstatusOffen =
                offen.multiply(BigDecimal.valueOf(100))
                        .divide(gesamt, 2, RoundingMode.HALF_UP)
                        .doubleValue();
    }

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

        this.kostenverteilungLabels =
                kostenverteilung.keySet().toArray(new String[0]);

        this.kostenverteilungDaten =
                kostenverteilung.values()
                        .stream()
                        .mapToDouble(BigDecimal::doubleValue)
                        .toArray();
    }

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

    private Component createFinanceHero() {
        Div hero = new Div();
        hero.addClassName("finance-hero");

        Div content = new Div();
        content.addClassName("finance-hero-content");

        Span eyebrow = new Span("Finance Cockpit");
        eyebrow.addClassName("finance-eyebrow");

        H2 title = new H2("Cashflow, Kosten und Rückstände im Blick");
        title.addClassName("finance-hero-title");

        Paragraph subtitle = new Paragraph(
                "Filtere nach Zeitraum, Immobilie, Einheit oder Mieter und erkenne sofort, wie sich dein Portfolio finanziell entwickelt."
        );
        subtitle.addClassName("finance-hero-subtitle");

        Div actions = new Div();
        actions.addClassName("finance-hero-actions");

        Button addBooking = new Button("Buchung anlegen", new Icon(VaadinIcon.PLUS));
        addBooking.addClassName("primary-button");
        addBooking.addClickListener(e ->
                UI.getCurrent().navigate("finanzen/buchung-neu")
        );

        Button showBookings = new Button("Alle Buchungen", new Icon(VaadinIcon.LIST));
        showBookings.addClassName("secondary-button");
        showBookings.addClickListener(e ->
                UI.getCurrent().navigate("finanzen/buchungen")
        );

        actions.add(addBooking, showBookings);
        content.add(eyebrow, title, subtitle, actions);

        Div visual = new Div();
        visual.addClassName("finance-hero-visual");

        Div glowCard = new Div();
        glowCard.addClassName("finance-hero-card");

        Span cardLabel = new Span("Cashflow");
        cardLabel.addClassName("finance-hero-card-label");

        H2 cardValue = new H2(formatEuro(cashflow));
        cardValue.addClassName("finance-hero-card-value");

        Span cardMeta = new Span(getZeitraumText());
        cardMeta.addClassName("finance-hero-card-meta");

        Div sparkline = new Div();
        sparkline.addClassName("finance-sparkline");

        glowCard.add(cardLabel, cardValue, cardMeta, sparkline);

        Div stats = new Div();
        stats.addClassName("finance-hero-stats");
        stats.add(
                financeHeroStat("Einnahmen", formatEuro(summeEinnahmen), "success"),
                financeHeroStat("Ausgaben", formatEuro(summeAusgaben), "danger"),
                financeHeroStat("Rückstände", formatEuro(rueckstaende), "warning")
        );

        visual.add(glowCard, stats);
        hero.add(content, visual);

        return hero;
    }

    private Component financeHeroStat(String label, String value, String type) {
        Div stat = new Div();
        stat.addClassNames("finance-hero-stat", type);

        Span labelText = new Span(label);
        labelText.addClassName("finance-hero-stat-label");

        Span valueText = new Span(value);
        valueText.addClassName("finance-hero-stat-value");

        stat.add(labelText, valueText);
        return stat;
    }

    private Component createFilterBar() {
        Div filterBar = new Div();
        filterBar.addClassNames("finance-filter-bar", "finance-glass-card");

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

        markiereAktivenFilter(oneMonth, threeMonths, sixMonths, ytd);

        oneMonth.addClickListener(e -> wechselZeitraum(ZeitraumFilter.EIN_MONAT));
        threeMonths.addClickListener(e -> wechselZeitraum(ZeitraumFilter.DREI_MONATE));
        sixMonths.addClickListener(e -> wechselZeitraum(ZeitraumFilter.SECHS_MONATE));
        ytd.addClickListener(e -> wechselZeitraum(ZeitraumFilter.YTD));

        List<FilterOption<Immobilie>> immobilienOptionen = new ArrayList<>();

        immobilienOptionen.add(
                new FilterOption<>(null, "Alle Immobilien", null)
        );

        immobilieService.findeAlleImmobilien().forEach(immobilie ->
                immobilienOptionen.add(
                        new FilterOption<>(
                                immobilie.getId(),
                                immobilie.getBezeichnung(),
                                immobilie
                        )
                )
        );

        List<FilterOption<Mieteinheit>> einheitenOptionen = new ArrayList<>();

        einheitenOptionen.add(
                new FilterOption<>(null, "Alle Einheiten", null)
        );

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

        List<FilterOption<Mieter>> mieterOptionen = new ArrayList<>();

        mieterOptionen.add(
                new FilterOption<>(null, "Alle Mieter", null)
        );

        mietvertragService.findeAlleMietvertraege()
                .stream()
                .filter(mietvertrag -> mietvertrag.getMieter() != null)
                .filter(mietvertrag -> mietvertrag.getMieteinheit() != null)
                .filter(mietvertrag -> {
                    Mieteinheit mieteinheit = mietvertrag.getMieteinheit();

                    if (ausgewaehlteMieteinheitId != null) {
                        return ausgewaehlteMieteinheitId.equals(
                                mieteinheit.getId()
                        );
                    }

                    if (ausgewaehlteImmobilieId != null) {
                        return mieteinheit.getImmobilie() != null
                                && ausgewaehlteImmobilieId.equals(
                                mieteinheit.getImmobilie().getId()
                        );
                    }

                    return true;
                })
                .map(Mietvertrag::getMieter)
                .filter(Objects::nonNull)
                .distinct()
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

        immobilieFilter = new ComboBox<>();
        immobilieFilter.setItems(immobilienOptionen);
        immobilieFilter.setItemLabelGenerator(FilterOption::label);
        immobilieFilter.addClassName("dashboard-filter-combo");
        immobilieFilter.setValue(
                findeOptionNachId(
                        immobilienOptionen,
                        ausgewaehlteImmobilieId
                )
        );

        einheitFilter = new ComboBox<>();
        einheitFilter.setItems(einheitenOptionen);
        einheitFilter.setItemLabelGenerator(FilterOption::label);
        einheitFilter.addClassName("dashboard-filter-combo");
        einheitFilter.setValue(
                findeOptionNachId(
                        einheitenOptionen,
                        ausgewaehlteMieteinheitId
                )
        );

        mieterFilter = new ComboBox<>();
        mieterFilter.setItems(mieterOptionen);
        mieterFilter.setItemLabelGenerator(FilterOption::label);
        mieterFilter.addClassName("dashboard-filter-combo");
        mieterFilter.setValue(
                findeOptionNachId(
                        mieterOptionen,
                        ausgewaehlterMieterId
                )
        );

        immobilieFilter.addValueChangeListener(event -> {
            FilterOption<Immobilie> option = event.getValue();

            ausgewaehlteImmobilieId =
                    option == null || option.isAll()
                            ? null
                            : option.id();

            ausgewaehlteMieteinheitId = null;
            ausgewaehlterMieterId = null;

            baueSeiteNeu();
        });

        einheitFilter.addValueChangeListener(event -> {
            FilterOption<Mieteinheit> option = event.getValue();

            if (option == null || option.isAll()) {
                ausgewaehlteMieteinheitId = null;
                ausgewaehlterMieterId = null;
            } else {
                Mieteinheit mieteinheit = option.value();

                ausgewaehlteMieteinheitId = mieteinheit.getId();

                if (mieteinheit.getImmobilie() != null) {
                    ausgewaehlteImmobilieId =
                            mieteinheit.getImmobilie().getId();
                }

                ausgewaehlterMieterId = null;
            }

            baueSeiteNeu();
        });

        mieterFilter.addValueChangeListener(event -> {
            FilterOption<Mieter> option = event.getValue();

            ausgewaehlterMieterId =
                    option == null || option.isAll()
                            ? null
                            : option.id();

            baueSeiteNeu();
        });

        left.add(
                oneMonth,
                threeMonths,
                sixMonths,
                ytd,
                immobilieFilter,
                einheitFilter,
                mieterFilter
        );

        Button addBooking = new Button("Buchung anlegen", new Icon(VaadinIcon.PLUS));
        Button showBookings = new Button("Alle Buchungen anzeigen", new Icon(VaadinIcon.LIST));

        addBooking.addClickListener(e ->
                UI.getCurrent().navigate("finanzen/buchung-neu")
        );

        showBookings.addClickListener(e ->
                UI.getCurrent().navigate("finanzen/buchungen")
        );

        addBooking.addClassName("primary-button");
        showBookings.addClassName("secondary-button");

        HorizontalLayout bookingButtons = new HorizontalLayout(addBooking, showBookings);
        bookingButtons.setSpacing(true);

        filterBar.setWidthFull();
        filterBar.add(left, bookingButtons);

        return filterBar;
    }

    private <T> FilterOption<T> findeOptionNachId(
            List<FilterOption<T>> optionen,
            Long id
    ) {
        return optionen.stream()
                .filter(option -> id == null
                        ? option.id() == null
                        : id.equals(option.id()))
                .findFirst()
                .orElse(optionen.getFirst());
    }

    private void wechselZeitraum(ZeitraumFilter filter) {
        this.aktuellerFilter = filter;
        baueSeiteNeu();
    }

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

    private Component createKpiGrid() {
        Div grid = new Div();
        grid.addClassName("finance-kpi-grid");

        grid.add(
                kpiCard(
                        "Summe Einnahmen",
                        formatEuro(summeEinnahmen),
                        getZeitraumText(),
                        VaadinIcon.TRENDING_UP,
                        "success"
                ),
                kpiCard(
                        "Summe Ausgaben",
                        formatEuro(summeAusgaben),
                        getZeitraumText(),
                        VaadinIcon.TRENDING_DOWN,
                        "danger"
                ),
                kpiCard(
                        "Rückstände",
                        formatEuro(rueckstaende),
                        "Offene Zahlungseingänge",
                        VaadinIcon.REFRESH,
                        "warning"
                ),
                kpiCard(
                        "Cashflow",
                        formatEuro(cashflow),
                        "Einnahmen minus Ausgaben",
                        VaadinIcon.WALLET,
                        cashflow.signum() >= 0 ? "primary" : "danger"
                )
        );

        return grid;
    }

    private String getZeitraumText() {
        return switch (aktuellerFilter) {
            case EIN_MONAT -> "Aktueller Monat";
            case DREI_MONATE -> "Letzte 3 Monate";
            case SECHS_MONATE -> "Letzte 6 Monate";
            case YTD -> "Seit Jahresbeginn";
        };
    }

    private Component kpiCard(
            String title,
            String value,
            String trend,
            VaadinIcon icon,
            String color
    ) {
        Div card = new Div();
        card.addClassNames("finance-kpi-card", color);

        Div top = new Div();
        top.addClassName("finance-kpi-top");

        Div text = new Div();
        text.addClassName("finance-kpi-text");

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
        chartCard.addClassNames("card", "finance-main-chart-card");

        Div header = new Div();
        header.addClassName("finance-card-header");

        Div titleBox = new Div();

        H3 chartTitle = new H3("Einnahmen vs. Ausgaben");
        chartTitle.addClassName("card-title");

        Paragraph subtitle = new Paragraph(getZeitraumText());
        subtitle.addClassName("card-subtitle");

        titleBox.add(chartTitle, subtitle);

        Span badge = new Span(chartMonate.length + " Monate");
        badge.addClassNames("status-badge", "primary");

        header.add(titleBox, badge);
        chartCard.add(header, createLineChart());

        Div side = new Div();
        side.addClassName("finance-side-column");

        side.add(createPaymentStatusCard());

        Component costDistributionCard = createCostDistributionCard();

        if (costDistributionCard != null) {
            side.add(costDistributionCard);
        }

        grid.add(chartCard, side);

        return grid;
    }

    private Component createLineChart() {
        Div wrapper = new Div();
        wrapper.addClassName("finance-chart-wrapper");

        Element canvas = new Element("canvas");
        canvas.setAttribute("id", "financeLineChart");

        wrapper.getElement().appendChild(canvas);

        executeJsIfUiAvailable("""
            setTimeout(() => {
                const ctx = document.getElementById('financeLineChart');

                if (!ctx || !window.Chart) return;

                const styles = getComputedStyle(document.documentElement);
                const success = styles.getPropertyValue('--color-success').trim() || '#10b981';
                const danger = styles.getPropertyValue('--color-danger').trim() || '#ef4444';
                const grid = styles.getPropertyValue('--color-border').trim() || '#e5e7eb';

                if (window.financeLineChartInstance) {
                    window.financeLineChartInstance.destroy();
                }

                window.financeLineChartInstance = new Chart(ctx, {
                    type: 'line',
                    data: {
                        labels: $0,
                        datasets: [
                            {
                                label: 'Einnahmen',
                                data: $1,
                                borderColor: success,
                                backgroundColor: success,
                                tension: 0.4,
                                fill: false,
                                pointRadius: 4,
                                pointHoverRadius: 7
                            },
                            {
                                label: 'Ausgaben',
                                data: $2,
                                borderColor: danger,
                                backgroundColor: danger,
                                tension: 0.4,
                                fill: false,
                                pointRadius: 4,
                                pointHoverRadius: 7
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

    private Component createPaymentStatusCard() {
        Div card = new Div();
        card.addClassNames("card", "finance-insight-card", "payment");

        Div header = new Div();
        header.addClassName("finance-card-header");

        H3 title = new H3("Zahlungsstatus");
        title.addClassName("card-title");

        Span badge = new Span(String.format(Locale.GERMANY, "%.0f %% bezahlt", zahlungsstatusBezahlt));
        badge.addClassNames("status-badge", zahlungsstatusOffen > 0 ? "warning" : "success");

        header.add(title, badge);

        Div chartWrapper = new Div();
        chartWrapper.addClassName("finance-donut-wrapper");

        Element canvas = new Element("canvas");
        canvas.setAttribute("id", "paymentStatusChart");

        chartWrapper.getElement().appendChild(canvas);

        executeJsIfUiAvailable("""
            setTimeout(() => {
                const ctx = document.getElementById('paymentStatusChart');

                if (!ctx || !window.Chart) return;

                const styles = getComputedStyle(document.documentElement);
                const success = styles.getPropertyValue('--color-success').trim() || '#10b981';
                const warning = styles.getPropertyValue('--color-warning').trim() || '#f59e0b';

                if (window.paymentStatusChartInstance) {
                    window.paymentStatusChartInstance.destroy();
                }

                window.paymentStatusChartInstance = new Chart(ctx, {
                    type: 'doughnut',
                    data: {
                        labels: [
                            'Bezahlt',
                            'Offen'
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
                        cutout: '70%',
                        plugins: {
                            legend: {
                                position: 'bottom',
                                labels: {
                                    usePointStyle: true,
                                    boxWidth: 8,
                                    boxHeight: 8
                                }
                            }
                        }
                    }
                });
            }, 300);
        """, zahlungsstatusBezahlt, zahlungsstatusOffen);

        Div stats = new Div();
        stats.addClassName("finance-mini-grid");

        stats.add(miniBox("Bezahlt", String.format(Locale.GERMANY, "%.1f %%", zahlungsstatusBezahlt)));
        stats.add(miniBox("Offen", String.format(Locale.GERMANY, "%.1f %%", zahlungsstatusOffen)));

        card.add(header, chartWrapper, stats);

        return card;
    }

    private Component createCostDistributionCard() {
        if (!hatKostenverteilungDaten()) {
            return null;
        }

        Div card = new Div();
        card.addClassNames("card", "finance-insight-card", "cost");

        Div header = new Div();
        header.addClassName("finance-card-header");

        H3 title = new H3("Kostenverteilung");
        title.addClassName("card-title");

        Span badge = new Span(kostenverteilungLabels.length + " Kategorien");
        badge.addClassNames("status-badge", "primary");

        header.add(title, badge);

        Div wrapper = new Div();
        wrapper.addClassName("finance-donut-wrapper");

        Element canvas = new Element("canvas");
        canvas.setAttribute("id", "costDistributionChart");

        wrapper.getElement().appendChild(canvas);

        executeJsIfUiAvailable("""
            setTimeout(() => {
                const ctx = document.getElementById('costDistributionChart');

                if (!ctx || !window.Chart) return;

                const styles = getComputedStyle(document.documentElement);
                const primary = styles.getPropertyValue('--color-primary').trim() || '#2563eb';
                const success = styles.getPropertyValue('--color-success').trim() || '#10b981';
                const warning = styles.getPropertyValue('--color-warning').trim() || '#f59e0b';
                const danger = styles.getPropertyValue('--color-danger').trim() || '#ef4444';

                if (window.costDistributionChartInstance) {
                    window.costDistributionChartInstance.destroy();
                }

                window.costDistributionChartInstance = new Chart(ctx, {
                    type: 'doughnut',
                    data: {
                        labels: $0,
                        datasets: [{
                            data: $1,
                            backgroundColor: [primary, success, warning, danger, '#6366f1', '#14b8a6'],
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
                            }
                        },
                        cutout: '68%'
                    }
                });
            }, 300);
        """, kostenverteilungLabels, kostenverteilungDaten);

        card.add(header, wrapper);

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

    private Component transactionTable(
            String title,
            String subtitle,
            List<BuchungTabellenZeile> rows
    ) {
        Div card = new Div();
        card.addClassNames("table-card", "finance-transaction-card");

        Div titleBox = new Div();
        titleBox.addClassName("finance-table-title-box");

        H3 titleText = new H3(title);
        titleText.addClassName("card-title");

        Paragraph subtitleText = new Paragraph(subtitle);
        subtitleText.addClassName("card-subtitle");

        titleBox.add(titleText, subtitleText);

        Div table = new Div();
        table.addClassName("finance-table");

        table.add(tableHeader());

        if (rows == null || rows.isEmpty()) {
            table.add(emptyTableRow());
        } else {
            for (BuchungTabellenZeile row : rows) {
                table.add(tableRow(row));
            }
        }

        card.add(titleBox, table);
        return card;
    }

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

    private Component emptyTableRow() {
        Div row = new Div();
        row.addClassNames("finance-table-row", "finance-table-empty");

        Span status = new Span("Keine Daten");
        status.addClassNames("status-badge", "neutral");

        row.add(
                new Span(getZeitraumText()),
                new Span("-"),
                new Span("Keine Buchungen vorhanden"),
                status,
                new Span(formatEuro(BigDecimal.ZERO))
        );

        return row;
    }

    private Component tableRow(BuchungTabellenZeile data) {
        Div row = new Div();
        row.addClassNames("finance-table-row", "clickable-table-row");

        Span status = new Span(data.status());
        status.addClassNames(
                "status-badge",
                data.status().equals("Bezahlt / Erledigt")
                        ? "success"
                        : "warning"
        );

        Span amount = new Span(data.betrag());
        amount.addClassName(data.typ() == BuchungTyp.EINNAHME ? "amount-positive" : "amount-negative");

        row.add(
                new Span(data.datum()),
                new Span(data.objekt()),
                new Span(data.kategorie()),
                status,
                amount
        );

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

    private double[] berechneEinnahmenChartDaten(
            LocalDate startDatum,
            LocalDate endDatum,
            Long immobilieId,
            Long mieteinheitId,
            Long mieterId
    ) {
        List<YearMonth> monate = ermittleMonateImZeitraum(startDatum, endDatum);
        double[] daten = new double[monate.size()];

        for (int i = 0; i < monate.size(); i++) {
            YearMonth monat = monate.get(i);

            BigDecimal summe =
                    zahlungsEingangService.berechneBezahlteZahlungseingaengeImZeitraum(
                            monat.atDay(1),
                            monat.atEndOfMonth(),
                            immobilieId,
                            mieteinheitId,
                            mieterId
                    );

            daten[i] = summe.doubleValue();
        }

        return daten;
    }

    private double[] berechneAusgabenChartDaten(
            LocalDate startDatum,
            LocalDate endDatum,
            Long immobilieId
    ) {
        List<YearMonth> monate = ermittleMonateImZeitraum(startDatum, endDatum);
        double[] daten = new double[monate.size()];

        for (int i = 0; i < monate.size(); i++) {
            YearMonth monat = monate.get(i);

            BigDecimal summe =
                    ausgabeService.berechneBezahlteAusgabenImZeitraum(
                            monat.atDay(1),
                            monat.atEndOfMonth(),
                            immobilieId
                    );

            daten[i] = summe.doubleValue();
        }

        return daten;
    }

    private String[] berechneChartMonate(
            LocalDate startDatum,
            LocalDate endDatum
    ) {
        List<YearMonth> monate = ermittleMonateImZeitraum(startDatum, endDatum);

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("MMM", Locale.GERMANY);

        String[] labels = new String[monate.size()];

        for (int i = 0; i < monate.size(); i++) {
            labels[i] = monate.get(i).format(formatter);
        }

        return labels;
    }

    private List<YearMonth> ermittleMonateImZeitraum(
            LocalDate startDatum,
            LocalDate endDatum
    ) {
        List<YearMonth> monate = new ArrayList<>();

        YearMonth start = YearMonth.from(startDatum);
        YearMonth ende = YearMonth.from(endDatum);

        YearMonth aktuell = start;

        while (!aktuell.isAfter(ende)) {
            monate.add(aktuell);
            aktuell = aktuell.plusMonths(1);
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

    @Override
    public String getPageTitle() {
        return "Finanz-Dashboard";
    }

    @Override
    public String getPageSubtitle() {
        return "Übersicht über Einnahmen, Ausgaben und Cashflow";
    }
}
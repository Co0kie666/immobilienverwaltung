package de.hsbi.immobilienverwaltung.ui.mieter;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Route;
import de.hsbi.immobilienverwaltung.domain.Mieter;
import de.hsbi.immobilienverwaltung.domain.Mieteinheit;
import de.hsbi.immobilienverwaltung.domain.Mietvertrag;
import de.hsbi.immobilienverwaltung.domain.enums.Vertragsstatus;
import de.hsbi.immobilienverwaltung.service.interfaces.MieterService;
import de.hsbi.immobilienverwaltung.service.interfaces.MietvertragService;
import de.hsbi.immobilienverwaltung.ui.layout.HasPageHeader;
import de.hsbi.immobilienverwaltung.ui.layout.MainLayout;
import jakarta.annotation.security.PermitAll;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Route(value = "mieter-vertraege", layout = MainLayout.class)
@PermitAll
public class MieterVertraegeView extends Div implements HasPageHeader, AfterNavigationObserver {

    private final MieterService mieterService;
    private final MietvertragService mietvertragService;

    private TabellenModus aktiverModus = TabellenModus.MIETER;
    private Tabs tabs;
    private Button addButton;
    private Grid.Column<Mietvertrag> laufzeitColumn;

    private Tabs archivTabs;
    private boolean archivierteMieterAnzeigen = false;

    private final Grid<Mieter> mieterGrid = new Grid<>(Mieter.class, false);
    private final Grid<Mietvertrag> mietvertragGrid = new Grid<>(Mietvertrag.class, false);

    private final TextField searchField = new TextField();
    private final Select<String> statusFilter = new Select<>();

    private enum TabellenModus {
        MIETER,
        VERTRAEGE,
        ARCHIV
    }

    public MieterVertraegeView(MieterService mieterService, MietvertragService mietvertragService) {
        this.mieterService = mieterService;
        this.mietvertragService = mietvertragService;

        addClassNames("page-content", "tenant-contract-page");

        add(
                createHeroSection(),
                createOverviewCards(),
                createTableCard()
        );
    }

    private Component createHeroSection() {
        Div hero = new Div();
        hero.addClassName("tenant-hero");

        Div content = new Div();
        content.addClassName("tenant-hero-content");

        Span eyebrow = new Span("Mieter-Management");
        eyebrow.addClassName("tenant-hero-eyebrow");

        H1 title = new H1("Mieter & Mietverträge im Griff");
        title.addClassName("tenant-hero-title");

        Paragraph subtitle = new Paragraph(
                "Verwalte Stammdaten, laufende Mietverhältnisse und archivierte Verträge in einer zentralen Übersicht."
        );
        subtitle.addClassName("tenant-hero-subtitle");

        Div actions = new Div();
        actions.addClassName("tenant-hero-actions");

        Button createTenant = new Button("Neuer Mieter", VaadinIcon.USER.create());
        createTenant.addClassName("primary-button");
        createTenant.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate(MieterFormView.class))
        );

        Button createContract = new Button("Neuer Vertrag", VaadinIcon.FILE_TEXT.create());
        createContract.addClassName("secondary-button");
        createContract.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate(MietvertragFormView.class))
        );

        actions.add(createTenant, createContract);
        content.add(eyebrow, title, subtitle, actions);

        Div visual = new Div();
        visual.addClassName("tenant-hero-visual");
        visual.add(
                heroMetric("Aktive Verträge", String.valueOf(zaehleAktiveVertraege()), VaadinIcon.CHECK_CIRCLE, "success"),
                heroMetric("Läuft aus", String.valueOf(zaehleAuslaufendeVertraege()), VaadinIcon.CLOCK, "warning")
        );

        hero.add(content, visual);
        return hero;
    }

    private Component heroMetric(String label, String value, VaadinIcon icon, String color) {
        Div card = new Div();
        card.addClassNames("tenant-hero-metric", color);

        Div iconBox = new Div(new Icon(icon));
        iconBox.addClassNames("tenant-hero-metric-icon", color);

        Span valueText = new Span(value);
        valueText.addClassName("tenant-hero-metric-value");

        Span labelText = new Span(label);
        labelText.addClassName("tenant-hero-metric-label");

        card.add(iconBox, valueText, labelText);
        return card;
    }

    private Component createOverviewCards() {
        Div grid = new Div();
        grid.addClassName("tenant-kpi-grid");

        List<Mieter> mieter = mieterService.findeAlleMieter();
        List<Mietvertrag> vertraege = mietvertragService.findeAlleMietvertraege();

        long aktiveMieter = mieter.stream()
                .filter(person -> "Aktiv".equals(ermittleMieterStatus(person)))
                .count();

        long aktiveVertraege = vertraege.stream()
                .filter(this::istAktiverVertrag)
                .count();

        long auslaufendeVertraege = vertraege.stream()
                .filter(this::istAuslaufenderVertrag)
                .count();

        long archivierteVertraege = vertraege.stream()
                .filter(this::istArchivVertrag)
                .count();

        grid.add(
                kpiCard("Mieter gesamt", String.valueOf(mieter.size()), aktiveMieter + " aktiv", VaadinIcon.USERS, "primary"),
                kpiCard("Aktive Verträge", String.valueOf(aktiveVertraege), "laufende Mietverhältnisse", VaadinIcon.FILE_TEXT, "success"),
                kpiCard("Läuft aus", String.valueOf(auslaufendeVertraege), "gekündigt, aber noch aktiv", VaadinIcon.CLOCK, "warning"),
                kpiCard("Archiv", String.valueOf(archivierteVertraege), "beendete Verträge", VaadinIcon.ARCHIVE, "neutral")
        );

        return grid;
    }

    private Component kpiCard(String title, String value, String subtitle, VaadinIcon icon, String color) {
        Div card = new Div();
        card.addClassNames("tenant-kpi-card", color);

        Div top = new Div();
        top.addClassName("tenant-kpi-top");

        Div text = new Div();

        Span titleText = new Span(title);
        titleText.addClassName("tenant-kpi-title");

        H2 valueText = new H2(value);
        valueText.addClassName("tenant-kpi-value");

        Span subtitleText = new Span(subtitle);
        subtitleText.addClassName("tenant-kpi-subtitle");

        text.add(titleText, valueText, subtitleText);

        Div iconBox = new Div(new Icon(icon));
        iconBox.addClassNames("tenant-kpi-icon", color);

        top.add(text, iconBox);
        card.add(top);

        return card;
    }

    private Component createTableCard() {
        Div tableCard = new Div();
        tableCard.addClassNames("table-card", "tenant-table-card");

        configureMieterGrid();
        configureMietvertragGrid();

        mietvertragGrid.setVisible(false);

        tableCard.add(
                createTableHeader(),
                mieterGrid,
                mietvertragGrid
        );

        aktualisiereStatusFilterOptionen();
        aktualisiereTabellen();

        return tableCard;
    }

    private void configureMieterGrid() {
        mieterGrid.removeAllColumns();
        mieterGrid.setWidthFull();
        mieterGrid.setAllRowsVisible(true);
        mieterGrid.addClassNames("tenant-grid", "tenant-mieter-grid");

        mieterGrid.addColumn(new ComponentRenderer<>(this::createMieterCell))
                .setHeader("Mieter")
                .setAutoWidth(true)
                .setFlexGrow(2);

        mieterGrid.addColumn(mieter -> textOderStrich(mieter.getTelefonnummer()))
                .setHeader("Telefon")
                .setAutoWidth(true)
                .setFlexGrow(1);

        mieterGrid.addColumn(mieter -> textOderStrich(mieter.getEmail()))
                .setHeader("E-Mail")
                .setAutoWidth(true)
                .setFlexGrow(2);

        mieterGrid.addColumn(this::findeAktuelleEinheit)
                .setHeader("Aktuelle Einheit")
                .setAutoWidth(true)
                .setFlexGrow(2);

        mieterGrid.addColumn(new ComponentRenderer<>(this::createMieterStatusBadge))
                .setHeader("Status")
                .setAutoWidth(true)
                .setFlexGrow(1);

        mieterGrid.addItemClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate(MieterListView.class, String.valueOf(event.getItem().getId())))
        );
    }

    private void configureMietvertragGrid() {
        mietvertragGrid.removeAllColumns();
        mietvertragGrid.setWidthFull();
        mietvertragGrid.setAllRowsVisible(true);
        mietvertragGrid.addClassNames("tenant-grid", "tenant-contract-grid");

        mietvertragGrid.addColumn(new ComponentRenderer<>(this::createContractNumberCell))
                .setHeader("Vertrag")
                .setAutoWidth(true)
                .setFlexGrow(1);

        mietvertragGrid.addColumn(mietvertrag -> formatMieterName(mietvertrag.getMieter()))
                .setHeader("Mieter")
                .setAutoWidth(true)
                .setFlexGrow(2);

        mietvertragGrid.addColumn(this::formatMietobjekt)
                .setHeader("Einheit")
                .setAutoWidth(true)
                .setFlexGrow(2);

        laufzeitColumn = mietvertragGrid.addColumn(this::formatLaufzeitOderZeitraum)
                .setHeader("Laufzeit")
                .setAutoWidth(true)
                .setFlexGrow(1);

        mietvertragGrid.addColumn(new ComponentRenderer<>(this::createWarmmieteCell))
                .setHeader("Miete mtl.")
                .setAutoWidth(true)
                .setFlexGrow(1);

        mietvertragGrid.addColumn(new ComponentRenderer<>(this::createMietvertragStatusBadge))
                .setHeader("Status")
                .setAutoWidth(true)
                .setFlexGrow(1);

        mietvertragGrid.addItemClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate(MietvertragListView.class, String.valueOf(event.getItem().getId())))
        );
    }

    private Component createMieterCell(Mieter mieter) {
        Div cell = new Div();
        cell.addClassName("tenant-person-cell");

        Div avatar = new Div();
        avatar.addClassName("tenant-avatar");
        avatar.setText(ermittleInitialen(mieter));

        Div text = new Div();
        text.addClassName("tenant-person-text");

        Span name = new Span(formatMieterName(mieter));
        name.addClassName("tenant-person-name");

        Span meta = new Span(textOderStrich(mieter == null ? null : mieter.getEmail()));
        meta.addClassName("tenant-person-meta");

        text.add(name, meta);
        cell.add(avatar, text);

        return cell;
    }

    private Component createContractNumberCell(Mietvertrag mietvertrag) {
        Div cell = new Div();
        cell.addClassName("tenant-contract-number-cell");

        Span number = new Span("MV-" + mietvertrag.getId());
        number.addClassName("tenant-contract-number");

        Span meta = new Span(formatVertragsstatus(mietvertrag));
        meta.addClassName("tenant-contract-meta");

        cell.add(number, meta);
        return cell;
    }

    private Component createWarmmieteCell(Mietvertrag mietvertrag) {
        Span value = new Span(formatWarmmiete(mietvertrag));
        value.addClassName("tenant-rent-value");
        return value;
    }

    private Component createTableHeader() {
        Div header = new Div();
        header.addClassName("tenant-table-header");

        Div titleBox = new Div();
        titleBox.addClassName("tenant-table-title-box");

        H3 title = new H3("Mieter- und Vertragsübersicht");
        title.addClassName("card-title");

        Paragraph subtitle = new Paragraph("Wechsle zwischen aktiven Mietern, laufenden Verträgen und dem Archiv.");
        subtitle.addClassName("card-subtitle");

        titleBox.add(title, subtitle);

        tabs = new Tabs(
                new Tab("Mieter"),
                new Tab("Verträge"),
                new Tab("Archiv")
        );
        tabs.addClassName("tenant-tabs");

        tabs.addSelectedChangeListener(event -> {
            if (tabs.getSelectedIndex() == 0) {
                setAktiverModus(TabellenModus.MIETER);
            } else if (tabs.getSelectedIndex() == 1) {
                setAktiverModus(TabellenModus.VERTRAEGE);
            } else {
                setAktiverModus(TabellenModus.ARCHIV);
            }
        });

        archivTabs = new Tabs(
                new Tab("Verträge"),
                new Tab("Mieter")
        );
        archivTabs.addClassName("tenant-archive-tabs");
        archivTabs.setVisible(false);

        archivTabs.addSelectedChangeListener(event -> {
            archivierteMieterAnzeigen = archivTabs.getSelectedIndex() == 1;
            setAktiverModus(TabellenModus.ARCHIV);
        });

        searchField.setPlaceholder("Mieter, Vertrag oder Einheit suchen...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.addClassName("tenant-search-field");
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(event -> aktualisiereTabellen());

        statusFilter.addClassName("tenant-status-filter");
        statusFilter.addValueChangeListener(event -> aktualisiereTabellen());

        addButton = new Button("Anlegen", VaadinIcon.PLUS.create());
        addButton.addClassName("primary-button");

        addButton.addClickListener(event -> {
            if (aktiverModus == TabellenModus.MIETER) {
                getUI().ifPresent(ui -> ui.navigate(MieterFormView.class));
            } else if (aktiverModus == TabellenModus.VERTRAEGE) {
                getUI().ifPresent(ui -> ui.navigate(MietvertragFormView.class));
            }
        });

        Div controls = new Div();
        controls.addClassName("tenant-table-controls");
        controls.add(archivTabs, searchField, statusFilter, addButton);

        Div top = new Div();
        top.addClassName("tenant-table-top");
        top.add(titleBox, tabs);

        header.add(top, controls);

        return header;
    }

    private void setAktiverModus(TabellenModus modus) {
        aktiverModus = modus;

        boolean mieterAktiv = modus == TabellenModus.MIETER || (modus == TabellenModus.ARCHIV && archivierteMieterAnzeigen);

        mieterGrid.setVisible(mieterAktiv);
        mietvertragGrid.setVisible(!mieterAktiv);

        if (laufzeitColumn != null) {
            laufzeitColumn.setHeader(aktiverModus == TabellenModus.ARCHIV ? "Zeitraum" : "Laufzeit");
        }

        aktualisiereStatusFilterOptionen();

        if (archivTabs != null) {
            archivTabs.setVisible(aktiverModus == TabellenModus.ARCHIV);
        }

        if (addButton != null) {
            addButton.setVisible(aktiverModus != TabellenModus.ARCHIV);
            addButton.setText(aktiverModus == TabellenModus.VERTRAEGE ? "Vertrag anlegen" : "Mieter anlegen");
        }

        if (tabs != null) {
            int zielIndex = switch (modus) {
                case MIETER -> 0;
                case VERTRAEGE -> 1;
                case ARCHIV -> 2;
            };

            if (tabs.getSelectedIndex() != zielIndex) {
                tabs.setSelectedIndex(zielIndex);
            }
        }

        aktualisiereTabellen();
    }

    private void aktualisiereStatusFilterOptionen() {
        statusFilter.setVisible(aktiverModus != TabellenModus.ARCHIV);

        String alterWert = statusFilter.getValue();

        List<String> erlaubteOptionen = switch (aktiverModus) {
            case MIETER -> List.of(
                    "Alle Status",
                    "Aktiv",
                    "Läuft aus",
                    "Ohne aktiven Vertrag"
            );
            case VERTRAEGE -> List.of(
                    "Alle Status",
                    "Aktiv",
                    "Läuft aus"
            );
            case ARCHIV -> List.of("Alle Status");
        };

        statusFilter.setItems(erlaubteOptionen);

        if (alterWert != null && erlaubteOptionen.contains(alterWert)) {
            statusFilter.setValue(alterWert);
        } else {
            statusFilter.setValue("Alle Status");
        }
    }

    private void aktualisiereTabellen() {
        if (aktiverModus == TabellenModus.MIETER) {
            mieterGrid.setItems(filterMieter(mieterService.findeAlleMieter()));
            return;
        }

        if (aktiverModus == TabellenModus.ARCHIV && archivierteMieterAnzeigen) {
            mieterGrid.setItems(filterMieter(mieterService.findeArchivierteMieter()));
            return;
        }

        mietvertragGrid.setItems(filterMietvertraege(mietvertragService.findeAlleMietvertraege()));
    }

    private List<Mieter> filterMieter(List<Mieter> mieterListe) {
        String suche = getSuche();
        String status = statusFilter.getValue();

        return mieterListe.stream()
                .filter(mieter -> suche.isBlank()
                        || formatMieterName(mieter).toLowerCase().contains(suche)
                        || textOderLeer(mieter.getEmail()).toLowerCase().contains(suche)
                        || textOderLeer(mieter.getTelefonnummer()).toLowerCase().contains(suche)
                        || findeAktuelleEinheit(mieter).toLowerCase().contains(suche)
                )
                .filter(mieter -> status == null
                        || "Alle Status".equals(status)
                        || ermittleMieterStatus(mieter).equals(status)
                )
                .toList();
    }

    private List<Mietvertrag> filterMietvertraege(List<Mietvertrag> mietvertraege) {
        String suche = getSuche();
        String status = statusFilter.getValue();

        return mietvertraege.stream()
                .filter(this::passtZumAktivenVertragsTab)
                .filter(mietvertrag -> suche.isBlank()
                        || ("MV-" + mietvertrag.getId()).toLowerCase().contains(suche)
                        || formatMieterName(mietvertrag.getMieter()).toLowerCase().contains(suche)
                        || formatMietobjekt(mietvertrag).toLowerCase().contains(suche)
                )
                .filter(mietvertrag -> aktiverModus != TabellenModus.VERTRAEGE
                        || status == null
                        || "Alle Status".equals(status)
                        || formatVertragsstatus(mietvertrag).equals(status)
                )
                .toList();
    }

    private boolean passtZumAktivenVertragsTab(Mietvertrag mietvertrag) {
        if (aktiverModus == TabellenModus.VERTRAEGE) {
            return istAktiverVertrag(mietvertrag) || istAuslaufenderVertrag(mietvertrag);
        }

        if (aktiverModus == TabellenModus.ARCHIV) {
            return istArchivVertrag(mietvertrag);
        }

        return true;
    }

    private boolean istAktiverVertrag(Mietvertrag mietvertrag) {
        return mietvertrag != null && mietvertrag.getStatus() == Vertragsstatus.AKTIV;
    }

    private boolean istAuslaufenderVertrag(Mietvertrag mietvertrag) {
        return mietvertrag != null
                && mietvertrag.getStatus() == Vertragsstatus.GEKUENDIGT
                && !istAusgelaufen(mietvertrag);
    }

    private boolean istArchivVertrag(Mietvertrag mietvertrag) {
        return mietvertrag != null && (mietvertrag.getStatus() == Vertragsstatus.BEENDET || (mietvertrag.getStatus() == Vertragsstatus.GEKUENDIGT && istAusgelaufen(mietvertrag)));
    }

    private boolean istAusgelaufen(Mietvertrag mietvertrag) {
        return mietvertrag.getEnddatum() != null && mietvertrag.getEnddatum().isBefore(LocalDate.now());
    }

    private String getSuche() {
        if (searchField.getValue() == null) {
            return "";
        }
        return searchField.getValue().trim().toLowerCase();
    }

    private Component createMieterStatusBadge(Mieter mieter) {
        String status = ermittleMieterStatus(mieter);

        Span badge = new Span(status);
        badge.addClassNames("status-badge", getMieterStatusStyle(status));

        return badge;
    }

    private Component createMietvertragStatusBadge(Mietvertrag mietvertrag) {
        String status = formatVertragsstatus(mietvertrag);

        Span badge = new Span(status);
        badge.addClassNames("status-badge", getVertragsStatusStyle(mietvertrag));

        return badge;
    }

    private String ermittleMieterStatus(Mieter mieter) {
        List<Mietvertrag> vertraege = mietvertragService.findeMietvertraegeNachMieter(mieter.getId());

        boolean hatAktivenVertrag = vertraege.stream().anyMatch(this::istAktiverVertrag);

        if (hatAktivenVertrag) {
            return "Aktiv";
        }

        boolean hatAuslaufendenVertrag = vertraege.stream().anyMatch(this::istAuslaufenderVertrag);

        if (hatAuslaufendenVertrag) {
            return "Läuft aus";
        }

        return "Ohne aktiven Vertrag";
    }

    private String findeAktuelleEinheit(Mieter mieter) {
        List<Mietvertrag> vertraege = mietvertragService.findeMietvertraegeNachMieter(mieter.getId());

        return vertraege.stream()
                .filter(this::istAktiverVertrag)
                .findFirst()
                .map(this::formatMietobjekt)
                .orElseGet(() -> vertraege.stream()
                        .filter(this::istAuslaufenderVertrag)
                        .findFirst()
                        .map(this::formatMietobjekt)
                        .orElse("-")
                );
    }

    private long zaehleAktiveVertraege() {
        return mietvertragService.findeAlleMietvertraege()
                .stream()
                .filter(this::istAktiverVertrag)
                .count();
    }

    private long zaehleAuslaufendeVertraege() {
        return mietvertragService.findeAlleMietvertraege()
                .stream()
                .filter(this::istAuslaufenderVertrag)
                .count();
    }

    private String formatMieterName(Mieter mieter) {
        if (mieter == null) {
            return "-";
        }

        String name = (textOderLeer(mieter.getVorname()) + " " + textOderLeer(mieter.getNachname())).trim();

        return name.isBlank() ? "-" : name;
    }

    private String ermittleInitialen(Mieter mieter) {
        if (mieter == null) {
            return "?";
        }

        String vorname = textOderLeer(mieter.getVorname()).trim();
        String nachname = textOderLeer(mieter.getNachname()).trim();

        String ersteInitiale = vorname.isBlank() ? "" : vorname.substring(0, 1);
        String zweiteInitiale = nachname.isBlank() ? "" : nachname.substring(0, 1);
        String initialen = (ersteInitiale + zweiteInitiale).toUpperCase(Locale.GERMANY);

        return initialen.isBlank() ? "?" : initialen;
    }

    private String formatMietobjekt(Mietvertrag mietvertrag) {
        if (mietvertrag == null || mietvertrag.getMieteinheit() == null) {
            return "-";
        }

        Mieteinheit mieteinheit = mietvertrag.getMieteinheit();

        if (mieteinheit.getImmobilie() == null) {
            return mieteinheit.getBezeichnung();
        }

        return mieteinheit.getImmobilie().getBezeichnung() + " / " + mieteinheit.getBezeichnung();
    }

    private String formatLaufzeitOderZeitraum(Mietvertrag mietvertrag) {
        if (aktiverModus == TabellenModus.ARCHIV) {
            return formatZeitraum(mietvertrag);
        }

        if (mietvertrag.getEnddatum() == null) {
            return "unbefristet";
        }

        return "bis " + mietvertrag.getEnddatum().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }

    private String formatZeitraum(Mietvertrag mietvertrag) {
        String start = mietvertrag.getStartdatum() == null
                ? "-"
                : mietvertrag.getStartdatum().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));

        String ende = mietvertrag.getEnddatum() == null
                ? "-"
                : mietvertrag.getEnddatum().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));

        return start + " - " + ende;
    }

    private String formatWarmmiete(Mietvertrag mietvertrag) {
        double kaltmiete = mietvertrag.getKaltmiete() == null ? 0 : mietvertrag.getKaltmiete();
        double nebenkosten = mietvertrag.getNebenkosten() == null ? 0 : mietvertrag.getNebenkosten();

        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.GERMANY);
        return formatter.format(kaltmiete + nebenkosten);
    }

    private String formatVertragsstatus(Mietvertrag mietvertrag) {
        if (mietvertrag == null || mietvertrag.getStatus() == null) {
            return "-";
        }

        if (mietvertrag.getStatus() == Vertragsstatus.GEKUENDIGT && !istAuslaufenderVertrag(mietvertrag)) {
            return Vertragsstatus.BEENDET.getLabel();
        }

        return mietvertrag.getStatus().getLabel();
    }

    private String getMieterStatusStyle(String status) {
        return switch (status) {
            case "Aktiv" -> "success";
            case "Läuft aus" -> "warning";
            default -> "neutral";
        };
    }

    private String getVertragsStatusStyle(Mietvertrag mietvertrag) {
        if (mietvertrag == null || mietvertrag.getStatus() == null) {
            return "neutral";
        }

        if (istAktiverVertrag(mietvertrag)) {
            return "success";
        }

        if (istAuslaufenderVertrag(mietvertrag)) {
            return "warning";
        }

        return "neutral";
    }

    private String textOderLeer(String text) {
        return text == null ? "" : text;
    }

    private String textOderStrich(String text) {
        return text == null || text.isBlank() ? "-" : text;
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        String tab = event.getLocation()
                .getQueryParameters()
                .getParameters()
                .getOrDefault("tab", java.util.List.of("mieter"))
                .get(0);

        if ("vertraege".equalsIgnoreCase(tab)) {
            setAktiverModus(TabellenModus.VERTRAEGE);
        } else if ("archiv".equalsIgnoreCase(tab)) {
            setAktiverModus(TabellenModus.ARCHIV);
        } else {
            setAktiverModus(TabellenModus.MIETER);
        }
    }

    @Override
    public String getPageTitle() {
        return "Mieter & Verträge";
    }

    @Override
    public String getPageSubtitle() {
        return "Mieter, laufende Mietverhältnisse und Vertragsarchiv verwalten";
    }
}

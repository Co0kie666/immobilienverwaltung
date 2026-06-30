package de.hsbi.immobilienverwaltung.ui.mieter;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
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
import de.hsbi.immobilienverwaltung.domain.Mietvertrag;
import de.hsbi.immobilienverwaltung.domain.enums.Vertragsstatus;
import de.hsbi.immobilienverwaltung.service.interfaces.MieterService;
import de.hsbi.immobilienverwaltung.service.interfaces.MietvertragService;
import de.hsbi.immobilienverwaltung.ui.UiFormatUtils;
import de.hsbi.immobilienverwaltung.ui.layout.HasPageHeader;
import de.hsbi.immobilienverwaltung.ui.layout.MainLayout;
import jakarta.annotation.security.PermitAll;

import java.time.LocalDate;
import java.util.List;

@Route(value = "mieter-vertraege", layout = MainLayout.class)
@PermitAll
public class MieterVertraegeView extends Div implements HasPageHeader, AfterNavigationObserver {

    private final MieterService mieterService;
    private final MietvertragService mietvertragService;

    private TabellenModus aktiverModus = TabellenModus.MIETER;
    private Tabs tabs;
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
                erstelleKopfbereich(),
                erstelleUebersichtsKarten(),
                erstelleTabellenBereich()
        );
    }

    private Component erstelleKopfbereich() {
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

        hero.add(content);
        return hero;
    }

    // Die Karten oben geben nur einen schnellen Überblick,
    // bevor man in der Tabelle genauer filtert.
    private Component erstelleUebersichtsKarten() {
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
                erstelleKennzahlKarte("Mieter gesamt", String.valueOf(mieter.size()), aktiveMieter + " aktiv", VaadinIcon.USERS, "primary"),
                erstelleKennzahlKarte("Aktive Verträge", String.valueOf(aktiveVertraege), "laufende Mietverhältnisse", VaadinIcon.FILE_TEXT, "success"),
                erstelleKennzahlKarte("Läuft aus", String.valueOf(auslaufendeVertraege), "gekündigt, aber noch aktiv", VaadinIcon.CLOCK, "warning"),
                erstelleKennzahlKarte("Archiv", String.valueOf(archivierteVertraege), "beendete Verträge", VaadinIcon.ARCHIVE, "neutral")
        );

        return grid;
    }

    private Component erstelleKennzahlKarte(String title, String value, String subtitle, VaadinIcon icon, String color) {
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

    // In diesem Bereich liegen beide Tabellen.
    // Sichtbar ist immer nur die Tabelle, die zum aktiven Tab passt.
    private Component erstelleTabellenBereich() {
        Div tableCard = new Div();
        tableCard.addClassNames("table-card", "tenant-table-card");

        richteMieterTabelleEin();
        richteMietvertragTabelleEin();

        mietvertragGrid.setVisible(false);

        tableCard.add(
                erstelleTabellenKopf(),
                mieterGrid,
                mietvertragGrid
        );

        aktualisiereStatusFilter();
        aktualisiereTabellen();

        return tableCard;
    }

    // Die Mietertabelle zeigt nur Daten, die man für die Auswahl schnell braucht.
    // Die Detailansicht öffnet sich über einen Klick auf die Zeile.
    private void richteMieterTabelleEin() {
        mieterGrid.removeAllColumns();
        mieterGrid.setWidthFull();
        mieterGrid.setAllRowsVisible(true);
        mieterGrid.addClassNames("tenant-grid", "tenant-mieter-grid");

        mieterGrid.addColumn(new ComponentRenderer<>(this::erstelleMieterZelle))
                .setHeader("Mieter")
                .setAutoWidth(true)
                .setFlexGrow(2);

        mieterGrid.addColumn(mieter -> UiFormatUtils.wertOderStrich(mieter.getTelefonnummer()))
                .setHeader("Telefon")
                .setAutoWidth(true)
                .setFlexGrow(1);

        mieterGrid.addColumn(mieter -> UiFormatUtils.wertOderStrich(mieter.getEmail()))
                .setHeader("E-Mail")
                .setAutoWidth(true)
                .setFlexGrow(2);

        mieterGrid.addColumn(this::findeAktuelleEinheit)
                .setHeader("Aktuelle Einheit")
                .setAutoWidth(true)
                .setFlexGrow(2);

        mieterGrid.addColumn(new ComponentRenderer<>(this::erstelleMieterStatusBadge))
                .setHeader("Status")
                .setAutoWidth(true)
                .setFlexGrow(1);

        mieterGrid.addItemClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate(MieterListView.class, String.valueOf(event.getItem().getId())))
        );
    }

    // Die Vertragstabelle wird für laufende Verträge und für das Archiv genutzt.
    // Deshalb wird die Laufzeit-Spalte später je nach Tab umbenannt.
    private void richteMietvertragTabelleEin() {
        mietvertragGrid.removeAllColumns();
        mietvertragGrid.setWidthFull();
        mietvertragGrid.setAllRowsVisible(true);
        mietvertragGrid.addClassNames("tenant-grid", "tenant-contract-grid");

        mietvertragGrid.addColumn(new ComponentRenderer<>(this::erstelleVertragsNummerZelle))
                .setHeader("Vertrag")
                .setAutoWidth(true)
                .setFlexGrow(1);

        mietvertragGrid.addColumn(mietvertrag -> UiFormatUtils.formatiereMieterName(mietvertrag.getMieter()))
                .setHeader("Mieter")
                .setAutoWidth(true)
                .setFlexGrow(2);

        mietvertragGrid.addColumn(UiFormatUtils::formatiereMietobjekt)
                .setHeader("Einheit")
                .setAutoWidth(true)
                .setFlexGrow(2);

        laufzeitColumn = mietvertragGrid.addColumn(this::formatiereLaufzeitOderZeitraum)
                .setHeader("Laufzeit")
                .setAutoWidth(true)
                .setFlexGrow(1);

        mietvertragGrid.addColumn(new ComponentRenderer<>(this::erstelleWarmmieteZelle))
                .setHeader("Miete mtl.")
                .setAutoWidth(true)
                .setFlexGrow(1);

        mietvertragGrid.addColumn(new ComponentRenderer<>(this::erstelleMietvertragStatusBadge))
                .setHeader("Status")
                .setAutoWidth(true)
                .setFlexGrow(1);

        mietvertragGrid.addItemClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate(MietvertragListView.class, String.valueOf(event.getItem().getId())))
        );
    }

    private Component erstelleMieterZelle(Mieter mieter) {
        Div cell = new Div();
        cell.addClassName("tenant-person-cell");

        Div avatar = new Div();
        avatar.addClassName("tenant-avatar");
        avatar.setText(UiFormatUtils.erstelleInitialen(mieter));

        Div text = new Div();
        text.addClassName("tenant-person-text");

        Span name = new Span(UiFormatUtils.formatiereMieterName(mieter));
        name.addClassName("tenant-person-name");

        Span meta = new Span(UiFormatUtils.wertOderStrich(mieter == null ? null : mieter.getEmail()));
        meta.addClassName("tenant-person-meta");

        text.add(name, meta);
        cell.add(avatar, text);

        return cell;
    }

    private Component erstelleVertragsNummerZelle(Mietvertrag mietvertrag) {
        Div cell = new Div();
        cell.addClassName("tenant-contract-number-cell");

        Span number = new Span("MV-" + mietvertrag.getId());
        number.addClassName("tenant-contract-number");

        Span meta = new Span(formatiereVertragsstatus(mietvertrag));
        meta.addClassName("tenant-contract-meta");

        cell.add(number, meta);
        return cell;
    }

    private Component erstelleWarmmieteZelle(Mietvertrag mietvertrag) {
        Span value = new Span(UiFormatUtils.formatiereWarmmiete(mietvertrag));
        value.addClassName("tenant-rent-value");
        return value;
    }

    // Der Tabellenkopf enthält Tabs, Suche und Statusfilter.
    // Diese drei Dinge steuern zusammen, welche Daten unten angezeigt werden.
    private Component erstelleTabellenKopf() {
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
                wechselTabellenModus(TabellenModus.MIETER);
            } else if (tabs.getSelectedIndex() == 1) {
                wechselTabellenModus(TabellenModus.VERTRAEGE);
            } else {
                wechselTabellenModus(TabellenModus.ARCHIV);
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
            wechselTabellenModus(TabellenModus.ARCHIV);
        });

        searchField.setPlaceholder("Mieter, Vertrag oder Einheit suchen...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.addClassName("tenant-search-field");
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(event -> aktualisiereTabellen());

        statusFilter.addClassName("tenant-status-filter");
        statusFilter.addValueChangeListener(event -> aktualisiereTabellen());

        Div controls = new Div();
        controls.addClassName("tenant-table-controls");
        controls.add(archivTabs, searchField, statusFilter);

        Div top = new Div();
        top.addClassName("tenant-table-top");
        top.add(titleBox, tabs);

        header.add(top, controls);

        return header;
    }

    // Beim Wechseln des Tabs wird nicht die ganze Seite neu gebaut.
    // Es werden nur Tabelle, Filter und Spaltenüberschrift angepasst.
    private void wechselTabellenModus(TabellenModus modus) {
        aktiverModus = modus;

        boolean mieterAktiv = modus == TabellenModus.MIETER
                || (modus == TabellenModus.ARCHIV && archivierteMieterAnzeigen);

        mieterGrid.setVisible(mieterAktiv);
        mietvertragGrid.setVisible(!mieterAktiv);

        if (laufzeitColumn != null) {
            laufzeitColumn.setHeader(aktiverModus == TabellenModus.ARCHIV ? "Zeitraum" : "Laufzeit");
        }

        aktualisiereStatusFilter();

        if (archivTabs != null) {
            archivTabs.setVisible(aktiverModus == TabellenModus.ARCHIV);
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

    // Der Statusfilter hat je nach Tab andere Auswahlmöglichkeiten.
    // Im Archiv wird er ausgeblendet, weil dort sowieso nur Archivdaten stehen.
    private void aktualisiereStatusFilter() {
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

    // Je nach aktivem Tab werden Mieter, aktive Verträge oder Archivdaten geladen.
    private void aktualisiereTabellen() {
        if (aktiverModus == TabellenModus.MIETER) {
            mieterGrid.setItems(filtereMieter(mieterService.findeAlleMieter()));
            return;
        }

        if (aktiverModus == TabellenModus.ARCHIV && archivierteMieterAnzeigen) {
            mieterGrid.setItems(filtereMieter(mieterService.findeArchivierteMieter()));
            return;
        }

        mietvertragGrid.setItems(filtereMietvertraege(mietvertragService.findeAlleMietvertraege()));
    }

    private List<Mieter> filtereMieter(List<Mieter> mieterListe) {
        String suche = holeSuchtext();
        String status = statusFilter.getValue();

        return mieterListe.stream()
                .filter(mieter -> suche.isBlank()
                        || UiFormatUtils.formatiereMieterName(mieter).toLowerCase().contains(suche)
                        || UiFormatUtils.wertOderLeer(mieter.getEmail()).toLowerCase().contains(suche)
                        || UiFormatUtils.wertOderLeer(mieter.getTelefonnummer()).toLowerCase().contains(suche)
                        || findeAktuelleEinheit(mieter).toLowerCase().contains(suche)
                )
                .filter(mieter -> status == null
                        || "Alle Status".equals(status)
                        || ermittleMieterStatus(mieter).equals(status)
                )
                .toList();
    }

    private List<Mietvertrag> filtereMietvertraege(List<Mietvertrag> mietvertraege) {
        String suche = holeSuchtext();
        String status = statusFilter.getValue();

        return mietvertraege.stream()
                .filter(this::passtZumAktivenVertragsTab)
                .filter(mietvertrag -> suche.isBlank()
                        || ("MV-" + mietvertrag.getId()).toLowerCase().contains(suche)
                        || UiFormatUtils.formatiereMieterName(mietvertrag.getMieter()).toLowerCase().contains(suche)
                        || UiFormatUtils.formatiereMietobjekt(mietvertrag).toLowerCase().contains(suche)
                )
                .filter(mietvertrag -> aktiverModus != TabellenModus.VERTRAEGE
                        || status == null
                        || "Alle Status".equals(status)
                        || formatiereVertragsstatus(mietvertrag).equals(status)
                )
                .toList();
    }

    // Diese Methode trennt laufende Verträge vom Archiv.
    // Gekündigte Verträge bleiben aktiv, solange ihr Enddatum noch nicht vorbei ist.
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
        return mietvertrag != null
                && (
                mietvertrag.getStatus() == Vertragsstatus.BEENDET
                        || (mietvertrag.getStatus() == Vertragsstatus.GEKUENDIGT && istAusgelaufen(mietvertrag))
        );
    }

    private boolean istAusgelaufen(Mietvertrag mietvertrag) {
        return mietvertrag.getEnddatum() != null && mietvertrag.getEnddatum().isBefore(LocalDate.now());
    }

    private String holeSuchtext() {
        if (searchField.getValue() == null) {
            return "";
        }

        return searchField.getValue().trim().toLowerCase();
    }

    private Component erstelleMieterStatusBadge(Mieter mieter) {
        String status = ermittleMieterStatus(mieter);

        Span badge = new Span(status);
        badge.addClassNames("status-badge", ermittleMieterStatusStil(status));

        return badge;
    }

    private Component erstelleMietvertragStatusBadge(Mietvertrag mietvertrag) {
        String status = formatiereVertragsstatus(mietvertrag);

        Span badge = new Span(status);
        badge.addClassNames("status-badge", ermittleVertragsStatusStil(mietvertrag));

        return badge;
    }

    // Der Mieterstatus wird aus seinen Verträgen abgeleitet.
    // Ein aktiver Vertrag hat Vorrang vor einem auslaufenden Vertrag.
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

    // Für die Mietertabelle wird die aktuelle Einheit aus den Verträgen gesucht.
    // Wenn kein aktiver Vertrag existiert, wird noch ein auslaufender Vertrag angezeigt.
    private String findeAktuelleEinheit(Mieter mieter) {
        List<Mietvertrag> vertraege = mietvertragService.findeMietvertraegeNachMieter(mieter.getId());

        return vertraege.stream()
                .filter(this::istAktiverVertrag)
                .findFirst()
                .map(UiFormatUtils::formatiereMietobjekt)
                .orElseGet(() -> vertraege.stream()
                        .filter(this::istAuslaufenderVertrag)
                        .findFirst()
                        .map(UiFormatUtils::formatiereMietobjekt)
                        .orElse("-")
                );
    }

    private String formatiereLaufzeitOderZeitraum(Mietvertrag mietvertrag) {
        if (aktiverModus == TabellenModus.ARCHIV) {
            return UiFormatUtils.formatiereZeitraum(mietvertrag);
        }

        if (mietvertrag.getEnddatum() == null) {
            return "unbefristet";
        }

        return "bis " + UiFormatUtils.formatiereDatum(mietvertrag.getEnddatum());
    }

    // Ein gekündigter Vertrag wird nach dem Enddatum wie beendet angezeigt.
    // So landet er optisch passend im Archiv.
    private String formatiereVertragsstatus(Mietvertrag mietvertrag) {
        if (mietvertrag == null || mietvertrag.getStatus() == null) {
            return "-";
        }

        if (mietvertrag.getStatus() == Vertragsstatus.GEKUENDIGT && !istAuslaufenderVertrag(mietvertrag)) {
            return Vertragsstatus.BEENDET.getLabel();
        }

        return mietvertrag.getStatus().getLabel();
    }

    private String ermittleMieterStatusStil(String status) {
        return switch (status) {
            case "Aktiv" -> "success";
            case "Läuft aus" -> "warning";
            default -> "neutral";
        };
    }

    private String ermittleVertragsStatusStil(Mietvertrag mietvertrag) {
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

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        String tab = event.getLocation()
                .getQueryParameters()
                .getParameters()
                .getOrDefault("tab", java.util.List.of("mieter"))
                .getFirst();

        if ("vertraege".equalsIgnoreCase(tab)) {
            wechselTabellenModus(TabellenModus.VERTRAEGE);
        } else if ("archiv".equalsIgnoreCase(tab)) {
            wechselTabellenModus(TabellenModus.ARCHIV);
        } else {
            wechselTabellenModus(TabellenModus.MIETER);
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
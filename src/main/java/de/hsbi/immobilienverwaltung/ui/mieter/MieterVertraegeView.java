package de.hsbi.immobilienverwaltung.ui.mieter;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
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

        addClassName("page-content");
        add(createTableCard());
    }

    private Component createTableCard() {
        Div tableCard = new Div();
        tableCard.addClassName("table-card");

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
        mieterGrid.addClassName("immobilien-grid");

        mieterGrid.addColumn(this::formatMieterName)
                .setHeader("Mieter")
                .setAutoWidth(true)
                .setFlexGrow(2);

        mieterGrid.addColumn(mieter -> textOderLeer(mieter.getTelefonnummer()))
                .setHeader("Telefon")
                .setAutoWidth(true)
                .setFlexGrow(1);

        mieterGrid.addColumn(this::findeAktuelleEinheit)
                .setHeader("Aktuelle Einheit")
                .setAutoWidth(true)
                .setFlexGrow(2);

        mieterGrid.addColumn(new ComponentRenderer<>(this::createMieterStatusBadge))
                .setHeader("Status")
                .setAutoWidth(true)
                .setFlexGrow(1);

        mieterGrid.addItemClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate(MieterListView.class, String.valueOf(event.getItem().getId()))
                )
        );
    }

    private void configureMietvertragGrid() {
        mietvertragGrid.removeAllColumns();
        mietvertragGrid.setWidthFull();
        mietvertragGrid.setAllRowsVisible(true);
        mietvertragGrid.addClassName("immobilien-grid");

        mietvertragGrid.addColumn(mietvertrag -> "MV-" + mietvertrag.getId())
                .setHeader("Vertragsnummer")
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

        mietvertragGrid.addColumn(this::formatWarmmiete)
                .setHeader("Miete mtl.")
                .setAutoWidth(true)
                .setFlexGrow(1);

        mietvertragGrid.addColumn(new ComponentRenderer<>(this::createMietvertragStatusBadge))
                .setHeader("Status")
                .setAutoWidth(true)
                .setFlexGrow(1);

        mietvertragGrid.addItemClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate(MietvertragListView.class, String.valueOf(event.getItem().getId()))
                )
        );
    }

    private Component createTableHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.addClassName("table-card-header");
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        tabs = new Tabs(
                new Tab("Mieter"),
                new Tab("Verträge"),
                new Tab("Archiv")
        );

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
        archivTabs.setVisible(false);

        archivTabs.addSelectedChangeListener(event -> {
            archivierteMieterAnzeigen = archivTabs.getSelectedIndex() == 1;
            setAktiverModus(TabellenModus.ARCHIV);
        });

        searchField.setPlaceholder("Suchen...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.setWidth("320px");
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(event -> aktualisiereTabellen());

        statusFilter.setWidth("220px");
        statusFilter.addValueChangeListener(event -> aktualisiereTabellen());

        addButton = new Button(VaadinIcon.PLUS.create());
        addButton.addClassName("primary-button");

        addButton.addClickListener(event -> {
            if (aktiverModus == TabellenModus.MIETER) {
                getUI().ifPresent(ui -> ui.navigate(MieterFormView.class));
            } else if (aktiverModus == TabellenModus.VERTRAEGE) {
                getUI().ifPresent(ui -> ui.navigate(MietvertragFormView.class));
            }
        });

        HorizontalLayout rightArea = new HorizontalLayout();
        rightArea.setAlignItems(FlexComponent.Alignment.CENTER);
        rightArea.setSpacing(true);
        rightArea.add(archivTabs, searchField, statusFilter, addButton);

        header.add(tabs, rightArea);

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

    private String formatMieterName(Mieter mieter) {
        if (mieter == null) {
            return "-";
        }

        return textOderLeer(mieter.getVorname()) + " " + textOderLeer(mieter.getNachname());
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
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

    private final Grid<Mieter> mieterGrid = new Grid<>(Mieter.class, false);
    private final Grid<Mietvertrag> mietvertragGrid = new Grid<>(Mietvertrag.class, false);

    private final TextField searchField = new TextField();
    private final Select<String> statusFilter = new Select<>();

    private enum TabellenModus {
        MIETER,
        VERTRAEGE
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

        aktualisiereTabellen();

        return tableCard;
    }

    private void configureMieterGrid() {
        mieterGrid.removeAllColumns();
        mieterGrid.setWidthFull();
        mieterGrid.setAllRowsVisible(true);

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

        mieterGrid.addColumn(new ComponentRenderer<>(this::createMieterActionButtons))
                .setHeader("Aktionen")
                .setAutoWidth(true)
                .setFlexGrow(0);
    }

    private void configureMietvertragGrid() {
        mietvertragGrid.removeAllColumns();
        mietvertragGrid.setWidthFull();
        mietvertragGrid.setAllRowsVisible(true);

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

        mietvertragGrid.addColumn(this::formatLaufzeitBis)
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

        mietvertragGrid.addColumn(new ComponentRenderer<>(this::createMietvertragActionButtons))
                .setHeader("Aktionen")
                .setAutoWidth(true)
                .setFlexGrow(0);
    }

    private Component createTableHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.addClassName("table-card-header");
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        tabs = new Tabs(
                new Tab("Mieter"),
                new Tab("Verträge")
        );

        tabs.addSelectedChangeListener(event -> {
            if (tabs.getSelectedIndex() == 0) {
                setAktiverModus(TabellenModus.MIETER);
            } else {
                setAktiverModus(TabellenModus.VERTRAEGE);
            }
        });

        searchField.setPlaceholder("Suchen...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.setWidth("320px");
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(event -> aktualisiereTabellen());

        statusFilter.setItems("Alle Status", "Aktiv", "Gekündigt", "Beendet", "Ohne Vertrag");
        statusFilter.setValue("Alle Status");
        statusFilter.setWidth("180px");
        statusFilter.addValueChangeListener(event -> aktualisiereTabellen());

        Button addButton = new Button(VaadinIcon.PLUS.create());
        addButton.addClassName("primary-button");

        addButton.addClickListener(event -> {
            if (aktiverModus == TabellenModus.MIETER) {
                getUI().ifPresent(ui -> ui.navigate(MieterFormView.class));
            } else {
                getUI().ifPresent(ui -> ui.navigate(MietvertragFormView.class));
            }
        });

        HorizontalLayout rightArea = new HorizontalLayout();
        rightArea.setAlignItems(FlexComponent.Alignment.CENTER);
        rightArea.setSpacing(true);
        rightArea.add(searchField, statusFilter, addButton);

        header.add(tabs, rightArea);

        return header;
    }

    private void setAktiverModus(TabellenModus modus) {
        aktiverModus = modus;

        boolean mieterAktiv = modus == TabellenModus.MIETER;

        mieterGrid.setVisible(mieterAktiv);
        mietvertragGrid.setVisible(!mieterAktiv);

        if (tabs != null) {
            int zielIndex = mieterAktiv ? 0 : 1;

            if (tabs.getSelectedIndex() != zielIndex) {
                tabs.setSelectedIndex(zielIndex);
            }
        }

        aktualisiereTabellen();
    }

    private void aktualisiereTabellen() {
        if (aktiverModus == TabellenModus.MIETER) {
            mieterGrid.setItems(filterMieter(mieterService.findeAlleMieter()));
        } else {
            mietvertragGrid.setItems(filterMietvertraege(mietvertragService.findeAlleMietvertraege()));
        }
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
                .filter(mieter -> "Alle Status".equals(status) || ermittleMieterStatus(mieter).equals(status))
                .toList();
    }

    private List<Mietvertrag> filterMietvertraege(List<Mietvertrag> mietvertraege) {
        String suche = getSuche();
        String status = statusFilter.getValue();

        return mietvertraege.stream()
                .filter(mietvertrag -> suche.isBlank()
                        || ("MV-" + mietvertrag.getId()).toLowerCase().contains(suche)
                        || formatMieterName(mietvertrag.getMieter()).toLowerCase().contains(suche)
                        || formatMietobjekt(mietvertrag).toLowerCase().contains(suche)
                )
                .filter(mietvertrag -> "Alle Status".equals(status)
                        || formatVertragsstatus(mietvertrag.getStatus()).equals(status)
                )
                .toList();
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
        String status = formatVertragsstatus(mietvertrag.getStatus());

        Span badge = new Span(status);
        badge.addClassNames("status-badge", getVertragsStatusStyle(mietvertrag.getStatus()));

        return badge;
    }

    private Component createMieterActionButtons(Mieter mieter) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(false);

        Button detailsButton = new Button(VaadinIcon.EYE.create());
        detailsButton.addClassName("icon-button");

        detailsButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate(MieterListView.class, String.valueOf(mieter.getId())))
        );

        actions.add(detailsButton);

        return actions;
    }

    private Component createMietvertragActionButtons(Mietvertrag mietvertrag) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(false);

        Button detailsButton = new Button(VaadinIcon.EYE.create());
        detailsButton.addClassName("icon-button");

        detailsButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate(MietvertragListView.class, String.valueOf(mietvertrag.getId())))
        );

        actions.add(detailsButton);

        return actions;
    }

    private String ermittleMieterStatus(Mieter mieter) {
        List<Mietvertrag> vertraege = mietvertragService.findeMietvertraegeNachMieter(mieter.getId());

        boolean hatAktivenVertrag = vertraege.stream()
                .anyMatch(vertrag -> vertrag.getStatus() == Vertragsstatus.AKTIV);

        if (hatAktivenVertrag) {
            return "Aktiv";
        }

        boolean hatGekuendigtenVertrag = vertraege.stream()
                .anyMatch(vertrag -> vertrag.getStatus() == Vertragsstatus.GEKUENDIGT);

        if (hatGekuendigtenVertrag) {
            return "Gekündigt";
        }

        boolean hatBeendetenVertrag = vertraege.stream()
                .anyMatch(vertrag -> vertrag.getStatus() == Vertragsstatus.BEENDET);

        if (hatBeendetenVertrag) {
            return "Beendet";
        }

        return "Ohne Vertrag";
    }

    private String findeAktuelleEinheit(Mieter mieter) {
        return mietvertragService.findeMietvertraegeNachMieter(mieter.getId())
                .stream()
                .filter(vertrag -> vertrag.getStatus() == Vertragsstatus.AKTIV)
                .findFirst()
                .map(this::formatMietobjekt)
                .orElse("-");
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

    private String formatLaufzeitBis(Mietvertrag mietvertrag) {
        if (mietvertrag.getEnddatum() == null) {
            return "unbefristet";
        }

        return mietvertrag.getEnddatum().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }

    private String formatWarmmiete(Mietvertrag mietvertrag) {
        double kaltmiete = mietvertrag.getKaltmiete() == null ? 0 : mietvertrag.getKaltmiete();
        double nebenkosten = mietvertrag.getNebenkosten() == null ? 0 : mietvertrag.getNebenkosten();

        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.GERMANY);
        return formatter.format(kaltmiete + nebenkosten);
    }

    private String formatVertragsstatus(Vertragsstatus status) {
        if (status == null) {
            return "-";
        }

        return switch (status) {
            case AKTIV -> "Aktiv";
            case GEKUENDIGT -> "Gekündigt";
            case BEENDET -> "Beendet";
        };
    }

    private String getMieterStatusStyle(String status) {
        return switch (status) {
            case "Aktiv" -> "success";
            case "Gekündigt", "Beendet" -> "warning";
            default -> "neutral";
        };
    }

    private String getVertragsStatusStyle(Vertragsstatus status) {
        if (status == null) {
            return "neutral";
        }

        return switch (status) {
            case AKTIV -> "success";
            case GEKUENDIGT, BEENDET -> "warning";
        };
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
        return "Mieter und laufende Mietverhältnisse verwalten";
    }
}
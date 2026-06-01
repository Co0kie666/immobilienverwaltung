package de.hsbi.immobilienverwaltung.ui.immobilien;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import de.hsbi.immobilienverwaltung.domain.Adresse;
import de.hsbi.immobilienverwaltung.domain.Immobilie;
import de.hsbi.immobilienverwaltung.domain.enums.Immobilientyp;
import de.hsbi.immobilienverwaltung.security.LoginRequired;
import de.hsbi.immobilienverwaltung.service.interfaces.ImmobilieService;
import de.hsbi.immobilienverwaltung.service.interfaces.MieteinheitService;
import de.hsbi.immobilienverwaltung.ui.layout.HasPageHeader;
import de.hsbi.immobilienverwaltung.ui.layout.MainLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import java.util.ArrayList;
import java.util.List;

@Route(value = "immobilien", layout = MainLayout.class)
public class ImmobilienListView extends Div implements HasPageHeader, LoginRequired {

    private final Grid<Immobilie> grid = new Grid<>(Immobilie.class, false);
    private final ImmobilieService immobilieService;
    private final MieteinheitService mieteinheitService;

    private final Select<Immobilientyp> typSelect = new Select<>();
    private final Select<String> unitsSelect = new Select<>();
    private final Select<String> vacancySelect = new Select<>();
    private final TextField locationField = new TextField();

    // Attribute fuer Pagination
    private static final int IMMOBILIEN_PRO_SEITE = 12;
    private int aktuelleSeite = 0;
    private List<Immobilie> gefilterteImmobilien = new ArrayList<>();
    private final Button vorherigeSeiteButton = new Button("Zurück", VaadinIcon.ARROW_LEFT.create());
    private final Button naechsteSeiteButton = new Button("Weiter", VaadinIcon.ARROW_RIGHT.create());
    private final Span seitenInfo = new Span();

    public ImmobilienListView(ImmobilieService immobilieService, MieteinheitService mieteinheitenService) {
        this.immobilieService = immobilieService;
        this.mieteinheitService = mieteinheitenService;
        addClassName("immobilien-list-view");
        addClassName("page-content");

        add(
                createProperty(),
                createFilterCard(),
                createTableCard()
        );

        ladeImmobilien();
    }

    private void ladeImmobilien() {
        wendeFilterAn();
    }

    private String formatAdresse(Immobilie immobilie) {
        Adresse adresse = immobilie.getAdresse();

        if (adresse == null) {
            return "-";
        }

        return adresse.getStrasse() + " "
                + adresse.getHausnummer() + ", "
                + adresse.getPlz() + " "
                + adresse.getStadt();
    }

    private String formatTyp(Immobilientyp typ) {
        if (typ == null) {
            return "-";
        }

        return switch (typ) {
            case WOHNGEBAEUDE -> "Wohngebäude";
            case MEHRFAMILIENHAUS -> "Mehrfamilienhaus";
            case GEWERBEIMMOBILIE -> "Gewerbeimmobilie";
        };
    }

    // Damit bei der SelectBox kein Bindestrich angezeigt wird
    private String formatTypFilter(Immobilientyp typ) {
        if (typ == null) {
            return "Alle Typen";
        }

        return formatTyp(typ);
    }

    @Override
    public String getPageTitle() {
        return "Immobilienübersicht";
    }

    @Override
    public String getPageSubtitle() {
        return "Hier werden alle Immobilien angezeigt";
    }

    // Immobilie anlegen Button
    private Div createProperty() {
        Div createButton = new Div();
        createButton.addClassName("immobilien-action-bar");

        Button newButton = new Button("Neu anlegen", VaadinIcon.PLUS.create());
        newButton.addClassName("primary-button");

        newButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate(ImmobilieFormView.class))
        );

        createButton.add(newButton);

        return createButton;
    }

    // Filter mit Ort, Status, Einheiten und Leerstand
    private Div createFilterCard() {
        Div filterCard = new Div();
        filterCard.addClassName("filter-card");

        locationField.setLabel("Ort / PLZ");
        locationField.setPlaceholder("Alle Orte");
        locationField.setPrefixComponent(VaadinIcon.MAP_MARKER.create());
        locationField.setClearButtonVisible(true);
        locationField.setValueChangeMode(ValueChangeMode.LAZY); // Filter erst nach kurzer Pause anwenden

        typSelect.setLabel("Immobilientyp");
        typSelect.setItems(Immobilientyp.values());
        typSelect.setItemLabelGenerator(this::formatTypFilter);
        typSelect.setEmptySelectionAllowed(true);
        typSelect.setEmptySelectionCaption("Alle Typen");

        unitsSelect.setLabel("Einheiten");
        unitsSelect.setItems("Alle Größen", "1-5 Einheiten", "6-20 Einheiten", "20+ Einheiten");
        unitsSelect.setValue("Alle Größen");

        vacancySelect.setLabel("Leerstand");
        vacancySelect.setItems("Alle anzeigen", "Mit Leerstand", "Ohne Leerstand");
        vacancySelect.setValue("Alle anzeigen");

        typSelect.addValueChangeListener(event -> wendeFilterAn());
        unitsSelect.addValueChangeListener(event -> wendeFilterAn());
        vacancySelect.addValueChangeListener(event -> wendeFilterAn());
        locationField.addValueChangeListener(event -> wendeFilterAn());

        filterCard.add(locationField, typSelect, unitsSelect, vacancySelect);

        return filterCard;
    }

    private void wendeFilterAn() {
        gefilterteImmobilien = immobilieService.findeGefilterteImmobilien(
                locationField.getValue(),
                typSelect.getValue(),
                unitsSelect.getValue(),
                vacancySelect.getValue()
        );

        aktuelleSeite = 0;
        aktualisiereTabellenSeite();
    }

    // Tabelle erstellen
    private Div createTableCard() {
        Div tableCard = new Div();
        tableCard.addClassName("table-card");

        configureGrid();

        tableCard.add(grid, createPaginationBar());

        return tableCard;
    }

    private HorizontalLayout createPaginationBar() {
        HorizontalLayout paginationBar = new HorizontalLayout();
        paginationBar.addClassName("pagination-bar");

        vorherigeSeiteButton.addClassName("secondary-button");
        naechsteSeiteButton.addClassName("secondary-button");
        seitenInfo.addClassName("pagination-info");

        vorherigeSeiteButton.addClickListener(event -> {
            if (aktuelleSeite > 0) {
                aktuelleSeite--;
                aktualisiereTabellenSeite();
            }
        });

        naechsteSeiteButton.addClickListener(event -> {
            if (aktuelleSeite < berechneGesamtSeiten() - 1) {
                aktuelleSeite++;
                aktualisiereTabellenSeite();
            }
        });

        paginationBar.add(vorherigeSeiteButton, seitenInfo, naechsteSeiteButton);

        return paginationBar;
    }

    private void aktualisiereTabellenSeite() {
        if (gefilterteImmobilien.isEmpty()) {
            grid.setItems(new ArrayList<>());
            seitenInfo.setText("Keine Immobilien gefunden");
            vorherigeSeiteButton.setEnabled(false);
            naechsteSeiteButton.setEnabled(false);
            return;
        }

        int gesamtSeiten = berechneGesamtSeiten();

        if (aktuelleSeite >= gesamtSeiten) {
            aktuelleSeite = gesamtSeiten - 1;
        }

        int start = aktuelleSeite * IMMOBILIEN_PRO_SEITE;
        int ende = Math.min(start + IMMOBILIEN_PRO_SEITE, gefilterteImmobilien.size());

        List<Immobilie> immobilienAufAktuellerSeite = gefilterteImmobilien.subList(start, ende);

        grid.setItems(immobilienAufAktuellerSeite);

        seitenInfo.setText("Seite " + (aktuelleSeite + 1) + " von " + gesamtSeiten);

        vorherigeSeiteButton.setEnabled(aktuelleSeite > 0);
        naechsteSeiteButton.setEnabled(aktuelleSeite < gesamtSeiten - 1);
    }

    private int berechneGesamtSeiten() {
        return (int) Math.ceil((double) gefilterteImmobilien.size() / IMMOBILIEN_PRO_SEITE);
    }

    private void configureGrid() {
        grid.addClassName("immobilien-grid");
        grid.setAllRowsVisible(true);

        grid.addColumn(Immobilie::getBezeichnung)
            .setHeader("Immobilie")
            .setAutoWidth(true)
            .setFlexGrow(2);

        grid.addColumn(immobilie -> formatTyp(immobilie.getTyp()))
            .setHeader("Typ")
            .setAutoWidth(true);

        grid.addColumn(this::formatAdresse)
            .setHeader("Adresse")
            .setAutoWidth(true)
            .setFlexGrow(2);

        grid.addColumn(immobilie -> mieteinheitService.zaehleMieteinheiten(immobilie.getId()))
            .setHeader("Einheiten")
            .setAutoWidth(true);

        grid.addColumn(immobilie -> {
            long leerstand = mieteinheitService.zaehleFreieMieteinheiten(immobilie.getId());
            double leerstandsquote = mieteinheitService.berechneLeerstandsquote(immobilie.getId());

            return leerstand + " (" + String.format("%.1f%%", leerstandsquote) + ")";
        })
            .setHeader("Leerstand")
            .setAutoWidth(true);

        grid.addColumn(immobilie -> "-")
            .setHeader("Offene Posten")
            .setAutoWidth(true);

        grid.addItemClickListener(event ->
            getUI().ifPresent(ui -> ui.navigate("immobilien/" + event.getItem().getId()))
        );
    }
}
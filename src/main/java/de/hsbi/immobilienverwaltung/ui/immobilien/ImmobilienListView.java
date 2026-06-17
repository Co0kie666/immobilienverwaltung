package de.hsbi.immobilienverwaltung.ui.immobilien;

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
import de.hsbi.immobilienverwaltung.service.interfaces.ImmobilieService;
import de.hsbi.immobilienverwaltung.service.interfaces.MieteinheitService;
import de.hsbi.immobilienverwaltung.service.interfaces.ZahlungsEingangService;
import de.hsbi.immobilienverwaltung.ui.layout.HasPageHeader;
import de.hsbi.immobilienverwaltung.ui.layout.MainLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import jakarta.annotation.security.PermitAll;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Die View zeigt Immobilien nicht direkt aus dem Repository an,
// sondern nutzt den Service, damit Filter- und Berechnungslogik zentral bleibt.
@Route(value = "immobilien", layout = MainLayout.class)
@PermitAll
public class ImmobilienListView extends Div implements HasPageHeader {

    private final Grid<Immobilie> grid = new Grid<>(Immobilie.class, false);
    private final ImmobilieService immobilieService;
    private final MieteinheitService mieteinheitService;
    private final ZahlungsEingangService zahlungsEingangService;

    private final Select<Immobilientyp> typSelect = new Select<>();
    private final Select<String> unitsSelect = new Select<>();
    private final Select<String> vacancySelect = new Select<>();
    private final TextField locationField = new TextField();

    // Pagination Attribute
    private static final int IMMOBILIEN_PRO_SEITE = 12;
    private int aktuelleSeite = 0;
    private List<Immobilie> gefilterteImmobilien = new ArrayList<>();
    private final Button vorherigeSeiteButton = new Button("Zurück", VaadinIcon.ARROW_LEFT.create());
    private final Button naechsteSeiteButton = new Button("Weiter", VaadinIcon.ARROW_RIGHT.create());
    private final Span seitenInfo = new Span();

    public ImmobilienListView(ImmobilieService immobilieService, MieteinheitService mieteinheitenService, ZahlungsEingangService zahlungsEingangService) {
        this.immobilieService = immobilieService;
        this.mieteinheitService = mieteinheitenService;
        this.zahlungsEingangService = zahlungsEingangService;
        addClassName("immobilien-list-view");
        addClassName("page-content");

        add(
                immobilieAnlegen(),
                erstelleFilterKarten(),
                erstelleTabellenKarten()
        );

        wendeFilterAn();
    }

    private Div immobilieAnlegen() {
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

    // Filter für Ort, Status, Einheiten und Leerstand
    private Div erstelleFilterKarten() {
        Div filterCard = new Div();
        filterCard.addClassName("filter-card");

        locationField.setLabel("Ort / PLZ");
        locationField.setPlaceholder("Alle Orte");
        locationField.setPrefixComponent(VaadinIcon.MAP_MARKER.create());
        locationField.setClearButtonVisible(true);
        // Filter wird erst nach einer kurzen Eingabepause angewendet,
        // damit nicht bei jedem einzelnen Tastendruck sofort neu gefiltert wird.
        locationField.setValueChangeMode(ValueChangeMode.LAZY);

        typSelect.setLabel("Immobilientyp");
        typSelect.setItems(Immobilientyp.values());
        typSelect.setItemLabelGenerator(typ -> typ == null ? "Alle Typen" : typ.getLabel());
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

    // Die eigentliche Filterlogik liegt im Service.
    // Nach jeder Filteränderung wird wieder auf die erste Seite gewechselt.
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

    private Div erstelleTabellenKarten() {
        Div tableCard = new Div();
        tableCard.addClassName("table-card");

        konfiguriereImmobilienTabelle();

        tableCard.add(grid, erstelleSeitennavigation());

        return tableCard;
    }

    private HorizontalLayout erstelleSeitennavigation() {
        HorizontalLayout paginationBar = new HorizontalLayout();
        paginationBar.addClassName("pagination-bar");

        vorherigeSeiteButton.addClassName("secondary-button");
        naechsteSeiteButton.addClassName("secondary-button");
        naechsteSeiteButton.setIconAfterText(true);
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

    // Schneidet aus der gefilterten Gesamtliste nur die Einträge heraus,
    // die auf der aktuellen Seite angezeigt werden sollen
    private void aktualisiereTabellenSeite() {
        // wenn die Filter keine Ergebnisse liefern
        if (gefilterteImmobilien.isEmpty()) {
            // Tabelle leeren
            grid.setItems(new ArrayList<>());
            seitenInfo.setText("Keine Immobilien gefunden");
            vorherigeSeiteButton.setEnabled(false);
            naechsteSeiteButton.setEnabled(false);
            return;
        }

        int gesamtSeiten = berechneGesamtSeiten();

        // Falls sich die Anzahl der Seiten durch Filtern oder Löschen verkleinert,
        // wird die aktuelle Seite auf die letzte noch vorhandene Seite korrigiert
        if (aktuelleSeite >= gesamtSeiten) {
            aktuelleSeite = gesamtSeiten - 1;
        }

        int start = aktuelleSeite * IMMOBILIEN_PRO_SEITE;
        // Math.min(...) verhindert, dass das Ende größer als die Liste wird -> wichtig für letzte Seite
        int ende = Math.min(start + IMMOBILIEN_PRO_SEITE, gefilterteImmobilien.size());

        // subList(start, ende) schneidet aus der ganzen Liste nur den Teil heraus, der auf der aktuellen Seite angezeigt werden soll
        List<Immobilie> immobilienAufAktuellerSeite = gefilterteImmobilien.subList(start, ende);

        grid.setItems(immobilienAufAktuellerSeite);

        seitenInfo.setText("Seite " + (aktuelleSeite + 1) + " von " + gesamtSeiten);

        vorherigeSeiteButton.setEnabled(aktuelleSeite > 0);
        naechsteSeiteButton.setEnabled(aktuelleSeite < gesamtSeiten - 1);
    }

    // Aufrunden ist notwendig, damit auch eine nicht vollständig gefüllte letzte Seite angezeigt wird.
    private int berechneGesamtSeiten() {
        return (int) Math.ceil((double) gefilterteImmobilien.size() / IMMOBILIEN_PRO_SEITE);
    }

    private void konfiguriereImmobilienTabelle() {
        grid.addClassName("immobilien-grid");
        grid.setAllRowsVisible(true);

        grid.addColumn(Immobilie::getBezeichnung)
            .setHeader("Immobilie")
            .setAutoWidth(true)
            .setFlexGrow(2);

        grid.addColumn(immobilie -> immobilie.getTyp() == null ? "-" : immobilie.getTyp().getLabel())
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
            long frei = mieteinheitService.zaehleFreieMieteinheiten(immobilie.getId());
            long inRenovierung = mieteinheitService.zaehleMieteinheitenInRenovierung(immobilie.getId());
            long leerstand = frei + inRenovierung;
            double leerstandsquote = mieteinheitService.berechneLeerstandsquote(immobilie.getId());

            return leerstand + " (" + String.format("%.1f%%", leerstandsquote) + ")";
        })
            .setHeader("Leerstand")
            .setAutoWidth(true);

        grid.addColumn(immobilie -> formatiereBetrag(zahlungsEingangService.
                        berechneOffeneZahlungenFuerImmobilie(immobilie.getId())))
            .setHeader("Offene Posten")
            .setAutoWidth(true);

        // Beim Klick auf eine Tabellenzeile wird die ID der gewählten Immobilie
        // in die URL geschrieben. Die DetailView liest diese ID später als Route-Parameter aus.
        grid.addItemClickListener(event ->
            getUI().ifPresent(ui -> ui.navigate("immobilien/" + event.getItem().getId()))
        );
    }

    private String formatiereBetrag(BigDecimal betrag) {
        if (betrag == null) {
            return "0,00 €";
        }

        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.GERMANY);
        return formatter.format(betrag);
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

    @Override
    public String getPageTitle() {
        return "Immobilienübersicht";
    }

    @Override
    public String getPageSubtitle() {
        return "Hier werden alle Immobilien angezeigt";
    }
}
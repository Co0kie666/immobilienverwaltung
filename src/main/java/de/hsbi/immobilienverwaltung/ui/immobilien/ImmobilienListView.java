package de.hsbi.immobilienverwaltung.ui.immobilien;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import de.hsbi.immobilienverwaltung.domain.Immobilie;
import de.hsbi.immobilienverwaltung.domain.enums.Immobilientyp;
import de.hsbi.immobilienverwaltung.service.interfaces.ImmobilieService;
import de.hsbi.immobilienverwaltung.service.interfaces.MieteinheitService;
import de.hsbi.immobilienverwaltung.service.interfaces.ZahlungsEingangService;
import de.hsbi.immobilienverwaltung.ui.components.StatusBadge;
import de.hsbi.immobilienverwaltung.ui.layout.HasPageHeader;
import de.hsbi.immobilienverwaltung.ui.layout.MainLayout;
import jakarta.annotation.security.PermitAll;
import de.hsbi.immobilienverwaltung.ui.UiFormatUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Route(value = "immobilien", layout = MainLayout.class)
@PermitAll
public class ImmobilienListView extends Div implements HasPageHeader {

    private final ImmobilieService immobilieService;
    private final MieteinheitService mieteinheitService;
    private final ZahlungsEingangService zahlungsEingangService;

    private final Select<Immobilientyp> typSelect = new Select<>();
    private final Select<String> unitsSelect = new Select<>();
    private final Select<String> vacancySelect = new Select<>();
    private final TextField locationField = new TextField();

    private static final int IMMOBILIEN_PRO_SEITE = 12;
    private int aktuelleSeite = 0;
    private List<Immobilie> gefilterteImmobilien = new ArrayList<>();

    private final Div cardGrid = new Div();
    private final Div emptyState = new Div();
    private final Button vorherigeSeiteButton = new Button("Zurück", VaadinIcon.ARROW_LEFT.create());
    private final Button naechsteSeiteButton = new Button("Weiter", VaadinIcon.ARROW_RIGHT.create());
    private final Span seitenInfo = new Span();

    public ImmobilienListView(
            ImmobilieService immobilieService,
            MieteinheitService mieteinheitenService,
            ZahlungsEingangService zahlungsEingangService
    ) {
        this.immobilieService = immobilieService;
        this.mieteinheitService = mieteinheitenService;
        this.zahlungsEingangService = zahlungsEingangService;

        addClassName("immobilien-list-view");
        addClassName("page-content");

        add(
                erstelleHeroBereich(),
                erstelleFilterKarten(),
                erstelleImmobilienKartenBereich()
        );

        wendeFilterAn();
    }

    private Component erstelleHeroBereich() {
        Div hero = new Div();
        hero.addClassName("property-list-hero");

        Div content = new Div();
        content.addClassName("property-list-hero-content");

        Span eyebrow = new Span("Immobilienportfolio");
        eyebrow.addClassName("hero-eyebrow");

        H3 title = new H3("Alle Objekte im Überblick");
        title.addClassName("property-list-hero-title");

        Paragraph subtitle = new Paragraph("Filtern, prüfen und direkt in die Detailansicht springen – mit kompakten Kennzahlen pro Immobilie.");
        subtitle.addClassName("property-list-hero-subtitle");

        Button newButton = new Button("Neue Immobilie", VaadinIcon.BUILDING.create());
        newButton.addClassName("primary-button");
        newButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate(ImmobilieFormView.class))
        );

        content.add(eyebrow, title, subtitle, newButton);

        hero.add(content);
        return hero;
    }

    private Div erstelleFilterKarten() {
        Div filterCard = new Div();
        filterCard.addClassName("filter-card");
        filterCard.addClassName("filter-card-modern");

        locationField.setLabel("Ort / PLZ");
        locationField.setPlaceholder("z. B. Bielefeld");
        locationField.setPrefixComponent(VaadinIcon.MAP_MARKER.create());
        locationField.setClearButtonVisible(true);
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

    private void wendeFilterAn() {
        gefilterteImmobilien = immobilieService.findeGefilterteImmobilien(
                locationField.getValue(),
                typSelect.getValue(),
                unitsSelect.getValue(),
                vacancySelect.getValue()
        );

        aktuelleSeite = 0;
        aktualisiereKartenSeite();
    }

    private Div erstelleImmobilienKartenBereich() {
        Div panel = new Div();
        panel.addClassName("property-results-panel");

        cardGrid.addClassName("property-card-grid");
        konfiguriereEmptyState();

        panel.add(cardGrid, emptyState, erstelleSeitennavigation());
        return panel;
    }

    private void konfiguriereEmptyState() {
        emptyState.addClassName("property-empty-state");
        emptyState.setVisible(false);

        Div iconBox = new Div(VaadinIcon.SEARCH.create());
        iconBox.addClassName("property-empty-icon");

        H3 title = new H3("Keine Immobilien gefunden");
        Paragraph text = new Paragraph("Passe die Filter an oder lege eine neue Immobilie an, um dein Portfolio zu erweitern.");

        Button createButton = new Button("Immobilie anlegen", VaadinIcon.PLUS.create());
        createButton.addClassName("primary-button");
        createButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate(ImmobilieFormView.class))
        );

        emptyState.add(iconBox, title, text, createButton);
    }

    private HorizontalLayout erstelleSeitennavigation() {
        HorizontalLayout paginationBar = new HorizontalLayout();
        paginationBar.addClassName("pagination-bar");
        paginationBar.addClassName("pagination-bar-modern");

        vorherigeSeiteButton.addClassName("secondary-button");
        naechsteSeiteButton.addClassName("secondary-button");
        naechsteSeiteButton.setIconAfterText(true);
        seitenInfo.addClassName("pagination-info");

        vorherigeSeiteButton.addClickListener(event -> {
            if (aktuelleSeite > 0) {
                aktuelleSeite--;
                aktualisiereKartenSeite();
            }
        });

        naechsteSeiteButton.addClickListener(event -> {
            if (aktuelleSeite < berechneGesamtSeiten() - 1) {
                aktuelleSeite++;
                aktualisiereKartenSeite();
            }
        });

        paginationBar.add(vorherigeSeiteButton, seitenInfo, naechsteSeiteButton);
        return paginationBar;
    }

    private void aktualisiereKartenSeite() {
        cardGrid.removeAll();

        if (gefilterteImmobilien.isEmpty()) {
            emptyState.setVisible(true);
            seitenInfo.setText("Keine Immobilien gefunden");
            vorherigeSeiteButton.setEnabled(false);
            naechsteSeiteButton.setEnabled(false);
            return;
        }

        emptyState.setVisible(false);

        int gesamtSeiten = berechneGesamtSeiten();

        if (aktuelleSeite >= gesamtSeiten) {
            aktuelleSeite = gesamtSeiten - 1;
        }

        int start = aktuelleSeite * IMMOBILIEN_PRO_SEITE;
        int ende = Math.min(start + IMMOBILIEN_PRO_SEITE, gefilterteImmobilien.size());

        gefilterteImmobilien.subList(start, ende).forEach(immobilie ->
                cardGrid.add(erstelleImmobilienCard(immobilie))
        );

        seitenInfo.setText("Seite " + (aktuelleSeite + 1) + " von " + gesamtSeiten
                + " · " + gefilterteImmobilien.size() + " Objekt(e)");

        vorherigeSeiteButton.setEnabled(aktuelleSeite > 0);
        naechsteSeiteButton.setEnabled(aktuelleSeite < gesamtSeiten - 1);
    }

    private int berechneGesamtSeiten() {
        return (int) Math.ceil((double) gefilterteImmobilien.size() / IMMOBILIEN_PRO_SEITE);
    }

    private Component erstelleImmobilienCard(Immobilie immobilie) {
        Div card = new Div();
        card.addClassName("property-card");
        card.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate("immobilien/" + immobilie.getId()))
        );

        Div visual = new Div();
        visual.addClassNames("property-card-visual", ermittleTypCssKlasse(immobilie));

        Icon buildingIcon = VaadinIcon.BUILDING.create();
        buildingIcon.addClassName("property-card-visual-icon");

        Span typeChip = new Span(UiFormatUtils.formatiereImmobilientyp(immobilie.getTyp()));
        typeChip.addClassName("property-type-chip");

        visual.add(buildingIcon, typeChip);

        Div content = new Div();
        content.addClassName("property-card-content");

        Div titleRow = new Div();
        titleRow.addClassName("property-card-title-row");

        H3 title = new H3(UiFormatUtils.wertOderStrich(immobilie.getBezeichnung()));
        title.addClassName("property-card-title");

        titleRow.add(title, erstelleBelegungsBadge(immobilie));

        Paragraph address = new Paragraph(
                UiFormatUtils.formatiereAdresse(immobilie, "Adresse nicht hinterlegt")
        );
        address.addClassName("property-card-address");

        Div stats = new Div();
        stats.addClassName("property-stat-grid");
        stats.add(
                erstelleStatistikKachel("Einheiten", String.valueOf(mieteinheitService.zaehleMieteinheiten(immobilie.getId()))),
                erstelleStatistikKachel("Leerstand", ermittleLeerstandText(immobilie)),
                erstelleStatistikKachel("Offen", UiFormatUtils.formatiereBetragKurz(
                        zahlungsEingangService.berechneOffeneZahlungenFuerImmobilie(immobilie.getId())
                ))
        );

        Button detailsButton = new Button("Details ansehen", VaadinIcon.ARROW_RIGHT.create());
        detailsButton.addClassName("property-card-action");
        detailsButton.setIconAfterText(true);
        detailsButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate("immobilien/" + immobilie.getId()))
        );

        content.add(titleRow, address, stats, detailsButton);
        card.add(visual, content);

        return card;
    }

    private Component erstelleBelegungsBadge(Immobilie immobilie) {
        long gesamt = mieteinheitService.zaehleMieteinheiten(immobilie.getId());
        long frei = mieteinheitService.zaehleFreieMieteinheiten(immobilie.getId());
        long inRenovierung = mieteinheitService.zaehleMieteinheitenInRenovierung(immobilie.getId());
        long leerstand = frei + inRenovierung;

        if (gesamt == 0) {
            return StatusBadge.neutral("Keine Einheiten");
        }

        if (leerstand == 0) {
            return StatusBadge.success("Voll vermietet");
        }

        if (leerstand == gesamt) {
            return StatusBadge.warning("Leerstand");
        }

        return StatusBadge.warning(leerstand + " frei");
    }

    private Component erstelleStatistikKachel(String label, String value) {
        Div tile = new Div();
        tile.addClassName("property-stat-tile");

        Span labelSpan = new Span(label);
        labelSpan.addClassName("property-stat-label");

        Span valueSpan = new Span(value);
        valueSpan.addClassName("property-stat-value");

        tile.add(labelSpan, valueSpan);
        return tile;
    }

    private String ermittleLeerstandText(Immobilie immobilie) {
        long frei = mieteinheitService.zaehleFreieMieteinheiten(immobilie.getId());
        long inRenovierung = mieteinheitService.zaehleMieteinheitenInRenovierung(immobilie.getId());
        long leerstand = frei + inRenovierung;
        double leerstandsquote = mieteinheitService.berechneLeerstandsquote(immobilie.getId());

        return leerstand + " · " + String.format(Locale.GERMANY, "%.1f %%", leerstandsquote);
    }

    private String ermittleTypCssKlasse(Immobilie immobilie) {
        if (immobilie.getTyp() == null) {
            return "typ-default";
        }

        String typName = immobilie.getTyp()
                .name()
                .toLowerCase(Locale.ROOT)
                .replace('_', '-');

        return "typ-" + typName;
    }

    @Override
    public String getPageTitle() {
        return "Immobilienübersicht";
    }

    @Override
    public String getPageSubtitle() {
        return "Modernes Portfolio mit Kennzahlen, Leerstand und offenen Posten";
    }
}

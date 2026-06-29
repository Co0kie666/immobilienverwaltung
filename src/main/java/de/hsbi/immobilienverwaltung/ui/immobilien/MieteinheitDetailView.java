package de.hsbi.immobilienverwaltung.ui.immobilien;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import de.hsbi.immobilienverwaltung.domain.Mieteinheit;
import de.hsbi.immobilienverwaltung.domain.Mietvertrag;
import de.hsbi.immobilienverwaltung.domain.enums.Mieteinheitstatus;
import de.hsbi.immobilienverwaltung.domain.enums.Vertragsstatus;
import de.hsbi.immobilienverwaltung.service.interfaces.MieteinheitService;
import de.hsbi.immobilienverwaltung.service.interfaces.MietvertragService;
import de.hsbi.immobilienverwaltung.ui.components.ConfirmDeleteDialog;
import de.hsbi.immobilienverwaltung.ui.components.StatusBadge;
import de.hsbi.immobilienverwaltung.ui.layout.HasPageHeader;
import de.hsbi.immobilienverwaltung.ui.layout.MainLayout;
import de.hsbi.immobilienverwaltung.ui.mieter.MietvertragListView;
import jakarta.annotation.security.PermitAll;
import de.hsbi.immobilienverwaltung.ui.UiFormatUtils;
import java.util.List;

@Route(value = "immobilien/:immobilieId/einheiten/:mieteinheitId/details", layout = MainLayout.class)
@PermitAll
public class MieteinheitDetailView extends Div implements HasPageHeader, BeforeEnterObserver {

    private final MieteinheitService mieteinheitService;
    private final MietvertragService mietvertragService;

    private Long immobilieId;
    private Long mieteinheitId;

    private Mieteinheit mieteinheit;

    private List<Mietvertrag> mietvertraege = List.of();

    public MieteinheitDetailView(
            MieteinheitService mieteinheitService,
            MietvertragService mietvertragService
    ) {
        this.mieteinheitService = mieteinheitService;
        this.mietvertragService = mietvertragService;

        addClassName("page-content");
        addClassName("mieteinheit-detail-view");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Die IDs werden aus der Route gelesen.
        // Beispiel: /immobilien/1/einheiten/5/details
        // immobilieId = 1, mieteinheitId = 5
        this.immobilieId = event.getRouteParameters()
                .get("immobilieId")
                .map(Long::valueOf)
                .orElseThrow(() -> new IllegalArgumentException("Immobilie-ID fehlt."));

        this.mieteinheitId = event.getRouteParameters()
                .get("mieteinheitId")
                .map(Long::valueOf)
                .orElseThrow(() -> new IllegalArgumentException("Mieteinheit-ID fehlt."));

        ladeMieteinheit();
        ladeMietvertraege();

        removeAll();

        add(
                erstelleHeroBereich(),
                erstelleKennzahlenBereich(),
                erstelleDetailBereich(),
                erstelleMietvertragHistorieKarte()
        );
    }

    private void ladeMieteinheit() {
        this.mieteinheit = mieteinheitService.findeMieteinheitNachId(mieteinheitId)
                .orElseThrow(() -> new IllegalArgumentException("Mieteinheit wurde nicht gefunden."));
    }

    private void ladeMietvertraege() {
        this.mietvertraege = mietvertragService.findeMietvertraegeNachMieteinheit(mieteinheitId);

        if (this.mietvertraege == null) {
            this.mietvertraege = List.of();
        }
    }

    private Component erstelleHeroBereich() {
        Div hero = new Div();
        hero.addClassNames("unit-detail-hero", "unit-detail-hero-" + ermittleStatusCssKlasse());

        Div visual = new Div();
        visual.addClassName("unit-hero-visual");

        Div iconCircle = new Div(VaadinIcon.HOME.create());
        iconCircle.addClassName("unit-hero-icon");

        Span typeLabel = new Span(UiFormatUtils.formatiereMieteinheitTyp(mieteinheit));
        typeLabel.addClassName("unit-hero-type");

        visual.add(iconCircle, typeLabel);

        Div content = new Div();
        content.addClassName("unit-hero-content");

        Div eyebrowRow = new Div();
        eyebrowRow.addClassName("unit-hero-eyebrow-row");

        Span eyebrow = new Span("Mieteinheit");
        eyebrow.addClassName("unit-hero-eyebrow");

        eyebrowRow.add(eyebrow, erstelleMieteinheitStatusBadge());

        H2 title = new H2(UiFormatUtils.wertOderStrich(mieteinheit.getBezeichnung()));
        title.addClassName("unit-hero-title");

        Paragraph subtitle = new Paragraph(
                UiFormatUtils.formatiereMieteinheitTyp(mieteinheit.getTyp())
                        + " • "
                        + UiFormatUtils.formatiereFlaeche(mieteinheit.getGroesse())
                        + " • "
                        + UiFormatUtils.formatiereZimmer(mieteinheit.getZimmerzahl())
        );
        subtitle.addClassName("unit-hero-subtitle");

        Div facts = new Div();
        facts.addClassName("unit-hero-facts");
        facts.add(
                erstelleHeroFact("Stockwerk", UiFormatUtils.wertOderStrich(mieteinheit.getStockwerk())),
                erstelleHeroFact("Historie", zaehleHistorischeVertraege() + " Vertrag(e)"),
                erstelleHeroFact("Warmmiete", formatiereAktuelleWarmmiete())
        );

        content.add(eyebrowRow, title, subtitle, facts);

        Div actions = new Div();
        actions.addClassName("unit-hero-actions");

        Button zurueckButton = new Button("Zurück", VaadinIcon.ARROW_LEFT.create());
        zurueckButton.addClassName("secondary-button");
        zurueckButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate("immobilien/" + immobilieId))
        );

        Button bearbeitenButton = new Button("Bearbeiten", VaadinIcon.EDIT.create());
        bearbeitenButton.addClassName("primary-button");
        bearbeitenButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate(
                        "immobilien/" + immobilieId + "/einheiten/" + mieteinheitId + "/bearbeiten"
                ))
        );

        Button loeschenButton = new Button("Löschen", VaadinIcon.TRASH.create());
        loeschenButton.addClassName("danger-button");
        loeschenButton.addClickListener(event -> oeffneLoeschDialog());

        actions.add(zurueckButton, bearbeitenButton, loeschenButton);

        hero.add(visual, content, actions);

        return hero;
    }

    private Component erstelleHeroFact(String labelText, String valueText) {
        Div fact = new Div();
        fact.addClassName("unit-hero-fact");

        Span label = new Span(labelText);
        label.addClassName("unit-hero-fact-label");

        Span value = new Span(valueText);
        value.addClassName("unit-hero-fact-value");

        fact.add(label, value);
        return fact;
    }

    private Component erstelleKennzahlenBereich() {
        Div grid = new Div();
        grid.addClassName("unit-kpi-grid");

        Mietvertrag aktuellerMietvertrag = findeAktuellenMietvertrag();

        grid.add(
                erstelleKpiKarte(
                        "Status",
                        UiFormatUtils.formatiereMieteinheitStatus(mieteinheit.getStatus()),
                        aktuellerMietvertrag == null ? "Aktuell ohne laufenden Vertrag" : "Laufender Vertrag vorhanden",
                        VaadinIcon.INFO_CIRCLE,
                        ermittleStatusCssKlasse()
                ),
                erstelleKpiKarte(
                        "Größe",
                        UiFormatUtils.formatiereFlaeche(mieteinheit.getGroesse()),
                        UiFormatUtils.formatiereZimmer(mieteinheit.getZimmerzahl()),
                        VaadinIcon.EXPAND_SQUARE,
                        "primary"
                ),
                erstelleKpiKarte(
                        "Warmmiete",
                        formatiereAktuelleWarmmiete(),
                        aktuellerMietvertrag == null ? "Keine aktive Miete" : "Kaltmiete + Nebenkosten",
                        VaadinIcon.EURO,
                        aktuellerMietvertrag == null ? "neutral" : "success"
                ),
                erstelleKpiKarte(
                        "Vertragshistorie",
                        String.valueOf(mietvertraege.size()),
                        zaehleHistorischeVertraege() + " historische Vertrag(e)",
                        VaadinIcon.ARCHIVE,
                        "warning"
                )
        );

        return grid;
    }

    private Component erstelleKpiKarte(String titel, String wert, String untertitel, VaadinIcon symbol, String farbe) {
        Div karte = new Div();
        karte.addClassNames("unit-kpi-card", farbe);

        Div top = new Div();
        top.addClassName("unit-kpi-top");

        Div text = new Div();

        Span titelText = new Span(titel);
        titelText.addClassName("unit-kpi-title");

        Span wertText = new Span(wert);
        wertText.addClassName("unit-kpi-value");

        Span untertitelText = new Span(untertitel);
        untertitelText.addClassName("unit-kpi-subtitle");

        text.add(titelText, wertText, untertitelText);

        Div icon = new Div(symbol.create());
        icon.addClassNames("unit-kpi-icon", farbe);

        top.add(text, icon);
        karte.add(top);

        return karte;
    }

    private Component erstelleDetailBereich() {
        Div detailGrid = new Div();
        detailGrid.addClassName("unit-detail-grid");

        detailGrid.add(
                erstelleStammdatenKarte(),
                erstelleAktuellerMietvertragKarte()
        );

        return detailGrid;
    }

    private Component erstelleStammdatenKarte() {
        Div stammdatenKarte = new Div();
        stammdatenKarte.addClassNames("card", "unit-info-card");

        Div header = new Div();
        header.addClassName("unit-card-header");

        H3 titel = new H3("Stammdaten");
        titel.addClassName("card-title");

        Span subtitle = new Span("Technische Eckdaten der Einheit");
        subtitle.addClassName("card-subtitle");

        header.add(titel, subtitle);

        Div liste = new Div();
        liste.addClassName("unit-info-list");

        liste.add(
                erstelleInfoEintrag("Einheit-Nr.", UiFormatUtils.wertOderStrich(mieteinheit.getBezeichnung())),
                erstelleInfoEintrag("Typ", UiFormatUtils.formatiereMieteinheitTyp(mieteinheit.getTyp())),
                erstelleInfoEintrag("Größe", UiFormatUtils.formatiereFlaeche(mieteinheit.getGroesse())),
                erstelleInfoEintrag("Stockwerk", UiFormatUtils.wertOderStrich(mieteinheit.getStockwerk())),
                erstelleInfoEintrag("Zimmeranzahl", UiFormatUtils.wertOderStrich(mieteinheit.getZimmerzahl())),
                erstelleStatusEintrag("Status", erstelleMieteinheitStatusBadge())
        );

        stammdatenKarte.add(header, liste);

        return stammdatenKarte;
    }

    private Component erstelleAktuellerMietvertragKarte() {
        Mietvertrag aktuellerMietvertrag = findeAktuellenMietvertrag();

        if (aktuellerMietvertrag == null) {
            return erstelleLeereMietvertragKarte();
        }

        return erstelleLaufenderMietvertragKarte(aktuellerMietvertrag);
    }

    private Mietvertrag findeAktuellenMietvertrag() {
        // Aus allen Verträgen der Mieteinheit wird der erste laufende Vertrag gesucht
        return mietvertraege.stream()
                .filter(this::istLaufenderVertrag)
                .findFirst()
                .orElse(null);
    }

    private Component erstelleLeereMietvertragKarte() {
        Div karte = new Div();
        karte.addClassNames("card", "unit-info-card", "unit-empty-contract-card");

        Div icon = new Div(VaadinIcon.FILE_TEXT_O.create());
        icon.addClassName("unit-empty-icon");

        H3 titel = new H3("Aktueller Mietvertrag");
        titel.addClassName("card-title");

        Paragraph text = new Paragraph(
                "Für diese Mieteinheit ist aktuell kein laufender Mietvertrag vorhanden."
        );
        text.addClassName("card-subtitle");

        Button mietvertragAnlegenButton = new Button("Mietvertrag anlegen", VaadinIcon.PLUS.create());
        mietvertragAnlegenButton.addClassName("primary-button");
        mietvertragAnlegenButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate(
                        "mieter-vertraege/mietvertrag-anlegen?immobilieId=" + immobilieId + "&mieteinheitId=" + mieteinheitId
                ))
        );

        karte.add(icon, titel, text, mietvertragAnlegenButton);

        return karte;
    }

    private Component erstelleLaufenderMietvertragKarte(Mietvertrag mietvertrag) {
        Div karte = new Div();
        karte.addClassNames("card", "unit-info-card", "unit-active-contract-card");

        Div header = new Div();
        header.addClassName("unit-card-header-inline");

        Div titleBox = new Div();

        H3 titel = new H3("Aktueller Mietvertrag");
        titel.addClassName("card-title");

        Span subtitle = new Span("MV-" + mietvertrag.getId());
        subtitle.addClassName("card-subtitle");

        titleBox.add(titel, subtitle);
        header.add(titleBox, erstelleVertragsStatusBadge(mietvertrag));

        Div liste = new Div();
        liste.addClassName("unit-info-list");

        liste.add(
                erstelleInfoEintrag("Mieter", UiFormatUtils.formatiereMieterName(mietvertrag)),
                erstelleInfoEintrag("Vertragsbeginn", UiFormatUtils.formatiereDatum(mietvertrag.getStartdatum())),
                erstelleInfoEintrag("Vertragsende", UiFormatUtils.formatiereVertragsende(mietvertrag)),
                erstelleInfoEintrag("Kaltmiete", UiFormatUtils.formatiereEuro(mietvertrag.getKaltmiete())),
                erstelleInfoEintrag("Nebenkosten", UiFormatUtils.formatiereEuro(mietvertrag.getNebenkosten())),
                erstelleInfoEintrag("Warmmiete", UiFormatUtils.formatiereWarmmiete(mietvertrag))
        );

        Button mietvertragAnzeigenButton = new Button("Mietvertrag anzeigen", VaadinIcon.EYE.create());
        mietvertragAnzeigenButton.addClassName("secondary-button");
        mietvertragAnzeigenButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate(
                        MietvertragListView.class,
                        String.valueOf(mietvertrag.getId())
                ))
        );

        karte.add(header, liste, mietvertragAnzeigenButton);

        return karte;
    }

    private Component erstelleMietvertragHistorieKarte() {
        Div karte = new Div();
        karte.addClassNames("card", "unit-contract-history-card");

        Div header = new Div();
        header.addClassName("unit-card-header-inline");

        Div titleBox = new Div();

        H3 titel = new H3("Mietvertragshistorie");
        titel.addClassName("card-title");

        Span subtitle = new Span("Beendete oder ausgelaufene Mietverhältnisse dieser Einheit");
        subtitle.addClassName("card-subtitle");

        titleBox.add(titel, subtitle);

        Span counter = new Span(zaehleHistorischeVertraege() + " Eintrag(e)");
        counter.addClassNames("status-badge", "neutral");

        header.add(titleBox, counter);
        karte.add(header);

        // Für die Historie werden nur beendete oder bereits ausgelaufene Verträge angezeigt
        // sortiert nach dem Enddatum
        List<Mietvertrag> historischeVertraege =
                mietvertragService.findeHistorischeMietvertraegeNachMieteinheit(mieteinheitId);

        if (historischeVertraege.isEmpty()) {
            karte.add(erstelleLeerenHistorieHinweis());
            return karte;
        }

        Div liste = new Div();
        liste.addClassName("unit-contract-history-list");

        historischeVertraege.forEach(mietvertrag ->
                liste.add(erstelleHistorieEintrag(mietvertrag))
        );

        karte.add(liste);
        return karte;
    }

    private Component erstelleLeerenHistorieHinweis() {
        Div empty = new Div();
        empty.addClassName("unit-empty-history");

        Div icon = new Div(VaadinIcon.ARCHIVE.create());
        icon.addClassName("unit-empty-history-icon");

        Span title = new Span("Noch keine Historie vorhanden");
        title.addClassName("unit-empty-history-title");

        Span text = new Span("Sobald Mietverträge beendet werden, erscheinen sie hier als Verlauf.");
        text.addClassName("unit-empty-history-text");

        empty.add(icon, title, text);
        return empty;
    }

    private Component erstelleHistorieEintrag(Mietvertrag mietvertrag) {
        Div eintrag = new Div();
        eintrag.addClassName("unit-contract-history-row");

        Div main = new Div();
        main.addClassName("unit-contract-history-main");

        Span title = new Span("MV-" + mietvertrag.getId() + " • " + UiFormatUtils.formatiereMieterName(mietvertrag));
        title.addClassName("unit-contract-history-title");

        Span subtitle = new Span(UiFormatUtils.formatiereZeitraum(mietvertrag));
        subtitle.addClassName("unit-contract-history-subtitle");

        main.add(title, subtitle);

        Div rent = new Div();
        rent.addClassName("unit-contract-history-rent");
        rent.add(
                new Span("Warmmiete"),
                new Span(UiFormatUtils.formatiereWarmmiete(mietvertrag))
        );

        Button anzeigenButton = new Button("Anzeigen", VaadinIcon.EYE.create());
        anzeigenButton.addClassName("secondary-button");
        anzeigenButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate(
                        MietvertragListView.class,
                        String.valueOf(mietvertrag.getId())
                ))
        );

        eintrag.add(
                main,
                rent,
                erstelleVertragsStatusBadge(mietvertrag),
                anzeigenButton
        );

        return eintrag;
    }

    private void oeffneLoeschDialog() {
        ConfirmDeleteDialog dialog = new ConfirmDeleteDialog(
                "Mieteinheit löschen?",
                "Möchtest du die Mieteinheit \"" + mieteinheit.getBezeichnung() + "\" wirklich löschen?",
                () -> {
                    try {
                        mieteinheitService.loescheMieteinheit(mieteinheitId);
                        Notification.show("Mieteinheit wurde gelöscht.");
                        getUI().ifPresent(ui -> ui.navigate("immobilien/" + immobilieId));

                    } catch (Exception ex) {
                        Notification.show(ex.getMessage(), 4000, Notification.Position.MIDDLE);
                    }
                }
        );

        dialog.open();
    }

    private int zaehleHistorischeVertraege() {
        return mietvertragService.findeHistorischeMietvertraegeNachMieteinheit(mieteinheitId).size();
    }

    private boolean istLaufenderVertrag(Mietvertrag mietvertrag) {
        return mietvertragService.istLaufenderVertrag(mietvertrag);
    }

    private Component erstelleInfoEintrag(String beschriftung, String wert) {
        Div eintrag = new Div();
        eintrag.addClassName("unit-info-item");

        Span beschriftungText = new Span(beschriftung);
        beschriftungText.addClassName("unit-info-label");

        Span wertText = new Span(wert);
        wertText.addClassName("unit-info-value");

        eintrag.add(beschriftungText, wertText);

        return eintrag;
    }

    private Component erstelleStatusEintrag(String beschriftung, Component statusBadge) {
        Div eintrag = new Div();
        eintrag.addClassName("unit-info-item");

        Span beschriftungText = new Span(beschriftung);
        beschriftungText.addClassName("unit-info-label");

        Div badgeWrapper = new Div(statusBadge);
        badgeWrapper.addClassName("unit-info-value");

        eintrag.add(beschriftungText, badgeWrapper);

        return eintrag;
    }

    private Component erstelleMieteinheitStatusBadge() {
        if (mieteinheit == null || mieteinheit.getStatus() == null) {
            return StatusBadge.neutral("-");
        }

        Mieteinheitstatus status = mieteinheit.getStatus();

        return switch (status) {
            case FREI -> StatusBadge.success(status.getLabel());
            case IN_RENOVIERUNG -> StatusBadge.warning(status.getLabel());
            case VERMIETET -> StatusBadge.primary(status.getLabel());
        };
    }

    private Component erstelleVertragsStatusBadge(Mietvertrag mietvertrag) {
        if (mietvertrag == null || mietvertrag.getStatus() == null) {
            return StatusBadge.neutral("-");
        }

        if (mietvertrag.getStatus() == Vertragsstatus.AKTIV) {
            return StatusBadge.success("Aktiv");
        }

        if (mietvertrag.getStatus() == Vertragsstatus.GEKUENDIGT && istLaufenderVertrag(mietvertrag)) {
            return StatusBadge.warning("Läuft aus");
        }

        return StatusBadge.neutral("Beendet");
    }

    private String ermittleStatusCssKlasse() {
        if (mieteinheit == null || mieteinheit.getStatus() == null) {
            return "neutral";
        }

        return switch (mieteinheit.getStatus()) {
            case FREI -> "success";
            case IN_RENOVIERUNG -> "warning";
            case VERMIETET -> "primary";
        };
    }

    private String formatiereAktuelleWarmmiete() {
        Mietvertrag aktuellerMietvertrag = findeAktuellenMietvertrag();

        if (aktuellerMietvertrag == null) {
            return "-";
        }

        return UiFormatUtils.formatiereWarmmiete(aktuellerMietvertrag);
    }

    @Override
    public String getPageTitle() {
        return mieteinheit != null
                ? "Mieteinheit " + mieteinheit.getBezeichnung()
                : "Mieteinheit";
    }

    @Override
    public String getPageSubtitle() {
        return mieteinheit != null
                ? "Immobilien > Mieteinheit " + mieteinheit.getBezeichnung()
                : "Immobilien > Mieteinheit";
    }
}
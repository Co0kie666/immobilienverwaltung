package de.hsbi.immobilienverwaltung.ui.immobilien;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import de.hsbi.immobilienverwaltung.domain.Adresse;
import de.hsbi.immobilienverwaltung.domain.Immobilie;
import de.hsbi.immobilienverwaltung.domain.Mieteinheit;
import de.hsbi.immobilienverwaltung.domain.enums.Immobilientyp;
import de.hsbi.immobilienverwaltung.domain.enums.Mieteinheitstatus;
import de.hsbi.immobilienverwaltung.service.interfaces.ImmobilieService;
import de.hsbi.immobilienverwaltung.service.interfaces.MieteinheitService;
import de.hsbi.immobilienverwaltung.service.interfaces.ZahlungsEingangService;
import de.hsbi.immobilienverwaltung.ui.components.ConfirmDeleteDialog;
import de.hsbi.immobilienverwaltung.ui.components.StatusBadge;
import de.hsbi.immobilienverwaltung.ui.layout.HasPageHeader;
import de.hsbi.immobilienverwaltung.ui.layout.MainLayout;
import jakarta.annotation.security.PermitAll;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

@Route(value = "immobilien/:immobilieId", layout = MainLayout.class)
@PermitAll
public class ImmobilieDetailView extends Div implements HasPageHeader, BeforeEnterObserver {

    private Long immobilieId;
    private Immobilie immobilie;

    private final ImmobilieService immobilieService;
    private final MieteinheitService mieteinheitService;
    private final ZahlungsEingangService zahlungsEingangService;

    private final Grid<Mieteinheit> mieteinheitenTabelle = new Grid<>(Mieteinheit.class, false);
    private final TextField mieteinheitenSuchfeld = new TextField();

    public ImmobilieDetailView(
            MieteinheitService mieteinheitService,
            ImmobilieService immobilieService,
            ZahlungsEingangService zahlungsEingangService
    ) {
        this.mieteinheitService = mieteinheitService;
        this.immobilieService = immobilieService;
        this.zahlungsEingangService = zahlungsEingangService;

        addClassName("page-content");
        addClassName("immobilie-detail-view");

        konfiguriereMieteinheitenSuchfeld();

        // Die Tabelle wird nur einmal konfiguriert. Beim Laden der Seite werden später
        // nur noch die angezeigten Daten über setItems(...) aktualisiert.
        konfiguriereMieteinheitenTabelle();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Die ID stammt aus der URL, die beim Klick in der ImmobilienListView erzeugt wurde.
        // Beispiel: /immobilien/3 -> immobilieId = 3
        this.immobilieId = event.getRouteParameters()
                .get("immobilieId")
                .map(Long::valueOf)
                .orElseThrow(() -> new IllegalArgumentException("Immobilie-ID fehlt."));

        ladeImmobilie();

        // Vor dem Neuaufbau wird die View geleert, damit bei erneutem Aufruf
        // keine alten Komponenten aus einer vorherigen Anzeige bestehen bleiben.
        removeAll();

        add(
                erstelleAktionsleiste(),
                erstelleHeroBereich(),
                erstelleKennzahlenBereich(),
                erstelleUebersichtsBereich(),
                erstelleMieteinheitenKarte()
        );

        ladeMieteinheiten();
    }

    private void ladeImmobilie() {
        this.immobilie = immobilieService.findeImmobilieNachId(immobilieId)
                .orElseThrow(() -> new IllegalArgumentException("Immobilie wurde nicht gefunden."));
    }

    private void ladeMieteinheiten() {
        wendeMieteinheitenSucheAn();
    }

    private void wendeMieteinheitenSucheAn() {
        if (immobilieId == null) {
            return;
        }

        mieteinheitenTabelle.setItems(
                mieteinheitService.sucheMieteinheitenDerImmobilie(immobilieId, mieteinheitenSuchfeld.getValue())
        );
    }

    private Component erstelleAktionsleiste() {
        HorizontalLayout aktionsleiste = new HorizontalLayout();
        aktionsleiste.addClassName("detail-action-row");

        Button zurueckButton = new Button("Zurück", VaadinIcon.ARROW_LEFT.create());
        zurueckButton.addClassName("secondary-button");
        zurueckButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate(ImmobilienListView.class))
        );

        Button bearbeitenButton = new Button("Bearbeiten", VaadinIcon.EDIT.create());
        bearbeitenButton.addClassName("secondary-button");
        bearbeitenButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate("immobilien/" + immobilieId + "/bearbeiten"))
        );

        Button loeschenButton = new Button("Löschen", VaadinIcon.TRASH.create());
        loeschenButton.addClassName("danger-button");
        loeschenButton.addClickListener(event -> oeffneLoeschDialog());

        Button einheitHinzufuegenButton = new Button("Einheit hinzufügen", VaadinIcon.PLUS.create());
        einheitHinzufuegenButton.addClassName("primary-button");
        einheitHinzufuegenButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate("immobilien/" + immobilieId + "/einheiten/neu"))
        );

        aktionsleiste.add(
                zurueckButton,
                bearbeitenButton,
                loeschenButton,
                einheitHinzufuegenButton
        );

        return aktionsleiste;
    }

    private void oeffneLoeschDialog() {
        ConfirmDeleteDialog dialog = new ConfirmDeleteDialog(
                "Immobilie löschen?",
                "Möchtest du die Immobilie \"" + immobilie.getBezeichnung() + "\" wirklich löschen?",
                () -> {
                    try {
                        immobilieService.loescheImmobilie(immobilieId);
                        Notification.show("Immobilie wurde gelöscht.");
                        getUI().ifPresent(ui -> ui.navigate("immobilien"));

                    } catch (Exception ex) {
                        Notification.show(ex.getMessage(), 4000, Notification.Position.MIDDLE);
                    }
                }
        );

        dialog.open();
    }


    private Component erstelleHeroBereich() {
        Div hero = new Div();
        hero.addClassName("property-detail-hero");

        Div visual = new Div();
        visual.addClassNames("property-detail-visual", ermittleTypCssKlasse(immobilie));

        Div visualOverlay = new Div();
        visualOverlay.addClassName("property-detail-visual-overlay");
        visualOverlay.add(VaadinIcon.BUILDING.create(), new Span(formatiereImmobilientyp(immobilie.getTyp())));

        visual.add(visualOverlay);

        Div content = new Div();
        content.addClassName("property-detail-hero-content");

        Span eyebrow = new Span("Immobilien-Exposé");
        eyebrow.addClassName("hero-eyebrow");

        H2 title = new H2(wertOderStrich(immobilie.getBezeichnung()));
        title.addClassName("property-detail-title");

        Paragraph address = new Paragraph(formatiereAdresse(immobilie));
        address.addClassName("property-detail-address");

        Div badges = new Div();
        badges.addClassName("property-detail-badges");
        badges.add(
                StatusBadge.primary(formatiereImmobilientyp(immobilie.getTyp())),
                ermittleDetailStatusBadge()
        );

        long einheitenGesamt = mieteinheitService.zaehleMieteinheiten(immobilieId);
        long vermietet = mieteinheitService.zaehleVermieteteMieteinheiten(immobilieId);
        long frei = mieteinheitService.zaehleFreieMieteinheiten(immobilieId);
        long inRenovierung = mieteinheitService.zaehleMieteinheitenInRenovierung(immobilieId);

        Div quickFacts = new Div();
        quickFacts.addClassName("property-detail-quickfacts");
        quickFacts.add(
                erstelleHeroStat("Einheiten", String.valueOf(einheitenGesamt), VaadinIcon.BUILDING),
                erstelleHeroStat("Vermietet", String.valueOf(vermietet), VaadinIcon.HOME),
                erstelleHeroStat("Frei/Renovierung", String.valueOf(frei + inRenovierung), VaadinIcon.WARNING),
                erstelleHeroStat("Fläche", formatiereFlaeche(immobilie.getFlaeche()), VaadinIcon.HOME)
        );

        content.add(eyebrow, title, address, badges, quickFacts);
        hero.add(visual, content);

        return hero;
    }

    private Component erstelleHeroStat(String label, String value, VaadinIcon icon) {
        Div item = new Div();
        item.addClassName("property-detail-quickfact");

        Div iconBox = new Div(icon.create());
        iconBox.addClassName("property-detail-quickfact-icon");

        Div text = new Div();

        Span labelText = new Span(label);
        labelText.addClassName("property-detail-quickfact-label");

        Span valueText = new Span(value);
        valueText.addClassName("property-detail-quickfact-value");

        text.add(labelText, valueText);
        item.add(iconBox, text);

        return item;
    }

    private Component ermittleDetailStatusBadge() {
        long gesamt = mieteinheitService.zaehleMieteinheiten(immobilieId);
        long frei = mieteinheitService.zaehleFreieMieteinheiten(immobilieId);
        long inRenovierung = mieteinheitService.zaehleMieteinheitenInRenovierung(immobilieId);
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

        return StatusBadge.warning(leerstand + " freie Einheit(en)");
    }

    private Component erstelleKennzahlenBereich() {
        Div kennzahlenBereich = new Div();
        kennzahlenBereich.addClassName("detail-kpi-grid");

        long einheitenGesamt = mieteinheitService.zaehleMieteinheiten(immobilieId);
        long freieEinheiten = mieteinheitService.zaehleFreieMieteinheiten(immobilieId);
        long einheitenInRenovierung = mieteinheitService.zaehleMieteinheitenInRenovierung(immobilieId);
        long vermieteteEinheiten = mieteinheitService.zaehleVermieteteMieteinheiten(immobilieId);

        long leerstand = freieEinheiten + einheitenInRenovierung;
        double leerstandsquote = mieteinheitService.berechneLeerstandsquote(immobilieId);

        BigDecimal offeneZahlungenSumme = zahlungsEingangService.berechneOffeneZahlungenFuerImmobilie(immobilieId);

        long offeneZahlungenAnzahl = zahlungsEingangService.zaehleOffeneZahlungenFuerImmobilie(immobilieId);

        kennzahlenBereich.add(
                erstelleKennzahlenKarte(
                        "Einheiten Gesamt",
                        String.valueOf(einheitenGesamt),
                        vermieteteEinheiten + " vermietet, " + einheitenInRenovierung + " in Renovierung",
                        "primary",
                        VaadinIcon.BUILDING
                ),
                erstelleKennzahlenKarte(
                        "Leerstand",
                        String.valueOf(leerstand),
                        String.format("%.1f%% Leerstandsquote", leerstandsquote),
                        "warning",
                        VaadinIcon.HOME
                ),
                erstelleKennzahlenKarte(
                        "Offene Zahlungen",
                        formatiereBetrag(offeneZahlungenSumme),
                        offeneZahlungenAnzahl + " offene Buchung(en)",
                        "danger",
                        VaadinIcon.WARNING
                )
        );

        return kennzahlenBereich;
    }

    private Component erstelleKennzahlenKarte(String titel, String wert, String untertitel, String typ, VaadinIcon symbol) {
        Div karte = new Div();
        karte.addClassNames("card", "detail-kpi-card");

        Paragraph titelText = new Paragraph(titel);
        titelText.addClassName("kpi-title");

        Span wertText = new Span(wert);
        wertText.addClassName("kpi-value");

        Paragraph untertitelText = new Paragraph(untertitel);
        untertitelText.addClassName("kpi-subtitle");

        Div symbolBox = new Div();
        symbolBox.addClassNames("kpi-icon-box", typ);
        symbolBox.add(symbol.create());

        karte.add(titelText, wertText, untertitelText, symbolBox);

        return karte;
    }

    private Component erstelleUebersichtsBereich() {
        Div uebersicht = new Div();
        uebersicht.addClassName("detail-overview-grid");

        uebersicht.add(
                erstelleImmobilienInfoKarte(),
                erstelleBelegungsKarte()
        );

        return uebersicht;
    }

    private Component erstelleImmobilienInfoKarte() {
        Div karte = new Div();
        karte.addClassNames("card", "immobilie-info-card");

        H3 titel = new H3("Stammdaten");
        titel.addClassName("card-title");

        karte.add(
                titel,
                erstelleInfoEintrag("Bezeichnung", wertOderStrich(immobilie.getBezeichnung())),
                erstelleInfoEintrag("Typ", formatiereImmobilientyp(immobilie.getTyp())),
                erstelleInfoEintrag("Baujahr", wertOderStrich(immobilie.getBaujahr())),
                erstelleInfoEintrag("Fläche", formatiereFlaeche(immobilie.getFlaeche())),
                erstelleInfoEintrag("Adresse", formatiereAdresse(immobilie))
        );

        return karte;
    }

    private Component erstelleInfoEintrag(String beschriftung, String wert) {
        Div eintrag = new Div();
        eintrag.addClassName("info-item");

        Span beschriftungText = new Span(beschriftung);
        beschriftungText.addClassName("info-label");

        Span wertText = new Span(wert);
        wertText.addClassName("info-value");

        eintrag.add(beschriftungText, wertText);

        return eintrag;
    }

    private void konfiguriereMieteinheitenSuchfeld() {
        mieteinheitenSuchfeld.addClassName("detail-table-search");
        mieteinheitenSuchfeld.setPlaceholder("Suchen...");
        mieteinheitenSuchfeld.setPrefixComponent(VaadinIcon.SEARCH.create());
        mieteinheitenSuchfeld.setClearButtonVisible(true);

        // Die Suche wird erst nach einer kurzen Eingabepause ausgelöst,
        // damit nicht bei jedem einzelnen Tastendruck neu geladen wird.
        mieteinheitenSuchfeld.setValueChangeMode(ValueChangeMode.LAZY);

        mieteinheitenSuchfeld.addValueChangeListener(event -> wendeMieteinheitenSucheAn());
    }

    private Component erstelleMieteinheitenKarte() {
        Div karte = new Div();
        karte.addClassName("table-card");

        HorizontalLayout kopfbereich = new HorizontalLayout();
        kopfbereich.addClassName("detail-table-header");

        H3 titel = new H3("Mieteinheiten");
        titel.addClassName("card-title");

        kopfbereich.add(titel, mieteinheitenSuchfeld);

        karte.add(kopfbereich, mieteinheitenTabelle);

        return karte;
    }

    private void konfiguriereMieteinheitenTabelle() {
        mieteinheitenTabelle.addClassName("mieteinheiten-grid");
        mieteinheitenTabelle.setAllRowsVisible(true);

        mieteinheitenTabelle.addColumn(mieteinheit -> wertOderStrich(mieteinheit.getBezeichnung()))
                .setHeader("Bezeichnung")
                .setAutoWidth(true);

        mieteinheitenTabelle.addColumn(mieteinheit ->
                        mieteinheit.getTyp() == null ? "-" : mieteinheit.getTyp().getLabel()
                )
                .setHeader("Typ")
                .setAutoWidth(true);

        mieteinheitenTabelle.addColumn(mieteinheit ->
                        formatiereFlaeche(mieteinheit.getGroesse())
                )
                .setHeader("Größe")
                .setAutoWidth(true);

        mieteinheitenTabelle.addColumn(mieteinheit -> wertOderStrich(mieteinheit.getStockwerk()))
                .setHeader("Stockwerk")
                .setAutoWidth(true);

        mieteinheitenTabelle.addColumn(mieteinheit -> wertOderStrich(mieteinheit.getZimmerzahl()))
                .setHeader("Zimmer")
                .setAutoWidth(true);

        mieteinheitenTabelle.addComponentColumn(this::erstelleMieteinheitStatusBadge)
                .setHeader("Status")
                .setAutoWidth(true);

        mieteinheitenTabelle.addItemClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate(
                        "immobilien/" + immobilieId + "/einheiten/" + event.getItem().getId() + "/details"
                ))
        );
    }

    private Component erstelleMieteinheitStatusBadge(Mieteinheit mieteinheit) {
        if (mieteinheit.getStatus() == null) {
            return StatusBadge.neutral("-");
        }

        Mieteinheitstatus status = mieteinheit.getStatus();

        return switch (status) {
            case FREI -> StatusBadge.success(status.getLabel());
            case IN_RENOVIERUNG -> StatusBadge.warning(status.getLabel());
            case VERMIETET -> StatusBadge.danger(status.getLabel());
        };
    }

    private Component erstelleBelegungsKarte() {
        Div karte = new Div();
        karte.addClassNames("card", "belegung-card");

        H3 titel = new H3("Leerstandsquote");
        titel.addClassName("card-title");

        long vermietet = mieteinheitService.zaehleVermieteteMieteinheiten(immobilieId);
        long frei = mieteinheitService.zaehleFreieMieteinheiten(immobilieId);
        long inRenovierung = mieteinheitService.zaehleMieteinheitenInRenovierung(immobilieId);
        long leerstand = frei + inRenovierung;

        double leerstandsquote = mieteinheitService.berechneLeerstandsquote(immobilieId);
        String mittentext = String.format("%.1f%%", leerstandsquote);

        Html canvas = new Html("""
                <div style="width:100%; max-width:280px; margin:auto;">
                    <canvas id="belegungChart"></canvas>
                </div>
                """);

        karte.add(titel, canvas);

        // charts js
        karte.getElement().executeJs("""
                const vermietet = Number($0);
                const leerstand = Number($1);
                const centerText = $2;

                function renderBelegungChart() {
                    const ctx = document.getElementById('belegungChart');

                    if (window.belegungChartInstance) {
                        window.belegungChartInstance.destroy();
                    }

                    window.belegungChartInstance = new Chart(ctx, {
                        type: 'doughnut',
                        data: {
                            labels: ['Vermietet', 'Leerstand'],
                            datasets: [{
                                data: [vermietet, leerstand],
                                borderWidth: 0
                            }]
                        },
                        options: {
                            cutout: '70%',
                            responsive: true,
                            plugins: {
                                legend: {
                                    position: 'bottom'
                                },
                                tooltip: {
                                    callbacks: {
                                        label: function(context) {
                                            const total = context.dataset.data.reduce((a, b) => a + b, 0);

                                            if (total === 0) {
                                                return context.label + ': 0%';
                                            }

                                            const value = context.raw;
                                            const percent = ((value / total) * 100).toFixed(1);

                                            return context.label + ': ' + value + ' Einheit(en), ' + percent + '%';
                                        }
                                    }
                                }
                            }
                        },
                        plugins: [{
                            id: 'centerText',
                            beforeDraw(chart) {
                                const { width, height, ctx } = chart;
                                ctx.restore();

                                const fontSize = (height / 120).toFixed(2);
                                ctx.font = `bold ${fontSize}em sans-serif`;
                                ctx.textAlign = 'center';
                                ctx.textBaseline = 'middle';

                                const centerX = (chart.chartArea.left + chart.chartArea.right) / 2;
                                const centerY = (chart.chartArea.top + chart.chartArea.bottom) / 2;

                                ctx.fillText(centerText, centerX, centerY);
                                ctx.save();
                            }
                        }]
                    });
                }

                if (!window.Chart) {
                    const script = document.createElement('script');
                    script.src = 'https://cdn.jsdelivr.net/npm/chart.js';
                    script.onload = () => renderBelegungChart();
                    document.head.appendChild(script);
                } else {
                    renderBelegungChart();
                }
                """, vermietet, leerstand, mittentext);

        return karte;
    }

    // Formatierungsmethoden für eine bessere Darstellung
    // TODO eventuell noch auslagern
    private String formatiereImmobilientyp(Immobilientyp typ) {
        return typ == null ? "-" : typ.getLabel();
    }

    private String formatiereAdresse(Immobilie immobilie) {
        Adresse adresse = immobilie.getAdresse();

        if (adresse == null) {
            return "-";
        }

        String strasseUndHausnummer = (
                wertOderLeer(adresse.getStrasse()) + " " + wertOderLeer(adresse.getHausnummer())
        ).trim();

        String plzUndStadt = (
                wertOderLeer(adresse.getPlz()) + " " + wertOderLeer(adresse.getStadt())
        ).trim();

        if (strasseUndHausnummer.isBlank() && plzUndStadt.isBlank()) {
            return "-";
        }

        if (strasseUndHausnummer.isBlank()) {
            return plzUndStadt;
        }

        if (plzUndStadt.isBlank()) {
            return strasseUndHausnummer;
        }

        return strasseUndHausnummer + ", " + plzUndStadt;
    }

    private String formatiereFlaeche(Integer flaeche) {
        return flaeche == null ? "-" : flaeche + " m²";
    }

    private String formatiereBetrag(BigDecimal betrag) {
        if (betrag == null) {
            return "0,00 €";
        }

        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.GERMANY);
        return formatter.format(betrag);
    }

    private String wertOderStrich(Object wert) {
        if (wert == null) {
            return "-";
        }

        if (wert instanceof String text && text.isBlank()) {
            return "-";
        }

        return wert.toString();
    }

    private String wertOderLeer(String wert) {
        return wert == null ? "" : wert;
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
        return immobilie != null ? immobilie.getBezeichnung() : "Immobilie";
    }

    @Override
    public String getPageSubtitle() {
        return immobilie != null
                ? "Immobilien > " + immobilie.getBezeichnung()
                : "Immobilien > Detailansicht";
    }
}
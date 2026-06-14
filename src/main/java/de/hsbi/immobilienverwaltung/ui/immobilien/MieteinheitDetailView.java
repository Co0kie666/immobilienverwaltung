package de.hsbi.immobilienverwaltung.ui.immobilien;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
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

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

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
                erstelleAktionsleiste(),
                erstelleInfoBereich(),
                erstelleMietvertragHistorieKarte()
        );
    }

    private void ladeMieteinheit() {
        this.mieteinheit = mieteinheitService.findeMieteinheitNachId(mieteinheitId)
                .orElseThrow(() -> new IllegalArgumentException("Mieteinheit wurde nicht gefunden."));
    }

    private void ladeMietvertraege() {
        this.mietvertraege = mietvertragService.findeMietvertraegeNachMieteinheit(mieteinheitId);
    }

    private Component erstelleAktionsleiste() {
        HorizontalLayout aktionsleiste = new HorizontalLayout();
        aktionsleiste.addClassName("detail-action-row");

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

        aktionsleiste.add(zurueckButton, bearbeitenButton, loeschenButton);

        return aktionsleiste;
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

    private Component erstelleInfoBereich() {
        Div infoBereich = new Div();
        infoBereich.addClassName("mieteinheit-info-grid");

        infoBereich.add(
                erstelleStammdatenKarte(),
                erstelleAktuellerMietvertragKarte()
        );

        return infoBereich;
    }

    private Component erstelleStammdatenKarte() {
        Div stammdatenKarte = new Div();
        stammdatenKarte.addClassNames("card", "mieteinheit-info-card");

        H3 titel = new H3("Stammdaten");
        titel.addClassName("card-title");

        stammdatenKarte.add(
                titel,
                erstelleInfoEintrag("Einheit-Nr.", wertOderStrich(mieteinheit.getBezeichnung())),
                erstelleInfoEintrag("Typ", formatiereMieteinheitTyp()),
                erstelleInfoEintrag("Größe", formatiereFlaeche(mieteinheit.getGroesse())),
                erstelleInfoEintrag("Stockwerk", wertOderStrich(mieteinheit.getStockwerk())),
                erstelleInfoEintrag("Zimmeranzahl", wertOderStrich(mieteinheit.getZimmerzahl())),
                erstelleStatusEintrag("Status", erstelleMieteinheitStatusBadge())
        );

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
        return mietvertraege.stream()
                .filter(this::istLaufenderVertrag)
                .findFirst()
                .orElse(null);
    }

    private Component erstelleLeereMietvertragKarte() {
        Div karte = new Div();
        karte.addClassNames("card", "mieteinheit-info-card");

        H3 titel = new H3("Aktueller Mietvertrag");
        titel.addClassName("card-title");

        Paragraph text = new Paragraph(
                "Für diese Mieteinheit ist aktuell kein laufender Mietvertrag vorhanden."
        );
        text.addClassName("card-subtitle");

        Button mietvertragAnlegenButton = new Button("Mietvertrag anlegen", VaadinIcon.PLUS.create());
        mietvertragAnlegenButton.addClassName("primary-button");

        // Die IDs werden als Query-Parameter übergeben, damit Immobilie und Mieteinheit
        // im Mietvertragsformular direkt vorausgewählt werden können.
        mietvertragAnlegenButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate(
                        "mietvertrag-anlegen?immobilieId=" + immobilieId + "&mieteinheitId=" + mieteinheitId
                ))
        );

        karte.add(titel, text, mietvertragAnlegenButton);

        return karte;
    }

    private Component erstelleLaufenderMietvertragKarte(Mietvertrag mietvertrag) {
        Div karte = new Div();
        karte.addClassNames("card", "mieteinheit-info-card");

        H3 titel = new H3("Aktueller Mietvertrag");
        titel.addClassName("card-title");

        karte.add(
                titel,
                erstelleInfoEintrag("Mieter", formatiereMieterName(mietvertrag)),
                erstelleStatusEintrag("Status", erstelleVertragsStatusBadge(mietvertrag)),
                erstelleInfoEintrag("Vertragsbeginn", formatiereDatum(mietvertrag.getStartdatum())),
                erstelleInfoEintrag("Vertragsende", formatiereVertragsende(mietvertrag)),
                erstelleInfoEintrag("Kaltmiete", formatiereEuro(mietvertrag.getKaltmiete())),
                erstelleInfoEintrag("Nebenkosten", formatiereEuro(mietvertrag.getNebenkosten())),
                erstelleInfoEintrag("Warmmiete", formatiereWarmmiete(mietvertrag))
        );

        Button mietvertragAnzeigenButton = new Button("Mietvertrag anzeigen", VaadinIcon.EYE.create());
        mietvertragAnzeigenButton.addClassName("secondary-button");
        mietvertragAnzeigenButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate(
                        MietvertragListView.class,
                        String.valueOf(mietvertrag.getId())
                ))
        );

        karte.add(mietvertragAnzeigenButton);

        return karte;
    }

    private Component erstelleMietvertragHistorieKarte() {
        Div karte = new Div();
        karte.addClassNames("card", "mieteinheit-contract-card");

        H3 titel = new H3("Mietvertragshistorie");
        titel.addClassName("card-title");

        List<Mietvertrag> historischeVertraege = mietvertraege.stream()
                .filter(this::istHistorischerVertrag)
                .sorted(this::vergleicheNachEnddatumAbsteigend)
                .toList();

        karte.add(titel);

        if (historischeVertraege.isEmpty()) {
            Paragraph text = new Paragraph(
                    "Bisher sind keine beendeten Mietverträge für diese Mieteinheit vorhanden."
            );
            text.addClassName("card-subtitle");
            karte.add(text);
            return karte;
        }

        historischeVertraege.forEach(mietvertrag ->
                karte.add(erstelleHistorieEintrag(mietvertrag))
        );

        return karte;
    }

    private Component erstelleHistorieEintrag(Mietvertrag mietvertrag) {
        Div eintrag = new Div();
        eintrag.addClassName("contract-history-row");

        Button anzeigenButton = new Button("Anzeigen", VaadinIcon.EYE.create());
        anzeigenButton.addClassName("secondary-button");
        anzeigenButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate(
                        MietvertragListView.class,
                        String.valueOf(mietvertrag.getId())
                ))
        );

        eintrag.add(
                erstelleInfoEintrag("Mieter", formatiereMieterName(mietvertrag)),
                erstelleInfoEintrag("Zeitraum", formatiereZeitraum(mietvertrag)),
                erstelleInfoEintrag("Warmmiete", formatiereWarmmiete(mietvertrag)),
                erstelleStatusEintrag("Status", erstelleVertragsStatusBadge(mietvertrag)),
                anzeigenButton
        );

        return eintrag;
    }

    // Ein gekündigter Vertrag gilt noch als laufend, solange sein Enddatum
    // nicht in der Vergangenheit liegt.
    private boolean istLaufenderVertrag(Mietvertrag mietvertrag) {
        if (mietvertrag == null || mietvertrag.getStatus() == null) {
            return false;
        }

        if (mietvertrag.getStatus() == Vertragsstatus.AKTIV) {
            return true;
        }

        return mietvertrag.getStatus() == Vertragsstatus.GEKUENDIGT
                && (
                mietvertrag.getEnddatum() == null
                        || !mietvertrag.getEnddatum().isBefore(LocalDate.now())
        );
    }

    // Historisch sind beendete Verträge oder gekündigte Verträge,
    // deren Enddatum bereits überschritten wurde.
    private boolean istHistorischerVertrag(Mietvertrag mietvertrag) {
        if (mietvertrag == null || mietvertrag.getStatus() == null) {
            return false;
        }

        return mietvertrag.getStatus() == Vertragsstatus.BEENDET
                || (
                mietvertrag.getStatus() == Vertragsstatus.GEKUENDIGT
                        && mietvertrag.getEnddatum() != null
                        && mietvertrag.getEnddatum().isBefore(LocalDate.now())
        );
    }

    // Die neuesten historischen Verträge sollen oben stehen.
    private int vergleicheNachEnddatumAbsteigend(Mietvertrag ersterVertrag, Mietvertrag zweiterVertrag) {
        LocalDate erstesEnddatum = ersterVertrag.getEnddatum();
        LocalDate zweitesEnddatum = zweiterVertrag.getEnddatum();

        if (erstesEnddatum == null && zweitesEnddatum == null) {
            return 0;
        }

        if (erstesEnddatum == null) {
            return 1;
        }

        if (zweitesEnddatum == null) {
            return -1;
        }

        return zweitesEnddatum.compareTo(erstesEnddatum);
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

    private Component erstelleStatusEintrag(String beschriftung, Component statusBadge) {
        Div eintrag = new Div();
        eintrag.addClassName("info-item");

        Span beschriftungText = new Span(beschriftung);
        beschriftungText.addClassName("info-label");

        eintrag.add(beschriftungText, statusBadge);

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
            case VERMIETET -> StatusBadge.danger(status.getLabel());
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

    private String formatiereMieteinheitTyp() {
        if (mieteinheit == null || mieteinheit.getTyp() == null) {
            return "-";
        }

        return mieteinheit.getTyp().getLabel();
    }

    private String formatiereMieterName(Mietvertrag mietvertrag) {
        if (mietvertrag == null || mietvertrag.getMieter() == null) {
            return "-";
        }

        String vorname = mietvertrag.getMieter().getVorname() == null
                ? ""
                : mietvertrag.getMieter().getVorname();

        String nachname = mietvertrag.getMieter().getNachname() == null
                ? ""
                : mietvertrag.getMieter().getNachname();

        String name = (vorname + " " + nachname).trim();

        return name.isBlank() ? "-" : name;
    }

    private String formatiereZeitraum(Mietvertrag mietvertrag) {
        return formatiereDatum(mietvertrag.getStartdatum())
                + " - "
                + formatiereDatum(mietvertrag.getEnddatum());
    }

    private String formatiereVertragsende(Mietvertrag mietvertrag) {
        if (mietvertrag.getEnddatum() == null) {
            return "unbefristet";
        }

        return formatiereDatum(mietvertrag.getEnddatum());
    }

    private String formatiereDatum(LocalDate datum) {
        if (datum == null) {
            return "-";
        }

        return datum.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }

    private String formatiereFlaeche(Integer flaeche) {
        if (flaeche == null) {
            return "-";
        }

        return flaeche + " m²";
    }

    private String formatiereWarmmiete(Mietvertrag mietvertrag) {
        double kaltmiete = mietvertrag.getKaltmiete() == null ? 0 : mietvertrag.getKaltmiete();
        double nebenkosten = mietvertrag.getNebenkosten() == null ? 0 : mietvertrag.getNebenkosten();

        return formatiereEuro(kaltmiete + nebenkosten);
    }

    private String formatiereEuro(Double betrag) {
        double wert = betrag == null ? 0 : betrag;
        return NumberFormat.getCurrencyInstance(Locale.GERMANY).format(wert);
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
package de.hsbi.immobilienverwaltung.ui.immobilien;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import de.hsbi.immobilienverwaltung.domain.Adresse;
import de.hsbi.immobilienverwaltung.domain.Immobilie;
import de.hsbi.immobilienverwaltung.domain.enums.Immobilientyp;
import de.hsbi.immobilienverwaltung.service.interfaces.ImmobilieService;
import de.hsbi.immobilienverwaltung.ui.layout.HasPageHeader;
import de.hsbi.immobilienverwaltung.ui.layout.MainLayout;
import jakarta.annotation.security.PermitAll;
import de.hsbi.immobilienverwaltung.ui.UiFormatUtils;

@Route(value = "immobilien/:immobilieId/bearbeiten", layout = MainLayout.class)
@PermitAll
public class ImmobilieEditView extends Div implements HasPageHeader, BeforeEnterObserver {

    private final ImmobilieService immobilieService;

    private Long immobilieId;
    private Immobilie immobilie;

    private final TextField bezeichnungFeld = new TextField("Bezeichnung");
    private final Select<Immobilientyp> immobilientypAuswahl = new Select<>();
    private final IntegerField baujahrFeld = new IntegerField("Baujahr");
    private final IntegerField gesamtflaecheFeld = new IntegerField("Gesamtfläche in m²");

    private final TextField strasseFeld = new TextField("Straße");
    private final TextField hausnummerFeld = new TextField("Hausnummer");
    private final TextField plzFeld = new TextField("PLZ");
    private final TextField ortFeld = new TextField("Ort");

    private final Span previewBezeichnung = new Span("Immobilie wird geladen");
    private final Span previewTyp = new Span("Typ wird geladen");
    private final Span previewAdresse = new Span("Adresse wird geladen");
    private final Span previewBaujahr = new Span("-");
    private final Span previewFlaeche = new Span("-");
    private final Span previewId = new Span("-");

    private final Binder<Immobilie> immobilieFormularBinder = new Binder<>(Immobilie.class);
    private final Binder<Adresse> adresseFormularBinder = new Binder<>(Adresse.class);

    public ImmobilieEditView(ImmobilieService immobilieService) {
        this.immobilieService = immobilieService;

        addClassNames("page-content", "property-form-page", "property-edit-page");

        konfiguriereFormularBinder();
        konfiguriereFormularFelder();
        konfiguriereVorschauUpdates();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Die ID stammt aus der URL.
        // Beispiel: /immobilien/5/bearbeiten -> immobilieId = 5
        this.immobilieId = event.getRouteParameters()
                .get("immobilieId")
                .map(Long::valueOf)
                .orElseThrow(() -> new IllegalArgumentException("Immobilie-ID fehlt."));

        ladeImmobilie();

        Adresse adresse = immobilie.getAdresse();

        if (adresse == null) {
            throw new IllegalStateException("Diese Immobilie hat keine Adresse.");
        }

        // Die vorhandenen Daten werden in die Formularfelder geladen.
        immobilieFormularBinder.readBean(immobilie);
        adresseFormularBinder.readBean(adresse);
        aktualisiereVorschau();

        removeAll();
        add(erstelleSeiteninhalt());
    }

    private void ladeImmobilie() {
        this.immobilie = immobilieService.findeImmobilieNachId(immobilieId)
                .orElseThrow(() -> new IllegalArgumentException("Immobilie wurde nicht gefunden."));
    }

    // Immobilie und Adresse werden getrennt gebunden, weil Adresse als @Embedded
    // in der Immobilie gespeichert wird und keine eigene Entity ist.
    private void konfiguriereFormularBinder() {
        immobilieFormularBinder.forField(bezeichnungFeld)
                .asRequired("Bezeichnung darf nicht leer sein.")
                .bind(Immobilie::getBezeichnung, Immobilie::setBezeichnung);

        immobilieFormularBinder.forField(immobilientypAuswahl)
                .asRequired("Immobilientyp muss ausgewählt werden.")
                .bind(Immobilie::getTyp, Immobilie::setTyp);

        immobilieFormularBinder.forField(baujahrFeld)
                .withValidator(
                        baujahr -> baujahr == null || baujahr >= 0,
                        "Baujahr darf nicht negativ sein."
                )
                .bind(Immobilie::getBaujahr, Immobilie::setBaujahr);

        immobilieFormularBinder.forField(gesamtflaecheFeld)
                .withValidator(
                        flaeche -> flaeche == null || flaeche >= 0,
                        "Fläche darf nicht negativ sein."
                )
                .bind(Immobilie::getFlaeche, Immobilie::setFlaeche);

        adresseFormularBinder.forField(strasseFeld)
                .asRequired("Straße darf nicht leer sein.")
                .bind(Adresse::getStrasse, Adresse::setStrasse);

        adresseFormularBinder.forField(hausnummerFeld)
                .asRequired("Hausnummer darf nicht leer sein.")
                .bind(Adresse::getHausnummer, Adresse::setHausnummer);

        adresseFormularBinder.forField(plzFeld)
                .asRequired("PLZ darf nicht leer sein.")
                .bind(Adresse::getPlz, Adresse::setPlz);

        adresseFormularBinder.forField(ortFeld)
                .asRequired("Ort darf nicht leer sein.")
                .bind(Adresse::getStadt, Adresse::setStadt);
    }

    private Component erstelleSeiteninhalt() {
        Div wrapper = new Div();
        wrapper.addClassName("property-form-wrapper");

        Div content = new Div();
        content.addClassName("property-form-content");

        Div grid = new Div();
        grid.addClassName("property-form-grid");
        grid.add(
                erstelleStammdatenKarte(),
                erstelleAdresseKarte()
        );

        content.add(
                erstelleHeroBereich(),
                grid,
                erstelleHinweisKarte(),
                erstelleFormularAktionen()
        );

        wrapper.add(content);
        return wrapper;
    }

    private Component erstelleHeroBereich() {
        Div hero = new Div();
        hero.addClassNames("property-form-hero", "property-edit-hero");

        Div text = new Div();
        text.addClassName("property-form-hero-content");

        Span eyebrow = new Span("Portfolio aktualisieren");
        eyebrow.addClassName("property-form-eyebrow");

        H2 title = new H2("Immobilie bearbeiten");
        title.addClassName("property-form-hero-title");

        Paragraph subtitle = new Paragraph(
                "Passe Stammdaten und Adresse an. Die Änderungen werden direkt in der bestehenden Immobilie gespeichert."
        );
        subtitle.addClassName("property-form-hero-subtitle");

        Div heroStats = new Div();
        heroStats.addClassName("property-form-hero-stats");
        heroStats.add(
                erstelleHeroStat("01", "Prüfen"),
                erstelleHeroStat("02", "Anpassen"),
                erstelleHeroStat("03", "Speichern")
        );

        text.add(eyebrow, title, subtitle, heroStats);

        Div preview = new Div();
        preview.addClassName("property-form-preview");

        Div visual = new Div();
        visual.addClassName("property-preview-visual");
        visual.add(VaadinIcon.BUILDING.create());

        Span editBadge = new Span("Bearbeitungsmodus");
        editBadge.addClassName("property-edit-preview-badge");
        visual.add(editBadge);

        Div previewText = new Div();
        previewText.addClassName("property-preview-text");

        previewBezeichnung.addClassName("property-preview-title");
        previewTyp.addClassName("property-preview-type");
        previewAdresse.addClassName("property-preview-address");

        Div metaGrid = new Div();
        metaGrid.addClassName("property-preview-meta-grid");
        metaGrid.add(
                erstellePreviewMeta("ID", previewId),
                erstellePreviewMeta("Baujahr", previewBaujahr),
                erstellePreviewMeta("Fläche", previewFlaeche)
        );

        previewText.add(previewBezeichnung, previewTyp, previewAdresse, metaGrid);
        preview.add(visual, previewText);

        hero.add(text, preview);
        return hero;
    }

    private Component erstelleHeroStat(String nummer, String text) {
        Div stat = new Div();
        stat.addClassName("property-form-hero-stat");

        Span number = new Span(nummer);
        number.addClassName("property-form-hero-stat-number");

        Span label = new Span(text);
        label.addClassName("property-form-hero-stat-label");

        stat.add(number, label);
        return stat;
    }

    private Component erstellePreviewMeta(String labelText, Span value) {
        Div box = new Div();
        box.addClassName("property-preview-meta");

        Span label = new Span(labelText);
        label.addClassName("property-preview-meta-label");
        value.addClassName("property-preview-meta-value");

        box.add(label, value);
        return box;
    }

    private Component erstelleStammdatenKarte() {
        FormLayout formular = erstelleZweiSpaltenFormular();

        formular.add(
                bezeichnungFeld,
                immobilientypAuswahl,
                baujahrFeld,
                gesamtflaecheFeld
        );

        return erstelleFormularKarte(
                "Stammdaten",
                "Name, Typ und Eckdaten der bestehenden Immobilie.",
                VaadinIcon.BUILDING,
                formular
        );
    }

    private Component erstelleAdresseKarte() {
        FormLayout formular = erstelleZweiSpaltenFormular();

        formular.add(
                strasseFeld,
                hausnummerFeld,
                plzFeld,
                ortFeld
        );

        return erstelleFormularKarte(
                "Adresse",
                "Standortdaten für Suche, Übersicht und Detailansicht.",
                VaadinIcon.MAP_MARKER,
                formular
        );
    }

    private Component erstelleHinweisKarte() {
        Div card = new Div();
        card.addClassNames("property-form-card", "property-form-full-card", "property-edit-info-card");

        Div iconBox = new Div();
        iconBox.addClassName("property-form-card-icon");
        iconBox.add(VaadinIcon.INFO_CIRCLE.create());

        Div text = new Div();
        text.addClassName("property-edit-info-text");

        H3 title = new H3("Änderungen an der Immobilie");
        title.addClassName("property-form-card-title");

        Paragraph subtitle = new Paragraph(
                "Diese Bearbeitung betrifft nur die Stammdaten und Adresse. Mieteinheiten, Mietverträge und Buchungen bleiben unverändert."
        );
        subtitle.addClassName("property-form-card-subtitle");

        text.add(title, subtitle);
        card.add(iconBox, text);

        return card;
    }

    private Div erstelleFormularKarte(
            String titleText,
            String subtitleText,
            VaadinIcon icon,
            Component content
    ) {
        Div card = new Div();
        card.addClassName("property-form-card");

        Div header = new Div();
        header.addClassName("property-form-card-header");

        Div iconBox = new Div();
        iconBox.addClassName("property-form-card-icon");
        iconBox.add(icon.create());

        Div titleBox = new Div();
        titleBox.addClassName("property-form-card-title-box");

        H3 title = new H3(titleText);
        title.addClassName("property-form-card-title");

        Paragraph subtitle = new Paragraph(subtitleText);
        subtitle.addClassName("property-form-card-subtitle");

        titleBox.add(title, subtitle);
        header.add(iconBox, titleBox);

        Div body = new Div();
        body.addClassName("property-form-card-content");
        body.add(content);

        card.add(header, body);
        return card;
    }

    private void konfiguriereFormularFelder() {
        bezeichnungFeld.setPlaceholder("z. B. Parkresidenz Süd");
        bezeichnungFeld.setPrefixComponent(VaadinIcon.TAG.create());

        immobilientypAuswahl.setLabel("Immobilientyp");
        immobilientypAuswahl.setItems(Immobilientyp.values());
        immobilientypAuswahl.setItemLabelGenerator(Immobilientyp::getLabel);

        baujahrFeld.setPlaceholder("z. B. 1998");
        baujahrFeld.setMin(0);
        baujahrFeld.setErrorMessage("Baujahr darf nicht negativ sein");

        gesamtflaecheFeld.setPlaceholder("z. B. 850");
        gesamtflaecheFeld.setMin(0);
        gesamtflaecheFeld.setErrorMessage("Fläche darf nicht negativ sein");

        strasseFeld.setPlaceholder("z. B. Gartenstraße");
        strasseFeld.setPrefixComponent(VaadinIcon.MAP_MARKER.create());

        hausnummerFeld.setPlaceholder("z. B. 12a");
        plzFeld.setPlaceholder("z. B. 33602");
        ortFeld.setPlaceholder("z. B. Bielefeld");
        ortFeld.setPrefixComponent(VaadinIcon.MAP_MARKER.create());

        bezeichnungFeld.setRequiredIndicatorVisible(true);
        immobilientypAuswahl.setRequiredIndicatorVisible(true);
        strasseFeld.setRequiredIndicatorVisible(true);
        hausnummerFeld.setRequiredIndicatorVisible(true);
        plzFeld.setRequiredIndicatorVisible(true);
        ortFeld.setRequiredIndicatorVisible(true);

        bezeichnungFeld.setWidthFull();
        immobilientypAuswahl.setWidthFull();
        baujahrFeld.setWidthFull();
        gesamtflaecheFeld.setWidthFull();
        strasseFeld.setWidthFull();
        hausnummerFeld.setWidthFull();
        plzFeld.setWidthFull();
        ortFeld.setWidthFull();
    }

    private void konfiguriereVorschauUpdates() {
        bezeichnungFeld.addValueChangeListener(event -> aktualisiereVorschau());
        immobilientypAuswahl.addValueChangeListener(event -> aktualisiereVorschau());
        baujahrFeld.addValueChangeListener(event -> aktualisiereVorschau());
        gesamtflaecheFeld.addValueChangeListener(event -> aktualisiereVorschau());
        strasseFeld.addValueChangeListener(event -> aktualisiereVorschau());
        hausnummerFeld.addValueChangeListener(event -> aktualisiereVorschau());
        plzFeld.addValueChangeListener(event -> aktualisiereVorschau());
        ortFeld.addValueChangeListener(event -> aktualisiereVorschau());
    }

    private void aktualisiereVorschau() {
        previewBezeichnung.setText(
                UiFormatUtils.istLeer(bezeichnungFeld.getValue())
                        ? "Immobilie ohne Bezeichnung"
                        : bezeichnungFeld.getValue().trim()
        );

        previewTyp.setText(
                immobilientypAuswahl.getValue() == null
                        ? "Typ noch offen"
                        : immobilientypAuswahl.getValue().getLabel()
        );

        previewAdresse.setText(erstelleAdressVorschau());
        previewId.setText(immobilieId == null ? "-" : "#" + immobilieId);
        previewBaujahr.setText(
                baujahrFeld.getValue() == null
                        ? "offen"
                        : String.valueOf(baujahrFeld.getValue())
        );
        previewFlaeche.setText(
                gesamtflaecheFeld.getValue() == null
                        ? "offen"
                        : gesamtflaecheFeld.getValue() + " m²"
        );
    }

    private String erstelleAdressVorschau() {
        return UiFormatUtils.formatiereAdresse(
                strasseFeld.getValue(),
                hausnummerFeld.getValue(),
                plzFeld.getValue(),
                ortFeld.getValue(),
                "Adresse wird während der Bearbeitung angezeigt"
        );
    }

    private Div erstelleFormularAktionen() {
        Div aktionen = new Div();
        aktionen.addClassName("property-form-actions");

        Div helper = new Div();
        helper.addClassName("property-form-actions-helper");
        helper.add(
                VaadinIcon.INFO_CIRCLE.create(),
                new Span("Die Änderungen werden erst übernommen, wenn du sie speicherst.")
        );

        Div buttons = new Div();
        buttons.addClassName("property-form-action-buttons");

        Button abbrechenButton = new Button("Abbrechen", VaadinIcon.CLOSE.create());
        abbrechenButton.addClassName("secondary-button");
        abbrechenButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate("immobilien/" + immobilieId))
        );

        Button speichernButton = new Button("Änderungen speichern", VaadinIcon.CHECK.create());
        speichernButton.addClassName("primary-button");
        speichernButton.addClickListener(event -> speichereAenderungen());

        buttons.add(abbrechenButton, speichernButton);
        aktionen.add(helper, buttons);

        return aktionen;
    }

    private void speichereAenderungen() {
        try {
            // Der Binder validiert die Eingaben und schreibt sie erst danach
            // zurück in die bereits geladene Immobilie und Adresse.
            immobilieFormularBinder.writeBean(immobilie);
            adresseFormularBinder.writeBean(immobilie.getAdresse());

            immobilieService.speichereImmobilie(immobilie);

            Notification.show("Immobilie wurde aktualisiert");

            getUI().ifPresent(ui -> ui.navigate("immobilien/" + immobilieId));

        } catch (ValidationException ex) {
            Notification.show(
                    "Bitte überprüfe die Eingaben.",
                    4000,
                    Notification.Position.BOTTOM_END
            );

        } catch (Exception ex) {
            Notification.show(
                    "Fehler beim Speichern: " + ex.getMessage(),
                    4000,
                    Notification.Position.BOTTOM_END
            );
        }
    }

    private FormLayout erstelleZweiSpaltenFormular() {
        FormLayout formular = new FormLayout();
        formular.addClassName("property-form-layout");
        formular.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("650px", 2)
        );
        return formular;
    }

    @Override
    public String getPageTitle() {
        return "Immobilie bearbeiten";
    }

    @Override
    public String getPageSubtitle() {
        return "Immobilien > Immobilie bearbeiten";
    }
}

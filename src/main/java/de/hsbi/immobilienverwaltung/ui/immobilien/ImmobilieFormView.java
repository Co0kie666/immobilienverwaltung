package de.hsbi.immobilienverwaltung.ui.immobilien;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
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
import com.vaadin.flow.router.Route;
import de.hsbi.immobilienverwaltung.domain.Adresse;
import de.hsbi.immobilienverwaltung.domain.Immobilie;
import de.hsbi.immobilienverwaltung.domain.Mieteinheit;
import de.hsbi.immobilienverwaltung.domain.enums.Immobilientyp;
import de.hsbi.immobilienverwaltung.domain.enums.MieteinheitTyp;
import de.hsbi.immobilienverwaltung.domain.enums.Mieteinheitstatus;
import de.hsbi.immobilienverwaltung.service.interfaces.ImmobilieService;
import de.hsbi.immobilienverwaltung.service.interfaces.MieteinheitService;
import de.hsbi.immobilienverwaltung.ui.layout.HasPageHeader;
import de.hsbi.immobilienverwaltung.ui.layout.MainLayout;
import jakarta.annotation.security.PermitAll;
import de.hsbi.immobilienverwaltung.ui.UiFormatUtils;

@Route(value = "immobilien/neu", layout = MainLayout.class)
@PermitAll
public class ImmobilieFormView extends Div implements HasPageHeader {

    private final TextField bezeichnungFeld = new TextField("Bezeichnung");
    private final Select<Immobilientyp> immobilientypAuswahl = new Select<>();
    private final IntegerField baujahrFeld = new IntegerField("Baujahr");
    private final IntegerField gesamtflaecheFeld = new IntegerField("Gesamtfläche in m²");

    private final TextField strasseFeld = new TextField("Straße");
    private final TextField hausnummerFeld = new TextField("Hausnummer");
    private final TextField plzFeld = new TextField("PLZ");
    private final TextField ortFeld = new TextField("Ort");

    private final Checkbox gesamtobjektErstellenCheckbox =
            new Checkbox("Gesamtobjekt als einzelne Mieteinheit erstellen");

    private final Span previewBezeichnung = new Span("Neue Immobilie");
    private final Span previewTyp = new Span("Typ noch offen");
    private final Span previewAdresse = new Span("Adresse wird während der Eingabe angezeigt");
    private final Span previewBaujahr = new Span("Baujahr offen");
    private final Span previewFlaeche = new Span("Fläche offen");
    private final Span previewEinheit = new Span("Mieteinheiten später anlegen");

    private final ImmobilieService immobilieService;
    private final MieteinheitService mieteinheitService;

    private final Binder<Immobilie> immobilieFormularBinder = new Binder<>(Immobilie.class);
    private final Binder<Adresse> adresseFormularBinder = new Binder<>(Adresse.class);

    public ImmobilieFormView(ImmobilieService immobilieService, MieteinheitService mieteinheitService) {
        this.immobilieService = immobilieService;
        this.mieteinheitService = mieteinheitService;

        addClassNames("page-content", "property-form-page");

        konfiguriereFormularBinder();
        konfiguriereFormularFelder();
        konfiguriereVorschauUpdates();

        add(erstelleSeiteninhalt());
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
                erstelleGesamtobjektKarte(),
                erstelleFormularAktionen()
        );

        wrapper.add(content);
        return wrapper;
    }

    private Component erstelleHeroBereich() {
        Div hero = new Div();
        hero.addClassName("property-form-hero");

        Div text = new Div();
        text.addClassName("property-form-hero-content");

        Span eyebrow = new Span("Portfolio erweitern");
        eyebrow.addClassName("property-form-eyebrow");

        H2 title = new H2("Neue Immobilie anlegen");
        title.addClassName("property-form-hero-title");

        Paragraph subtitle = new Paragraph(
                "Erfasse Stammdaten, Adresse und optional direkt ein Gesamtobjekt als erste Mieteinheit."
        );
        subtitle.addClassName("property-form-hero-subtitle");

        Div heroStats = new Div();
        heroStats.addClassName("property-form-hero-stats");
        heroStats.add(
                erstelleHeroStat("01", "Stammdaten"),
                erstelleHeroStat("02", "Adresse"),
                erstelleHeroStat("03", "Speichern")
        );

        text.add(eyebrow, title, subtitle, heroStats);

        Div preview = new Div();
        preview.addClassName("property-form-preview");

        Div visual = new Div();
        visual.addClassName("property-preview-visual");
        visual.add(VaadinIcon.BUILDING.create());

        Div previewText = new Div();
        previewText.addClassName("property-preview-text");

        previewBezeichnung.addClassName("property-preview-title");
        previewTyp.addClassName("property-preview-type");
        previewAdresse.addClassName("property-preview-address");

        Div metaGrid = new Div();
        metaGrid.addClassName("property-preview-meta-grid");
        metaGrid.add(
                erstellePreviewMeta("Baujahr", previewBaujahr),
                erstellePreviewMeta("Fläche", previewFlaeche),
                erstellePreviewMeta("Einheit", previewEinheit)
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
                "Name, Typ und Eckdaten der Immobilie.",
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
                "Standortdaten für Übersicht, Suche und Detailansicht.",
                VaadinIcon.MAP_MARKER,
                formular
        );
    }

    private Component erstelleGesamtobjektKarte() {
        Div card = new Div();
        card.addClassNames("property-form-card", "property-form-full-card", "property-unit-option-card");

        Div iconBox = new Div();
        iconBox.addClassName("property-form-card-icon");
        iconBox.add(VaadinIcon.HOME.create());

        Div text = new Div();
        text.addClassName("property-unit-option-text");

        H3 title = new H3("Gesamtobjekt automatisch anlegen");
        title.addClassName("property-form-card-title");

        Paragraph subtitle = new Paragraph(
                "Praktisch für Häuser oder Gewerbeobjekte, die nicht in mehrere Einheiten aufgeteilt werden sollen."
        );
        subtitle.addClassName("property-form-card-subtitle");

        text.add(title, subtitle);

        gesamtobjektErstellenCheckbox.addClassName("property-unit-option-checkbox");

        card.add(iconBox, text, gesamtobjektErstellenCheckbox);
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
        immobilientypAuswahl.setPlaceholder("Typ auswählen");
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
        gesamtobjektErstellenCheckbox.addValueChangeListener(event -> aktualisiereVorschau());

        aktualisiereVorschau();
    }

    private void aktualisiereVorschau() {
        previewBezeichnung.setText(
                UiFormatUtils.istLeer(bezeichnungFeld.getValue())
                        ? "Neue Immobilie"
                        : bezeichnungFeld.getValue().trim()
        );

        previewTyp.setText(
                immobilientypAuswahl.getValue() == null
                        ? "Typ noch offen"
                        : immobilientypAuswahl.getValue().getLabel()
        );

        previewAdresse.setText(erstelleAdressVorschau());
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
        previewEinheit.setText(
                Boolean.TRUE.equals(gesamtobjektErstellenCheckbox.getValue())
                        ? "Gesamtobjekt wird angelegt"
                        : "später anlegen"
        );
    }

    private String erstelleAdressVorschau() {
        return UiFormatUtils.formatiereAdresse(
                strasseFeld.getValue(),
                hausnummerFeld.getValue(),
                plzFeld.getValue(),
                ortFeld.getValue(),
                "Adresse wird während der Eingabe angezeigt"
        );
    }

    private Div erstelleFormularAktionen() {
        Div aktionen = new Div();
        aktionen.addClassName("property-form-actions");

        Div helper = new Div();
        helper.addClassName("property-form-actions-helper");
        helper.add(
                VaadinIcon.INFO_CIRCLE.create(),
                new Span("Pflichtfelder sind markiert. Nach dem Speichern erscheint die Immobilie in der Übersicht.")
        );

        Div buttons = new Div();
        buttons.addClassName("property-form-action-buttons");

        Button abbrechenButton = new Button("Abbrechen", VaadinIcon.CLOSE.create());
        abbrechenButton.addClassName("secondary-button");
        abbrechenButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate(ImmobilienListView.class))
        );

        Button speichernButton = new Button("Immobilie speichern", VaadinIcon.CHECK.create());
        speichernButton.addClassName("primary-button");
        speichernButton.addClickListener(event -> speichereImmobilie());

        buttons.add(abbrechenButton, speichernButton);
        aktionen.add(helper, buttons);

        return aktionen;
    }

    private void speichereImmobilie() {
        try {
            Immobilie immobilie = new Immobilie();
            Adresse adresse = new Adresse();

            // Der Binder validiert die Eingaben und schreibt sie erst danach
            // in die jeweiligen Objekte.
            immobilieFormularBinder.writeBean(immobilie);
            adresseFormularBinder.writeBean(adresse);

            immobilie.setAdresse(adresse);

            Immobilie gespeicherteImmobilie = immobilieService.speichereImmobilie(immobilie);

            erstelleGesamtobjektFallsAusgewaehlt(gespeicherteImmobilie);

            Notification.show(
                    "Immobilie wurde gespeichert: " + gespeicherteImmobilie.getBezeichnung()
            );

            getUI().ifPresent(ui -> ui.navigate(ImmobilienListView.class));

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

    // Diese Option ist für Immobilien gedacht, die nicht in einzelne Wohnungen,
    // Büros oder Gewerbeflächen aufgeteilt werden sollen.
    private void erstelleGesamtobjektFallsAusgewaehlt(Immobilie immobilie) {
        if (!Boolean.TRUE.equals(gesamtobjektErstellenCheckbox.getValue())) {
            return;
        }

        Mieteinheit gesamtobjekt = new Mieteinheit(
                "Gesamtobjekt",
                Mieteinheitstatus.FREI,
                MieteinheitTyp.GESAMTOBJEKT,
                immobilie.getFlaeche(),
                null,
                "Gesamtobjekt"
        );

        mieteinheitService.speichereMieteinheit(immobilie.getId(), gesamtobjekt);
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
        return "Immobilie anlegen";
    }

    @Override
    public String getPageSubtitle() {
        return "Immobilien > Immobilie anlegen";
    }
}

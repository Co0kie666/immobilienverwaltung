package de.hsbi.immobilienverwaltung.ui.immobilien;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
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

    private final ImmobilieService immobilieService;
    private final MieteinheitService mieteinheitService;

    private final Binder<Immobilie> immobilieFormularBinder = new Binder<>(Immobilie.class);
    private final Binder<Adresse> adresseFormularBinder = new Binder<>(Adresse.class);

    public ImmobilieFormView(
            ImmobilieService immobilieService,
            MieteinheitService mieteinheitService
    ) {
        this.immobilieService = immobilieService;
        this.mieteinheitService = mieteinheitService;

        addClassName("page-content");
        addClassName("immobilie-form-view");

        konfiguriereFormularBinder();

        add(erstelleFormularKarte());
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

    private Div erstelleFormularKarte() {
        Div formularKarte = new Div();
        formularKarte.addClassName("form-card");

        Div formularKopf = new Div();
        formularKopf.addClassName("form-card-header");

        H3 titel = new H3("Stammdaten");
        titel.addClassName("form-card-title");

        formularKopf.add(titel);

        FormLayout formular = new FormLayout();
        formular.addClassName("form-card-content");

        konfiguriereFormularFelder();

        formular.add(
                bezeichnungFeld,
                immobilientypAuswahl,
                baujahrFeld,
                gesamtflaecheFeld,
                strasseFeld,
                hausnummerFeld,
                plzFeld,
                ortFeld,
                gesamtobjektErstellenCheckbox
        );

        formular.setColspan(gesamtobjektErstellenCheckbox, 2);

        formularKarte.add(
                formularKopf,
                formular,
                erstelleFormularAktionen()
        );

        return formularKarte;
    }

    private void konfiguriereFormularFelder() {
        bezeichnungFeld.setPlaceholder("z. B. Parkresidenz Süd");

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

        bezeichnungFeld.setRequiredIndicatorVisible(true);
        immobilientypAuswahl.setRequiredIndicatorVisible(true);
        strasseFeld.setRequiredIndicatorVisible(true);
        hausnummerFeld.setRequiredIndicatorVisible(true);
        plzFeld.setRequiredIndicatorVisible(true);
        ortFeld.setRequiredIndicatorVisible(true);
    }

    private Div erstelleFormularAktionen() {
        Div aktionen = new Div();
        aktionen.addClassName("form-actions");

        Button abbrechenButton = new Button("Abbrechen");
        abbrechenButton.addClassName("secondary-button");
        abbrechenButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate(ImmobilienListView.class))
        );

        Button speichernButton = new Button("Speichern", VaadinIcon.CHECK.create());
        speichernButton.addClassName("primary-button");
        speichernButton.addClickListener(event -> speichereImmobilie());

        aktionen.add(abbrechenButton, speichernButton);

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

    @Override
    public String getPageTitle() {
        return "Immobilie anlegen";
    }

    @Override
    public String getPageSubtitle() {
        return "Immobilien > Immobilie anlegen";
    }
}
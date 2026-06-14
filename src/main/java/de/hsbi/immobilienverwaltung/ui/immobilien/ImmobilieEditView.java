package de.hsbi.immobilienverwaltung.ui.immobilien;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
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

    private final Binder<Immobilie> immobilieFormularBinder = new Binder<>(Immobilie.class);
    private final Binder<Adresse> adresseFormularBinder = new Binder<>(Adresse.class);

    public ImmobilieEditView(ImmobilieService immobilieService) {
        this.immobilieService = immobilieService;

        addClassName("page-content");
        addClassName("immobilie-edit-view");

        konfiguriereFormularBinder();

        add(erstelleFormularKarte());
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

    private Component erstelleFormularKarte() {
        Div formularKarte = new Div();
        formularKarte.addClassName("form-card");

        Div formularKopf = new Div();
        formularKopf.addClassName("form-card-header");

        H3 titel = new H3("Immobilie bearbeiten");
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
                ortFeld
        );

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
                getUI().ifPresent(ui -> ui.navigate("immobilien/" + immobilieId))
        );

        Button speichernButton = new Button("Änderungen speichern", VaadinIcon.CHECK.create());
        speichernButton.addClassName("primary-button");
        speichernButton.addClickListener(event -> speichereAenderungen());

        aktionen.add(abbrechenButton, speichernButton);

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

    @Override
    public String getPageTitle() {
        return "Immobilie bearbeiten";
    }

    @Override
    public String getPageSubtitle() {
        return "Immobilien > Immobilie bearbeiten";
    }
}
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
    private Immobilie immobilie;

    private Long immobilieId;

    private final TextField bezeichnungField = new TextField("Bezeichnung");
    private final Select<Immobilientyp> typSelect = new Select<>();
    private final IntegerField baujahrField = new IntegerField("Baujahr");
    private final IntegerField gesamtflaecheField = new IntegerField("Gesamtfläche in m²");

    private final TextField strasseField = new TextField("Straße");
    private final TextField hausnummerField = new TextField("Hausnummer");
    private final TextField plzField = new TextField("PLZ");
    private final TextField ortField = new TextField("Ort");

    private final Binder<Immobilie> immobilieBinder = new Binder<>(Immobilie.class);
    private final Binder<Adresse> adresseBinder = new Binder<>(Adresse.class);

    public ImmobilieEditView(ImmobilieService immobilieService) {
        this.immobilieService = immobilieService;
        addClassName("page-content");
        addClassName("immobilie-edit-view");
        konfiguriereBinder();
        add(createFormCard());
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        this.immobilieId = event.getRouteParameters()
                .get("immobilieId")
                .map(Long::valueOf)
                .orElseThrow(() -> new IllegalArgumentException("Immobilie-ID fehlt."));

        // Immobilie  laden
        this.immobilie = immobilieService.findeImmobilieNachId(immobilieId)
                .orElseThrow(() -> new IllegalArgumentException("Immobilie wurde nicht gefunden."));

        Adresse adresse = immobilie.getAdresse();

        if (adresse == null) {
            throw new IllegalStateException("Diese Immobilie hat keine Adresse.");
        }

        immobilieBinder.readBean(immobilie);
        adresseBinder.readBean(adresse);
    }

    private void konfiguriereBinder() {
        immobilieBinder.forField(bezeichnungField)
                .asRequired("Bezeichnung darf nicht leer sein.")
                .bind(Immobilie::getBezeichnung, Immobilie::setBezeichnung);

        immobilieBinder.forField(typSelect)
                .asRequired("Immobilientyp muss ausgewählt werden.")
                .bind(Immobilie::getTyp, Immobilie::setTyp);

        immobilieBinder.forField(baujahrField)
                .withValidator(
                        baujahr -> baujahr == null || baujahr >= 0,
                        "Baujahr darf nicht negativ sein."
                )
                .bind(Immobilie::getBaujahr, Immobilie::setBaujahr);

        immobilieBinder.forField(gesamtflaecheField)
                .withValidator(
                        flaeche -> flaeche == null || flaeche >= 0,
                        "Fläche darf nicht negativ sein."
                )
                .bind(Immobilie::getFlaeche, Immobilie::setFlaeche);

        adresseBinder.forField(strasseField)
                .asRequired("Straße darf nicht leer sein.")
                .bind(Adresse::getStrasse, Adresse::setStrasse);

        adresseBinder.forField(hausnummerField)
                .asRequired("Hausnummer darf nicht leer sein.")
                .bind(Adresse::getHausnummer, Adresse::setHausnummer);

        adresseBinder.forField(plzField)
                .asRequired("PLZ darf nicht leer sein.")
                .bind(Adresse::getPlz, Adresse::setPlz);

        adresseBinder.forField(ortField)
                .asRequired("Ort darf nicht leer sein.")
                .bind(Adresse::getStadt, Adresse::setStadt);
    }

    private Component createFormCard() {
        Div card = new Div();
        card.addClassName("form-card");

        Div header = new Div();
        header.addClassName("form-card-header");

        H3 title = new H3("Immobilie bearbeiten");
        title.addClassName("form-card-title");

        header.add(title);

        FormLayout form = new FormLayout();
        form.addClassName("form-card-content");

        bezeichnungField.setPlaceholder("z. B. Parkresidenz Süd");

        typSelect.setLabel("Immobilientyp");
        typSelect.setItems(Immobilientyp.values());
        typSelect.setItemLabelGenerator(Immobilientyp::getLabel);

        baujahrField.setPlaceholder("z. B. 1998");

        gesamtflaecheField.setPlaceholder("z. B. 850");

        form.add(
                bezeichnungField,
                typSelect,
                baujahrField,
                gesamtflaecheField,
                strasseField,
                hausnummerField,
                plzField,
                ortField
        );

        Div actions = new Div();
        actions.addClassName("form-actions");

        Button abbrechenButton = new Button("Abbrechen");
        abbrechenButton.addClassName("secondary-button");
        abbrechenButton.addClickListener(event ->
                getUI().ifPresent(ui -> ui.navigate("immobilien/" + immobilieId))
        );

        Button speichernButton = new Button("Änderungen speichern", VaadinIcon.CHECK.create());
        speichernButton.addClassName("primary-button");
        speichernButton.addClickListener(event -> speichereAenderungen());

        actions.add(abbrechenButton, speichernButton);

        card.add(header, form, actions);

        return card;
    }

    private void speichereAenderungen() {
        try {
            immobilieBinder.writeBean(immobilie);
            adresseBinder.writeBean(immobilie.getAdresse());

            immobilieService.speichereImmobilie(immobilie);

            Notification.show("Immobilie wurde aktualisiert");

            getUI().ifPresent(ui -> ui.navigate("immobilien/" + immobilieId));

        } catch (ValidationException ex) {
            Notification.show("Bitte überprüfe die Eingaben.", 4000, Notification.Position.BOTTOM_END);

        } catch (Exception ex) {
            Notification.show("Fehler beim Speichern: " + ex.getMessage(), 4000, Notification.Position.BOTTOM_END);
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
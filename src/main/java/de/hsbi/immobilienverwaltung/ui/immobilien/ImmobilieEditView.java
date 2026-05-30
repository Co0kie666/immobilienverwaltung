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
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import de.hsbi.immobilienverwaltung.domain.Adresse;
import de.hsbi.immobilienverwaltung.domain.Immobilie;
import de.hsbi.immobilienverwaltung.domain.enums.Immobilientyp;
import de.hsbi.immobilienverwaltung.security.LoginRequired;
import de.hsbi.immobilienverwaltung.service.interfaces.ImmobilieService;
import de.hsbi.immobilienverwaltung.ui.layout.HasPageHeader;
import de.hsbi.immobilienverwaltung.ui.layout.MainLayout;

@Route(value = "immobilien/:immobilieId/bearbeiten", layout = MainLayout.class)
public class ImmobilieEditView extends Div implements HasPageHeader, BeforeEnterObserver, LoginRequired {

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

    public ImmobilieEditView(ImmobilieService immobilieService) {
        this.immobilieService = immobilieService;
        addClassName("page-content");
        addClassName("immobilie-edit-view");

        add(createFormCard());
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        this.immobilieId = event.getRouteParameters()
                .get("immobilieId")
                .map(Long::valueOf)
                .orElse(null);

        // Immobilie  laden
        this.immobilie = immobilieService.findeImmobilieNachId(immobilieId)
                .orElseThrow(() -> new IllegalArgumentException("Immobilie wurde nicht gefunden."));

        fuelleFelder();
    }

    private void fuelleFelder() {
        bezeichnungField.setValue(immobilie.getBezeichnung());
        typSelect.setValue(immobilie.getTyp());
        baujahrField.setValue(immobilie.getBaujahr());
        gesamtflaecheField.setValue(immobilie.getFlaeche());

        if (immobilie.getAdresse() != null) {
            strasseField.setValue(immobilie.getAdresse().getStrasse());
            hausnummerField.setValue(immobilie.getAdresse().getHausnummer());
            plzField.setValue(immobilie.getAdresse().getPlz());
            ortField.setValue(immobilie.getAdresse().getStadt());
        }
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
        typSelect.setItemLabelGenerator(this::formatImmobilientyp);

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
            immobilie.setBezeichnung(bezeichnungField.getValue());
            immobilie.setTyp(typSelect.getValue());
            immobilie.setBaujahr(baujahrField.getValue());
            immobilie.setFlaeche(gesamtflaecheField.getValue());

            Adresse adresse = immobilie.getAdresse();

            if (adresse == null) {
                adresse = new Adresse();
                immobilie.setAdresse(adresse);
            }

            adresse.setStrasse(strasseField.getValue());
            adresse.setHausnummer(hausnummerField.getValue());
            adresse.setPlz(plzField.getValue());
            adresse.setStadt(ortField.getValue());

            immobilieService.speichereImmobilie(immobilie);

            Notification.show("Immobilie wurde aktualisiert");

            getUI().ifPresent(ui -> ui.navigate("immobilien/" + immobilieId));

        } catch (Exception ex) {
            Notification.show("Fehler beim Speichern: " + ex.getMessage(), 4000, Notification.Position.MIDDLE);
        }
    }

    private String formatImmobilientyp(Immobilientyp typ) {
        return switch (typ) {
            case WOHNGEBAEUDE -> "Wohngebäude";
            case MEHRFAMILIENHAUS -> "Mehrfamilienhaus";
            case GEWERBEIMMOBILIE -> "Gewerbeimmobilie";
        };
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